package hestia.grafana;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Escaper;
import hestia.HestiaWebapp;
import hestia.web.base.HAction;

// HTMX
public class BackupDashboardAction extends HAction {
    private String html = "<span class=\"tfRed\">?</span>";
    
    @Override
    protected void execute() {
        String grafanaId = ctx.pathParam("g");
        String dashboardId = ctx.pathParam("d");
        
        try {
            GrafanaService sv = HestiaWebapp.config.byGrafanaId(b(), grafanaId);
            var gitRepo = HestiaWebapp.config.getRepository(b());
            sv.backup(dashboardId, gitRepo);
            html = "<i class=\"fa fa-check greenbook\"></i>";
        } catch (Exception e) {
            Logger.error("BackupDashboardAction Grafana: " + grafanaId + ", Dashboard: " + dashboardId);
            Logger.error(e);
            html = "<span class=\"tfRed\">" + Escaper.esc(e.getMessage()) + "</span>";
        }
    }

    @Override
    protected String render() {
        return html;
    }
}
