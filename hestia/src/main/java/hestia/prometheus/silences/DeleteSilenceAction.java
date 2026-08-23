package hestia.prometheus.silences;

import hestia.HestiaWebapp;
import hestia.web.base.HAction;

public class DeleteSilenceAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");

        new PrometheusSilencesService(HestiaWebapp.config.getAlertmanagerHost()).expireSilence(id);
        
        ctx.redirect("/qas");
    }
}
