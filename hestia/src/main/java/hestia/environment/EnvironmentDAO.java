package hestia.environment;

import java.io.File;
import java.util.List;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.base.StringService;
import hestia.persist.AbstractJsonListSimpleDAO;
import hestia.persist.IRepository;

/**
 * Stores all environments in a single JSON file.
 */
public class EnvironmentDAO extends AbstractJsonListSimpleDAO<Environment> {

    /**
     * Creates an environment DAO.
     *
     * @param gitRepository the Git repository access
     */
    public EnvironmentDAO(IRepository gitRepository) {
        super(gitRepository, Environment.class);
    }

    @Override
    protected void insertExtras(Environment env, List<Environment> list) {
        for (Environment i : list) {
            if (i.getCustomer().equals(env.getCustomer())) {
                env.setCustomerKey(i.getCustomerKey());
                break;
            }
        }
        if (StringService.isNullOrEmpty(env.getCustomerKey())) {
            env.setCustomerKey(IdGenerator.createId25());
        }
    }

    @Override
    public String getPath(String unused) {
        return "environments/environments.json";
    }

    @Override
    protected String getItemNameForCommitMessage() {
        return "environment";
    }
    
    public File getFile() {
        return getFile("");
    }
}
