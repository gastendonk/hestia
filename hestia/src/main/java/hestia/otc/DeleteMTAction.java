package hestia.otc;

import hestia.base.HAction;

public class DeleteMTAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id"); // environment
        String id2 = ctx.pathParam("id2"); // MonitoredTarget

        var list = MonitoredTargetDAO.load(id);
        if (list.removeIf(i -> i.getId().equals(id2))) {
            MonitoredTargetDAO.save(id, list);
        }
     
        ctx.redirect("/mt/" + id);
    }
}
