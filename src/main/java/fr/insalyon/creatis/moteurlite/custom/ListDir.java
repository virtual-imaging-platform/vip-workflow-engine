package fr.insalyon.creatis.moteurlite.custom;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insalyon.creatis.grida.common.bean.GridPathInfo;
import fr.insalyon.creatis.moteurlite.MoteurLiteException;
import fr.insalyon.creatis.moteurlite.boutiques.model.BoutiquesDescriptor;
import fr.insalyon.creatis.moteurlite.boutiques.model.Custom;

import fr.insalyon.creatis.grida.common.bean.GridData;
import fr.insalyon.creatis.grida.client.GRIDAClient;
import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.grida.client.StandaloneGridaClient;

import org.apache.log4j.Logger;

public class ListDir {
    private static final Logger logger = Logger.getLogger(ListDir.class);
    /**
     * <p>
     * Checks whether input path names are files or directories: files are left as is,
     * and directories are expanded into a list of files matching the provided patterns.
     * Raises an exception on I/O error, or when an inputs ends of with zero values.
     * </p>
     * <pre>
     * "vip:listDir":{"inputs":{"input1":{"patterns":["*.nii","*.nii.gz"],...}}}
     * </pre>
     */
    public static Map<String, List<String>> listDir(Map<String, List<String>> inputsMap,
                                                    BoutiquesDescriptor boutiquesDescriptor)
            throws MoteurLiteException {
        logger.info("XXX entering listDir");
        // Get vip:listDir items, leave inputsMap unchanged if key is missing or empty
        Custom custom = boutiquesDescriptor.getCustom();
        if (custom == null)
            return inputsMap;
        Custom.VipListDir vipListDir = custom.vipListDir;
        if (vipListDir == null)
            return inputsMap;
        Map<String, Custom.VipListDir.VipListDirItem> dirItems = vipListDir.inputs;
        if (dirItems == null)
            return inputsMap;
        // Load GRIDAClient for file access
        // XXX TODO use grida standalone, get settings from settings.conf
        // GRIDAClient client = new GRIDAClient("localhost", 9006, "/var/www/html/workflows/x509up_server");
        GRIDAClient client = new StandaloneGridaClient("/var/www/html/workflows/x509up_server",
                new File("/var/www/prod/grida/grida-server.conf"));
        logger.info("XXX GRIDAClient="+client);
        // Expand inputsMap into result
        Map<String, List<String>> result = new HashMap<>();
        for (String inputId : inputsMap.keySet()) {
            List<String> values = inputsMap.get(inputId);
            Custom.VipListDir.VipListDirItem dirItem = dirItems.get(inputId);
            if (dirItem != null) { // process vip:listDir for inputId
                List<String> files = new ArrayList<>();
                for (String pathName : values) {
                    try {
                        if (pathIsDirectory(client, pathName)) { // expand directory into a list of files
                            files.addAll(expandDirToFiles(client, inputId, pathName, dirItem));
                        } else { // path is a file, keep as is
                            files.add(pathName);
                        }
                    } catch (GRIDAClientException e) {
                        throw new MoteurLiteException("GRIDAClientException:", e);
                    }
                }
                if (files.isEmpty()) { // do not allow an input to have no value
                    throw new MoteurLiteException("vip:listDir: empty files list for input " + inputId);
                }
                result.put(inputId, files);
            } else { // keep other inputs as is
                result.put(inputId, values);
            }
        }
        return result;
    }

    private static boolean pathIsDirectory(GRIDAClient client, String pathName)
            throws GRIDAClientException {
        if (!(pathName.startsWith("lfn:") || pathName.startsWith("file:")))
            return false;
        GridPathInfo pathInfo = client.getPathInfo(pathName);
        return pathInfo.getType() == GridData.Type.Folder;
    }

    private static List<String> expandDirToFiles(GRIDAClient client, String inputId, String pathName,
                                                 Custom.VipListDir.VipListDirItem dirItem)
            throws GRIDAClientException {
        // get all files in directory
        List<GridData> allFiles = client.getFolderData(pathName, true);
        // build list of pattern matchers. A missing or empty "patterns" list generate no matches.
        List<PathMatcher> matchers = new ArrayList<>();
        if (dirItem.patterns != null) {
            for (String pattern : dirItem.patterns) {
                matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + pattern));
            }
        }
        // filter files that match at least one pattern
        List<String> matchingFiles = new ArrayList<>();
        for (GridData file : allFiles) {
            if (file.getType() == GridData.Type.File) {
                String filename = file.getName();
                for (PathMatcher matcher : matchers) {
                    Path dirname = Paths.get(filename);
                    if (matcher.matches(dirname)) {
                        // XXX preserve lfn/file prefix and proper append dirname+filename
                        matchingFiles.add(dirname.resolve(filename).toString());
                        break;
                    }
                }
            }
        }
        return matchingFiles;
    }
}