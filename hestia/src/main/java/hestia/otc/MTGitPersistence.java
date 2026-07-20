package hestia.otc;

import java.util.List;

import hestia.persist.Committer;
import hestia.persist.Persistence;

public class MTGitPersistence implements Persistence<MonitoredTarget> {
    private final Committer committer;
    private List<MonitoredTarget> loaded;
    
    public MTGitPersistence(Committer committer) {
        this.committer = committer;
    }
    
    @Override
    public List<MonitoredTarget> load(String envId, String unused) {
        return loaded = MonitoredTargetDAO.load(envId);
    }

    @Override
    public MonitoredTarget loadOne(String envId, String unused, String id) {
        return load(envId, unused).stream().filter(i -> i.getId().equals(id)).findFirst().orElseThrow();
    }

    @Override
    public void save(String envId, String unused, MonitoredTarget mt, boolean add) {
        if (add) {
            load(envId, null);
            loaded.add(mt);
        }
        MonitoredTargetDAO.save(envId, loaded);
        committer.commit("save monitored target");
    }

    @Override
    public void delete(String envId, String unused, String id) {
        if (load(envId, unused).removeIf(i -> i.getId().equals(id))) {
            MonitoredTargetDAO.save(envId, loaded);
            committer.commit("delete monitored target");
        }
    }
}
