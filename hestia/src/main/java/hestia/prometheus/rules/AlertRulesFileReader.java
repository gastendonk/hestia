package hestia.prometheus.rules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertRulesFileReader {

    public AlertRulesFile read(File file) throws IOException {
        AlertRulesFile ret = new AlertRulesFile();
        ret.load(file);
        ret.setName(file.getName());
        ret.setGroups(mapToAlertGroups(ret.getYaml()));
        return ret;
    }

    @SuppressWarnings("unchecked")
    private List<AlertGroup> mapToAlertGroups(Map<String, Object> yamlData) {
        List<AlertGroup> alertGroups = new ArrayList<>();
        if (yamlData == null || !yamlData.containsKey("groups")) {
            return alertGroups;
        }
        // 1. Hole die "groups"-Liste aus der Root-Map
        List<Map<String, Object>> rawGroups = (List<Map<String, Object>>) yamlData.get("groups");
        for (Map<String, Object> rawGroup : rawGroups) {
            AlertGroup group = new AlertGroup();
            group.setYaml(rawGroup);

            // 2. Hole die "rules"-Liste aus der aktuellen Gruppe
            if (rawGroup.containsKey("rules")) {
                List<Map<String, Object>> rawRules = (List<Map<String, Object>>) rawGroup.get("rules");
                if (rawRules != null) {
                    for (Map<String, Object> rawRule : rawRules) {
                        group.getRules().add(mapToAlertRule(rawRule));
                    }
                }
            }
            alertGroups.add(group);
        }
        return alertGroups;
    }
    
    @SuppressWarnings("unchecked")
    private AlertRule mapToAlertRule(Map<String, Object> rawRule) {
        AlertRule rule = new AlertRule();
        rule.setYaml(rawRule);
        rule.setAlert((String) rawRule.get("alert"));
        rule.setExpr((String) rawRule.get("expr"));
        rule.setDurationFor((String) rawRule.get("for")); // Mappt das YAML 'for' auf 'durationFor'
        
        // 3. Labels und Annotations extrahieren (falls vorhanden)
        if (rawRule.containsKey("labels")) {
            rule.setLabels((Map<String, String>) rawRule.get("labels"));
        }
        if (rawRule.containsKey("annotations")) {
            rule.setAnnotations((Map<String, String>) rawRule.get("annotations"));
        }
        return rule;
    }
}
