package org.ardaozcan.synk.io;

import static org.fusesource.jansi.Ansi.*;

public class Logger {
    public static void log(String heading, Color color, String msg) {
        heading = "[" + ansi().fg(color).a(heading).reset() + "]";
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
