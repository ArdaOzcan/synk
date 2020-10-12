package org.ardaozcan.net;

import org.ardaozcan.dotsynk.Config;

public class ServerInformation {
    String ip;
    public String name;
    public String version;

    public ServerInformation(String ip, String name, String version) {
        this.ip = ip;
        this.name = name;
        this.version = version;
    }

    public ServerInformation(String ip, Config config) {
        this.ip = ip;
        name = config.name;
        version = config.version;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) : %s", name, ip ,version);
    }
}
