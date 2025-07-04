package fr.insalyon.creatis.moteurlite.custom;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.insalyon.creatis.grida.common.bean.GridPathInfo;
import fr.insalyon.creatis.moteurlite.MoteurLiteConfiguration;
import fr.insalyon.creatis.moteurlite.MoteurLiteException;
import fr.insalyon.creatis.boutiques.model.BoutiquesDescriptor;
import fr.insalyon.creatis.boutiques.model.Custom;
import fr.insalyon.creatis.boutiques.model.CustomDirectoryInputsItem;

import fr.insalyon.creatis.grida.common.bean.GridData;
import fr.insalyon.creatis.grida.client.GRIDAClient;
import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.grida.client.StandaloneGridaClient;

public class DirectoryInputsService {
    GRIDAClient gridaClient;

    public DirectoryInputsService(MoteurLiteConfiguration config)
            throws MoteurLiteException {
        // Load GRIDAClient for directory listing:
        // check that config file exists to fail early and systematically
        File file = new File(config.getGridaServerConf());
        if (!file.isFile()) {
            throw new MoteurLiteException("Can't get GRIDAClient");
        }
        gridaClient = new StandaloneGridaClient(config.getGridaProxy(), file);
    }

    /**
     * <p>
     * Checks whether input path names are files or directories: files are left as is,
     * and directories are expanded into a list of files matching the provided patterns.
     * Raises an exception on I/O error, or when an input directory contains no matching file.
     * </p>
     * <pre>
     * "vip:directoryInputs":{"input1":{"patterns":["*.nii","*.nii.gz"]},...}
     * </pre>
     */
    public void updateInputs(Map<String, List<String>> inputsMap,
                             BoutiquesDescriptor boutiquesDescriptor)
            throws MoteurLiteException {
        // Get vip:directoryInputs items, leave inputsMap unchanged if key is missing or empty
        Custom custom = boutiquesDescriptor.getCustom();
        if (custom == null) {
            return;
        }
        Map<String, CustomDirectoryInputsItem> directoryInputs = custom.getDirectoryInputs();
        if (directoryInputs == null) {
            return;
        }
        for (String inputId : inputsMap.keySet()) {
            List<String> values = inputsMap.get(inputId);
            CustomDirectoryInputsItem dirItem = directoryInputs.get(inputId);
            if (dirItem != null) { // process vip:directoryInputs for inputId
                List<String> files = new ArrayList<>();
                for (String pathName : values) {
                    try {
                        if (pathIsDirectory(pathName)) { // expand directory into a list of files
                            List<String> dirFiles = expandDirToFiles(pathName, dirItem);
                            if (dirFiles.isEmpty()) { // do not allow an input directory to have no matching file
                                throw new MoteurLiteException("vip:directoryInputs: empty files list for input " + inputId);
                            }
                            files.addAll(dirFiles);
                        } else { // path is a file, keep as is
                            files.add(pathName);
                        }
                    } catch (GRIDAClientException e) {
                        throw new MoteurLiteException("GRIDAClientException:", e);
                    }
                }
                inputsMap.put(inputId, files);
            }
        }
    }

    private static String toGridaPath(String pathName) {
        return pathName.replaceFirst("^file:", "");
    }

    private boolean pathIsDirectory(String pathName)
            throws GRIDAClientException {
        if (!(pathName.startsWith("lfn:") || pathName.startsWith("file:"))) {
            return false;
        }
        GridPathInfo pathInfo = gridaClient.getPathInfo(toGridaPath(pathName));
        return pathInfo.exist() && pathInfo.getType() == GridData.Type.Folder;
    }

    private List<String> expandDirToFiles(String pathName, CustomDirectoryInputsItem dirItem)
            throws GRIDAClientException {
        // Get all files in directory
        List<GridData> allFiles = gridaClient.getFolderData(toGridaPath(pathName), true);
        // Build list of pattern matchers. A missing or empty "patterns" list generate no matches.
        List<PathMatcher> matchers = new ArrayList<>();
        List<String> patterns = dirItem.getPatterns();
        if (patterns != null) {
            for (String pattern : patterns) {
                matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + pattern));
            }
        }
        // Filter files that match at least one pattern
        Path dirname = Paths.get(pathName);
        List<String> matchingFiles = new ArrayList<>();
        for (GridData file : allFiles) {
            if (file.getType() == GridData.Type.File) {
                String basename = file.getName();
                for (PathMatcher matcher : matchers) {
                    if (matcher.matches(Paths.get(basename))) {
                        // Build absolute filename, preserving prefix
                        String filename = dirname.resolve(basename).toString();
                        matchingFiles.add(filename);
                        break;
                    }
                }
            }
        }
        return matchingFiles;
    }
}