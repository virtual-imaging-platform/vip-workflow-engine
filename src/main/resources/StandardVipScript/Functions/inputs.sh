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