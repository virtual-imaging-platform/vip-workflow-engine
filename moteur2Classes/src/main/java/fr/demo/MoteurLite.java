package fr.insalyon.creatis.moteurlite;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        GaswParser gaswParser = new GaswParser();
        gaswParser.parse(gaswFilePath);
        Map<String, String> inputsMap = new DataSetParser(inputsFilePath).getInputValues();
        GaswInput gaswInput = gaswParser.getGaswInput(inputsMap);
        GaswMonitor gaswMonitor = new GaswMonitor();
        gasw.setNotificationClient(gaswMonitor);
        gaswMonitor.start();
        String jobID = gasw.submit(gaswInput);
        System.out.println("job launched : " + jobID);
        System.out.println("Waiting for Notification");
    }
    public class GaswMonitor extends Thread {     
        public GaswMonitor() {}
        public void run() {        
            labelx:
            while(!MoteurLite.this.stop) {
                synchronized(this) {
                    try {
                        this.wait();
                    } catch (InterruptedException var10) {
                        var10.printStackTrace();
                    }
                }
            java.util.Iterator<GaswOutput> i$ = MoteurLite.this.gasw.getFinishedJobs().iterator();
            GaswOutput gaswOutput;

                System.out.println(i$);
                if (!i$.hasNext()) {
                    MoteurLite.this.gasw.waitForNotification();
                    continue labelx;
                } 
            gaswOutput = (GaswOutput)i$.next();
            
            System.out.println("Status : " + gaswOutput.getExitCode());
            MoteurLite.this.stop = true;
            }
            try {
                gasw.terminate();

                System.out.println("Exiting");
            } catch (GaswException e) {
                e.printStackTrace();
            }}}
            
}
