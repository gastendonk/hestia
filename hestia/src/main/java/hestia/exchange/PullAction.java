package hestia.exchange;

import org.pmw.tinylog.Logger;

import hestia.web.DeployAction;
import hestia.web.base.HAction;

public class PullAction extends HAction {

    @Override
    protected void execute() {
        // Update configuration
        Logger.info("Update configuration (pull) ----");
        new ExchangeService().pull();
        
        // Deploy
        Logger.info("deploy ----");
        DeployAction.deploy(b(), environmentDAO());

        ctx.redirect("/");
    }
}
