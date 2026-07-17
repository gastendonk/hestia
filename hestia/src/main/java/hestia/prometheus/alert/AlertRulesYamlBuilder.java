package hestia.prometheus.alert;

import java.util.List;

import github.soltaufintel.amalia.base.StringService;

/**
 * Creates alert-rules.yml file content out of AlertGroup objects
 */
public class AlertRulesYamlBuilder {
    private final List<AlertGroup> groups;

    public AlertRulesYamlBuilder(List<AlertGroup> groups) {
        this.groups = groups;
    }

    public String build() {
        String ret = "";
        for (AlertGroup g : groups) {
            if (!g.getRules().stream().anyMatch(i -> i.isActive())) {
                continue;
            }
            ret += "- name: " + g.getName() + "\n";
            if (!StringService.isNullOrEmpty(g.getInterval())) {
                ret += "  interval: \"" + g.getInterval() + "\"\n";
            }
            if (g.getLimit() > 0) {
                ret += "  limit: " + g.getLimit() + "\n";
            }
            ret += rules(g.getRules()) + "\n";
        }
        return ret.isEmpty() ? ret : "groups:\n" + ret;
    }

    private String rules(List<AlertRule> rules) {
        String ret = "  rules:\n";
        for (AlertRule r : rules) {
            ret += "  - alert: " + r.getAlert() + "\n";
            ret += "    for: " + r.getDurationFor() + "\n";
            var s = !StringService.isNullOrEmpty(r.getSummary());
            var d = !StringService.isNullOrEmpty(r.getDescription());
            if (s || d) {
                ret += "    annotations:\n";
                if (s) {
                    ret += "      summary: " + r.getSummary() + "\n";
                }
                if (d) {
                    ret += "      description: " + r.getDescription() + "\n";
                }
            }
            if (!StringService.isNullOrEmpty(r.getKeepFiringFor())) {
                ret += "    keepFiringFor: " + r.getKeepFiringFor() + "\n";
            }
        }
        return ret;
    }
}
