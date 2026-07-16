package hestia.prometheus.queryalerts;

import java.util.List;

import github.soltaufintel.amalia.rest.REST;

public class PrometheusQueryAlertsService {
    private static final String ENDPOINT3 = "/api/v1/query";
    private final String prometheusHost;

    /**
     * @param prometheusHost e.g. "http://server:9090"
     */
    public PrometheusQueryAlertsService(String prometheusHost) {
        this.prometheusHost = prometheusHost;
    }

    public List<PrometheusResult> queryAlerts() {
        return new REST(prometheusHost + ENDPOINT3 + "?query=ALERTS")
                .get()
                .fromJson(PrometheusResponse.class)
                .data()
                .result();
    }
}
