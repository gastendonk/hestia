package hestia.otc.model;

import hestia.persist.AbstractJsonListDAO;
import hestia.persist.IRepository;

public class SecretDAO extends AbstractJsonListDAO<Secret> {

    public SecretDAO(IRepository gitRepository) {
        super(gitRepository, Secret.class);
    }

    @Override
    public String getPath(String environmentId) {
        return "monitoredtargets/secrets/" + environmentId + ".json";
    }

    @Override
    protected String getItemNameForCommitMessage() {
        return "secrets";
    }
}