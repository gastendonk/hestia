package hestia.otc.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.pmw.tinylog.Logger;

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
    
    /**
     * @param environments -
     * @return Map: key: secret name, value: decoded secret
     */
    public Map<String, String> loadAllAsMap(Collection<String> environments) {
        Map<String, String> ret = new HashMap<>();
        for (Secret secret : loadAll(environments)) {
            if (ret.containsKey(secret.getName())) {
                Logger.warn("Secret name \"" + secret.getName() + "\" is not unique");
            }
            ret.put(secret.getName(), Secret.decode(secret.getSecret()));
        }
        return ret;
    }
}