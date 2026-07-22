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
    protected String getItemNameForCommitMessage() {
        return "alert group";
    }
}
