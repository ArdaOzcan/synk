package org.ardaozcan.synk.net;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.ardaozcan.synk.Manager;
import org.ardaozcan.synk.io.FileManager;
import org.ardaozcan.synk.io.Logger;
import org.ardaozcan.synk.net.message.FileResponseMessage;
import org.ardaozcan.synk.net.message.RequestMessage;
import org.ardaozcan.synk.net.message.ServerInformationResponseMessage;

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

    void sendServerInformation(ServerInformation info) throws IOException {
        client.send(new Gson().toJson(new ServerInformationResponseMessage(info)));
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
        boolean running = true;
        boolean authenticated = false;
        try {
            byte[] msg = client.receive();
            try {
                RequestMessage requestMsg = new Gson().fromJson(new String(msg), RequestMessage.class);

                switch (requestMsg.messageType) {
                    case "authenticate":
                        System.out.println();
                        String hashed = Hashing.sha256().hashString(requestMsg.code, StandardCharsets.UTF_8).toString();
                        authenticated = hashed.trim().equals(manager.config.code.trim());
                        break;
                    case "getServerInformation":
                        sendServerInformation(new ServerInformation(client.ip, manager.config.name, manager.VERSION));
                        break;
                }
            } catch (JsonSyntaxException e) {
                Logger.logError("Wrong message format");
            }
        } catch (IOException | NumberFormatException e) {
            return;
        }

        if (!authenticated) {
            return;
        }

        Logger.logInfo("Started thread for " + client.ip);

        while (running && authenticated) {
            try {
                byte[] msg = client.receive();

                try {
                    RequestMessage requestMsg = new Gson().fromJson(new String(msg), RequestMessage.class);
                    Logger.logInfo("Message type: '" + requestMsg.messageType + "'");

                    switch (requestMsg.messageType) {
                        case "getDirectory":
                            sendDirectory();
                            break;
                        case "disconnect":
                            running = false;
                            break;
                        default:
                            Logger.logError(String.format("Wrong message type '%s'", requestMsg.messageType));
                            break;
                    }
                } catch (JsonSyntaxException e) {
                    Logger.logError("Wrong message format");
                }

            } catch (SocketException e) {
                e.printStackTrace();
                break;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

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
