package hestia.otc;

public abstract class AbstractMonitoredTarget implements MonitoredTarget {
    private String name;
    private boolean active;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
