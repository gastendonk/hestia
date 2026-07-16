package hestia.prometheus;

import github.soltaufintel.amalia.rest.REST;

/**
 * General service for managing Prometheus and the Prometheus Alertmanager
 */
public class PrometheusService {
    private final String prometheusHost;
    private final String alertmanagerHost;
    
    public PrometheusService(String prometheusHost, String alertmanagerHost) {
        this.prometheusHost = prometheusHost;
        this.alertmanagerHost = alertmanagerHost;
    }

    public void reloadPrometheus() {
        REST.post(prometheusHost + "/-/reload", "");
    }
    
    public void reloadAlertmanager() {
        REST.post(alertmanagerHost + "/-/reload", "");
    }
}
