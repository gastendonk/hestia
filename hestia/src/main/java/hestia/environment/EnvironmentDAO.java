package hestia.environment;

import java.util.List;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.base.StringService;
import hestia.persist.AbstractJsonListDAO;
import hestia.persist.IRepository;

/**
 * Stores all environments in a single JSON file.
 */
public class EnvironmentDAO extends AbstractJsonListDAO<Environment> {
    private static final String UNUSED_ENVIRONMENT_ID = "";

    /**
     * Creates an environment DAO.
     *
     * @param gitRepository the Git repository access
     */
    public EnvironmentDAO(IRepository gitRepository) {
        super(gitRepository, Environment.class);
    }

    /**
     * Loads all environments.
     *
     * @return all stored environments
     */
    public List<Environment> load() {
        return super.load(UNUSED_ENVIRONMENT_ID);
    }

    /**
     * Loads one environment by its identifier.
     *
     * @param id the environment identifier
     * @return the matching environment
     */
    public Environment loadOne(String id) {
        return super.loadOne(UNUSED_ENVIRONMENT_ID, id);
    }

    /**
     * Inserts an environment and creates a local Git commit.
     *
     * @param environment the environment to insert
     */
    public void insert(Environment environment) {
        super.insert(UNUSED_ENVIRONMENT_ID, environment);
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

    /**
     * Updates an environment and creates a local Git commit.
     *
     * @param environment the replacement environment
     */
    public void update(Environment environment) {
        super.update(UNUSED_ENVIRONMENT_ID, environment);
    }

    /**
     * Deletes an environment and creates a local Git commit.
     *
     * @param id the environment identifier
     */
    public void delete(String id) {
        super.delete(UNUSED_ENVIRONMENT_ID, id);
    }

    @Override
    public String getPath(String unused) {
        return "environments/environments.json";
    }

    @Override
    protected String getItemNameForCommitMessage() {
        return "environment";
    }
}
