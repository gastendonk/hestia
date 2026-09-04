package hestia.prometheus.queryalerts;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public record QasAlert(
        Map<String, String> labels,
        Map<String, String> annotations,
        String state,
        String activeAt,
        String value) {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public String getAlertName() {
        return labels != null ? labels.get("alertname") : null;
    }

    public String getAlertState() {
        return state;
    }

    public String getAlertValue() {
        return value;
    }

    public String getInstance() {
        return labels != null ? labels.get("instance") : null;
    }

    public String getFormattedTimestamp() {
        if (activeAt == null || activeAt.isBlank()) {
            return "N/A";
        }
        return OffsetDateTime.parse(activeAt)
                .atZoneSameInstant(ZoneId.systemDefault())
                .format(DATE_FORMAT);
    }
}
