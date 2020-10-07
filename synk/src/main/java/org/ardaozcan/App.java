package org.ardaozcan;

import java.io.IOException;

public class App {
    public static void main(String[] args) {
        String path = ".";
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
