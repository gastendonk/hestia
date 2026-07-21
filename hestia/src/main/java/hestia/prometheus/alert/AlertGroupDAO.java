package hestia.prometheus.alert;

import hestia.base.AbstractJsonListDAO;
import hestia.base.IRepository;

/**
 * Stores alert groups grouped by environment.
 */
public class AlertGroupDAO extends AbstractJsonListDAO<AlertGroup> {

    /**
     * Creates an alert group DAO.
     *
     * @param gitRepository the Git repository access
     */
    public AlertGroupDAO(IRepository gitRepository) {
        super(gitRepository, AlertGroup.class);
    }

    @Override
    public String getPath(String environmentId) {
        return "alerts/" + environmentId + ".json";
    }

    @Override
    protected String getInsertCommitMessage(String environmentId, AlertGroup object) {
        return "add alert group";
    }

    @Override
    protected String getUpdateCommitMessage(String environmentId, AlertGroup object) {
        return "update alert group";
    }

    @Override
    protected String getDeleteCommitMessage(String environmentId, String id) {
        return "delete alert group";
    }
}
