package fr.insalyon.creatis.moteurlite;

import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswExitCode;
import fr.insalyon.creatis.gasw.GaswOutput;
import fr.insalyon.creatis.gasw.execution.GaswStatus;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class GaswMonitor extends Thread {
    private static final Logger logger = Logger.getLogger(GaswMonitor.class);

    private String workflowId;
    private String applicationName;
    private int numberOfInvocations;
    private Gasw gasw;
    private WorkflowsDbRepository workflowsDbRepository;

    public GaswMonitor(String workflowId, String applicationName, int numberOfInvocations, Gasw gasw) {
        this.workflowId = workflowId;
        this.applicationName = applicationName;
        this.numberOfInvocations = numberOfInvocations;
        this.gasw = gasw;
    }

    @Override
    public void run() {
        Integer finishedJobsNumber = 0;
        Integer successfulJobsNumber = 0;
        Integer failedJobsNumber = 0;

        while (finishedJobsNumber < numberOfInvocations) {
            synchronized (this) {
                try {
                    gasw.waitForNotification();
                    this.wait();
                } catch (InterruptedException e) {
                    logger.error("Interrupted exception while waiting for notification: ", e);
                }
            }

            List<GaswOutput> finishedJobs = gasw.getFinishedJobs();
            logger.info("Number of finished jobs: " + finishedJobs.size());

            if (finishedJobs.isEmpty()) {
                gasw.waitForNotification();
                continue;
            }

            for (GaswOutput gaswOutput : finishedJobs) {
                logger.info("Status: " + gaswOutput.getJobID() + " " + gaswOutput.getExitCode());
                try {
                    GaswExitCode exitCode = gaswOutput.getExitCode();
                    if (exitCode == GaswExitCode.SUCCESS) {
                        successfulJobsNumber++;
                    } else {
                        failedJobsNumber++;
                    }

                    List<URI> uploadedResults = gaswOutput.getUploadedResults();
                    if (uploadedResults != null && !uploadedResults.isEmpty()) {
                        workflowsDbRepository.persistOutputs(workflowId, null, uploadedResults);
                    }
                } catch (Exception e) {
                    logger.error("Error while processing finished job output: ", e);
                }
            }

            finishedJobsNumber += finishedJobs.size();
            try {
                workflowsDbRepository.persistProcessors(workflowId, applicationName, numberOfInvocations - finishedJobsNumber, successfulJobsNumber, failedJobsNumber);
            } catch (Exception e) {
                logger.error("Error while persisting processors during processing: ", e);
            }
        }

        try {
            // Determine the final status of the processor based on the jobs' status
            GaswStatus finalStatus = successfulJobsNumber > 0 ? GaswStatus.COMPLETED : GaswStatus.ERROR;

            // Persist the final workflow status
            workflowsDbRepository.persistWorkflows(workflowId, finalStatus);

            gasw.terminate();
            logger.info("Completed execution of workflow");
        } catch (GaswException e) {
            logger.error("Error while terminating Gasw: ", e);
        } catch (Exception e) {
            logger.error("Error while persisting final workflow status: ", e);
        }
    }
}