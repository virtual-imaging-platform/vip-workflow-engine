#!/bin/bash
rm -rf Static_script DISABLE_WATCHDOG_CPU_WALLCLOCK_CHECK

# Read the Job configuration file and assign values to variables
applicationName=$(jq -r '.jobConfiguration.applicationName' configuration.json)
jsonFileName=$(jq -r '.jobConfiguration.jsonFileName' configuration.json)
downloads=$(jq -r '.jobConfiguration.downloads' configuration.json)
invocationString=$(jq -r '.jobConfiguration.invocationString' configuration.json)
envVariables=$(jq -r '.jobConfiguration.envVariables' configuration.json)
parameters=$(jq -r '.jobConfiguration.parameters' configuration.json)
uploads=$(jq -r '.jobConfiguration.uploads' configuration.json)

# Gasw constants
LAB_DEFAULT_BACKGROUD_SCRIPT=$(jq -r '.gaswConstants.LAB_DEFAULT_BACKGROUD_SCRIPT' configuration.json)
LAB_DEFAULT_CPUTIME=$(jq -r '.gaswConstants.LAB_DEFAULT_CPUTIME' configuration.json)
LAB_DEFAULT_ENVIRONMENT=$(jq -r '.gaswConstants.LAB_DEFAULT_ENVIRONMENT' configuration.json)
LAB_DEFAULT_EXECUTOR=$(jq -r '.gaswConstants.LAB_DEFAULT_EXECUTOR' configuration.json)
LAB_DEFAULT_REQUIREMENTS=$(jq -r '.gaswConstants.LAB_DEFAULT_REQUIREMENTS' configuration.json)
LAB_DEFAULT_RETRY_COUNT=$(jq -r '.gaswConstants.LAB_DEFAULT_RETRY_COUNT' configuration.json)
LAB_DEFAULT_SLEEPTIME=$(jq -r '.gaswConstants.LAB_DEFAULT_SLEEPTIME' configuration.json)
LAB_DEFAULT_TIMEOUT=$(jq -r '.gaswConstants.LAB_DEFAULT_TIMEOUT' configuration.json)
LAB_FAILOVER_ENABLED=$(jq -r '.gaswConstants.LAB_FAILOVER_ENABLED' configuration.json)
LAB_FAILOVER_HOME=$(jq -r '.gaswConstants.LAB_FAILOVER_HOME' configuration.json)
LAB_FAILOVER_HOST=$(jq -r '.gaswConstants.LAB_FAILOVER_HOST' configuration.json)
LAB_FAILOVER_PORT=$(jq -r '.gaswConstants.LAB_FAILOVER_PORT' configuration.json)
LAB_FAILOVER_RETRY=$(jq -r '.gaswConstants.LAB_FAILOVER_RETRY' configuration.json)
LAB_MINORSTATUS_ENABLED=$(jq -r '.gaswConstants.LAB_MINORSTATUS_ENABLED' configuration.json)
LAB_PLUGIN_DB=$(jq -r '.gaswConstants.LAB_PLUGIN_DB' configuration.json)
LAB_PLUGIN_EXECUTOR=$(jq -r '.gaswConstants.LAB_PLUGIN_EXECUTOR' configuration.json)
LAB_PLUGIN_LISTENER=$(jq -r '.gaswConstants.LAB_PLUGIN_LISTENER' configuration.json)
LAB_VO_DEFAULT_SE=$(jq -r '.gaswConstants.LAB_VO_DEFAULT_SE' configuration.json)
LAB_VO_NAME=$(jq -r '.gaswConstants.LAB_VO_NAME' configuration.json)
LAB_VO_USE_CLOSE_SE=$(jq -r '.gaswConstants.LAB_VO_USE_CLOSE_SE' configuration.json)
LAB_BOSH_CVMFS_PATH=$(jq -r '.gaswConstants.LAB_BOSH_CVMFS_PATH' configuration.json)
LAB_CONTAINERS_CVMFS_PATH=$(jq -r '.gaswConstants.LAB_CONTAINERS_CVMFS_PATH' configuration.json)
LAB_UDOCKER_TAG=$(jq -r '.gaswConstants.LAB_UDOCKER_TAG' configuration.json)
LAB_MIN_AVG_DOWNLOAD_THROUGHPUT=$(jq -r '.gaswConstants.LAB_MIN_AVG_DOWNLOAD_THROUGHPUT' configuration.json)
CONNECT_TIMEOUT=$(jq -r '.gaswConstants.CONNECT_TIMEOUT' configuration.json)
BDII_TIMEOUT=$(jq -r '.gaswConstants.BDII_TIMEOUT' configuration.json)
SRM_TIMEOUT=$(jq -r '.gaswConstants.SRM_TIMEOUT' configuration.json)
SCRIPT_ROOT=$(jq -r '.gaswConstants.SCRIPT_ROOT' configuration.json)
JDL_ROOT=$(jq -r '.gaswConstants.JDL_ROOT' configuration.json)
OUT_ROOT=$(jq -r '.gaswConstants.OUT_ROOT' configuration.json)
ERR_ROOT=$(jq -r '.gaswConstants.ERR_ROOT' configuration.json)
CACHE_DIR=$(jq -r '.gaswConstants.CACHE_DIR' configuration.json)
CACHE_FILE=$(jq -r '.gaswConstants.CACHE_FILE' configuration.json)
INVO_DIR=$(jq -r '.gaswConstants.INVO_DIR' configuration.json)
OUT_EXT=$(jq -r '.gaswConstants.OUT_EXT' configuration.json)
OUT_APP_EXT=$(jq -r '.gaswConstants.OUT_APP_EXT' configuration.json)
ERR_EXT=$(jq -r '.gaswConstants.ERR_EXT' configuration.json)
ERR_APP_EXT=$(jq -r '.gaswConstants.ERR_APP_EXT' configuration.json)
ENV_EXECUTOR=$(jq -r '.gaswConstants.ENV_EXECUTOR' configuration.json)
numberOfReplicas=$(jq -r '.gaswConstants.numberOfReplicas' configuration.json)

