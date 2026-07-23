package hestia.web;

import hestia.otc.OtcService;
import hestia.prometheus.PrometheusService;
import hestia.web.base.HAction;

public class DeployAction extends HAction {

    @Override
    protected void execute() {
        var envs = environmentDAO().load().stream().filter(i -> i.isActive()).map(i -> i.getId()).toList();
        new OtcService().deploy(envs, b());
        new PrometheusService().deploy(envs, b());
        
        backToStartpage();
    }
}
