package hestia.grafana;

import hestia.web.base.HAction;

public class DeleteGrafanaAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        grafanaDAO().delete(id);

        ctx.redirect("/" + esc(b().getBranch()) + "/grafana");
    }
}
