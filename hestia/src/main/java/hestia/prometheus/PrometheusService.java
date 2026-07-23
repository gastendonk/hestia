package hestia.prometheus;

import java.util.Collection;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.base.StringService;
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
        if (StringService.isNullOrEmpty(HestiaWebapp.config.getPrometheusHost())) {
            return;
        }
        var dao = HestiaWebapp.config.alertGroupDAO(branch);
        List<AlertGroup> groups = dao.loadAll(environments);
        var yaml = new AlertRulesYamlBuilder(groups).build();
        FileService.savePlainTextFile(HestiaWebapp.config.getAlertRulesFile(), yaml);
        reloadPrometheus();
    }

    public void reloadPrometheus() {
        if (!StringService.isNullOrEmpty(HestiaWebapp.config.getPrometheusHost())) {
            var url = HestiaWebapp.config.getPrometheusHost() + "/-/reload";
            Logger.info("reloadPrometheus: " + url);
            REST.post(url, "");
        }
    }
    
    public void reloadAlertmanager() {
        if (!StringService.isNullOrEmpty(HestiaWebapp.config.getAlertmanagerHost())) {
            var url = HestiaWebapp.config.getAlertmanagerHost() + "/-/reload";
            Logger.info("reloadAlertmanager: " + url);
            REST.post(url, "");
        }
    }
}
