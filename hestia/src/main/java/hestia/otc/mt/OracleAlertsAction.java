package hestia.otc.mt;

import hestia.otc.OtcService;
import hestia.web.base.HAction;

public class OracleAlertsAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");

        String branch = b().getBranch();
        new OtcService().oracleAlerts(b(), id, n("istDown"));
        
        ctx.redirect("/" + branch + "/alert/" + id);
    }
}
