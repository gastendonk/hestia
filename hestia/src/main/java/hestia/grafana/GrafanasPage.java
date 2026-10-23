package hestia.grafana;

import java.util.List;

import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import hestia.HestiaWebapp;
import hestia.grafana.GrafanaData.DashboardName;
import hestia.web.base.HPage;

public class GrafanasPage extends HPage {

    @Override
    protected void execute() {
        if (!HestiaWebapp.config.isGrafanaAllowed()) {
            throw new RuntimeException("Not allowed");
        }
        List<Grafana> grafanas = GrafanaService.loadWithDashboardNames(b());

        header("Grafana");
        put("branch", esc(b().getBranch()));
        displayConfig(grafanas);
    }
    
    private void displayConfig(List<Grafana> grafanas) {
        var list = list("list");
        for (Grafana g : grafanas) {
            var m = list.add();
            m.put("id", esc(g.getId()));
            m.put("name", esc(g.getName()));
            m.put("host", esc(g.getHost()));
            displayDashboards(g, m, grafanas.size() >= 2);
        }
        String link = "/{{branch}}/grafana/{{i.id}}/";
        Cols cols = Cols.of(
                Col.si("Name", "name"),
                Col.si("Host", "host", "{{i.host}}"),
                new Col("", "<a href=\"" + link + "edit\" class=\"btn btn-xs btn-default\"><i class=\"fa fa-pencil\"></i> {{N.Edit}}</a>"
                        + " <a href=\"/" + link + "delete\" class=\"btn btn-xs btn-danger\""
                        + " onclick=\"return confirm('{{N.Delete}}?');\" title=\"{{N.Delete}}\"><i class=\"fa fa-trash-o\"></i></a>"));
        put("table", new TableComponent("wauto", cols, model, "list"));
    }
    
    private void displayDashboards(Grafana grafana, DataMap parent, boolean atLeastTwo) {
        parent.put("reason", esc(grafana.getError()));
        var list = parent.list("dashboards");
        if (grafana.getDashboardNames() != null) {
            for (DashboardName d : grafana.getDashboardNames()) {
                var m = list.add();
                m.put("id", esc(d.getId()));
                m.put("name", esc(d.getName()));
            }
            String link = "/" + esc(b().getBranch()) + "/grafana/" + grafana.getId() + "/dashboard/{{i.id}}/";
            Cols cols = Cols.of(Col.si("Dashboard", "name"),
                    new Col("", "<a hx-get=\"" + link + "backup\" hx-swap=\"outerHTML\" hx-target=\"#res_{{i.id}}\" " + //
                            "class=\"btn btn-xs btn-default\">" + n("Backup") + "</a> <span id=\"res_{{i.id}}\"></span>" //
                            + " <a href=\"" + link + "transfer\" class=\"btn btn-xs btn-default\">" + n("Transfer") + "...</a>"));
            var table = new TableComponent("wauto mt0", cols, parent, "dashboards");
            table.init(ctx);
            parent.put("dtable", table.run());
        }
    }
}
