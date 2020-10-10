package org.ardaozcan.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientData {
    final Socket socket;
    InputStream input;
    OutputStream output;
    String ip;

    public ClientData(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = "" + socket.getInetAddress();
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
    }

    public byte[] receive() throws IOException {
        byte[] header = input.readNBytes(ClientThread.HEADER_SIZE);
        int msgLength = Integer.parseInt(new String(header).strip());

        byte[] msg = input.readNBytes(msgLength);
        return msg;
    }

    public void send(String msg) throws IOException {
        String header = "";
        String msgLength = Integer.toString(msg.length());
        for (int i = 0; i < ClientThread.HEADER_SIZE; i++) {
            if (i < msgLength.length()) {
                header += msgLength.charAt(i);
            } else {
                header += ' ';
            }
        }

        byte[] finalMessage = (header.toString() + msg).getBytes();
        output.write(finalMessage);
        // Logger.logInfo("Sent '" + new String(finalMessage) + "'");
    }
}