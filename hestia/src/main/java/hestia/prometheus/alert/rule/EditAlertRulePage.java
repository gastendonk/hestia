package hestia.prometheus.alert.rule;

import hestia.base.HPage;
import hestia.prometheus.alert.AlertGroupDAO;

public class EditAlertRulePage extends HPage {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        String groupId = ctx.pathParam("g");
        String id = ctx.pathParam("id");

        var rule = AlertGroupDAO.load(env, groupId, id);
        
        if (isPOST()) {
            String alert = ctx.formParam("alert").replace(" ", "");
            if (alert.isBlank()) {
                throw new RuntimeException("Please enter ID");
            }

            rule.setAlert(alert);
            rule.setSummary(ctx.formParam("summary").trim());
            rule.setDescription(ctx.formParam("description"));
            rule.setExpr(ctx.formParam("expr"));
            rule.setDurationFor(ctx.formParam("durationFor"));
            rule.setKeepFiringFor(ctx.formParam("keepFiringFor"));
            rule.setActive("on".equals(ctx.formParam("active")));
            AlertGroupDAO.update(env, groupId, rule);
            
            ctx.redirect("/alert/" + env);
        } else {
            header(n("EditRule"));
            put("env", esc(env));
            put("groupId", esc(groupId));
            put("alertHint", "camelCase oder snake_case, keine Leerzeichen");
            put("id", esc(rule.getId()));
            put("alert", esc(rule.getAlert()));
            put("summary", esc(rule.getSummary()));
            put("description", esc(rule.getDescription()));
            put("expr", esc(rule.getExpr()));
            put("durationFor", esc(rule.getDurationFor()));
            put("keepFiringFor", esc(rule.getKeepFiringFor()));
            put("active", rule.isActive());
        }
    }
}
