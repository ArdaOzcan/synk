package org.ardaozcan.synk;

import java.io.IOException;

public class App {
    public static void main(String[] args) {
        String path = "copy";
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
