package org.ardaozcan.synk.io;

// import static org.fusesource.jansi.Ansi.*;

public class Logger {
    public static void log(String heading, String msg) {
        heading = "[" +  heading + "]";
        System.out.println(String.format("%s %s", heading, msg));
    }

    public static void logInfo(String msg) {
        log("INFO", msg);
    }

    public static void logError(String msg) {
        log("ERR", msg);
    }

    public static void logWarning(String msg) {
        log("WARN", msg);
    }
}
