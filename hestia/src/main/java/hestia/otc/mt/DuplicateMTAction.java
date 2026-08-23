package hestia.otc.mt;

import hestia.HestiaWebapp;
import hestia.otc.OtcService;
import hestia.web.base.HAction;

public class DuplicateMTAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id"); // environment
        String id2 = ctx.pathParam("id2"); // MonitoredTarget

        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        var idNeu = new OtcService().duplicate(b(), id, id2);
        
        ctx.redirect("/" + ctx.pathParam("branch") + "/mt/" + id + "/" + idNeu + "/edit");
    }
}
