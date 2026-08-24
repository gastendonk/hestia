package hestia.prometheus.alert.rule;

import hestia.web.base.HAction;

public class DuplicateAlertRuleAction extends HAction {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        String groupId = ctx.pathParam("g");
        String id = ctx.pathParam("id");

        var dao = alertGroupDAO();
        var group = dao.loadOne(env, groupId);
        var x = group.getRules().stream().filter(i -> i.getId().equals(id)).findFirst().get();
        var copy = x.copy();
        group.getRules().add(copy);
        dao.update(env, group);
        
        ctx.redirect(esc("/" + b().getBranch() + "/alert-rule/" + env + "/" + groupId + "/" + copy.getId() + "/edit"));
    }
}
