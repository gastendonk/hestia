package hestia.web;

import hestia.base.HAction;
import hestia.environment.EnvironmentDAO;
import hestia.otc.OtcService;
import hestia.prometheus.PrometheusService;

public class DeployAction extends HAction {

    @Override
    protected void execute() {
        var envs = EnvironmentDAO.load().stream().filter(i -> i.isActive()).map(i -> i.getId()).toList();
        new OtcService().deploy(envs);
        new PrometheusService().deploy(envs);
        
        ctx.redirect("/");
    }
}
