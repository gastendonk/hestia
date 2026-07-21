package hestia.prometheus.alert.rule;

import hestia.base.Identifiable;

public class AlertRule implements Identifiable {
    private String id;
    private String alert; // camelCase ID entered by user
    private String expr;
    private String durationFor;
    private String keepFiringFor;
    private String summary;
    private String description;
    private boolean active = true;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getExpr() {
        return expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public String getDurationFor() {
        return durationFor;
    }

    public void setDurationFor(String durationFor) {
        this.durationFor = durationFor;
    }

    public String getKeepFiringFor() {
        return keepFiringFor;
    }

    public void setKeepFiringFor(String keepFiringFor) {
        this.keepFiringFor = keepFiringFor;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
