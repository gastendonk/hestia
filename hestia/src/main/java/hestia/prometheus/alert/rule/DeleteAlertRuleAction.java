package hestia.prometheus.alert.rule;

import hestia.HestiaWebapp;
import hestia.base.HAction;
import hestia.prometheus.alert.AlertGroupDAO;

public class DeleteAlertRuleAction extends HAction {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        String g = ctx.pathParam("g");
        String id = ctx.pathParam("id");
        
        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        AlertGroupDAO.delete(env, g, id);
        
        ctx.redirect("/alert/" + env);
    }
}
