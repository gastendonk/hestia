package hestia.prometheus.alert;

import hestia.base.HAction;

public class DeleteAlertRuleAction extends HAction {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        String g = ctx.pathParam("g");
        String id = ctx.pathParam("id");
        
        AlertGroupDAO.delete(env, g, id);
        
        ctx.redirect("/alert/" + env);
    }
}
