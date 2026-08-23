package hestia.otc.model;

import github.soltaufintel.amalia.base.IdGenerator;

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

    @Override
    public MonitoredTarget copy() {
        var n = new Definition();
        n.setDefinition(getDefinition());
        n.setName(getName());
        n.setId(IdGenerator.createId25());
        n.setActive(isActive());
        return n;
    }
}
