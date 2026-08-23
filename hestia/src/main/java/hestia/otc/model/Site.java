package hestia.otc.model;

import github.soltaufintel.amalia.base.IdGenerator;

// HttpCheck
public class Site extends AbstractMonitoredTarget {
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getInfo() {
        return url;
    }

    @Override
    public MonitoredTarget copy() {
        var n = new Site();
        n.setName(getName());
        n.setUrl(getUrl());
        n.setId(IdGenerator.createId25());
        n.setActive(isActive());
        return n;
    }
}
