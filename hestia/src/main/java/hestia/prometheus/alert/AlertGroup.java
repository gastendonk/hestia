package hestia.prometheus.alert;

import java.util.ArrayList;
import java.util.List;

public class AlertGroup {
    private String name;
    private String interval;
    private int limit;
    private final List<AlertRule> rules = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<AlertRule> getRules() {
        return rules;
    }
}
