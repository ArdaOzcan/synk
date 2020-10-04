package org.ardaozcan.io;

public class Logger {
    public static String colored(String msg, Color color) {
        return color + msg + Color.RESET;
    }
    public static void log(String heading, Color headingColor, String msg) {
        heading = "[" + colored(heading, headingColor) + "]";
        System.out.println(String.format("%s %s", heading, msg));
    }

    public static void logInfo(String msg) {
        log("INFO", Color.BLUE, msg);
    }

    public static void logError(String msg) {
        log("ERR", Color.RED, msg);
    }

    public static void logWarning(String msg) {
        log("WARN", Color.YELLOW, msg);
    }
}
