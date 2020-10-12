package org.ardaozcan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.ardaozcan.dotsynk.Config;
import org.ardaozcan.dotsynk.Parser;
import org.ardaozcan.io.Logger;
import org.ardaozcan.net.Client;
import org.ardaozcan.net.ClientData;
import org.ardaozcan.net.message.RequestMessage;
import org.ardaozcan.net.message.ServerInformationResponseMessage;
import org.ardaozcan.net.ServerInformation;
import org.ardaozcan.net.ServerThread;

public class Manager {

    public final String ROOT_PATH;
    public final String DOT_SYNK_FILENAME = ".synk";
    public final int PORT = 4902;
    public final String VERSION = "v1.0";

    public Config config;

    Client client = null;

    public Manager(String rootPath) throws FileNotFoundException, IOException {
        this.ROOT_PATH = new File(rootPath).getCanonicalPath();
    }

    public void initializeDirectory() throws FileNotFoundException, IOException {
        Path absPath = Paths.get(ROOT_PATH, DOT_SYNK_FILENAME).toAbsolutePath();
        if (Files.exists(absPath)) {
            this.config = new Parser(absPath.toString()).parse();
            return;
        }

        String code = System.console().readLine("Directory pass: ");
        String serverName = System.console().readLine("Server name: ");
        serverName = serverName.toLowerCase().replace(' ', '-');
        String hashedCode = Hashing.sha256().hashString(code, StandardCharsets.UTF_8).toString();

        try {
            File file = new File(absPath.toString());
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(String.format("code: %s\n", hashedCode));
            writer.write(String.format("name: %s\n", serverName));
            writer.write(String.format("version: %s\n", VERSION));
            writer.close();
            Logger.logInfo(String.format("Directory initialized in '%s'.", absPath));

        } catch (IOException e) {
            Logger.logError("An error occurred.");
            e.printStackTrace();
        }

        this.config = new Parser(absPath.toString()).parse();
    }

    public void getInput() {
        executeCommand(System.console().readLine());
    }

    public void initiateInteractive() throws FileNotFoundException, IOException {
        initializeDirectory();
        getInput();
    }

    public void executeCommand(String input) {
        String[] args = input.split(" ");
        if (args.length == 0) {
            return;
        }
        String cmd = args[0];
        if (cmd != null) {
            switch (cmd) {
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
            }
        }

        getInput();
    }

    public void listAvailableServers() {
        List<ServerInformation> infos = getServerInformations();
        System.out.println("-----SERVERS-----");
        for (ServerInformation info : infos) {
            System.out.println(info);
        }
        System.out.println("-----------------");
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
            return infos; // exit method, otherwise "ip might not have been initialized"
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
