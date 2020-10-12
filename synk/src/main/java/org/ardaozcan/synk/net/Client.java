package org.ardaozcan.synk.net;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.ardaozcan.synk.Manager;
import org.ardaozcan.synk.io.Logger;
import org.ardaozcan.synk.net.message.FileResponseMessage;
import org.ardaozcan.synk.net.message.RequestMessage;

public class Client {
    ClientData socket;
    final Manager manager;

    public Client(Manager manager) {
        this.manager = manager;
    }

    public void connect(String ip, int port) throws IOException {
        Socket localSocket;
        localSocket = new Socket(ip, port);

        this.socket = new ClientData(localSocket);
        authenticate(System.console().readLine("Directory pass: "));
    }

    public String read(String msg) throws IOException {
        return this.socket.input.readAllBytes().toString();
    }

    public void authenticate(String code) throws IOException {
        socket.send(new Gson().toJson(new RequestMessage("authenticate", code)));
    }

    public void get() {
        try {
            socket.send(new Gson().toJson(new RequestMessage("getDirectory")));
            boolean eof = false;
            while (!eof) {
                byte[] response = socket.receive();

                try {
                    FileResponseMessage fileResponse = new Gson().fromJson(new String(response),
                            FileResponseMessage.class);

                    Logger.logInfo("Message type: '" + fileResponse.messageType + "'");

                    switch (fileResponse.messageType) {
                        case "file":
                            if (!fileResponse.fileName.equals(manager.DOT_SYNK_FILENAME)) {

                                Logger.logInfo("Got file " + fileResponse.fileName);

                                String filePath = Paths.get(manager.ROOT_PATH, fileResponse.fileName).toString();
                                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                                    fos.write(fileResponse.fileData);
                                }
                            }
                            break;
                        case "eof":
                            eof = true;
                            break;
                    }
                } catch (JsonSyntaxException e) {
                    Logger.logError("Wrong message format");
                }
            }

            Logger.logInfo("Got all.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
