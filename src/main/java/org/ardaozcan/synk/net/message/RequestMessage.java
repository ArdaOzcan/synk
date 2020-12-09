package org.ardaozcan.synk.net.message;

public class RequestMessage extends Message {

    public String code = null;

    public RequestMessage(String messageType) {
        super(messageType);
    }

    public RequestMessage(String messageType, String code) {
        super(messageType);
        this.code = code;
    }
}
