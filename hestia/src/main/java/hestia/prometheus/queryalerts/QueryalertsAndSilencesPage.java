package hestia.prometheus.queryalerts;

import java.time.format.DateTimeFormatter;
import java.util.List;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import hestia.HestiaWebapp;
import hestia.config.HestiaConfig;
import hestia.prometheus.silences.PrometheusSilencesService;
import hestia.prometheus.silences.Silence;
import hestia.web.base.HPage;

public class QueryalertsAndSilencesPage extends HPage {

    @Override
    protected void execute() {
        HestiaConfig c = HestiaWebapp.config;
        List<PrometheusResult> queryalerts = new PrometheusQueryAlertsService(c.getPrometheusHost()).queryAlerts();
        List<Silence> silences = new PrometheusSilencesService(c.getAlertmanagerHost()).getActiveSilences();
        
        header(n("QueryAlertsandSilences"));
        queryalerts(queryalerts);
        silences(silences);
    }
    
    private void queryalerts(List<PrometheusResult> queryalerts) {
        var list = list("queryalerts");
        for (PrometheusResult r : queryalerts) {
            var m = list.add();
            m.put("name", esc(r.getAlertName()));
            m.put("state", esc(r.getAlertState()));
            m.put("value", esc(r.getAlertValue()));
            m.put("ts", esc(r.getFormattedTimestamp()));
            m.put("instance", esc(r.getInstance()));
        }
        Cols cols = Cols.of(
                Col.si("Name", "name"),
                Col.si("State", "state"),
                Col.si("Value", "value"),
                Col.si("Datum", "ts"),
                Col.si("Instance", "instance")
                );
        put("table1", new TableComponent("wauto", cols, model, "queryalerts"));
    }

    private void silences(List<Silence> silences) {
        var list = list("silences");
        for (Silence s : silences) {
            var m = list.add();
            m.put("c", esc(s.getCreatedBy()));
            m.put("co", esc(s.getComment()));
            m.put("m", esc(s.getMatchersString()));
            m.put("s", esc(s.getStatus() == null ? "" : s.getStatus().getState()));
            m.put("end", esc(s.getEndsAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))));
        }
        Cols cols = Cols.of(
                Col.si("Created by", "c"),
                Col.si("Comment", "co"),
                Col.si("State", "s"),
                Col.si("End", "end"),
                Col.si("Matcher", "m")
                );
        put("table2", new TableComponent("wauto", cols, model, "silences"));
    }
}
