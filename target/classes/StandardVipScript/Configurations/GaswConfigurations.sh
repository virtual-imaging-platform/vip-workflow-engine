variables=(
    defaultSleeptime simulationID workflowID executionPath defaultBackgroundScript
    defaultCPUTime defaultEnvironment defaultExecutor voDefaultSE voUseCloseSE
    boshCVMFSPath containersCVMFSPath udockerTag failOverEnabled failOverHost
    failOverPort failOverHome minorStatusEnabled minAvgDownloadThroughput
    defaultRetryCount executorPlugins listenerPlugins sessionFactory failOverMaxRetry
    config
)

# Loop through the variables and read their values from the configuration file
for variable in "${variables[@]}"; do
    value=$(jq -r ".gaswConfiguration.$variable" "$configurationJson")
    eval "$variable=\"$value\""
done

for variable in "${variables[@]}"; do
    echo "$variable: ${!variable}"
done
