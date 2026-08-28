package hestia.prometheus.alert.rule;

import github.soltaufintel.amalia.base.IdGenerator;
import hestia.persist.Identifiable;

public class AlertRule implements Identifiable {
    private String id;
    private String alert; // camelCase ID entered by user
    private String expr;
    private String durationFor;
    private String keepFiringFor;
    private String summary;
    private String description;
    private boolean active = true;
    private String channel;
    private String escalationChannel;
    /** nur belegt wenn environment ID = "templates" ist */
    private String mttype;

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
    
    public AlertRule copy() {
        AlertRule n = new AlertRule();
        n.setId(IdGenerator.createId25());
        n.setAlert(alert);
        n.setExpr(expr);
        n.setDurationFor(durationFor);
        n.setKeepFiringFor(keepFiringFor);
        n.setSummary(summary);
        n.setDescription(description);
        n.setActive(true);
        n.setChannel(channel);
        n.setEscalationChannel(escalationChannel);
        return n;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getEscalationChannel() {
        return escalationChannel;
    }

    public void setEscalationChannel(String escalationChannel) {
        this.escalationChannel = escalationChannel;
    }

    public String getMttype() {
        return mttype;
    }

    public void setMttype(String mttype) {
        this.mttype = mttype;
    }
}