# Gasw configuration
defaultSleeptime=$(jq -r '.gaswConfiguration.defaultSleeptime' configuration.json)
simulationID=$(jq -r '.gaswConfiguration.simulationID' configuration.json)
workflowID=$(jq -r '.gaswConfiguration.workflowID' configuration.json)
executionPath=$(jq -r '.gaswConfiguration.executionPath' configuration.json)
defaultBackgroundScript=$(jq -r '.gaswConfiguration.defaultBackgroundScript' configuration.json)
defaultCPUTime=$(jq -r '.gaswConfiguration.defaultCPUTime' configuration.json)
defaultEnvironment=$(jq -r '.gaswConfiguration.defaultEnvironment' configuration.json)
defaultExecutor=$(jq -r '.gaswConfiguration.defaultExecutor' configuration.json)
voDefaultSE=$(jq -r '.gaswConfiguration.voDefaultSE' configuration.json)
voUseCloseSE=$(jq -r '.gaswConfiguration.voUseCloseSE' configuration.json)
boshCVMFSPath=$(jq -r '.gaswConfiguration.boshCVMFSPath' configuration.json)
containersCVMFSPath=$(jq -r '.gaswConfiguration.containersCVMFSPath' configuration.json)
udockerTag=$(jq -r '.gaswConfiguration.udockerTag' configuration.json)
failOverEnabled=$(jq -r '.gaswConfiguration.failOverEnabled' configuration.json)
failOverHost=$(jq -r '.gaswConfiguration.failOverHost' configuration.json)
failOverPort=$(jq -r '.gaswConfiguration.failOverPort' configuration.json)
failOverHome=$(jq -r '.gaswConfiguration.failOverHome' configuration.json)
minorStatusEnabled=$(jq -r '.gaswConfiguration.minorStatusEnabled' configuration.json)
minAvgDownloadThroughput=$(jq -r '.gaswConfiguration.minAvgDownloadThroughput' configuration.json)
defaultRetryCount=$(jq -r '.gaswConfiguration.defaultRetryCount' configuration.json)
executorPlugins=$(jq -r '.gaswConfiguration.executorPlugins' configuration.json)
listenerPlugins=$(jq -r '.gaswConfiguration.listenerPlugins' configuration.json)
sessionFactory=$(jq -r '.gaswConfiguration.sessionFactory' configuration.json)
failOverMaxRetry=$(jq -r '.gaswConfiguration.failOverMaxRetry' configuration.json)
config=$(jq -r '.gaswConfiguration.config' configuration.json)

# Output the variables
echo "Application Name: $applicationName"
echo "JSON File Name: $jsonFileName"
echo "Downloads: $downloads"
echo "Invocation String: $invocationString"
echo "Environment Variables: $envVariables"
echo "Parameters: $parameters"
echo "Uploads: $uploads"

# Output the gasw constants
echo "Lab Default Background Script: $LAB_DEFAULT_BACKGROUD_SCRIPT"
echo "Lab Default CPU Time: $LAB_DEFAULT_CPUTIME"
echo "Lab Default Environment: $LAB_DEFAULT_ENVIRONMENT"
echo "Lab Default Executor: $LAB_DEFAULT_EXECUTOR"
echo "Lab Default Requirements: $LAB_DEFAULT_REQUIREMENTS"
echo "Lab Default Retry Count: $LAB_DEFAULT_RETRY_COUNT"
echo "Lab Default Sleeptime: $LAB_DEFAULT_SLEEPTIME"
echo "Lab Default Timeout: $LAB_DEFAULT_TIMEOUT"
echo "Lab Failover Enabled: $LAB_FAILOVER_ENABLED"
echo "Lab Min Avg Download Throughput: $LAB_MIN_AVG_DOWNLOAD_THROUGHPUT"
echo "Connect Timeout: $CONNECT_TIMEOUT"
echo "BDII Timeout: $BDII_TIMEOUT"
echo "SRM Timeout: $SRM_TIMEOUT"
echo "Script Root: $SCRIPT_ROOT"
echo "JDL Root: $JDL_ROOT"
echo "Out Root: $OUT_ROOT"
echo "Err Root: $ERR_ROOT"
echo "Cache Dir: $CACHE_DIR"
echo "Cache File: $CACHE_FILE"
echo "Invo Dir: $INVO_DIR"
echo "Out Ext: $OUT_EXT"
echo "Out App Ext: $OUT_APP_EXT"
echo "Err Ext: $ERR_EXT"
echo "Err App Ext: $ERR_APP_EXT"
# Output more gasw constants as needed

# Output the gasw configuration
echo "Default Sleeptime: $defaultSleeptime"
echo "Simulation ID: $simulationID"
echo "Workflow ID: $workflowID"
echo "Execution Path: $executionPath"
echo "Default Background Script: $defaultBackgroundScript"
echo "Default CPU Time: $defaultCPUTime"
echo "Default Environment: $defaultEnvironment"
echo "Default Executor: $defaultExecutor"
echo "VO Default SE: $voDefaultSE"
echo "VO Use Close SE: $voUseCloseSE"
echo "Bosh CVMFS Path: $boshCVMFSPath"
echo "Containers CVMFS Path: $containersCVMFSPath"
echo "Udocker Tag: $udockerTag"
echo "Failover Enabled: $failOverEnabled"
echo "Failover Host: $failOverHost"
echo "Failover Port: $failOverPort"
echo "Failover Home: $failOverHome"
echo "Minor Status Enabled: $minorStatusEnabled"
echo "Min Avg Download Throughput: $minAvgDownloadThroughput"
echo "Default Retry Count: $defaultRetryCount"
echo "Executor Plugins: $executorPlugins"
echo "Listener Plugins: $listenerPlugins"
echo "SessionFactory: $sessionFactory"
echo "Failover Max Retry: $failOverMaxRetry"
echo "Config: $config"
# Output more gasw configuration variables as needed

function info {
  local D=`date`
  echo [ INFO - $D ] $*
}

function warning {
  local D=`date`
  echo [ WARN - $D ] $*
}

function error {
  local D=`date`
  echo [ ERROR - $D ] $* >&2
}

function startLog {
  echo "<$*>" >&1
  echo "<$*>" >&2
}

function stopLog {
  local logName=$1
  echo "</${logName}>" >&1
  echo "</${logName}>" >&2
}

