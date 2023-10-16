package fr.insalyon.creatis.moteurlite;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sound.sampled.Port;

import org.xml.sax.SAXException;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswConfiguration;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswExitCode;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.gasw.GaswNotification;
import fr.insalyon.creatis.gasw.GaswOutput;
import fr.insalyon.creatis.gasw.bean.Job;
import fr.insalyon.creatis.gasw.dao.DAOException;
import fr.insalyon.creatis.gasw.dao.DAOFactory;
import fr.insalyon.creatis.gasw.execution.GaswOutputParser;
import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.gasw.parser.GaswParser;
import fr.insalyon.creatis.gasw.plugin.ListenerPlugin;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.WorkflowsDBListener;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Workflow;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.WorkflowStatus;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowsDBDAOException;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowsDBDAOFactory;
import javassist.bytecode.Descriptor.Iterator;



public class MoteurLite {
    private boolean stop = false;
    Gasw gasw = Gasw.getInstance();
    GaswNotification gaswNotification;
    public static void main(String[] args) throws GaswException, SAXException, IOException, URISyntaxException {
        File f= new File("/home/r/Documents/MoteurLite1/moteurlite/input/new.tar.gz");           //file to be delete  (temporary lines-to be removed)
        f.delete(); 
        new MoteurLite(args);

    }

    public MoteurLite(String[] args) throws GaswException, SAXException, IOException, URISyntaxException {
        String gaswFilePath = args[0];
        String inputsFilePath = args[1];
        String workflowId = String.valueOf(new Random().nextInt());
        GaswParser gaswParser = new GaswParser();
        gaswParser.parse(gaswFilePath);
        Map<String, String> inputsMap = new DataSetParser(inputsFilePath).getInputValues();
        GaswInput gaswInput = gaswParser.getGaswInput(inputsMap);
        GaswMonitor gaswMonitor = new GaswMonitor();
        gasw.setNotificationClient(gaswMonitor);
        gaswMonitor.start();
        String jobID = gasw.submit(gaswInput);
        createWorkflow(workflowId);
        System.out.println("job launched : " + jobID);
        System.out.println("Waiting for Notification");
    }

    public void createWorkflow(String workflowId) {


        try {
            WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
            WorkflowDAO workflowDAO = workflowsDBDAOFactory.getWorkflowDAO();

            Workflow workflow = new Workflow();
            workflow.setId(workflowId);
            workflow.setUsername("sandesh");
            workflow.setStatus(WorkflowStatus.Queued);
            workflow.setStartedTime(new Date());
            


            System.out.println("adding workflow in workflowsdb : " + workflowId);
            workflowDAO.add(workflow);
            System.out.println("added workflow in workflowsdb : " + workflowId);

        } catch (WorkflowsDBDAOException e) {
            e.printStackTrace();
        }
        
    }

    public class GaswMonitor extends Thread {     
        public GaswMonitor() {}
        
        public void run() {        
            labelx:
            while(!MoteurLite.this.stop) {
                System.out.println("test");
                
                synchronized(this) {
                    try {
                        this.wait();
                    } catch (InterruptedException var10) {
                        var10.printStackTrace();
                    }
                }
            java.util.Iterator<GaswOutput> i$ = MoteurLite.this.gasw.getFinishedJobs().iterator();
            System.out.println(gasw.getFinishedJobs());
            GaswOutput gaswOutput;

            System.out.println(i$);
            //System.out.println(i$.next());  
            
                if (!i$.hasNext()) {
                    MoteurLite.this.gasw.waitForNotification();
                    continue labelx;
                } 
            gaswOutput = (GaswOutput)i$.next();
            
            System.out.println("Status : " + gaswOutput.getExitCode());
            
            System.out.println("Output Path : " + gaswOutput.getUploadedResults());
            MoteurLite.this.stop = true;
            }
            try {
                gasw.terminate();

                System.out.println("Exiting");
            } catch (GaswException e) {
                e.printStackTrace();
            }
        }}
}
