package org.ardaozcan.synk;

import java.io.IOException;

// import org.fusesource.jansi.AnsiConsole;

public class App {
    public static void main(String[] args) {
        // AnsiConsole.systemInstall();
        String path = "";
        if (args.length > 0) {
            path = args[0];
        }

        Manager manager;
        try {
            manager = new Manager(path);
            manager.initiateInteractive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
