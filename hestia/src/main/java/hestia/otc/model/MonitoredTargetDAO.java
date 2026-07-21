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
    protected String getInsertCommitMessage(String environmentId, MonitoredTarget object) {
        return "add monitored target";
    }

    @Override
    protected String getUpdateCommitMessage(String environmentId, MonitoredTarget object) {
        return "update monitored target";
    }

    @Override
    protected String getDeleteCommitMessage(String environmentId, String id) {
        return "delete monitored target";
    }
}
