package fr.insalyon.creatis.moteurlite;

import fr.insalyon.creatis.moteurlite.runner.MoteurLiteRunner;

public class MoteurLite {
    public static void main(String[] args) throws MoteurLiteException {
        if (args.length != 3) {
            throw new IllegalArgumentException("Exactly 3 arguments are required: workflowId, boutiquesFilePath, inputsFilePath.");
        } else if (args[0].trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid workflowId. It should be a simple non-empty string.");
        } else if ( ! args[1].endsWith(".json")) {
            throw new IllegalArgumentException("Invalid boutiquesFilePath. It should be a JSON file.");
        } else if ( ! args[2].endsWith(".xml")) {
            throw new IllegalArgumentException("Invalid inputsFilePath. It should be an XML file.");
        } else {
            new MoteurLiteRunner().run(args[0], args[1], args[2]);
        }
    }
}