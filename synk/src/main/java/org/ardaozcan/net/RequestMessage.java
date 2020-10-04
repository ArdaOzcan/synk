package org.ardaozcan.net;

public class RequestMessage extends Message{

    String code = null;

    public RequestMessage(String messageType) {
        super(messageType);
    }

    public RequestMessage(String messageType, String code) {
        super(messageType);
        this.code = code;
    }
}
