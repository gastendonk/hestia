package hestia.prometheus.alert.rule;

import hestia.HestiaWebapp;
import hestia.web.base.HAction;

public class DeleteAlertRuleAction extends HAction {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        String g = ctx.pathParam("g");
        String id = ctx.pathParam("id");
        
        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        alertRuleDAO().delete(env, g, id);
        
        ctx.redirect("/" + ctx.pathParam("branch") + "/alert/" + env);
    }
}
