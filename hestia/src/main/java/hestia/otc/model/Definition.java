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
        // first two lines
        int o = definition.indexOf("\n");
        if (o >= 0) {
            int oo = definition.indexOf("\n", o + 1);
            if (oo >= 0) {
                return definition.substring(0, oo).replace("\n", "");
            }
        }
        return "";
    }
}
