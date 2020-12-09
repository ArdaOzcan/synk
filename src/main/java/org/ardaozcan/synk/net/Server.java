package org.ardaozcan.synk.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.ardaozcan.synk.Manager;
import org.ardaozcan.synk.io.Logger;

public class Server {
    public final String IP;
    public final int PORT;

    final ServerSocket socket;
    final Manager manager;

    public Server(String ip, int port, Manager manager) throws UnknownHostException, IOException {
        IP = ip;
        PORT = port;

        this.manager = manager;
        if (IP != null) {
            socket = new ServerSocket(PORT, 50, InetAddress.getByName(IP));
        } else {
            socket = new ServerSocket(PORT);
        }
    }

    public void serve() {
        Logger.logInfo("Started server on address " + IP + ":" + PORT);
        while (true) {
            try {
                Socket client = socket.accept();
                ClientData data = new ClientData(client);
                new ClientThread(data, manager).start();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}
