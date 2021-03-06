package org.ardaozcan.synk.net;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.ardaozcan.synk.Manager;
import org.ardaozcan.synk.io.FileManager;
import org.ardaozcan.synk.io.Logger;
import org.ardaozcan.synk.net.message.FileResponseMessage;
import org.ardaozcan.synk.net.message.Message;
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
        String relativePath = FileManager.getRelativePath(manager.ROOT_PATH, file.getAbsolutePath().toString());
        byte[] fileData = Files.readAllBytes(file.toPath());

        String msg = new Gson().toJson(new FileResponseMessage("file", relativePath, fileData));
        client.send(msg);
    }

    void sendServerInformation(ServerInformation info) throws IOException {
        client.send(new Gson().toJson(new ServerInformationResponseMessage(info)));
    }

    public List<File> getFilesInDirectory(final File directoryPath, String relative) {
        List<File> files = new ArrayList<File>();
        for (final File fileEntry : directoryPath.listFiles()) {
            if (fileEntry.isDirectory()) {
                for (File file : getFilesInDirectory(fileEntry, Paths.get(relative, fileEntry.getName()).toString())) {
                    files.add(file);
                }
            } else {
                files.add(fileEntry);
            }
        }

        return files;
    }

    void sendDirectory() throws IOException {
        File dir = new File(manager.ROOT_PATH);
        for (File file : getFilesInDirectory(dir, "")) {
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

        try {
            if (!authenticated) {
                client.send(new Gson().toJson(new Message("rejected")));
                return;
            }

            Logger.logInfo("Started thread for " + client.ip);
            client.send(new Gson().toJson(new Message("authenticated")));
        } catch (IOException e2) {
            e2.printStackTrace();
            return;
        }

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
