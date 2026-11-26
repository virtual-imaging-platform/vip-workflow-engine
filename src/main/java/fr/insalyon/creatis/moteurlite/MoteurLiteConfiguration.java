package fr.insalyon.creatis.moteurlite;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MoteurLiteConfiguration {
    final private String gridaServerConf;
    final private String gridaProxy;
    final private int maxJobsPerWorkflow;
    final private int maxContentDispositionLength;
    final private ArrayList<String> validDomains;

    public MoteurLiteConfiguration()
            throws MoteurLiteException {
        try {
            PropertiesConfiguration config = parseConfig(new File("conf/settings.conf"));
            gridaServerConf = config.getString("moteurlite.grida.serverconf");
            gridaProxy = config.getString("moteurlite.grida.proxy");
            maxJobsPerWorkflow = config.getInt("moteurlite.maxjobsperworkflow", 1000);
            maxContentDispositionLength = config.getInt("moteurlite.maxcontentdispositionlength", 8192);
            validDomains = new ArrayList<>(Arrays.asList(config.getStringArray("moteurlite.httpurl.domain.whitelist")));
            if (gridaServerConf == null || gridaProxy == null) {
                throw new MoteurLiteException("Missing parameters");
            }
        } catch (ConfigurationException e) {
            throw new MoteurLiteException("Error parsing configuration", e);
        }
    }

    public static PropertiesConfiguration parseConfig(File file) throws ConfigurationException {
        PropertiesConfiguration config = new PropertiesConfiguration(file);
        config.setDelimiterParsingDisabled(false);
        config.setDefaultListDelimiter(',');
        return config;
    }

    public String getGridaProxy() {
        return gridaProxy;
    }

    public String getGridaServerConf() {
        return gridaServerConf;
    }

    public int getMaxJobsPerWorkflow() {
        return maxJobsPerWorkflow;
    }

    public int getMaxContentDispositionLength() {
        return maxContentDispositionLength;
    }

    public ArrayList<String> getValidDomains() {
        return validDomains;
    }
}
