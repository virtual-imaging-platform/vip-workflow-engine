startLog results_upload

# Perform service call if minor status is enabled
if [[ $minorStatusEnabled == true && $serviceCall ]]; then
    $serviceCall ${MOTEUR_WORKFLOWID} ${JOBID} 5
fi


# Iterate through output files to upload
for output_to_be_uploaded in "${outputs_to_be_uploaded[@]}"
do
    upload "$uploads" "$output_to_be_uploaded" "$(tr -dc '[:alpha:]' < /dev/urandom 2>/dev/null | head -c 32)" "$numberOfReplicas" false
done

__MOTEUR_OUT="$uploadsList"

stopLog results_upload