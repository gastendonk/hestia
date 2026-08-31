package hestia.prometheus.alert.rule;

import java.util.ArrayList;

import github.soltaufintel.amalia.base.IdGenerator;
import hestia.HestiaWebapp;
import hestia.otc.model.MonitoredTarget;
import hestia.web.base.HPage;

public class AddAlertRulePage extends HPage {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        String groupId = ctx.pathParam("g");

        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        if (isPOST()) {
            String alert = id(ctx.formParam("alert"));
            if (alert.isBlank()) {
                throw new RuntimeException("Please enter ID");
            }

            var rule = new AlertRule();
            rule.setId(IdGenerator.createId25());
            rule.setAlert(alert);
            rule.setSummary(ctx.formParam("summary").trim());
            rule.setDescription(ctx.formParam("description"));
            rule.setExpr(ctx.formParam("expr"));
            rule.setChannel(ctx.formParam("channel"));
            rule.setEscalationChannel(ctx.formParam("channel2"));
            rule.setDurationFor(ctx.formParam("durationFor"));
            rule.setKeepFiringFor(ctx.formParam("keepFiringFor"));
            rule.setMttype(ctx.formParam("mttype"));
            alertRuleDAO().insert(env, groupId, rule);
            
            ctx.redirect("/" + ctx.pathParam("branch") + "/alert/" + env);
        } else {
            put("env", esc(env));
            put("alertHint", "camelCase oder snake_case, keine Leerzeichen");
            combobox("mttypes", MonitoredTarget.MTTYPES, MonitoredTarget.MTTYPES.get(0), false);
            var channels = new ArrayList<>(HestiaWebapp.config.getChannels());
            if ("templates".equals(env)) {
                header(n("AddRule") + " Template");
                put("templates", true);
                channels.add("{customer}");
            } else {
                header(n("AddRule"));
                put("templates", false);
            }
            combobox("channels", channels, "", true);
            combobox("channels2", channels, "", true);
        }
    }

    public static String id(String name) {
        return name.replace(" ", "");
    }
}
