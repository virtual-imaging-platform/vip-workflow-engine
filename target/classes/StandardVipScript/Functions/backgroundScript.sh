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