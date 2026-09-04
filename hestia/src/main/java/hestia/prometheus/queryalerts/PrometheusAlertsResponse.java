package hestia.prometheus.queryalerts;

public record PrometheusAlertsResponse(
        String status,
        PrometheusAlertsData data) {
}
