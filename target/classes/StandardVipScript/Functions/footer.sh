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