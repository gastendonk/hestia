package hestia.grafana;

import github.soltaufintel.amalia.base.StringService;
import hestia.web.base.HPage;

public class EditGrafanaPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        GrafanaDAO dao = grafanaDAO();
        var g = dao.loadOne(id);
        if (isPOST()) {
            String name = ctx.formParam("name");
            if (StringService.isNullOrEmpty(name)) {
                throw new RuntimeException(n("PleaseEnterName"));
            }

            g.setName(name);
            g.setHost(ctx.formParam("host"));
            g.setToken(ctx.formParam("token"));
            dao.update(g);
            
            ctx.redirect("/" + esc(b().getBranch()) + "/grafana");
        } else {
            header(n("EditGrafana"));
            put("branch", esc(b().getBranch()));
            put("name", esc(g.getName()));
            put("host", esc(g.getHost()));
            put("token", esc(g.getToken()));
        }
    }
}
