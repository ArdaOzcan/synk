package org.ardaozcan.net;

import java.io.IOException;

import org.ardaozcan.Manager;

public class ServerThread extends Thread {
    final String ip;
    final int port;
    final Manager manager;

    public ServerThread(String ip, int port, Manager manager) {
        this.ip = ip;
        this.port = port;
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            Server s = new Server(ip, port, manager);
            s.serve();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
