package hestia.prometheus.alert;

import hestia.HestiaWebapp;
import hestia.base.HAction;

public class DeleteAlertGroupAction extends HAction {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        String id = ctx.pathParam("id");

        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        AlertGroupDAO.delete(env, id);

        ctx.redirect("/alert/" + env);
    }
}
