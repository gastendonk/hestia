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

            Grafana g = new Grafana();
            g.setId(IdGenerator.createId25());
            g.setName(name);
            g.setHost(ctx.formParam("host"));
            g.setToken(ctx.formParam("token"));

            ctx.redirect("/" + esc(b().getBranch()) + "/grafana");
        } else {
            header(n("AddGrafana"));
            put("branch", esc(b().getBranch()));
        }
    }
}
