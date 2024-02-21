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
        import java.sql.Connection;
        import java.sql.DriverManager;
        import java.sql.ResultSet;
        import java.sql.SQLException;
        import java.sql.Statement;
        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.Random;
        import java.util.Scanner;
        import java.util.Set;
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
        import fr.insalyon.creatis.gasw.GaswConstants;
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
        import fr.insalyon.creatis.gasw.parser.GaswArgument;
        import fr.insalyon.creatis.gasw.parser.GaswInputArg;
        import fr.insalyon.creatis.gasw.parser.GaswOutputArg;
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
            private List<URI> downloads;
            private GaswInputArg inputArg;
            private List<GaswArgument> arguments;
            private GaswOutputArg outputArg;
            private List<String> inputsList;
        
            public static void main(String[] args) throws GaswException, SAXException, IOException, URISyntaxException,
                    InvalidBoutiquesDescriptorException, FileNotFoundException, IOException, ParseException, PluginException {
                new MoteurLite(args);
            }
        
            public MoteurLite(String[] args) throws GaswException, SAXException, IOException, URISyntaxException,
                    InvalidBoutiquesDescriptorException, FileNotFoundException, IOException, ParseException, PluginException {
                //String gaswFilePath = args[0];
                String inputsFilePath = args[1];
                String boutiquesFilePath = args[2];   
                String workflowId = String.valueOf(new Random().nextInt());
                workflowId = "workflow" + workflowId;
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
        
                try (Scanner scanner = new Scanner(System.in)) {
                    System.out.print("Enter iteration Strategy:\n1. Cross Iteration\n2. Dot Iteration ");
                    int iteration = scanner.nextInt();
                    if (iteration == 1) {
                        inputs = crossInputs;
                        sizeOfInputs = crossInputs.size();
                    } else if (iteration == 2) {
                        inputs = dotInputs;
                        sizeOfInputs = dotInputs.size();
                    } else {
                        System.out.println("Invalid input");
                        return;
                    }
                }
                GaswMonitor gaswMonitor = new GaswMonitor();
                gasw.setNotificationClient(gaswMonitor);
                gaswMonitor.start();
                //String executableName = test(boutiquesFilePath);
                ParseBoutiquesFile parseBoutiquesFile = new ParseBoutiquesFile();
                String executableName = parseBoutiquesFile.ParseBoutiquesFile(boutiquesFilePath);
                String download = parseBoutiquesFile.getDownloadFile();
                HashMap<Integer, String> inputid = parseBoutiquesFile.getInputIdOfBoutiquesFile();
                HashMap<Integer, String> outputid = parseBoutiquesFile.getOutputIdOfBoutiquesFile();
        
                Map<String, String> inputsMap = new HashMap<>();
        
                for (Map<String, String> innerList : inputs) {
                    for (Map.Entry<String, String> entry : innerList.entrySet()) {
                        inputsMap.put(entry.getKey(), entry.getValue());
                    }
                    System.out.println(ANSI_GREEN + "inputsMap:" + inputsMap + ANSI_RESET);
                    //String executableName = getArgumentML(boutiquesFilePath);
        
                    GaswInput gaswInput = gaswParser.getGaswInput(inputsMap, boutiquesFilePath, executableName, inputid, outputid, download);
        
                    String jobID = gasw.submit(gaswInput);
                    System.out.println("job launched : " + jobID);
                }
        
                String url = "jdbc:h2:/home/r/MoteurLite/moteurlite/db/jobs"; // Change 'test' to your database name
                String user = "gasw";
                String password = "gasw";
        
                // Establish a connection
                try (Connection connection = DriverManager.getConnection(url, user, password)) {
                    System.out.println("Connected to the database!");
                    Statement statement = connection.createStatement();
        
                    // Execute a query to retrieve the status from the jobs table
                    String query = "Select * from JOBS";
                    ResultSet resultSet = statement.executeQuery(query);
                    System.out.println("Query executed: " + resultSet);
        
                    // Print the status
                    /*while (resultSet.next()) {
                        String status = resultSet.getString("status");
                        System.out.println("Status: " + status);
                    }*/
        
                    // Perform database operations here
                } catch (SQLException e) {
                    System.out.println("Connection failed!");
                    e.printStackTrace();
                }
        
        
                System.out.println("Waiting for Notification");
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
                /*private Connection connection;
                public GaswMonitor(Connection connection) {
                this.connection = connection;
                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery("SELECT status FROM jobs");
                    System.out.println("status : " + resultSet);
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                }*/
        
                // Constructor
                public GaswMonitor() {}
        
                // Run method
                public void run() {
                    Integer finishedJobsNumber = 0;
        
                    // Continuously monitor gasw until instructed to stop
                    while (finishedJobsNumber < sizeOfInputs) {
                        System.out.println("Waiting for Notification" + finishedJobsNumber);
                        System.out.println("Number of total jobs: " + sizeOfInputs); // Wait for notification
                        synchronized (this) {
                            try {
                                MoteurLite.this.gasw.waitForNotification();
                                this.wait(); // Wait for notification
        
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
        
                        // Get the list of finished jobs
                        List<GaswOutput> finishedJobs = MoteurLite.this.gasw.getFinishedJobs();
                        System.out.println("Number of finished jobs: " + finishedJobs.size());
        
                        // If there are no finished jobs, wait for notification again
                        //System.out.println("Number of finished jobs: " + finishedJobs.isEmpty());
                        if (finishedJobs.isEmpty()) {
                            MoteurLite.this.gasw.waitForNotification();
                            continue; // Continue to next iteration
                        }
        
                        // Process finished jobs using enhanced for loop
                        for (GaswOutput gaswOutput : finishedJobs) {
                            System.out.println("Status: " + gaswOutput.getJobID() + " " + gaswOutput.getExitCode()); // Print status
                        }
        
                        finishedJobsNumber += finishedJobs.size();
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
        