package org.ardaozcan.net;

public class FileResponseMessage extends Message{
    String fileName;
    byte[] fileData;

    public FileResponseMessage(String messageType, String fileName, byte[] fileData) {
        super(messageType);
        this.fileName = fileName;
        this.fileData = fileData;
    }
}
