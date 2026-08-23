package hestia.otc.mt;

import hestia.otc.OtcService;
import hestia.web.base.HAction;

/** F�r jede Site einen Alarm anlegen. */
public class SiteAlertsAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");

        String branch = b().getBranch();
        new OtcService().siteAlerts(b(), id, n("istDown"));
        
        ctx.redirect("/" + branch + "/alert/" + id);
    }
}
