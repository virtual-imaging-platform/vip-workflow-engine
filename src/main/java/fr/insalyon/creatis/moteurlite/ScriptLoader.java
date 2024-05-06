package fr.insalyon.creatis.moteurlite;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class ScriptLoader {

    public static String loadBashScript() {
        String bashScript = null;
        try (InputStream inputStream = ScriptLoader.class.getResourceAsStream("/script.sh")) {
            if (inputStream != null) {
                try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                    bashScript = scanner.useDelimiter("\\A").next();
                }
            } else {
                System.err.println("Script file not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bashScript;
    }
}
