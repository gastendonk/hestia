package hestia.web;

import java.util.List;

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
        if (HestiaWebapp.config.getCustomers().contains("BURG")) {
            envs = rawEnvs.stream().filter(i -> i.isActive() && i.getCustomer().equals("BURG")).map(i -> i.getId()).toList();
        } else {
            envs = rawEnvs.stream().filter(i -> i.isActive()).map(i -> i.getId()).toList();
        }
        new OtcService().deploy(envs, b);
        new PrometheusService().deploy(envs, b);
    }
}
