package hestia.otc;

// HttpCheck
public class Site implements MonitoredTarget {
    private String name;
    private String url;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
