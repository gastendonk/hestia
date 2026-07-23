package hestia.otc;

import hestia.base.HAction;

public class InstallOtelcolContribAction extends HAction {

    @Override
    protected void execute() {
        if (!new OtcService().installOtelcolContrib()) {
            throw new RuntimeException(n("installOtelcolContribFailed"));
        }
        
        ctx.redirect("/");
    }
}
