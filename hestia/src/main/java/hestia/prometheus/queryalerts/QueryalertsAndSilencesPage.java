package hestia.prometheus.queryalerts;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import hestia.HestiaWebapp;
import hestia.config.HestiaConfig;
import hestia.environment.Environment;
import hestia.prometheus.alert.AlertGroup;
import hestia.prometheus.alert.rule.AlertRule;
import hestia.prometheus.silences.PrometheusSilencesService;
import hestia.prometheus.silences.Silence;
import hestia.web.base.HPage;

public class QueryalertsAndSilencesPage extends HPage {
    // TODO extend silence
    private Map<String, List<AlertGroup>> groups;
    
    @Override
    protected void execute() {
        HestiaConfig c = HestiaWebapp.config;
        List<PrometheusResult> queryalerts;
        List<Silence> silences;
        try {
            queryalerts = new PrometheusQueryAlertsService(c.getPrometheusHost()).queryAlerts();
        } catch (Exception e) {
            Logger.error(e.getMessage());
            queryalerts = new ArrayList<>();
        }
        try {
            silences = new PrometheusSilencesService(c.getAlertmanagerHost()).getActiveSilences();
        } catch (Exception e) {
            Logger.error(e.getMessage());
            silences = new ArrayList<>();
        }
        // load all alert groups
        var groupDAO = c.alertGroupDAO(b());
        for (Environment env : c.environmentDAO(b()).load()) {
            groups.put(env.getId(), groupDAO.load(env.getId()));
        }
        
        header(n("QueryAlertsandSilences"));
        queryalerts(queryalerts);
        silences(silences);
    }
    
    private void queryalerts(List<PrometheusResult> queryalerts) {
        var list = list("queryalerts");
        for (PrometheusResult r : queryalerts) {
            var m = list.add();
            m.put("name", esc(r.getAlertName()));
            m.put("link", esc(link(r.getAlertName())));
            m.put("state", esc(r.getAlertState()));
            m.put("value", esc(r.getAlertValue()));
            m.put("ts", esc(r.getFormattedTimestamp()));
            m.put("instance", esc(r.getInstance()));
        }
        Cols cols = Cols.of(
                new Col("Name", "{{if i.link}}<a href=\"{{i.link}}\">{{i.name}}</a>{{else}}{{i.name}}{{/if}}"),
                Col.si("State", "state"),
                Col.si("Value", "value"),
                Col.si("Datum", "ts"),
                Col.si("Instance", "instance"),
                new Col("", "<a href=\"/qas/silence/{{i.name}}\" class=\"btn btn-xs btn-default\">" + n("DoSilence") + "</a>")
                );
        put("table1", new TableComponent("wauto", cols, model, "queryalerts"));
    }

    private String link(String alert) {
        String ret = ""; // Alarmregel nicht gefunden
        if (!HestiaWebapp.config.isCustomer()) {
            for (Entry<String, List<AlertGroup>> e : groups.entrySet()) {
                var pre = "/" + b().getBranch() + "/alert-rule/" + e.getKey() + "/";
                for (AlertGroup group : e.getValue()) {
                    for (AlertRule rule : group.getRules()) {
                        if (rule.getAlert().equalsIgnoreCase(alert)) {
                            if (ret.isEmpty()) {
                                ret = pre + group.getId() + "/" + rule.getId() + "/edit";
                            } else {
                                return ""; // Alarmregel ist mehrdeutig, dann kein Link liefern
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    private void silences(List<Silence> silences) {
        var list = list("silences");
        for (Silence s : silences) {
            var m = list.add();
            m.put("id", esc(s.getId()));
            m.put("c", esc(s.getCreatedBy()));
            m.put("co", esc(s.getComment()));
            m.put("m", esc(s.getMatchersString()));
            m.put("s", esc(s.getStatus() == null ? "" : s.getStatus().getState()));
            m.put("end", esc(s.getEndsAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))));
// TODO Zeitzone!
        }
        Cols cols = Cols.of(
                Col.si("Created by", "c"),
                Col.si("Comment", "co"),
                Col.si("State", "s"),
                Col.si("End", "end"),
                Col.si("Matcher", "m"),
                new Col("", "<a href=\"/qas/expire-silence/{{i.id}}\" class=\"btn btn-xs btn-danger\" onclick=\"return confirm('{{N.Delete}}?');\""
                        + " title=\"{{N.Delete}}\"><i class=\"fa fa-trash-o\"></i></a>")
                );
        put("table2", new TableComponent("wauto", cols, model, "silences"));
    }
}
