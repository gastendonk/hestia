package hestia.otc;

import hestia.HestiaWebapp;
import hestia.base.HAction;
import hestia.otc.model.MonitoredTargetDAO;

public class DeleteMTAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id"); // environment
        String id2 = ctx.pathParam("id2"); // MonitoredTarget

        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        var list = MonitoredTargetDAO.load(id);
        if (list.removeIf(i -> i.getId().equals(id2))) {
            MonitoredTargetDAO.save(id, list, "delete monitored target");
        }
     
        ctx.redirect("/mt/" + id);
    }
}
