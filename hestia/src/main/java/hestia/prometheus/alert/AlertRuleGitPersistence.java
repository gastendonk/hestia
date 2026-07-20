package hestia.prometheus.alert;

import java.util.List;

import hestia.persist.Committer;
import hestia.persist.Persistence;

public class AlertRuleGitPersistence implements Persistence<AlertRule> {
    private List<AlertGroup> loaded;
    private final Committer committer;

    public AlertRuleGitPersistence(Committer committer) {
        this.committer = committer;
    }

    @Override
    public List<AlertRule> load(String envId, String groupId) {
        loaded = AlertGroupDAO.load(envId);
        return loaded.stream().filter(i -> groupId.equals(i.getId())).findFirst().orElseThrow().getRules();
    }

    @Override
    public AlertRule loadOne(String envId, String groupId, String id) {
        return load(envId, groupId).stream().filter(i -> i.getId().equals(id)).findFirst().orElseThrow();
    }

    @Override
    public void save(String envId, String groupId, AlertRule rule, boolean add) {
        if (add) {
            load(envId, groupId).add(rule);
        }
        AlertGroupDAO.save(envId, loaded);
        committer.commit("save alert rule");
    }

    @Override
    public void delete(String envId, String groupId, String id) {
        if (load(envId, groupId).removeIf(i -> i.getId().equals(id))) {
            AlertGroupDAO.save(envId, loaded);
            committer.commit("delete alert rule");
        }
    }
}
