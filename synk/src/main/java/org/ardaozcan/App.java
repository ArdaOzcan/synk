package org.ardaozcan;

import java.io.IOException;

public class App {
    public static void main(String[] args) {
        Manager manager;
        try {
            manager = new Manager("sample2");
            manager.initiateInteractive();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
