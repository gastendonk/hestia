package hestia.prometheus.queryalerts;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public record PrometheusResult(Map<String, String> metric, //
        List<Object> value // Kombiniert Timestamp (Double/Long) und Wert (String "1")
) {

    public String getAlertName() {
        return metric != null ? metric.get("alertname") : null;
    }

    public String getAlertState() {
        return metric != null ? metric.get("alertstate") : null;
    }

    public String getInstance() {
        return metric != null ? metric.get("instance") : null;
    }

    /**
     * Gibt den Zeitstempel des Alarms zurueck.
     */
    public Double getTimestamp() {
        if (value != null && !value.isEmpty()) {
            Object ts = value.get(0);
            if (ts instanceof Number number) {
                return number.doubleValue();
            }
        }
        return null;
    }

    public String getFormattedTimestamp() {
        Double ts = getTimestamp();
        if (ts == null) {
            return "N/A";
        }

        // Prometheus liefert Sekunden. Instant benoetigt Sekunden + Nanosekunden-Anteil.
        long seconds = ts.longValue();
        long nanos = Math.round((ts - seconds) * 1_000_000_000);

        Instant instant = Instant.ofEpochSecond(seconds, nanos);

        // Formatter fuer das gewuenschte Format (hier deutsch/lokal)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault()); // Nutzt die Zeitzone des Servers

        return formatter.format(instant);
    }

    /**
     * Gibt den Wert des Alarms zurueck (bei ALERTS meistens "1").
     */
    public String getAlertValue() {
        if (value != null && value.size() > 1) {
            return String.valueOf(value.get(1));
        }
        return null;
    }
}
