package hestia.otc;

import hestia.HestiaWebapp;
import hestia.base.HAction;

public class DeleteMTAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id"); // environment
        String id2 = ctx.pathParam("id2"); // MonitoredTarget

        HestiaWebapp.persistenceFactory.monitoredTarget().delete(id, id2);
     
        ctx.redirect("/mt/" + id);
    }
}
