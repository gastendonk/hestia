package hestia.prometheus.alert.rule;

import hestia.HestiaWebapp;
import hestia.web.base.HPage;

public class EditAlertRulePage extends HPage {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        String groupId = ctx.pathParam("g");
        String id = ctx.pathParam("id");

        var dao = alertRuleDAO();
        var rule = dao.loadOne(env, groupId, id);
        
        if (isPOST()) {
            save(env, groupId, rule, dao);
        } else {
            display(env, groupId, rule);
        }
    }

    private void display(String env, String groupId, AlertRule rule) {
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

    private void save(String env, String groupId, AlertRule rule, AlertRuleDAO dao) {
        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
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
        dao.update(env, groupId, rule);
        
        ctx.redirect("/" + ctx.pathParam("branch") + "/alert/" + env);
    }
}
