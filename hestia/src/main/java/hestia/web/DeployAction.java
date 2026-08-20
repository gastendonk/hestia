package hestia.web;

import java.util.List;

import org.pmw.tinylog.Logger;

import hestia.HestiaWebapp;
import hestia.base.IBranch;
import hestia.environment.EnvironmentDAO;
import hestia.otc.OtcService;
import hestia.prometheus.PrometheusService;
import hestia.web.base.HAction;

public class DeployAction extends HAction {

    @Override
    protected void execute() {
        deploy(b(), environmentDAO());
        
        if ("o".equals(ctx.queryParam("r"))) {
            ctx.redirect("/otc/status");
        } else {
            backToStartpage();
        }
    }
    
    public static void deploy(IBranch b, EnvironmentDAO envDAO) {
        var rawEnvs = envDAO.load();
        List<String> envs;
        if (isBURG()) {
            envs = rawEnvs.stream().filter(i -> i.isActive() && i.getCustomer().equals("BURG")).map(i -> i.getId()).toList();
            Logger.info("BURG deploy: " + envs);
        } else {
            envs = rawEnvs.stream().filter(i -> i.isActive()).map(i -> i.getId()).toList();
            Logger.info("deploy: " + envs);
        }
        if (envs.isEmpty()) {
            Logger.info("Can't deploy because there are no environments.");
            return;
        }
        new OtcService().deploy(envs, b);
        new PrometheusService().deploy(envs, b);
    }
    
    public static boolean isBURG() {
        return HestiaWebapp.config.getCustomers().contains("BURG");
    }
}
