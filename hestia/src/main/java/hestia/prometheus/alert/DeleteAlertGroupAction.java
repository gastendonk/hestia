package hestia.prometheus.alert;

import hestia.HestiaWebapp;
import hestia.base.HAction;

public class DeleteAlertGroupAction extends HAction {

    @Override
    protected void execute() {
        String envId = ctx.pathParam("env");
        String groupId = ctx.pathParam("id");

        HestiaWebapp.persistenceFactory.alertGroup().delete(envId, groupId);

        ctx.redirect("/alert/" + envId);
    }
}
