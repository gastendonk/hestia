package hestia.prometheus.queryalerts;

import java.util.List;

import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.rest.REST;

/**
 * Service for loading active alerts
 */
public class PrometheusQueryAlertsService {
    private static final String ENDPOINT = "/api/v1/alerts";
    private final String prometheusHost;

    /**
     * @param prometheusHost e.g. "http://server:9090"
     */
    public PrometheusQueryAlertsService(String prometheusHost) {
        this.prometheusHost = prometheusHost;
    }

    public List<QasAlert> queryAlerts() {
        if (StringService.isNullOrEmpty(prometheusHost)) {
            return List.of();
        }
        var response = new REST(prometheusHost + ENDPOINT)
                .get()
                .fromJson(PrometheusAlertsResponse.class);
        
        if (response == null || response.data() == null || response.data().alerts() == null) {
            return List.of();
        }
        return response.data().alerts();
    }
}