function cleanup {
    if [[ $isGfalmountExec -eq 0 ]]    #flag checks if directories are mounted with gfal
    then
        unmountGfal    #unmounts all gfal mounted directories
        unlink /tmp/*_$(basename $PWD)
    fi
    startLog cleanup
    info "=== ls -a ==="
    ls -a
    info "=== ls $cacheDir/$cacheFile ==="
    ls $cacheDir/$cacheFile
    info "=== cat $cacheDir/$cacheFile === "
    cat $cacheDir/$cacheFile
    info "Cleaning up: rm * -Rf"
    #\rm * -Rf
    if [ "${BACKPID}" != "" ]
    then
        for i in `ps --ppid ${BACKPID} -o pid | grep -v PID`
        do
            info "Killing child of background script (pid ${i})"
            kill -9 ${i}
        done
        info "Killing background script (pid ${BACKPID})"
        kill -9 ${BACKPID}
    fi
    info "END date:"
    date +%s
    stopLog cleanup
    check_cleanup=true
}

export -f cleanup
trap 'echo "trap activation" && stopRefreshingToken | \
if [ "$check_cleanup" = true ]; then \
    echo "cleanup was already executed successfully"; \
else \
    echo "Executing cleanup" && cleanup; \
fi' INT EXIT

function checkCacheDownloadAndCacheLFN {

    local LFN=$1
    # the LFN is assumed to be in the /grid/biomed/... format (no leading lfn://lfc-biomed.in2p3.fr:5010/)
    # this variable is true <=> the file has to be downloaded again
    local download="true"
    # first check if the file is already in cache
    local LOCALPATH=$(awk -v L=${LFN} '$1==L {print $2}' "$cacheDir/$cacheFile")
    if [ "${LOCALPATH}" != "" ]; then
        info "There is an entry in the cache: test if the local file still here"
        local TIMESTAMP_LOCAL=""
        local TIMESTAMP_GRID=""
        local date_local=""
        if [ -f "${LOCALPATH}" ]; then
            info "The file exists: checking if it was modified since it was added to the cache"
            local YEAR=$(date +%Y)
            local YEARBEFORE=$(expr ${YEAR} - 1)
            local currentDate=$(date +%s)
            local TIMESTAMP_CACHE=$(awk -v L=${LFN} '$1==L {print $3}' "$cacheDir/$cacheFile")
            local LOCALMONTH=$(ls -la "${LOCALPATH}" | awk -F' ' '{print $6}')
            local MONTHTIME=$(date -d "${LOCALMONTH} 1 00:00" +%s)
            date_local=$(ls -la "${LOCALPATH}" | awk -F' ' '{print $6, $7, $8}')
            if [ "${MONTHTIME}" -gt "${currentDate}" ]; then
                TIMESTAMP_LOCAL=$(date -d "${date_local} ${YEARBEFORE}" +%s)
            else
                TIMESTAMP_LOCAL=$(date -d "${date_local} ${YEAR}" +%s)
            fi
            if [ "${TIMESTAMP_CACHE}" = "${TIMESTAMP_LOCAL}" ]; then
                info "The file was not touched since it was added to the cache: test if it is up up-to-date"
                local date_grid_s=$(lfc-ls -l "${LFN}" | awk -F' ' '{print $6, $7, $8}')
                local MONTHGRID=$(echo "${date_grid_s}" | awk -F' ' '{print $1}')
                MONTHTIME=$(date -d "${MONTHGRID} 1 00:00" +%s)
                if [ "${MONTHTIME}" != "" ] && [ "${date_grid_s}" != "" ]; then
                    if [ "${MONTHTIME}" -gt "${currentDate}" ]; then
                        # it must be last year
                        TIMESTAMP_GRID=$(date -d "${date_grid_s} ${YEARBEFORE}" +%s)
                    else
                        TIMESTAMP_GRID=$(date -d "${date_grid_s} ${YEAR}" +%s)
                    fi
                    if [ "${TIMESTAMP_LOCAL}" -gt "${TIMESTAMP_GRID}" ]; then
                        info "The file is up-to-date ; there is no need to download it again"
                        download="false"
                    else
                        warning "The cache entry is outdated (local modification date is ${TIMESTAMP_LOCAL} - ${date_local} while grid is ${TIMESTAMP_GRID} ${date_grid_s})"
                    fi
                else
                    warning "Cannot determine file timestamp on the LFC"
                fi
            else
                warning "The cache entry was modified since it was created (cache time is ${TIMESTAMP_CACHE} and file time is ${TIMESTAMP_LOCAL} - ${date_local})"
            fi
        else
            warning "The cache entry disappeared"
        fi
    else
        info "There is no entry in the cache"
    fi
    if [ "${download}" = "false" ]; then
        info "Linking file from cache: ${LOCALPATH}"
        BASE=$(basename "${LFN}")
        info "ln -s ${LOCALPATH} ./${BASE}"
        ln -s "${LOCALPATH}" "./${BASE}"
        return 0
    fi
    if [ "${download}" = "true" ]; then
        downloadLFN "${LFN}"
        if [ $? != 0 ]; then
            return 1
        fi
        addToCache "${LFN}" $(basename "${LFN}")
        return 0
    fi
}

export -f checkCacheDownloadAndCacheLFN


SHANOIR_TOKEN_LOCATION="${PWD}/cache/SHANOIR_TOKEN.txt"
SHANOIR_REFRESH_TOKEN_LOCATION="${PWD}/cache/SHANOIR_REFRESH_TOKEN.txt"
REFRESHING_JOB_STARTED=false
REFRESH_PID=""

#
# This is a background process to refresh shanoir token
# URIs are of the form of the following example. A single "/", instead
# of 3, after "shanoir:" is also allowed.
# shanoir:/path/to/file/filename?&refreshToken=eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lk....&keycloak_client_id=....&keycloak_client_secret=...
# The mandatory arguments are: keycloak_client_id, keycloak_client_secret.
#
function refresh_token {
    touch "$SHANOIR_TOKEN_LOCATION"
    touch "$SHANOIR_REFRESH_TOKEN_LOCATION"

    local subshell_refresh_token=$(cat "$SHANOIR_REFRESH_TOKEN_LOCATION")

    echo "refresh token process started !"

    local URI=$1
    local keycloak_client_id=$(echo "$URI" | sed -r 's/^.*[?&]keycloak_client_id=([^&]*)(&.*)?$/\1/i')
    local refresh_token_url=$(echo "$URI" | sed -r 's/^.*[?&]refresh_token_url=([^&]*)(&.*)?$/\1/i')

    if [[ ! "$subshell_refresh_token" ]]; then
        # initializing the refresh token
        subshell_refresh_token=$(echo "$URI" | sed -r 's/^.*[?&]refreshToken=([^&]*)(&.*)?$/\1/i')
        echo "$subshell_refresh_token" > "$SHANOIR_REFRESH_TOKEN_LOCATION"
    fi

    while :; do

        # get the new refresh token
        subshell_refresh_token=$(cat "$SHANOIR_REFRESH_TOKEN_LOCATION")

        # the response format is "{"status":"status"}"
        # this response format is made to handle error while getting the refreshed token in the same time
        COMMAND() {
            curl -w "{\"status\":\"%{http_code}\"}" -sb -o --request POST "${refresh_token_url}" --header "Content-Type: application/x-www-form-urlencoded" --data-urlencode "client_id=${keycloak_client_id}" --data-urlencode "grant_type=refresh_token" --data-urlencode "refresh_token=${subshell_refresh_token}"
        }

        refresh_response=$(COMMAND)
        status_code=$(echo "$refresh_response" | grep -o '"status":"[^"]*' | grep -o '[^"]*$')

        if [[ "$status_code" -ne 200 ]]; then
            error_message=$(echo "$refresh_response" | grep -o '"error_description":"[^"]*' | grep -o '[^"]*$')
            error "error while refreshing the token with status : ${status_code} and message error : ${error_message}"
            exit 1
        fi

        # setting the new tokens
        echo "$refresh_response" | grep -o '"access_token":"[^"]*' | grep -o '[^"]*$' > "$SHANOIR_TOKEN_LOCATION"
        echo "$refresh_response" | grep -o '"refresh_token":"[^"]*' | grep -o '[^"]*$' > "$SHANOIR_REFRESH_TOKEN_LOCATION"

        sleep 240
    done

}
#
# Cleanup method: stop the refreshing process
#

function stopRefreshingToken {
    if [ "${REFRESH_PID}" != "" ]; then
        info "Killing background refresh token process with id : ${REFRESH_PID}"
        kill -9 "${REFRESH_PID}"
        REFRESH_PID=""
        echo "refresh token process ended !"
    fi
}
#
# The refresh token may take some time, this method is for that purpose
# and it exit the program if it's timed out
#

function wait_for_token {
    local token=""
    local attempts=0

    while [[ "${attempts}" -ne 3 ]]; do
        token=$(cat "$SHANOIR_TOKEN_LOCATION")
        if [[ "${token}" == "" ]]; then
            echo "token is not refreshed yet, waiting for 3 seconds..."
            echo "attempts : ${attempts}"
            attempts=$((attempts + 1))
            sleep 3
        else
            echo "token is refreshed !"
            break
        fi
    done
}

function downloadLFN {

    local LFN=$1

    # Sanitize LFN:
    # - "lfn:" at the beginning is optional for dirac-dms-* commands,
    #    but does not work as expected with comdirac commands like
    #    dmkdir.
    # - "//" are not accepted, neither by dirac-dms-*, nor by dmkdir.
    LFN=$(echo ${LFN} | sed -r -e 's/^lfn://' -e 's#//#/#g')

    echo "getting file size and computing sendReceiveTimeout"
    local size=$(dirac-dms-lfn-metadata ${LFN} | grep Size | sed -r 's/.* ([0-9]+)L,/\1/')
    local sendReceiveTimeout=$((${size:-0} / ${minAvgDownloadThroughput} / 1024))
    
    if [ -z "$sendReceiveTimeout" ] || [ $sendReceiveTimeout -le 900 ]; then
        echo "sendReceiveTimeout empty or too small, setting it to 900s"
        sendReceiveTimeout=900
    else
        echo "sendReceiveTimeout is $sendReceiveTimeout"
    fi

    local LOCAL=${PWD}/`basename ${LFN}`
    echo "Removing file ${LOCAL} in case it is already here"
    \rm -f ${LOCAL}

    local totalTimeout=$((${timeout} + ${srmTimeout} + ${sendReceiveTimeout}))

    local LINE="time -p dirac-dms-get-file -d -o /Resources/StorageElements/GFAL_TIMEOUT=${totalTimeout} ${LFN}"
    echo ${LINE}
    (${LINE}) &> get-file.log

    if [ $? = 0 ]; then
        echo "dirac-dms-get-file worked fine"
        local source=$(grep "generating url" get-file.log | tail -1 | sed -r 's/^.* (.*)\.$/\1/')
        local duration=$(grep -P '^real[ \t]' get-file.log | sed -r 's/real[ \t]//')
        echo "DownloadCommand=dirac-dms-get-file Source=${source} Destination=$(hostname) Size=${size} Time=${duration}"
        RET_VAL=0
    else
        echo "dirac-dms-get-file failed"
        echo "`cat get-file.log`"
        RET_VAL=1
    fi

    \rm get-file.log
    return ${RET_VAL}
}

export -f downloadLFN

#
# URI are of the form of the following example. A single "/", instead
# of 3, after "girder:" is also allowed.
# girder:///control_3DT1.nii?apiurl=http://localhost:8080/api/v1&fileId=5ae1a8fc371210092e0d2936&token=TFT2FdxP9hzM7WKsidBjMJMmN69
#
#
# The code is quite the same as the uploadGirderFile function.  Any
# changes should be done the same in both functions.
#
function downloadGirderFile {
    local URI=$1

    # The regexpes are written so that case is ignored and the
    # arguments can be in any order.
    local fileName=$(echo $URI | sed -r 's#^girder:/(//)?([^/].*)\?.*$#\2#i')
    local apiUrl=$(echo $URI | sed -r 's/^.*[?&]apiurl=([^&]*)(&.*)?$/\1/i')
    local fileId=$(echo $URI | sed -r 's/^.*[?&]fileid=([^&]*)(&.*)?$/\1/i')
    local token=$(echo $URI | sed -r 's/^.*[?&]token=([^&]*)(&.*)?$/\1/i')

    if [ ! $(which girder-client) ]; then
        pip install --user girder-client
        if [ $? != 0 ]; then
            echo "girder-client not in PATH, and an error occurred while trying to install it."
            echo "Exiting with return value 1"
            exit 1
        fi
    fi

    COMMLINE="girder-client --api-url ${apiUrl} --token ${token} download --parent-type file ${fileId} ./${fileName}"
    echo "downloadGirderFile, command line is ${COMMLINE}"
    ${COMMLINE}
}

export -f downloadGirderFile


# This function identifies the gfal path and extracts the basename of the directory to be mounted,
# and creates a directory with the exact name on $PWD of the node. This directory gets mounted with
# the corresponding directory on the SE.
#
# This function checks for all the gfal mounts in the current folder.

check_mount='$(test -z $(for file in *; do findmnt -t fuse.gfalFS -lo Target -n -T $(realpath ${file}); done) && echo 1 || echo 0)'
isGfalmountExec=1

function mountGfal {
    local URI=$1

    local fileName=$(echo $URI | sed -r 's#^srm:/(//)?([^/].*)\?.*$#\2#i')
    local gfal_basename=$(basename ${fileName})
    local job_id=${gfal_basename}_$(basename $PWD)

    CREATE_DIR_COMMAND="mkdir -p $gfal_basename"
    SYM_LINK_COMMAND="ln -s $PWD/$gfal_basename /tmp/$job_id"
    GFAL_COMMAND="gfalFS -s /tmp/$job_id ${fileName}"

    ${CREATE_DIR_COMMAND}
    ${SYM_LINK_COMMAND}
    ${GFAL_COMMAND}
    # Let nfs-kernel-server export the directory and write logs
    sleep 30
    eval echo $check_mount
}

export -f mountGfal


# This function un-mounts all the gfal mounted directories by searching them with 'findmnt'
# and filtering them with FSTYPE 'fuse.gfalFS'. This function gets called in the cleanup function,
# either after the execution of the job, failure of the job, or interruptions of the job.

function unmountGfal {
    START=$SECONDS
    while [ $(eval echo $check_mount) = 0 ]; do
        for file in $PWD/*; do
            findmnt -t fuse.gfalFS -lo Target -n -T $(realpath ${file}) && gfalFS_umount $(realpath ${file})
        done
        sleep 2
        if [[ $(($SECONDS - $START)) -gt 600 ]]; then # while loop breaks automatically after 10 minutes
            echo "WARNING - gfal directory couldn't be unmounted: timeout"
            break
        fi

    done
    eval echo $check_mount
}

export -f unmountGfal


# URI are of the form of the following example. A single "/", instead
# of 3, after "shanoir:" is also allowed.
# shanoir:/download.dcm?apiurl=https://shanoir-ng-nginx/shanoir-ng/datasets/carmin-data/path&format=dcm&datasetId=1
#
# This method depends on the refresh token process to refresh the token when it needs
#

function downloadShanoirFile {
    local URI=$1
    
    wait_for_token

    local token=$(cat $SHANOIR_TOKEN_LOCATION)

    echo "token inside download : ${token}"

    local fileName=$(echo $URI | sed -r 's#^shanoir:/(//)?([^/].*)\?.*$#\2#i')
    local apiUrl=$(echo $URI | sed -r 's/^.*[?&]apiurl=([^&]*)(&.*)?$/\1/i')
    local format=$(echo $URI | sed -r 's/^.*[?&]format=([^&]*)(&.*)?$/\1/i')
    local datasetId=$(echo $URI | sed -r 's/^.*[?&]datasetId=([^&]*)(&.*)?$/\1/i')

    COMMAND(){
        curl --write-out '%{http_code}' -o ${fileName} --request GET "${apiUrl}/${datasetId}?format=${format}" --header "Authorization: Bearer ${token}"
    }

    status_code=$(COMMAND)
    echo "downloadShanoirFIle, status code is : ${status_code}"
    
    if [[ "$status_code" -ne 200 ]]; then
       echo "error while downloading the file with status : ${status_code}"
       stopRefreshingToken
       exit 1
    fi

    if [[ $format = "nii" ]]; then
       echo "its a nifti, shanoir has zipped it"
       TMP_UNZIP_DIR="tmp_unzip_dir"
       mkdir $TMP_UNZIP_DIR
       mv $fileName $TMP_UNZIP_DIR/tmp.zip
       cd $TMP_UNZIP_DIR
       unzip tmp.zip
       if [[ $(ls -1q *.nii.gz | wc -l) -ne 1 ]]; then
            echo "too many files in shanoir nifti, supporting only 1"
            stopRefreshingToken
            exit 1
       fi
       cd ..
       mv $TMP_UNZIP_DIR/*.nii.gz $fileName
       rm -rf $TMP_UNZIP_DIR
    fi
}

function downloadURI {

    local URI=$1
    local URI_LOWER=$(echo $1 | awk '{print tolower($0)}')

    if [[ ${URI_LOWER} == lfn* ]] || [[ $URI_LOWER == /* ]]; then
        ## Extract the path part from the uri, and remove // if
        ## present in path.
        LFN=$(echo "${URI}" | sed -r -e 's%^\w+://[^/]*(/[^?]+)(\?.*)?$%\1%' -e 's#//#/#g')

        checkCacheDownloadAndCacheLFN $LFN
        validateDownload "Cannot download LFN file"
    fi

    if [[ ${URI_LOWER} == file:/* ]]; then
        local FILENAME=$(echo $URI | sed 's%file://*%/%')
        cp $FILENAME .
        validateDownload "Cannot copy input file: $FILENAME"
    fi

    if [[ ${URI_LOWER} == http://* ]]; then
        curl --insecure -O ${URI}
        validateDownload "Cannot download HTTP file"
    fi

    if [[ ${URI_LOWER} == girder:/* ]]; then
        downloadGirderFile ${URI}
        validateDownload "Cannot download Girder file"
    fi

    if [[ ${URI_LOWER} == shanoir:/* ]]; then
        if [[ "$REFRESHING_JOB_STARTED" == false ]]; then
           #set( $D = '$' )
           refresh_token ${URI} &
           REFRESH_PID=${D}!
           REFRESHING_JOB_STARTED=true
        fi
        downloadShanoirFile ${URI}
        validateDownload "Cannot download shanoir file"
    fi

    if [[ ${URI_LOWER} == srm:/* ]]; then
            if [[ $(mountGfal ${URI}) -eq 0 ]]; then
                isGfalmountExec=0
            else
                echo "Cannot download gfal file"
            fi
    fi
}

function validateDownload() {

    if [ $? != 0 ]; then
        echo "$1"
        echo "Exiting with return value 1"
        exit 1
    fi
}


function addToCache {
    mkdir -p "$cacheDir"
    touch "$cacheDir/$cacheFile"
    local LFN="$1"
    local FILE=$(basename "$2")
    local i=0
    local exist="true"
    local NAME=""
    while [ "$exist" = "true" ]; do
        NAME="$cacheDir/${FILE}-cache-${i}"
        test -f "${NAME}"
        if [ $? != 0 ]; then
            exist="false"
        fi
        i=$(expr $i + 1)
    done
    info "Removing all cache entries for ${LFN} (files will stay locally in case anyone else needs them)"
    local TEMP=$(mktemp temp.XXXXXX)
    awk -v L="${LFN}" '$1!=L {print}' "$cacheDir/$cacheFile" > "${TEMP}"
    \mv -f "${TEMP}" "$cacheDir/$cacheFile"
    info "Adding file ${FILE} to cache and setting the timestamp"
    \cp -f "${FILE}" "${NAME}"
    local date_local=$(ls -la "${NAME}" | awk -F' ' '{print $6, $7, $8}')
    local TIMESTAMP=$(date -d "${date_local}" +%s)
    echo "${LFN} ${NAME} ${TIMESTAMP}" >> "$cacheDir/$cacheFile"
}

export -f addToCache

function addToFailOver {
    local LFN="$1"
    local FILE="$2"
    local REMOTEFILE=$(lcg-lr "lfn:${LFN}" | grep "$failOverHost")
    generated=${REMOTEFILE#*generated}
    local RPFILE="${generated}"

    lcg-del --nobdii --defaultsetype srmv2 -v "srm://$failOverHost:$failOverPort/srm/managerv2?SFN=$failOverHome${RPFILE}" &>/dev/null
    lfc-ls "${LFN}"
    if [ $? = 0 ]; then
        lfc-rename "${LFN}" "${LFN}-garbage-$(date +"%Y-%m-%d-%H-%M-%S")"
    fi
    lfc-mkdir -p "$(dirname "${LFN}")"
    local FILENAME=$(echo "$RANDOM$RANDOM" | md5sum | awk '{print $1}')
    local FOLDERNAME=$(date +"%Y-%m-%d")
    local OPTS="--nobdii --defaultsetype srmv2"
    DM_DEST="srm://$failOverHost:$failOverPort/srm/managerv2?SFN=$failOverHome/${FOLDERNAME}/file-${FILENAME}"
    GUID=$(lcg-cr ${OPTS} -d "${DM_DEST}" "file:${FILE}")
    if [ $? = 0 ]; then
        lcg-aa "${GUID}" "lfn:${LFN}"
        if [ $? = 0 ]; then
            info "Data successfully copied to Fail Over."
        else
            error "Unable to create LFN alias ${LFN} to ${GUID}"
            return 1
        fi
    else
        error "Unable to copy data to Fail Over."
        return 1
    fi
}

export -f addToFailOver

function upload {

    local URI="$1"
    local FILENAME="$2"
    local ID="$2"
    local NREP="$3"
    local TEST="$4"

    startLog file_upload uri="${URI}"

    ## The pattern must NOT be put between quotation marks.
    if [[ ${URI} == shanoir:* ]]; then
        if [ "${TEST}" != "true" ]; then
            if [[ "$REFRESHING_JOB_STARTED" == false ]]; then
                #set( $D = '$' )
                refresh_token "${URI}" &
                REFRESH_PID="${D}!"
                REFRESHING_JOB_STARTED=true
            fi
            uploadShanoirFile "${URI}"
        fi
    elif [[ ${URI} == girder:* ]]; then
        if [ "${TEST}" != "true" ]; then
            uploadGirderFile "${URI}"
        fi
    elif [[ ${URI} == file:* ]]; then
        local DEST_DIR=$(echo "${URI}" | sed 's%file://*%/%')
        local DEST_FILE="${DEST_DIR}/${FILENAME}"
        NAME=$(echo "$FILENAME" | sed "s|$outputs_to_be_uploaded||")

        if [ -e "$DEST_FILE" ]; then
            error "Result file already exists: $DEST_FILE"
            error "Exiting with return value 1"
            exit 1
        fi

        if [ "${TEST}" = "true" ]; then
            echo "test result" > "${DEST_FILE}"
        fi

        \mv "$FILENAME" "$DEST_FILE"
        if [ $? != 0 ]; then
            error "Error while moving result local file."
            error "Exiting with return value 1"
            exit 1
        fi
    else
        ## Extract the path part from the uri.
        local LFN=$(echo "${URI}" | sed -r 's%^\w+://[^/]*(/[^?]+)(\?.*)?$%\1%')
        local fileName=$(basename "${LFN}")

        if [ "${TEST}" = "true" ]; then
            LFN="${LFN}-uploadTest"
            echo "test result" > "${fileName}"
        fi

        uploadLfnFile "${LFN}" "${PWD}/${fileName}" "${NREP}"

        if [ "${TEST}" = "true" ]; then
            \rm -f "${fileName}"
        fi
    fi
    stopLog file_upload
}

function delete {

    local URI="$1"
    local TEST="$2"

    startLog file_delete uri="${URI}"

    ## The pattern must NOT be put between quotation marks.
    if [[ ${URI} == girder:* ]]; then
        info "delete not supported for girder"
    elif [[ ${URI} == file:* ]]; then
        local FILENAME=$(echo "$URI" | sed 's%file://*%/%')

        info "Removing local file ${FILENAME}..."
        \rm -f "$FILENAME"
    else
        ## Extract the path part from the uri, and sanitize it.
        ## "//" are not accepted by dirac commands.
        local LFN=$(echo "${URI}" | sed -r -e 's%^\w+://[^/]*(/[^?]+)(\?.*)?$%\1%' -e 's#//#/#g')

        if [ "${TEST}" = true ]; then
            LFN="${LFN}-uploadTest"
        fi

        info "Deleting file ${LFN}..."
        dirac-dms-remove-files "${LFN}"
    fi

    stopLog file_delete
}

# Start log
START=$(date +%s)
echo "START date is ${START}"

# Execution environment setup
export GASW_JOB_ENV=NORMAL
export GASW_EXEC_ENV=EGEE

# Builds the custom environment
BASEDIR=${PWD}
ENV=$defaultEnvironment
export $ENV
__MOTEUR_ENV=$defaultEnvironment
export SE=$voDefaultSE
USE_CLOSE_SE=$voUseCloseSE
export BOSH_CVMFS_PATH=$boshCVMFSPath
export CONTAINERS_CVMFS_PATH=$containersCVMFSPath
export UDOCKER_TAG=$udockerTag

export MOTEUR_WORKFLOWID="$simulationID"
export MOTEUR_WORKFLOWID="$workflowID"

# If the execution environment is a cluster, add the vlet binaries to the path
if [[ "$GASW_EXEC_ENV" == "PBS" ]]; then
    export PATH=${VLET_INSTALL}/bin:$PATH
fi

DIAG=/home/grid/session/$(basename ${PWD}).diag

# Create execution directory
DIRNAME=$(basename $0 .sh)
mkdir ${DIRNAME}
if [ $? -eq 0 ]; then
    echo "cd ${DIRNAME}"
    cd ${DIRNAME} || exit 7
else
    echo "Unable to create directory ${DIRNAME}"
    echo "Exiting with return value 7"
    exit 7
fi

#if( $minorStatusEnabled && $serviceCall )
$serviceCall ${MOTEUR_WORKFLOWID} ${JOBID} 1
#end

BACKPID=""

# DIRAC may wrongly position this variable
if [ ! -d ${X509_CERT_DIR} ]; then
    echo "Unsetting invalid X509_CERT_DIR (${X509_CERT_DIR})"
    unset X509_CERT_DIR
fi

# Stop log
echo "END date is $(date +%s)"

# Start log
echo "SE Linux mode is:"
/usr/sbin/getenforce
echo "gLite Job Id is ${GLITE_WMS_JOBID}"
echo "===== uname ===== "
uname -a
domainname -a
echo "===== network config ===== "
/sbin/ifconfig eth0
dmesg_line=$(dmesg | grep 'Link is Up' | uniq)
netspeed=$(echo $dmesg_line | grep -o '[0-9]*[[:space:]][a-zA-Z]bps'| awk '{gsub(/ /,"",$0);print}')
echo "NetSpeed = $netspeed ($dmesg_line)"
echo "===== CPU info ===== "
cat /proc/cpuinfo
echo "===== Memory info ===== "
cat /proc/meminfo
echo "===== lcg-cp location ===== "
which lcg-cp
echo "===== ls -a . ===== "
ls -a
echo "===== ls -a .. ===== "
ls -a ..
echo "===== env ====="
env
echo "===== rpm -qa  ===="
rpm -qa

mkdir -p $cacheDir

# Stop log

# Start log
startLog background

# Execute service call if minor status is enabled and service call is provided
if [[ "$minorStatusEnabled" == true && -n "$serviceCall" ]]; then
    $serviceCall $MOTEUR_WORKFLOWID $JOBID 2
fi

# Check cache, download, and cache LFN for the background script
checkCacheDownloadAndCacheLFN $backgroundScript

# Execute the background script
bash $(basename $backgroundScript) 1>background.out 2>background.err &
BACKPID=$!

# Stop log
stopLog background

# Start log for inputs download
startLog inputs_download

echo $invocationString
Job_Id="$0"
echo $Job_Id

# Print a message indicating the JSON file has been created
# This part needs to be handled in the code that processes the VTL template

echo $invocationString
invocationParameters='$invocationString'

# Execute service call if minor status is enabled and service call is provided
if [[ "$minorStatusEnabled" == true && -n "$serviceCall" ]]; then
    $serviceCall $MOTEUR_WORKFLOWID $JOBID 3
fi

# Create a file to disable watchdog CPU wallclock check
touch ../DISABLE_WATCHDOG_CPU_WALLCLOCK_CHECK

# Download each file specified in $downloads
for download in $downloads; do
    downloadURI "$download"
    ls -a
    __MOTEUR_IN="${__MOTEUR_IN};$download"
done

# Change permissions of all files in the directory
chmod 755 *

# Record the timestamp after downloads
AFTERDOWNLOAD=$(date +%s)

# Stop log for inputs download
stopLog inputs_download

# Start log for application environment
startLog application_environment
echo "$variables"

# Iterate through each variable in $variables and export them
# Assuming $variables is a string containing key-value pairs separated by space
for variable in $variables; do
    key=$(echo "$variable" | cut -d'=' -f1)
    value=$(echo "$variable" | cut -d'=' -f2)
    export "$key"="$value"
done

# Stop log for application environment
stopLog application_environment


function download_udocker {
  #installation of udocker
  info "cloning udocker ${UDOCKER_TAG} "
  git clone --depth=1 --branch ${UDOCKER_TAG} https://github.com/indigo-dc/udocker.git
  (cd udocker/udocker; ln -s maincmd.py udocker)
  export PATH=`pwd`/udocker/udocker:$PATH
  
  #creating a temporary directory for udocker containers
  mkdir -p containers
  export UDOCKER_CONTAINERS=$PWD/containers
  
  #find pre-deployed containers on CVMFS, and create a symlink to the udocker containers directory
  ## use a global velocity escape to avoid velocity escaping issue
  for d in ${CONTAINERS_CVMFS_PATH}/*/ ;
     do mkdir containers/$(basename "${d%/}") && ln -s "${d%/}"/* containers/$(basename "${d%/}")/
  done
  cat >docker <<'EOF'
        #!/bin/bash
        MYARGS=$*
        echo "executing ./udocker/udocker/udocker $MYARGS"
        ./udocker/udocker/udocker $MYARGS
EOF
  chmod a+x docker
  export PATH=$PWD:$PATH
}

if ! command -v docker
then
    download_udocker
fi

function checkBosh {
  local BOSH_CVMFS_PATH=$1
  #by default, use CVMFS bosh
  ${BOSH_CVMFS_PATH}/bosh create foo.sh
  if [ $? != 0 ]
  then
    info "CVMFS bosh in ${BOSH_CVMFS_PATH} not working, checking for a local version"
    bosh create foo.sh
    if [ $? != 0 ]
    then
        info "bosh is not found in PATH or it is does not work fine, searching for another local version"
        local HOMEBOSH=`find $HOME -name bosh`
        if [ -z "$HOMEBOSH" ]
        then
            info "bosh not found, trying to install it"
            pip install --trusted-host pypi.org --trusted-host pypi.python.org --trusted-host files.pythonhosted.org boutiques --prefix $PWD 
            if [ $? != 0 ]
            then
                error "pip install boutiques failed"
                exit 1
            else
                export BOSHEXEC="$PWD/bin/bosh"
            fi
        else
            info "local bosh found in $HOMEBOSH"
            export BOSHEXEC=$HOMEBOSH
        fi
    else # bosh is found in PATH and works fine
        info "local bosh found in $PATH"
        export BOSHEXEC="bosh"
    fi
  else # if bosh CVMFS works fine
    export BOSHEXEC="${BOSH_CVMFS_PATH}/bosh"
  fi
}
checkBosh $BOSH_CVMFS_PATH

function getInvocationFile {
  # Define the directory where invocation files will be stored

  invocationDirectory=../invo


  # Construct the full path for the invocation file
    local invocationFile="${invocationDirectory}/$(basename "$0" .sh)-invocation.json"

  # Return the invocation file path
    echo "$invocationFile"
}

invocationFile=$(getInvocationFile)

echo $invocationFile

function boutiques_exec {
echo $JSONFILE
ls -ultra
ll
echo "import sys; sys.setdefaultencoding(\"UTF8\")" > sitecustomize.py
COMMAND_LINE="PYTHONPATH=".:$PYTHONPATH" $BOSHEXEC exec launch -x --provenance_path ./provenance_file.json $jsonFileName $invocationFile -v $PWD/../cache:$PWD/../cache"
readarray -t lines < <(eval "$COMMAND_LINE")
Boutiques_provenance_logs="[ INFO ] Data capture from execution saved to cache as"
for line in "${lines[@]}"; do
    echo "$line"
    if [[ $line == "$Boutiques_provenance_logs"* ]];
    then
    Provenance_file=${line#*"$Boutiques_provenance_logs"}
    fi
done

if [ $? != 0 ]
then
    error "VIP_test execution failed!"
    exit 1
fi

info "Execution of VIP_test completed."
echo "Provenance file: $Provenance_file"
}


# Perform service call if minor status is enabled
if [[ $minorStatusEnabled == true && $serviceCall ]]; then
    $serviceCall ${MOTEUR_WORKFLOWID} ${JOBID} 4
fi

# Add a delay to ensure file creation before proceeding
echo "BEFORE_EXECUTION_REFERENCE" > BEFORE_EXECUTION_REFERENCE_FILE
sleep 1
echo "$params parameters"


# Export current directory to LD_LIBRARY_PATH
export LD_LIBRARY_PATH=${PWD}:${LD_LIBRARY_PATH}


# Check if execution was successful
if [ $? -ne 0 ]; then
    error "Exiting with return value 6"
    BEFOREUPLOAD=$(date +%s)
    info "Execution time: $(expr ${BEFOREUPLOAD} - ${AFTERDOWNLOAD}) seconds"
    stopLog application_execution
    cleanup
    exit 6
fi

BEFOREUPLOAD=$(date +%s)
stopLog application_execution

info "Execution time was $(expr ${BEFOREUPLOAD} - ${AFTERDOWNLOAD})s"

__MOTEUR_ARGS="$params"
__MOTEUR_EXE="$executableName"

# Function to process provenance and extract output file names
function provenance() {
    Provenance_file=$PWD/provenance_file.json
    keys_with_file_name=$(jq -r '."public-output"."output-files" | to_entries[] | "\(.key) \(.value."file-name")"' $Provenance_file)
    output_name=($(echo ${keys_with_file_name}))
    for (( c=0; c<=$(wc -w <<< "$keys_with_file_name")-1; c++))
    do
        c=$(expr $c + 1)
        echo ${output_name[$c]}
        outputs_to_be_uploaded+=(${output_name[$c]})
    done
}

startLog results_upload

# Perform service call if minor status is enabled
if [[ $minorStatusEnabled == true && $serviceCall ]]; then
    $serviceCall ${MOTEUR_WORKFLOWID} ${JOBID} 5
fi
boutiques_exec
provenance

echo "${uploads.numberOfReplicas}"

# Iterate through output files to upload
for output_to_be_uploaded in "${outputs_to_be_uploaded[@]}"
do
    upload "$uploadURI" "$output_to_be_uploaded" "$(tr -dc '[:alpha:]' < /dev/urandom 2>/dev/null | head -c 32)" "$upload.NumberOfReplicas" false
done

__MOTEUR_OUT="$uploadsList"

stopLog results_upload

startLog footer
# Perform service call if minor status is enabled
if [[ $minorStatusEnabled == true && $serviceCall ]]; then
    $serviceCall ${MOTEUR_WORKFLOWID} ${JOBID} 6
fi

cleanup

STOP=$(date +%s)
info "Stop date is ${STOP}"
TOTAL=$((STOP - START))
info "Total running time: $TOTAL seconds"
UPLOAD=$((STOP - BEFOREUPLOAD))
DOWNLOAD=$((AFTERDOWNLOAD - START))
info "Input download time: ${DOWNLOAD} seconds"
info "Execution time: $(expr $BEFOREUPLOAD - $AFTERDOWNLOAD) seconds"
info "Results upload time: ${UPLOAD} seconds"
info "Exiting with return value 0"
info "(HACK for ARC: writing it in ${DIAG})"
echo "exitcode=0" >> ${DIAG}
exit 0

stopLog footer