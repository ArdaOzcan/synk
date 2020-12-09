package org.ardaozcan.synk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.ardaozcan.synk.dotsynk.Config;
import org.ardaozcan.synk.dotsynk.Parser;
import org.ardaozcan.synk.io.FileManager;
import org.ardaozcan.synk.io.Logger;
import org.ardaozcan.synk.net.Client;
import org.ardaozcan.synk.net.ClientData;
import org.ardaozcan.synk.net.message.RequestMessage;
import org.ardaozcan.synk.net.message.ServerInformationResponseMessage;
import org.ardaozcan.synk.net.ServerInformation;
import org.ardaozcan.synk.net.ServerThread;

public class Manager {

    public final String ROOT_PATH;
    public final String DOT_SYNK_FILENAME = ".synk";
    public final int PORT = 4902;
    public final String VERSION = "v1.0";

    public Config config;

    Client client = null;

    public Path getDotSynkPath() {
        return Paths.get(ROOT_PATH, DOT_SYNK_FILENAME).toAbsolutePath();
    }

    public Manager(String rootPath) throws FileNotFoundException, IOException {
        this.ROOT_PATH = new File(rootPath).getCanonicalPath();
    }

    public String getInput() {
        return System.console().readLine();
    }

    public void initiateInteractive() throws FileNotFoundException, IOException {
        FileManager.initializeDirectory(getDotSynkPath());
        this.config = new Parser(getDotSynkPath().toString()).parse();
        executeCommand();
    }

    public void executeCommand() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            String[] args = input.split(" ");
            if (args.length == 0) {         
                continue;
            }
            String cmd = args[0];
            if (cmd != null) {
                switch (cmd) {
                    case "exit":
                        scanner.close();
                        return;
                    case "serve":
                        if (args.length < 2) {
                            new ServerThread(null, PORT, this).start();
                        } else {
                            new ServerThread(args[1], PORT, this).start();
                        }
                        break;
                    case "connect":
                        try {
                            if (this.client == null) {
                                this.client = new Client(this);
                            }

                            if (args.length < 2) {
                                Logger.logError("No argument for 'IP' was found. Skipping...");
                                break;
                            }

                            client.connect(args[1], PORT);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "get":
                        if (client != null) {
                            client.get();
                        } else {
                            Logger.logError("You are not connected to any servers.");
                        }
                        break;
                    case "list":
                        listAvailableServers();
                        break;
                    default:
                        Logger.logError("Command '" + cmd + "' is not available");
                }
            }
        }
    }

    public void listAvailableServers() {
        List<ServerInformation> infos = getServerInformations();
        System.out.println("---------------SERVERS---------------");
        System.out.println(String.format("%-22s%-22s%-22s", "IP", "Name", "Version"));
        for (ServerInformation info : infos) {
            System.out.println(String.format("%-22s%-22s%-22s", info.ip, info.name, info.version));
        }
        System.out.println("-------------------------------------");
    }

    public ServerInformation synkServerOpen(String ip) throws IOException {
        Socket s;
        try {
            s = new Socket(ip, PORT);
        } catch (IOException e) {
            return null;
        }

        ClientData cd = new ClientData(s);

        try {
            cd.send(new Gson().toJson(new RequestMessage("getServerInformation")));

            byte[] response = cd.receive();

            try {
                ServerInformationResponseMessage infoResponse = new Gson().fromJson(new String(response),
                        ServerInformationResponseMessage.class);

                switch (infoResponse.messageType) {
                    case "serverInfo":
                        return new ServerInformation(ip, infoResponse.name, infoResponse.version);
                }
            } catch (JsonSyntaxException e) {
                Logger.logError("Wrong message format");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        s.close();
        return new ServerInformation(ip, null, null);
    }

    public List<ServerInformation> getServerInformations() {
        final byte[] ip;
        List<ServerInformation> infos = new ArrayList<>();
        try {
            ip = InetAddress.getLocalHost().getAddress();
        } catch (Exception e) {
            return infos;
        }
        Thread lastThread = null;

        for (int i = 1; i <= 254; i++) {
            final int j = i;
            lastThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        ip[3] = (byte) j;
                        InetAddress address = InetAddress.getByAddress(ip);
                        String output = address.toString().substring(1);

                        if (address.isReachable(5000)) {
                            ServerInformation si = synkServerOpen(output);
                            if (si != null) {
                                infos.add(si);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            lastThread.start();
        }

        try {
            if (lastThread != null) {
                lastThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return infos;
    }
}
