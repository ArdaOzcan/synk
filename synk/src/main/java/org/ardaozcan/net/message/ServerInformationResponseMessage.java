package org.ardaozcan.net.message;

import org.ardaozcan.net.ServerInformation;

public class ServerInformationResponseMessage extends Message {

    public String name;
    public String version;

    public ServerInformationResponseMessage(ServerInformation info) {
        super("serverInfo");
        this.name = info.name;
        this.version = info.version;
    }

    public ServerInformationResponseMessage(String name, String version) {
        super("serverInfo");
        this.name = name;
        this.version = version;
    }
    
}
