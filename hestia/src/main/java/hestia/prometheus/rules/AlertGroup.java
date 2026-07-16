package hestia.prometheus.rules;

import java.util.ArrayList;
import java.util.List;

import hestia.base.YamlAccess;

public class AlertGroup extends YamlAccess {
    private List<AlertRule> rules = new ArrayList<>();

    public String getName() {
        return str("name");
    }

    public void setName(String name) {
        put("name", name);
    }

    public List<AlertRule> getRules() {
        return rules;
    }
    
    public void setRules(List<AlertRule> rules) {
        this.rules = rules;
    }

    public void add(AlertRule rule) {
        rules.add(rule);
        getYaml().put("rules", rules);
    }
    
    public void remove(AlertRule rule) {
        rules.remove(rule);
        getYaml().put("rules", rules);
    }

    public String getInterval() {
        return str("interval");
    }

    public void setInterval(String v) {
        put("interval", v);
    }

    /**
     * @return 0: no limit
     */
    public int getLimit() {
        try {
            return Integer.parseInt(str("limit"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * @param limit 0: no limit
     */
    public void setLimit(int limit) {
        put("interval", "" + limit);
    }
    
    @Override
    public String toString() {
        return "GROUP: " + getName();
    }
}
