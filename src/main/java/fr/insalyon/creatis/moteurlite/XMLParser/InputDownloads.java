package fr.insalyon.creatis.moteurlite.XMLParser;

import java.net.URI;

public class InputDownloads {
    private static boolean endsWithExtension(String path) {
        return path.matches(".*\\.[^.]+$");
    }

    public static boolean isFileURI(String uriString) {
        try {
            URI uri = new URI(uriString);
            String path = uri.getPath();
            return endsWithExtension(path);
        } catch (Exception e) {
            return false;
        }
    }
    
}
