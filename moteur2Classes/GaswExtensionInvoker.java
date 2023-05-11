//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fr.cnrs.i3s.moteur2.execution;

import fr.cnrs.i3s.moteur2.data.Data;
import fr.cnrs.i3s.moteur2.data.DataItem;
import fr.cnrs.i3s.moteur2.data.DataLine;
import fr.cnrs.i3s.moteur2.execution.Invoker.Invocation;
import fr.cnrs.i3s.moteur2.graph.Port;
import fr.cnrs.i3s.moteur2.processor.GaswExtension;
import fr.cnrs.i3s.moteur2.processor.InputPort;
import fr.cnrs.i3s.moteur2.processor.OutputPort;
import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswExitCode;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.gasw.GaswOutput;
import fr.insalyon.creatis.gasw.parser.GaswParser;
import grool.configuration.GroolDefaultValues;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import jigsaw.exception.JigsawException;
import jigsaw.transfer.JigsawFileSystem;

public class GaswExtensionInvoker extends Invoker {
    private static final HashMap<Workflow, GaswExtensionInvoker> invokers = new HashMap();
    private GaswExtensionInvoker.GaswExtensionThread gthread;
    private LinkedList<Invocation> events;
    private JigsawFileSystem transfer;
    private static File gaswDir = null;

    public static GaswExtensionInvoker getInvoker(Workflow workflow) {
        GaswExtensionInvoker invoker = null;
        synchronized(invokers) {
            invoker = (GaswExtensionInvoker)invokers.get(workflow);
        }

        if (invoker == null) {
            invoker = new GaswExtensionInvoker(workflow);
            synchronized(invokers) {
                invokers.put(workflow, invoker);
            }
        }

        if (invoker.thread == null) {
            invoker.startInvoker();
        }

        return invoker;
    }

    protected GaswExtensionInvoker(Workflow workflow) {
        super(workflow);
    }

    protected void invoke(Invocation invocation) {
        try {
            if (this.gthread == null) {
                this.events = new LinkedList();
                this.gthread = new GaswExtensionInvoker.GaswExtensionThread();
                this.gthread.start();
            }

            synchronized(this.gthread) {
                this.events.add(invocation);
                this.gthread.notify();
            }
        } catch (GaswException var5) {
            var5.printStackTrace();
        }

    }

    protected void terminate() {
        super.terminate();
        if (this.gthread != null) {
            this.gthread.terminate();
            this.gthread = null;
            this.thread = null;
        }

    }

