package hestia.prometheus.alert;

import java.util.List;

import hestia.persist.Committer;
import hestia.persist.Persistence;

public class AlertGroupGitPersistence implements Persistence<AlertGroup> {
    private final Committer committer;
    private List<AlertGroup> loaded;

    public AlertGroupGitPersistence(Committer committer) {
        this.committer = committer;
    }

    @Override
    public List<AlertGroup> load(String envId, String unused) {
        return loaded = AlertGroupDAO.load(envId);
    }

    @Override
    public AlertGroup loadOne(String envId, String unused, String id) {
        return load(envId, unused).stream().filter(i -> i.getId().equals(id)).findFirst().orElseThrow();
    }

    @Override
    public void save(String envId, String unused, AlertGroup group, boolean add) {
        if (add) {
            load(envId, unused).add(group);
        }
        AlertGroupDAO.save(envId, loaded);
        committer.commit("save alert group");
    }

    @Override
    public void delete(String envId, String unused, String id) {
        if (load(envId, unused).removeIf(i -> i.getId().equals(id))) {
            AlertGroupDAO.save(envId, loaded);
            committer.commit("delete alert group");
        }
    }
}
