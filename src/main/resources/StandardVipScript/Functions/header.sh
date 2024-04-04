startLog header
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

if [[ $minorStatusEnabled == true && $serviceCall ]]; then
    $serviceCall ${MOTEUR_WORKFLOWID} ${JOBID} 1
fi

BACKPID=""

# DIRAC may wrongly position this variable
if [ ! -d ${X509_CERT_DIR} ]; then
    echo "Unsetting invalid X509_CERT_DIR (${X509_CERT_DIR})"
    unset X509_CERT_DIR
fi

echo "END date is $(date +%s)"

stopLog header
