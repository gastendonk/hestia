package hestia.grafana;

import java.util.List;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import hestia.grafana.GrafanaData.DashboardName;
import hestia.web.base.HPage;

// HTMX
public class GrafanaDashboardsPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");

        var grafana = grafanaDAO().loadOne(id);
        var sv = new GrafanaService(grafana);
        List<DashboardName> names;
        String error = null;
        try {
            names = sv.loadDashboardNames();
        } catch (Exception e) {
            error = e.getMessage();
            names = List.of();
        }

        var list = list("dashboards");
        for (DashboardName name : names) {
            var m = list.add();
            m.put("id", esc(name.getId()));
            m.put("name", esc(name.getName()));
        }
        String link = "/" + esc(b().getBranch()) + "/grafana/" + grafana.getId() + "/dashboard/{{i.id}}/";
        Cols cols = Cols.of( //
                Col.si("Dashboard", "name"),
                new Col("", "<a hx-get=\"" + link + "backup\" hx-swap=\"outerHTML\" hx-target=\"#res_{{i.id}}\" " + //
                        "class=\"btn btn-xs btn-default\">" + n("Backup") + "</a> <span id=\"res_{{i.id}}\"></span>" //
                        + " <a href=\"" + link + "transfer\" class=\"btn btn-xs btn-default\">" + n("Transfer") + "...</a>"));
        put("table", new TableComponent("wauto mt0", cols, model, "dashboards"));
        put("error", esc(error));
    }
}
