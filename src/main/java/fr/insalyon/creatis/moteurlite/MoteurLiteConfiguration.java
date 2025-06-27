package fr.insalyon.creatis.moteurlite;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;

public class MoteurLiteConfiguration {
    final private String gridaServerConf;
    final private String gridaProxy;
    final private int maxJobsPerWorkflow;

    public MoteurLiteConfiguration()
            throws MoteurLiteException {
        try {
            PropertiesConfiguration config = new PropertiesConfiguration(new File("conf/settings.conf"));
            gridaServerConf = config.getString("moteurlite.grida.serverconf");
            gridaProxy = config.getString("moteurlite.grida.proxy");
            maxJobsPerWorkflow = config.getInt("moteurlite.maxjobsperworkflow", 1000);
            if (gridaServerConf == null || gridaProxy == null) {
                throw new MoteurLiteException("Missing parameters");
            }
        } catch (ConfigurationException e) {
            throw new MoteurLiteException("Error parsing configuration", e);
        }
    }

    public String getGridaProxy() { return gridaProxy; }
    public String getGridaServerConf() { return gridaServerConf; }
    public int getMaxJobsPerWorkflow() { return maxJobsPerWorkflow; }
}
