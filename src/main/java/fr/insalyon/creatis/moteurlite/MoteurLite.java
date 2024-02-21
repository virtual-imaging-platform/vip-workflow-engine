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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.sound.sampled.Port;

import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import fr.cnrs.i3s.moteur2.plugins.PluginException;
import fr.cnrs.i3s.moteur2.execution.*;
import fr.cnrs.i3s.moteur2.gui.NodeEditor;
import fr.cnrs.i3s.moteur2.gui.ProcessorPanel;

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
    int sizeOfInputs;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static void main(String[] args) throws GaswException, SAXException, IOException, URISyntaxException,
            InvalidBoutiquesDescriptorException, FileNotFoundException, IOException, ParseException, PluginException {
        File f1 = new File("/home/r/MoteurLite/moteurlite/input/new.tar.gz");
        File f2 = new File("/home/r/MoteurLite/moteurlite/output/new1.tar.gz"); // file to be delete (temporary lines-to be removed)
        File f3 = new File("/home/r/MoteurLite/moteurlite/output/new1_1.tar.gz");
        f1.delete();
        f2.delete();
        f3.delete();
        new MoteurLite(args);
    }

    public MoteurLite(String[] args) throws GaswException, SAXException, IOException, URISyntaxException,
            InvalidBoutiquesDescriptorException, FileNotFoundException, IOException, ParseException, PluginException {
        String gaswFilePath = args[0];
        String inputsFilePath = args[1];
        String boutiquesFilePath = args[2];
        String workflowId = "55555555555"; // args[3];
        String value = "sandesh";
        value = "file://" + new URI(value + ".tar.gz").getPath();
        System.out.println("Value:" + value);
        String processor = "r";
        createWorkflow(workflowId, processor);
        GaswParser gaswParser = new GaswParser();
        System.out.println(boutiquesFilePath);
        List<Map<String, String>> crossInputs = new DataSetParser(inputsFilePath).getInputValuesCross();
        List<Map<String, String>> dotInputs = new DataSetParser(inputsFilePath).getInputValuesDot();
        List<Map<String, String>> inputs = new ArrayList<>();
        /*try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter iteration Strategy:\n1. Cross Iteration\n2. Dot Iteration ");
            int iteration = scanner.nextInt();
            if (iteration == 1) {
                inputs = crossInputs;
            } else if (iteration == 2) {
                inputs = dotInputs;
            } else {
                System.out.println("Invalid input");
                return;
            }
        }*/
        inputs = dotInputs;
        GaswMonitor gaswMonitor = new GaswMonitor();
        gasw.setNotificationClient(gaswMonitor);
        gaswMonitor.start();
        Map<String, String> inputsMap = new HashMap<>();
        for (Map<String, String> innerList : inputs) {
            for (Map.Entry<String, String> entry : innerList.entrySet()) {
                inputsMap.put(entry.getKey(), entry.getValue());
            }
            System.out.println(ANSI_GREEN + "inputsMap:" + inputsMap + ANSI_RESET);
            GaswInput gaswInput = gaswParser.getGaswInput(inputsMap, boutiquesFilePath);
            System.out.println(ANSI_GREEN + System.getProperty("user.dir") + ANSI_RESET);
            String jobID = gasw.submit(gaswInput);
            System.out.println("job launched : " + jobID);
        }
        System.out.println("Waiting for Notification");
        System.out.println(ANSI_RED + System.getProperty("user.dir") + "RRRRRRRRRRRRR" + ANSI_RESET);
        System.out.println(ANSI_RED + "Navigating to directory: " + System.getProperty("user.dir") + ANSI_RESET);
        File A = new File("temp");
        A.mkdirs();
        System.out.println(ANSI_RED + "Navigating to directory: " + A + ANSI_RESET);
    }

    public static String test() {
        return "e";
    }

    public void createWorkflow(String workflowId, String processor) throws PluginException {
        try {
            WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
            WorkflowDAO workflowDAO = workflowsDBDAOFactory.getWorkflowDAO();
            ProcessorDAO processorDAO = workflowsDBDAOFactory.getProcessorDAO();
            Workflow workflow = new Workflow();
            System.out.println("workflowId:" + " " + workflowId);
            workflow.setId(workflowId);
            workflow.setUsername("sandesh");
            workflow.setStatus(WorkflowStatus.Queued);
            workflow.setStartedTime(new Date());
            Processor p = new Processor();
            ProcessorID processorID = new ProcessorID(workflowId, processor);
            p.setProcessorID(processorID);
            processorDAO.add(p);
            System.out.println("adding workflow in workflowsdb : " + workflowId);
            workflowDAO.add(workflow);
            System.out.println("added workflow in workflowsdb : " + workflowId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class GaswMonitor extends Thread {
        public GaswMonitor() {
        }

        public void run() {
            labelx: while (!MoteurLite.this.stop) {
                System.out.println("Waiting for Notification");
                synchronized (this) {
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
                if (!i$.hasNext()) {
                    MoteurLite.this.gasw.waitForNotification();
                    continue labelx;
                }
                gaswOutput = (GaswOutput) i$.next();
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
