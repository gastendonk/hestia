package hestia.otc.model;

import github.soltaufintel.amalia.base.IdGenerator;

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

    @Override
    public String getInfo() {
        return host;
    }
    
    @Override
    public String getType2() {
        return super.getType2() + "/" + type.name();
    }

    @Override
    public MonitoredTarget copy() {
        var n = new Server();
        n.setHost(getHost());
        n.setName(getName());
        n.setPath(getPath());
        n.setType(getType());
        n.setId(IdGenerator.createId25());
        n.setActive(true);
        return n;
    }
}
