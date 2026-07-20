package hestia.prometheus.alert;

import java.util.List;

import hestia.persist.Persistence;

public class AlertRuleCustomerPersistence implements Persistence<AlertRule> {
    
    @Override
    public List<AlertRule> load(String envId, String groupId) {
        // TODO
        return null;
    }

    @Override
    public AlertRule loadOne(String envId, String groupId, String id) {
        return load(envId, groupId).stream().filter(i -> i.getId().equals(id)).findFirst().orElseThrow();
    }

    @Override
    public void save(String envId, String groupId, AlertRule rule, boolean add) {
        // TODO
    }

    @Override
    public void delete(String envId, String groupId, String id) {
        // TODO
    }
}
