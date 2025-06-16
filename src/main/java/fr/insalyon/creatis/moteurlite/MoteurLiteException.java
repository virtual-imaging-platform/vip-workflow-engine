package fr.insalyon.creatis.moteurlite;

public class MoteurLiteException extends Exception {
    public MoteurLiteException(String message) {
        super(message);
    }

    public MoteurLiteException(String message, Throwable cause) {
        super(message, cause);
    }

    public MoteurLiteException(Throwable t) {
        super(t);
    }
}
