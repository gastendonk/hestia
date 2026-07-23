package hestia.otc.model;

import hestia.base.AbstractJsonListDAO;
import hestia.base.IRepository;

/**
 * Stores monitored targets grouped by environment.
 */
public class MonitoredTargetDAO extends AbstractJsonListDAO<MonitoredTarget> {

    /**
     * Creates a monitored target DAO.
     *
     * @param gitRepository the Git repository access
     */
    public MonitoredTargetDAO(IRepository gitRepository) {
        super(gitRepository, MonitoredTarget.class);
    }

    @Override
    public String getPath(String environmentId) {
        return "monitoredtargets/" + environmentId + ".json";
    }

    @Override
    protected String getItemNameForCommitMessage() {
        return "monitored target";
    }
    
    public int count(String envId) {
        return (int) load(envId).stream().filter(i -> i.isActive()).count();
    }
}
