package org.ardaozcan.net;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.ardaozcan.Manager;
import org.ardaozcan.io.FileManager;
import org.ardaozcan.io.Logger;

public class ClientThread extends Thread {
    final ClientData client;
    static final int HEADER_SIZE = 20;

    final Manager manager;

    public ClientThread(ClientData client, Manager manager) {
        this.client = client;
        this.manager = manager;
    }

    void sendFile(File file) throws IOException {
        String fileName = file.getName();
        byte[] fileData = Files.readAllBytes(file.toPath());

        String msg = new Gson().toJson(new FileResponseMessage("file", fileName, fileData));
        client.send(msg);
    }

    void sendDirectory() throws IOException {
        for (File file : FileManager.getFilesInDirectory(new File(manager.ROOT_PATH))) {
            if (!file.getName().equals(manager.DOT_SYNK_FILENAME)) {
                sendFile(file);
            }
        }

        client.send(new Gson().toJson(new FileResponseMessage("eof", null, null)));
    }

    @Override
    public void run() {
        Logger.logInfo("Started thread for " + client.ip);
        boolean running = true;
        boolean authenticated = false;
        do {
            try {
                Logger.logInfo("Waiting for client...");
                byte[] msg = client.read();

                try {
                    RequestMessage requestMsg = new Gson().fromJson(new String(msg), RequestMessage.class);
                    Logger.logInfo("Message type: '" + requestMsg.messageType + "'");

                    switch (requestMsg.messageType) {
                        case "getDirectory":
                            sendDirectory();
                            break;
                        case "authenticate":
                            System.out.println();
                            String hashed = Hashing.sha256().hashString(requestMsg.code, StandardCharsets.UTF_8).toString();
                            authenticated = hashed.trim().equals(manager.config.code.trim());
                            break;
                        case "disconnect":
                            running = false;
                            break;
                    }
                } catch (JsonSyntaxException e) {
                    Logger.logInfo("Wrong message format");
                }

            } catch (SocketException e) {
                e.printStackTrace();
                break;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } while (running && authenticated);

        try {
            client.input.close();
            client.output.close();
            client.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.logInfo("Ended...");
    }
}
