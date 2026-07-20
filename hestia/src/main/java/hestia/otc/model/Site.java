package hestia.otc.model;

// HttpCheck
public class Site extends AbstractMonitoredTarget {
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
