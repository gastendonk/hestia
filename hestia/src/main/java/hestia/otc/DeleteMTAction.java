package hestia.otc;

import hestia.HestiaWebapp;
import hestia.web.base.HAction;

public class DeleteMTAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id"); // environment
        String id2 = ctx.pathParam("id2"); // MonitoredTarget

        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        mtDAO().delete(id, id2);
     
        ctx.redirect("/" + ctx.pathParam("branch") + "/mt/" + id);
    }
}
