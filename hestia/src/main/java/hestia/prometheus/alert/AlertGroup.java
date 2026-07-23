package hestia.prometheus.alert;

import java.util.ArrayList;
import java.util.List;

import hestia.persist.Identifiable;
import hestia.prometheus.alert.rule.AlertRule;

public class AlertGroup implements Identifiable {
    private String id;
    private String name;
    private String interval;
    private int limit;
    private final List<AlertRule> rules = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
