package hestia.prometheus.queryalerts;

public record PrometheusResponse(String status, PrometheusData data) {
}
