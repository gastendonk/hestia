package hestia.otc.model;

/**
 * Freestyle definition for the config.yaml
 */
public class Definition extends AbstractMonitoredTarget {
    private String definition;

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @Override
    public String getInfo() {
        return "";
    }
}
