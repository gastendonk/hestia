package hestia.prometheus.alert;

import org.pmw.tinylog.Logger;

import hestia.prometheus.alert.rule.AlertRuleTemplateService;
import hestia.web.base.HAction;

public class ApplyTemplatesAction extends HAction {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        
        Logger.info(b().getBranch() + " | apply alert rule templates: " + env);
        new AlertRuleTemplateService().applyAlertRuleTemplates(b(), env);
        
        ctx.redirect("/" + b().getBranch() + "/alert/" + env);
    }
}
