package hestia.prometheus.queryalerts;

import java.util.List;

public record PrometheusAlertsData(
        List<QasAlert> alerts) {
}
