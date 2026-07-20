package hestia.prometheus.alert;

import github.soltaufintel.amalia.base.IdGenerator;
import hestia.HestiaWebapp;
import hestia.base.HPage;

public class AddAlertRulePage extends HPage {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        String groupId = ctx.pathParam("g");

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
            HestiaWebapp.persistenceFactory.alertRule().save(env, groupId, rule, true);
            
            ctx.redirect("/alert/" + env);
        } else {
            header(n("AddRule"));
            put("env", esc(env));
            put("alertHint", "camelCase oder snake_case, keine Leerzeichen");
        }
    }

}
