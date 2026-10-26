package hestia.grafana;

import github.soltaufintel.amalia.base.FileService;
import hestia.web.base.HPage;

public class GrafanaDashboardJsonPage extends HPage {

    @Override
    protected void execute() {
        String grafanaId = ctx.pathParam("g");
        String dashboardId = ctx.pathParam("d");

        var gc = grafanaDAO().loadOne(grafanaId);
        var sv = new GrafanaService(gc);
        var d = sv.loadDashboard(dashboardId);

        header("Dashboard JSON: " + d.getDashboardTitle());
        put("json", esc(FileService.prettyJSON(d.getSpec())));
    }
}
