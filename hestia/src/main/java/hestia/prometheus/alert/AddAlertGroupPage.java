package hestia.prometheus.alert;

import github.soltaufintel.amalia.base.IdGenerator;
import hestia.HestiaWebapp;
import hestia.web.base.HPage;

public class AddAlertGroupPage extends HPage {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");

        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        if (isPOST()) {
            String name = ctx.queryParam("name").trim();
            if (name.isBlank()) {
                throw new RuntimeException("Please enter name");
            }

            AlertGroup g = new AlertGroup();
            g.setId(IdGenerator.createId25());
            g.setName(name);
            alertGroupDAO().insert(env, g);

            ctx.redirect("/" + ctx.pathParam("branch") + "/alert/" + env);
        } else {
            header(n("NewGroup"));
            put("env", esc(env));
        }
    }
}
