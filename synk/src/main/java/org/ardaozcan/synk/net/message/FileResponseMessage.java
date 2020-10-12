package org.ardaozcan.synk.net.message;

public class FileResponseMessage extends Message {
    public String fileName;
    public byte[] fileData;

    public FileResponseMessage(String messageType, String fileName, byte[] fileData) {
        super(messageType);
        this.fileName = fileName;
        this.fileData = fileData;
    }
}
