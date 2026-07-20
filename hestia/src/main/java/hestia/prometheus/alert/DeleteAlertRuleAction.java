package hestia.prometheus.alert;

import hestia.HestiaWebapp;
import hestia.base.HAction;

public class DeleteAlertRuleAction extends HAction {

    @Override
    protected void execute() {
        String envId = ctx.pathParam("env");
        String groupId = ctx.pathParam("g");
        String id = ctx.pathParam("id");
        
        HestiaWebapp.persistenceFactory.alertRule().delete(envId, groupId, id);
        
        ctx.redirect("/alert/" + envId);
    }
}
