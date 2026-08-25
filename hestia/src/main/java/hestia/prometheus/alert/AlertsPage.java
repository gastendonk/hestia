package hestia.prometheus.alert;

import java.util.ArrayList;
import java.util.List;

import hestia.prometheus.alert.rule.AlertRule;
import hestia.web.base.HPage;

public class AlertsPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("env");

        var env = environmentDAO().loadOne(id);
        List<AlertGroup> groups = new ArrayList<>(alertGroupDAO().load(id));
        
        header(n("alertRules"));
        cenv(env);
        put("env", esc(id));
        var list = list("groups");
        var first = true;
        groups.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        for (AlertGroup g : groups) {
            var m = list.add();
            m.put("id", esc(g.getId()));
            m.put("name", esc(g.getName()));
            m.put("first", first);
            first = false;
            var list2 = m.list("rules");
            g.getRules().sort((a, b) -> a.getAlert().compareToIgnoreCase(b.getAlert()));
            for (AlertRule r : g.getRules()) {
                var m2 = list2.add();
                m2.put("id", esc(r.getId()));
                m2.put("alert", esc(r.getAlert()));
                m2.put("summary", esc(r.getSummary()));
                m2.put("active", r.isActive());
                m2.put("channel", esc(r.getChannel()));
                m2.put("channel2", esc(r.getEscalationChannel()));
            }
        }
    }
}
