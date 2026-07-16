package hestia.prometheus.rules;

import java.util.ArrayList;
import java.util.List;

import hestia.base.YamlAccess;

public class AlertRulesFile extends YamlAccess {
    /** filename */
    private String name;
    private List<AlertGroup> groups = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AlertGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<AlertGroup> groups) {
        this.groups = groups;
    }

    public AlertGroup add(String name) {
        AlertGroup group = new AlertGroup();
        group.setName(name);
        groups.add(group);
        getYaml().put("groups", groups);
        return group;
    }
    
    public void remove(AlertGroup group) {
        groups.remove(group);
        getYaml().put("groups", groups);
    }
    
    @Override
    public String toString() {
        return "FILE: " + getName();
    }
}
