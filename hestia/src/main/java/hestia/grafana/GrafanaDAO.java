package hestia.grafana;

import hestia.persist.AbstractJsonListSimpleDAO;
import hestia.persist.IRepository;

public class GrafanaDAO extends AbstractJsonListSimpleDAO<Grafana> {

    public GrafanaDAO(IRepository gitRepository) {
        super(gitRepository, Grafana.class);
    }

    @Override
    public String getPath(String environmentId) {
        return "grafana/grafana.json";
    }

    @Override
    protected String getItemNameForCommitMessage() {
        return "Grafana";
    }
}
