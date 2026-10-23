package hestia.grafana;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.base.StringService;
import hestia.web.base.HPage;

public class AddGrafanaPage extends HPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String name = ctx.formParam("name");
            if (StringService.isNullOrEmpty(name)) {
                throw new RuntimeException(n("PleaseEnterName"));
            }
            String host = ctx.formParam("host");
            if (host != null) {
                host = host.trim();
                while (host.endsWith("/")) {
                    host = host.substring(0, host.length() - 1).trim();
                }
            }

            Grafana g = new Grafana();
            g.setId(IdGenerator.createId25());
            g.setName(name);
            g.setHost(host);
            g.setToken(ctx.formParam("token"));
            grafanaDAO().insert(g);

            ctx.redirect("/" + esc(b().getBranch()) + "/grafana");
        } else {
            header(n("AddGrafana"));
            put("branch", esc(b().getBranch()));
        }
    }
}