    private synchronized String getDescriptorFile(GaswExtension processor) throws JigsawException, URISyntaxException {
        if (gaswDir == null) {
            gaswDir = processor.getWorkflow().getGaswTmpDir();
            if (!gaswDir.exists()) {
                gaswDir.mkdir();
                gaswDir.setWritable(true, false);
            }
        }

        URI descriptor = processor.getDescriptor();
        File descFile = new File(descriptor.getPath());

        String dirname;
        for(dirname = descFile.getParent().replaceAll("/", "-"); dirname.startsWith("-"); dirname = dirname.substring(1)) {
        }

        File dir = new File(gaswDir, dirname);
        if (!dir.exists()) {
            dir.mkdirs();
            dir.setWritable(true, false);
        }

        File gaswFile = new File(dir, descFile.getName());
        boolean isRemoteFile = true;
        String scheme = descriptor.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme) && !"ftp".equalsIgnoreCase(scheme)) {
            if (!"lfn".equalsIgnoreCase(scheme) && !descriptor.getPath().startsWith("/grid/")) {
                gaswFile = new File(descriptor.getPath());
                isRemoteFile = false;
            } else {
                descriptor = new URI("lfn", (String)null, GroolDefaultValues.getDefaultLfcServerHost(), GroolDefaultValues.getDefaultLfcServerPort(), descriptor.getPath(), (String)null, (String)null);
                File proxy = processor.getWorkflow().getProxy();
                if (proxy == null) {
                    throw new JigsawException("No proxy available: initialize a grid proxy first.");
                }

                this.transfer = new JigsawFileSystem(proxy, processor.getWorkflow().getVomsServer().getName());
            }
        } else {
            this.transfer = new JigsawFileSystem();
        }

        if (isRemoteFile) {
            boolean exists = gaswFile.exists();
            boolean updated = true;
            if (exists) {
                long remoteCreationDate = this.transfer.getModificationTime(descriptor);
                if (gaswFile.lastModified() < remoteCreationDate) {
                    updated = false;
                }
            }

            if (!exists || !updated) {
                int retryCount = 0;

                while(retryCount < 3) {
                    try {
                        this.transfer.copy(descriptor, gaswFile.toURI());
                        break;
                    } catch (JigsawException var13) {
                        ++retryCount;
                        if (retryCount == 3) {
                            throw var13;
                        }
                    }
                }
            }
        }

        return gaswFile.getAbsolutePath();
    }

    class GaswExtensionThread extends Thread {
        private boolean stop = false;
        private Gasw gasw;
        private GaswExtensionInvoker.GaswExtensionThread.GaswMonitorThread gaswMonitor;
        private volatile Map<String, Invocation> invocationsMap = new HashMap();

        public GaswExtensionThread() throws GaswException {
            if (this.gaswMonitor == null) {
                this.gaswMonitor = new GaswExtensionInvoker.GaswExtensionThread.GaswMonitorThread();
                this.gaswMonitor.start();
            }

            this.gasw = Gasw.getInstance();
            this.gasw.setNotificationClient(this.gaswMonitor);
        }

        public void run() {
            while(!this.stop) {
                boolean cond;
                synchronized(this) {
                    cond = !GaswExtensionInvoker.this.events.isEmpty();
                }

                while(cond) {
                    synchronized(this) {
                        Invocation invocation = (Invocation)GaswExtensionInvoker.this.events.removeFirst();
                        this.addInvocation(invocation);
                        cond = !GaswExtensionInvoker.this.events.isEmpty();
                    }
                }

                try {
                    synchronized(this) {
                        this.wait();
                    }
                } catch (InterruptedException var7) {
                }
            }

        }

        private void addInvocation(Invocation invocation) {
            try {
                GaswExtension processor = (GaswExtension)invocation.getProcessor();
                String fileName = GaswExtensionInvoker.this.getDescriptorFile(processor);
                GaswParser gaswParser = new GaswParser();
                gaswParser.parse(fileName);
                DataLine line = invocation.getDataLine();
                Map<String, String> inputsMap = new HashMap();
                Iterator i$ = processor.getInputPorts().iterator();

                while(i$.hasNext()) {
                    Port p = (Port)i$.next();
                    InputPort port = (InputPort)p;
                    Data data = line.get(port);
                    String portName = port.getName();
                    if (portName.startsWith("input")) {
                        portName = gaswParser.getInputName(new Integer(portName.substring(5)));
                    }

                    String value = data.dataString();
                    inputsMap.put(portName, value);
                }

                GaswInput gaswInput = gaswParser.getGaswInput(inputsMap);
                String jobID = this.gasw.submit(gaswInput);
                this.invocationsMap.put(jobID, invocation);
            } catch (GaswException var13) {
                var13.printStackTrace();
                GaswExtensionInvoker.this.notifyFailed(invocation, "GaswExtension execution failure: processor " + invocation.getProcessor().getName() + ":\n" + var13.toString());
            } catch (URISyntaxException var14) {
                var14.printStackTrace();
                GaswExtensionInvoker.this.notifyFailed(invocation, "GaswExtension execution failure: processor " + invocation.getProcessor().getName() + ":\n" + var14.toString());
            } catch (JigsawException var15) {
                var15.printStackTrace();
                GaswExtensionInvoker.this.notifyFailed(invocation, "Jigsaw parsing failure: processor " + invocation.getProcessor().getName() + ":\n" + var15.toString());
            }

        }

        public void terminate() {
            try {
                this.gasw.terminate();
            } catch (GaswException var7) {
                var7.printStackTrace();
            }

            this.stop = true;
            synchronized(this.gaswMonitor) {
                this.gaswMonitor.notify();
            }

            synchronized(this) {
                this.notify();
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException var4) {
            }

        }

        private class GaswMonitorThread extends Thread {
            public GaswMonitorThread() {
            }

            public void run() {
                label54:
                while(!GaswExtensionThread.this.stop) {
                    synchronized(this) {
                        try {
                            this.wait();
                        } catch (InterruptedException var10) {
                            var10.printStackTrace();
                        }
                    }

                    Iterator i$ = GaswExtensionThread.this.gasw.getFinishedJobs().iterator();

                    while(true) {
                        GaswOutput gaswOutput;
                        Invocation invocation;
                        do {
                            if (!i$.hasNext()) {
                                GaswExtensionThread.this.gasw.waitForNotification();
                                continue label54;
                            }

                            gaswOutput = (GaswOutput)i$.next();
                            invocation = (Invocation)GaswExtensionThread.this.invocationsMap.get(gaswOutput.getJobID());
                        } while(invocation == null);

                        if (gaswOutput.getExitCode() != GaswExitCode.SUCCESS) {
                            GaswExtensionInvoker.this.notifyFailed(invocation, "GASW Exit code: " + gaswOutput.getExitCode());
                        } else {
                            GaswExtension processor = (GaswExtension)invocation.getProcessor();
                            HashMap<OutputPort, Data> produced = invocation.getProduced();
                            Iterator<Port> iterator = processor.getOutputPorts().iterator();

                            for(int i = 0; i < processor.getOutputPorts().size(); ++i) {
                                OutputPort port = (OutputPort)iterator.next();
                                Data data = new DataItem(gaswOutput.getUploadedResults().get(i), (int[])null);
                                produced.put(port, data);
                            }

                            GaswExtensionInvoker.this.notifyRan(invocation);
                        }

                        GaswExtensionThread.this.invocationsMap.remove(gaswOutput.getJobID());
                    }
                }

            }
        }
    }
}

