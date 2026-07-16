package hestia.prometheus.queryalerts;

import java.util.List;

public record PrometheusData(String resultType, List<PrometheusResult> result) {
}
