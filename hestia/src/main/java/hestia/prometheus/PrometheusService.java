package hestia.prometheus;

import java.util.Collection;
import java.util.List;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.rest.REST;
import hestia.HestiaWebapp;
import hestia.base.IBranch;
import hestia.prometheus.alert.AlertGroup;
import hestia.prometheus.alert.AlertRulesYamlBuilder;

/**
 * General service for managing Prometheus and the Prometheus Alertmanager
 */
public class PrometheusService {

    public void deploy(Collection<String> environments, IBranch branch) {
        var dao = HestiaWebapp.config.alertGroupDAO(branch);
        List<AlertGroup> groups = dao.loadAll(environments);
        var yaml = new AlertRulesYamlBuilder(groups).build();
        FileService.saveJsonFile(HestiaWebapp.config.getAlertRulesFile(), yaml);
        reloadPrometheus();
    }

    public void reloadPrometheus() {
        REST.post(HestiaWebapp.config.getPrometheusHost() + "/-/reload", "");
    }
    
    public void reloadAlertmanager() {
        REST.post(HestiaWebapp.config.getAlertmanagerHost() + "/-/reload", "");
    }
}
