package fr.insalyon.creatis.moteurlite;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sound.sampled.Port;

import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import fr.cnrs.i3s.moteur2.plugins.PluginException;
import fr.cnrs.i3s.moteur2.execution.*;
import fr.cnrs.i3s.moteur2.gui.NodeEditor;
import fr.cnrs.i3s.moteur2.gui.ProcessorPanel;

//import com.google.gwt.core.ext.typeinfo.ParseException;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswConfiguration;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswExitCode;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.gasw.GaswNotification;
import fr.insalyon.creatis.gasw.GaswOutput;
import fr.insalyon.creatis.gasw.GaswUpload;
import fr.insalyon.creatis.gasw.bean.Job;
import fr.insalyon.creatis.gasw.dao.DAOException;
import fr.insalyon.creatis.gasw.dao.DAOFactory;
import fr.insalyon.creatis.gasw.execution.GaswOutputParser;
import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.gasw.parser.GaswParser;
import fr.insalyon.creatis.gasw.plugin.ListenerPlugin;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.WorkflowsDBListener;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.WorkflowsDBListenerFactory;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Workflow;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Processor;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.ProcessorID;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.WorkflowStatus;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.ProcessorDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowsDBDAOException;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowsDBDAOFactory;
import fr.insalyon.creatis.moteurlite.JacksonParser.BoutiquesEntities;
import fr.insalyon.creatis.moteurlite.JacksonParser.BoutiquesInput;
import fr.insalyon.creatis.moteurlite.JacksonParser.JacksonParser;
import fr.insalyon.creatis.moteurlite.JacksonParser.InvalidBoutiquesDescriptorException;
import javassist.bytecode.Descriptor.Iterator;


public class MoteurLite {
    private boolean stop = false;
    Gasw gasw = Gasw.getInstance();
    GaswNotification gaswNotification;
    String R;
    public static void main(String[] args) throws GaswException, SAXException, IOException, URISyntaxException, InvalidBoutiquesDescriptorException, FileNotFoundException, IOException, ParseException, PluginException{
        File f= new File("/home/r/MoteurLite/moteurlite/input/new.tar.gz");           //file to be delete  (temporary lines-to be removed)
        f.delete(); 
        new MoteurLite(args);
    }

    public MoteurLite(String[] args) throws GaswException, SAXException, IOException, URISyntaxException, InvalidBoutiquesDescriptorException, FileNotFoundException, IOException, ParseException, PluginException{
        String gaswFilePath = args[0];
        String inputsFilePath = args[1];
        String boutiquesFilePath = args[2];
        String value = "sandesh";
        value = "file://" + new URI(value + ".tar.gz").getPath();
        System.out.println("Value:" + value);



        
        String workflowId = String.valueOf(new Random().nextInt());
        GaswParser gaswParser = new GaswParser();

        //boutiques
        
        System.out.println(boutiquesFilePath);

        //ParseBoutiquesFile parseBoutiquesFile = new ParseBoutiquesFile();
        //String processor = parseBoutiquesFile.ParseBoutiquesFile(boutiquesFilePath);
        String processor = "r";
        
        
       
        
        //gaswParser.parse(gaswFilePath);
        Map<String, String> inputsMap = new DataSetParser(inputsFilePath).getInputValues();
        System.out.println("inputsMap:" + inputsMap);
 
      
        GaswMonitor gaswMonitor = new GaswMonitor();
        gasw.setNotificationClient(gaswMonitor);
        gaswMonitor.start();
        GaswInput gaswInput = gaswParser.getGaswInput(inputsMap, boutiquesFilePath);
        String jobID = gasw.submit(gaswInput);
        createWorkflow(workflowId, processor);
        
        System.out.println("job launched : " + jobID);
        System.out.println("Waiting for Notification");
    }


    public void createWorkflow(String workflowId, String processor) throws PluginException {


        try {
            WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
            WorkflowDAO workflowDAO = workflowsDBDAOFactory.getWorkflowDAO();
            ProcessorDAO processorDAO = workflowsDBDAOFactory.getProcessorDAO();
            WorkflowsDBListenerFactory workflowsDBListenerFactory = new WorkflowsDBListenerFactory();
            
            Workflow workflow = new Workflow();
            //fr.cnrs.i3s.moteur2.execution.Workflow wf = new fr.cnrs.i3s.moteur2.execution.Workflow();
            //fr.cnrs.i3s.moteur2.processor.Processor pr = new fr.cnrs.i3s.moteur2.processor.Processor() {

            //     @Override
            //     public fr.cnrs.i3s.moteur2.processor.Processor duplicate() {
            //         // TODO Auto-generated method stub
            //         throw new UnsupportedOperationException("Unimplemented method 'duplicate'");
            //     }

            //     @Override
            //     public Invoker getInvoker() {
            //         // TODO Auto-generated method stub
            //         throw new UnsupportedOperationException("Unimplemented method 'getInvoker'");
            //     }

            //     @Override
            //     public ProcessorPanel getPanel(NodeEditor arg0) {
            //         // TODO Auto-generated method stub
            //         throw new UnsupportedOperationException("Unimplemented method 'getPanel'");
            //     }
                
            // };

            

            //WorkflowsDBListener workflowsDBListener = new WorkflowsDBListener(wf);
            //System.out.println(workflowsDBListener.getClass());
            //workflowsDBListenerFactory.createWorkflowListner(wf);
            //workflowsDBListener = new WorkflowsDBListener(workflow);
            //workflowsDBListener.getClass();

            workflow.setId(workflowId);
            workflow.setUsername("sandesh");
            workflow.setStatus(WorkflowStatus.Queued);
            workflow.setStartedTime(new Date());

            Processor p = new Processor();
            ProcessorID processorID = new ProcessorID("workflow-"+ workflowId, processor);
            p.setProcessorID(processorID);
            //p.setCompleted(1);

            processorDAO.add(p);

           
            
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
                System.out.println("Waiting for Notification");
                
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
        }
    }
}



