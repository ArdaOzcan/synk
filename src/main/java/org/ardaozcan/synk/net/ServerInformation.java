package org.ardaozcan.synk.net;

public class ServerInformation {
    public String ip;
    public String name;
    public String version;

    public ServerInformation(String ip, String name, String version) {
        this.ip = ip;
        this.name = name;
        this.version = version;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) : %s", name, ip, version);
    }
}
