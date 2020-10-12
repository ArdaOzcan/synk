package org.ardaozcan.synk.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.ardaozcan.synk.Manager;

public class ServerThread extends Thread {
    final String ip;
    final int port;
    final Manager manager;

    public ServerThread(String ip, int port, Manager manager) {
        String tempIP = null;
        if (ip == null) {
            try {
                tempIP = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            tempIP = ip;
        }
        
        this.ip = tempIP;
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
