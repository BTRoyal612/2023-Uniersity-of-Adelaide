package main.datastore;

public class AddressPortPair {
    private String host;
    private int port;

    public AddressPortPair(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
