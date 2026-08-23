package hestia.otc;

import hestia.web.base.HAction;

/** Für jede Site einen Alarm anlegen. */
public class SiteAlertsAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");

        String branch = b().getBranch();
        new OtcService().siteAlerts(b(), id, n("istDown"));
        
        ctx.redirect("/" + branch + "/alert/" + id);
    }
}
