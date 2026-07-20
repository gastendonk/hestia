package hestia.prometheus.alert.rule;

import github.soltaufintel.amalia.base.IdGenerator;
import hestia.HestiaWebapp;
import hestia.base.HPage;
import hestia.prometheus.alert.AlertGroupDAO;

public class AddAlertRulePage extends HPage {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        String groupId = ctx.pathParam("g");

        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        if (isPOST()) {
            String alert = ctx.formParam("alert").replace(" ", "");
            if (alert.isBlank()) {
                throw new RuntimeException("Please enter ID");
            }

            var rule = new AlertRule();
            rule.setId(IdGenerator.createId25());
            rule.setAlert(alert);
            rule.setSummary(ctx.formParam("summary").trim());
            rule.setDescription(ctx.formParam("description"));
            rule.setExpr(ctx.formParam("expr"));
            rule.setDurationFor(ctx.formParam("durationFor"));
            rule.setKeepFiringFor(ctx.formParam("keepFiringFor"));
            AlertGroupDAO.insertRule(env, groupId, rule);
            
            ctx.redirect("/alert/" + env);
        } else {
            header(n("AddRule"));
            put("env", esc(env));
            put("alertHint", "camelCase oder snake_case, keine Leerzeichen");
        }
    }

}
