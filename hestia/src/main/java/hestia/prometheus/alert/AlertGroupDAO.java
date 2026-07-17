package hestia.prometheus.alert;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import github.soltaufintel.amalia.base.FileService;
import hestia.HestiaWebapp;

public class AlertGroupDAO {
    private static final Object LOCK = new Object();
    
    public static List<AlertGroup> load(String envId) {
        synchronized (LOCK) {
            AlertGroups e = FileService.loadJsonFile(file(envId), AlertGroups.class);
            return e == null || e.getList() == null ? new ArrayList<>() : e.getList();
        }
    }

    public static void save(String envId, List<AlertGroup> list) {
        synchronized (LOCK) {
            list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            for (AlertGroup g : list) {
                g.getRules().sort((a, b) -> a.getAlert().compareToIgnoreCase(b.getAlert()));
            }
            AlertGroups e = new AlertGroups();
            e.setList(list);
            FileService.saveJsonFile(file(envId), e);
        }
    }

    public static List<AlertGroup> loadAll(Collection<String> environmentIdList) {
        synchronized (LOCK) {
            return environmentIdList.stream().map(id -> load(id)).flatMap(List::stream).collect(Collectors.toList());
        }
    }
    
    public static AlertGroup load(String envId, String groupId) {
        synchronized (LOCK) {
            List<AlertGroup> groups = load(envId);
            for (AlertGroup g : groups) {
                if (g.getId().equals(groupId)) {
                    return g;
                }
            }
            throw new NoSuchElementException();
        }
    }
    
    public static void save(String envId, AlertGroup group) {
        synchronized (LOCK) {
            List<AlertGroup> groups = load(envId);
            groups.removeIf(i -> i.getId().equals(group.getId()));
            groups.add(group);
            save(envId, groups);
        }
    }
    
    public static AlertRule load(String envId, String groupId, String ruleId) {
        synchronized (LOCK) {
            List<AlertGroup> groups = load(envId);
            for (AlertGroup g : groups) {
                if (g.getId().equals(groupId)) {
                    for (AlertRule r : g.getRules()) {
                        if (ruleId.equals(r.getId())) {
                            return r;
                        }
                    }
                    break;
                }
            }
            throw new RuntimeException("Alert rule does not exist");
        }
    }
    
    public static void insert(String envId, String groupId, AlertRule rule) {
        synchronized (LOCK) {
            List<AlertGroup> groups = load(envId);
            for (AlertGroup g : groups) {
                if (g.getId().equals(groupId)) {
                    g.getRules().add(rule);
                    save(envId, groups);
                    return;
                }
            }
        }
    }

    public static void update(String envId, String groupId, AlertRule rule) {
        synchronized (LOCK) {
            List<AlertGroup> groups = load(envId);
            for (AlertGroup g : groups) {
                if (g.getId().equals(groupId)) {
                    List<AlertRule> rules = g.getRules();
                    for (int i = 0; i < rules.size(); i++) {
                        if (rules.get(i).getId().equals(rule.getId())) {
                            rules.set(i, rule);
                            save(envId, groups);
                            return;
                        }
                    }
                    break;
                }
            }
        }
    }

    public static void delete(String envId, String groupId) {
        synchronized (LOCK) {
            List<AlertGroup> groups = load(envId);
            if (groups.removeIf(i -> i.getId().equals(groupId))) {
                save(envId, groups);
            }
        }
    }

    public static void delete(String envId, String groupId, String id) {
        synchronized (LOCK) {
            List<AlertGroup> groups = load(envId);
            for (AlertGroup g : groups) {
                if (g.getId().equals(groupId)) {
                    if (g.getRules().removeIf(i -> i.getId().equals(id))) {
                        save(envId, groups);
                    }
                    return;
                }
            }
        }
    }

    /**
     * A AlertGroups file contains all alert groups for 1 environment.
     */
    public static class AlertGroups {
        private List<AlertGroup> list;

        public List<AlertGroup> getList() {
            return list;
        }

        public void setList(List<AlertGroup> list) {
            this.list = list;
        }
    }

    private static File file(String envId) {
        return new File(HestiaWebapp.config.getAlertsFolder(), envId + ".json");
    }
}
