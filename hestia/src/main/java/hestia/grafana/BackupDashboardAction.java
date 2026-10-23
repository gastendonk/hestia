package hestia.grafana;

import org.pmw.tinylog.Logger;

import hestia.HestiaWebapp;
import hestia.web.base.HAction;

// HTMX
public class BackupDashboardAction extends HAction {
    private String error = "?";
    
    @Override
    protected void execute() {
        String grafanaId = ctx.pathParam("g");
        String dashboardId = ctx.pathParam("d");
        
        try {
            GrafanaService sv = HestiaWebapp.config.byGrafanaId(b(), grafanaId);
            var gitRepo = HestiaWebapp.config.getRepository(b());
            sv.backup(dashboardId, gitRepo);
            error = null;
        } catch (Exception e) {
            Logger.error("BackupDashboardAction Grafana: " + grafanaId + ", Dashboard: " + dashboardId);
            Logger.error(e);
            error = e.getMessage();
        }
    }

    @Override
    protected String render() {
        return error == null ? "<i class=\"fa fa-check green\"></i>" : ("<span class=\"red\">" + esc(error) + "</span>");
    }
}
