package hestia.prometheus.queryalerts;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class QasAlert {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private Map<String, String> labels;
    //private Map<String, String> annotations;
    private String state;
    private String activeAt;
    private String value;

    public String getAlertName() {
        return labels != null ? labels.get("alertname") : null;
    }

    public String getAlertState() {
        return state;
    }
    
    public void setAlertState(String v) {
        state = v;
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
