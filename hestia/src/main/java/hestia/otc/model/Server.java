package hestia.otc;

// node_exporter
public class Server extends AbstractMonitoredTarget {
    private ServerType type = ServerType.LINUX;
    private String host;
    private String path;
    
    public ServerType getType() {
        return type;
    }

    public void setType(ServerType type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
