#!/bin/bash

nSEs() {
    local i=0
    for n in ${SELIST}; do
        i=$((i + 1))
    done
    return $i
}

getAndRemoveSE() {
    local index=$1
    local i=0
    local NSE=""
    RESULT=""
    for n in ${SELIST}; do
        if [ "$i" = "${index}" ]; then
            RESULT=$n
            info "result: $RESULT"
        else
            NSE="${NSE} $n"
        fi
        i=$((i + 1))
    done
    SELIST=${NSE}
    return 0
}

chooseRandomSE() {
    nSEs
    local n=$?
    if [ "$n" = "0" ]; then
        info "SE list is empty"
        RESULT=""
    else
        local r=${RANDOM}
        local id=$((r % n))
        getAndRemoveSE ${id}
    fi
}

uploadLfnFile() {
    local LFN=$1
    local FILE=$2
    local nrep=$3
    local SELIST=${SE}

    

    LFN=$(echo ${LFN} | sed -r -e 's/^lfn://' -e 's#//#/#g')

    info "getting file size and computing sendReceiveTimeout"
    local size=$(ls -l ${FILE} | awk -F' ' '{print $5}')
    local sendReceiveTimeout=$((size / minAvgDownloadThroughput / 1024))
    if [ -z "$sendReceiveTimeout" ] || [ "$sendReceiveTimeout" -le 900 ]; then
        info "sendReceiveTimeout empty or too small, setting it to 900s"
        sendReceiveTimeout=900
    else
        info "sendReceiveTimeout is $sendReceiveTimeout"
    fi

    local totalTimeout=$((timeout + srmTimeout + sendReceiveTimeout))

    local OPTS="-o /Resources/StorageElements/GFAL_TIMEOUT=${totalTimeout}"
    chooseRandomSE
    local DEST=${RESULT}
    local done=0
    while [ $nrep -gt $done ] && [ "${DEST}" != "" ]; do
        if [ "${done}" = "0" ]; then
            local command="dirac-dms-add-file"
            local source=$(hostname)
            dirac-dms-remove-files ${OPTS} ${LFN} &>/dev/null
            (time -p dirac-dms-add-file ${OPTS} ${LFN} ${FILE} ${DEST}) &> dirac.log
            local error_code=$?
        else
            local command="dirac-dms-replicate-lfn"
            (time -p dirac-dms-replicate-lfn -d ${OPTS} ${LFN} ${DEST}) &> dirac.log
            local error_code=$?

            local source=$(grep "operation 'getFileSize'" dirac.log | tail -1 | sed -r 's/^.* StorageElement (.*) is .*$/\1/')
        fi
        if [ ${error_code} = 0 ]; then
            info "Copy/Replication of ${LFN} to SE ${DEST} worked fine."
            done=$((done + 1))
            local duration=$(grep -P '^real[ \t]' dirac.log | sed -r 's/real[ \t]//')
            info "UploadCommand=${command} Source=${source} Destination=${DEST} Size=${size} Time=${duration}"
            if [ -z "${duration}" ]; then
                info "Missing duration info, printing the whole log file."
                cat dirac.log
            fi
        else
            error "$(cat dirac.log)"
            warning "Copy/Replication of ${LFN} to SE ${DEST} failed"
        fi
        \rm dirac.log
        chooseRandomSE
        DEST=${RESULT}
    done
    if [ "${done}" = "0" ]; then
        error "Cannot copy file ${FILE} to lfn ${LFN}"
        error "Exiting with return value 2"
        exit 2
    else
        addToCache ${LFN} ${FILE}
    fi
}

