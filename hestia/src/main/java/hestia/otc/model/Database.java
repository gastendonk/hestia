package hestia.otc.model;

import github.soltaufintel.amalia.base.IdGenerator;

public class Database extends AbstractMonitoredTarget {
    private DatabaseType type = DatabaseType.POSTGRES;
    private String host;
    private String user;
    private String password;

    public DatabaseType getType() {
        return type;
    }

    public void setType(DatabaseType type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
        var n = new Database();
        n.setHost(getHost());
        n.setName(getName());
        n.setUser(getUser());
        n.setPassword(getPassword());
        n.setId(IdGenerator.createId25());
        n.setActive(true);
        return n;
    }

    @Override
    public String replace(String text) {
        return text.replace("{name}", getName()).replace("{host}", host);
    }
}
