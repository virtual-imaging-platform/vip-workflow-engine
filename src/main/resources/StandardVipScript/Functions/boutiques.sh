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
####################################################################################################
####################################################################################################
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
####################################################################################################
function boutiques_exec {
echo $JSONFILE
echo "import sys; sys.setdefaultencoding(\"UTF8\")" > sitecustomize.py
COMMAND_LINE="PYTHONPATH=".:$PYTHONPATH" $BOSHEXEC exec launch -x --provenance_path ./provenance_file.json $jsonFileName ../inv/$invocationJson -v $PWD/../cache:$PWD/../cache"
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
####################################################################################################
####################################################################################################
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

boutiques_exec
provenance
