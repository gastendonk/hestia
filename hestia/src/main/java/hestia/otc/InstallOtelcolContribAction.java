package hestia.otc;

import hestia.base.HAction;

public class DeployOtelcolcontribAction extends HAction {

    @Override
    protected void execute() {
        if (!new OtcService().deployOtelcolContrib()) {
            throw new RuntimeException("otelcol-contrib deployment failed");
        }
        
        ctx.redirect("/");
    }
}
