package org.ardaozcan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.hash.Hashing;

import org.ardaozcan.dotsynk.Config;
import org.ardaozcan.dotsynk.Parser;
import org.ardaozcan.io.Logger;
import org.ardaozcan.net.Client;
import org.ardaozcan.net.ServerThread;

public class Manager {

    public final String ROOT_PATH;
    public final String DOT_SYNK_FILENAME = ".synk";

    public Config config;

    Client client = null;

    public Manager(String rootPath) throws FileNotFoundException, IOException {
        this.ROOT_PATH = rootPath;
    }

    public void initializeDirectory() throws FileNotFoundException, IOException {
        Path absPath = Paths.get(ROOT_PATH, DOT_SYNK_FILENAME).toAbsolutePath();
        if(Files.exists(absPath)) {
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

    public void executeCommand(String cmd) {
        if(cmd != null) {
            switch(cmd) {
                case "serve":
                    new ServerThread("192.168.1.137", 4902, this).start();
                    break;
                case "connect":
                    try {
                        if(this.client == null) {
                            this.client = new Client(this);
                        }

                        client.connect("192.168.1.137", 4902);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "get":
                    client.get();
                    break;
            }
        }
        
        getInput();
    }
}
