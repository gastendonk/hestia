package hestia.web;

import hestia.base.IBranch;
import hestia.environment.EnvironmentDAO;
import hestia.otc.OtcService;
import hestia.prometheus.PrometheusService;
import hestia.web.base.HAction;

public class DeployAction extends HAction {

    @Override
    protected void execute() {
        deploy(b(), environmentDAO());
        
        backToStartpage();
    }
    
    public static void deploy(IBranch b, EnvironmentDAO envDAO) {
        var envs = envDAO.load().stream().filter(i -> i.isActive()).map(i -> i.getId()).toList();
        new OtcService().deploy(envs, b);
        new PrometheusService().deploy(envs, b);
    }
}
