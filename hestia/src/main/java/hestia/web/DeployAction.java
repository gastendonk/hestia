package hestia.web;

import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.StringService;
import hestia.HestiaWebapp;
import hestia.base.IBranch;
import hestia.environment.EnvironmentDAO;
import hestia.otc.OtcService;
import hestia.prometheus.PrometheusService;
import hestia.web.base.HAction;

public class DeployAction extends HAction {
    private static final Object LOCK = new Object();
    public static String lastDeployed = "";

    @Override
    protected void execute() {
        deploy(b(), environmentDAO());
        
        if ("o".equals(ctx.queryParam("r"))) {
            ctx.redirect("/otc/" + esc(b().getBranch()) + "/status");
        } else {
            backToStartpage();
        }
    }
    
    public static void deploy(IBranch b, EnvironmentDAO envDAO) {
        synchronized (LOCK) {
            List<String> envs = OtcService.envs(b);
            if (envs.isEmpty()) {
                Logger.info("Can't deploy because there are no environments.");
                return;
            } else if (HestiaWebapp.config.isBURG()) {
                Logger.info("BURG deploy: " + envs);
            } else {
                Logger.info("deploy: " + envs);
            }
            new OtcService().deploy(envs, b);
            new PrometheusService().deploy(envs, b);
            lastDeployed = StringService.now();
        }
    }
}
