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

import org.ardaozcan.dotsynk.Config;
import org.ardaozcan.dotsynk.Parser;
import org.ardaozcan.io.Logger;
import org.ardaozcan.net.Client;
import org.ardaozcan.net.ServerThread;

public class Manager {

    public final String ROOT_PATH;
    public final String DOT_SYNK_FILENAME = ".synk";
    public final int PORT = 4902;

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
        String hashedCode = Hashing.sha256().hashString(code, StandardCharsets.UTF_8).toString();

        try {
            File file = new File(absPath.toString());
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(String.format("code: %s", hashedCode));
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
                    if(client != null) {
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
        List<String> networkIPs = getNetworkIPs();
        System.out.println("-----SERVERS-----");
        for (String ip : networkIPs) {
            System.out.println(ip);
        }
        System.out.println("-----------------");
    }

    public boolean synkServerOpen(String ip) throws IOException {
        Socket s;
        try {
            s = new Socket(ip, PORT);
        } catch (IOException e) {
            return false;
        }

        s.close();
        return true;
    }

    public List<String> getNetworkIPs() {
        final byte[] ip;
        List<String> ips = new ArrayList<>();
        try {
            ip = InetAddress.getLocalHost().getAddress();
        } catch (Exception e) {
            return ips; // exit method, otherwise "ip might not have been initialized"
        }
        Thread lastThread = null;

        for (int i = 1; i <= 254; i++) {
            final int j = i; // i as non-final variable cannot be referenced from inner class
            lastThread = new Thread(new Runnable() { // new thread for parallel execution
                public void run() {
                    try {
                        ip[3] = (byte) j;
                        InetAddress address = InetAddress.getByAddress(ip);
                        String output = address.toString().substring(1);
                        if (address.isReachable(5000)){
                            if(synkServerOpen(output)) {
                                ips.add(output);
                            }
                        } 
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            lastThread.start(); // dont forget to start the thread
        }

        try {
            if(lastThread != null) {
                lastThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return ips;
    }
}
