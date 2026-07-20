package hestia.prometheus.alert;

import java.util.List;

import hestia.base.HPage;
import hestia.environment.EnvironmentDAO;
import hestia.prometheus.alert.rule.AlertRule;

public class AlertsPage extends HPage {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");

        var environment = EnvironmentDAO.load(env);
        List<AlertGroup> groups = AlertGroupDAO.load(env);
        
        header(n("alertsFor") + " " + environment.getName());
        put("env", esc(env));
        var list = list("groups");
        var first = true;
        for (AlertGroup g : groups) {
            var m = list.add();
            m.put("id", esc(g.getId()));
            m.put("name", esc(g.getName()));
            m.put("first", first);
            first = false;
            var list2 = m.list("rules");
            for (AlertRule r : g.getRules()) {
                var m2 = list2.add();
                m2.put("id", esc(r.getId()));
                m2.put("alert", esc(r.getAlert()));
                m2.put("summary", esc(r.getSummary()));
                m2.put("active", r.isActive());
            }
        }
    }
}