uploadShanoirFile() {
    local URI=$1

    wait_for_token

    local token=$(cat $SHANOIR_TOKEN_LOCATION)

    local upload_url=$(echo $URI | sed -r 's/^.*[?&]upload_url=([^&]*)(&.*)?$/\1/i')
    local fileName=$(echo $URI | sed -r 's#^shanoir:/(//)?(.*/(.+))\?.*$#\3#i')
    local filePath=$(echo $URI | sed -r 's#^shanoir:/(//)?([^/].*)\?.*$#\2#i')
   
    local type=$(echo $URI | sed -r 's/^.*[?&]type=([^&]*)(&.*)?$/\1/i')
    local md5=$(echo $URI | sed -r 's/^.*[?&]md5=([^&]*)(&.*)?$/\1/i')

    COMMAND() { 
        (echo -n '{"base64Content": "'; base64 ${fileName}; echo '", "type":"'; echo ${type}; echo '", "md5":"'; echo ${md5} ; echo '"}') | curl --write-out '%{http_code}' --request PUT "${upload_url}/${filePath}"  --header "Authorization: Bearer ${token}"  --header "Content-Type: application/carmin+json" --header 'Accept: application/json, text/plain, */*' -d @-
    }

    status_code=$(COMMAND)
    echo "uploadShanoirFIle, status code is : ${status_code}"
    
    if [[ "$status_code" -ne 201 ]]; then
        error "error while uploading the file with status : ${status_code}"
        stopRefreshingToken
        exit 1
    fi
}

uploadGirderFile() {
    local URI=$1

    local fileName=$(echo $URI | sed -r 's#^girder:/(//)?(.*/)?([^/].*)\?.*$#\3#i')
    local apiUrl=$(echo $URI | sed -r 's/^.*[?&]apiurl=([^&]*)(&.*)?$/\1/i')
    local fileId=$(echo $URI | sed -r 's/^.*[?&]fileid=([^&]*)(&.*)?$/\1/i')
    local token=$(echo $URI | sed -r 's/^.*[?&]token=([^&]*)(&.*)?$/\1/i')

    if [ ! $(which girder-client) ]; then
        pip install --user girder-client
        if [ $? != 0 ]; then
            error "girder-client not in PATH, and an error occured while trying to install it."
            error "Exiting with return value 1"
            exit 1
        fi
    fi

    COMMLINE="girder-client --api-url ${apiUrl} --token ${token} upload --parent-type folder ${fileId} ./${fileName}"
    echo "uploadGirderFile, command line is ${COMMLINE}"
    ${COMMLINE}
    if [ $? != 0 ]; then
        error "Error while uploading girder file"
        error "Exiting with return value 1"
        exit 1
    fi
}

upload() {
    local URI=$1
    local FILENAME=$2
    local ID=$3
    local NREP=$4
    local TEST=$5
    
    startLog file_upload uri="${URI}"

    if [[ ${URI} == shanoir:/* ]]; then
        if [ "${TEST}" != "true" ]; then
            if [[ "$REFRESHING_JOB_STARTED" == false ]]; then
                refresh_token ${URI} & 
                REFRESH_PID=${D}!  
                REFRESHING_JOB_STARTED=true
            fi
            uploadShanoirFile ${URI}
        fi
    elif [[ ${URI} == girder:/* ]]; then
        if [ "${TEST}" != "true" ]; then
            uploadGirderFile ${URI}
        fi
    elif [[ ${URI} == file:/* ]]; then
        local DEST_DIR=$(echo $URI | sed 's%file://*%/%')
        local DEST_FILE=$DEST_DIR/$FILENAME
        local NAME=$(echo "$FILENAME" | sed "s|$outputs_to_be_uploaded||")

        if [ -e $DEST_FILE ]; then
            error "Result file already exists: $DEST_FILE"
            error "Exiting with return value 1"
            exit 1
        fi

        if [ "${TEST}" = "true" ]; then
            echo "test result" > ${DEST_FILE}
        fi

        \mv $FILENAME $DEST_FILE
        if [ $? != 0 ]; then
            error "Error while moving result local file."
            error "Exiting with return value 1"
            exit 1
        fi
    else
        local LFN=$(echo "${URI}" | sed -r 's%^\w+://[^/]*(/[^?]+)(\?.*)?$%\1%')
        local fileName='${LFN##*/}'
        local NAME=${fileName}

        if [ "${TEST}" = "true" ]; then
            LFN=${LFN}-uploadTest
            echo "test result" > ${NAME}
        fi

        uploadLfnFile ${LFN} ${PWD}/${NAME} ${NREP}

        if [ "${TEST}" = "true" ]; then
            \rm -f ${NAME}
        fi
    fi
    stopLog file_upload
}