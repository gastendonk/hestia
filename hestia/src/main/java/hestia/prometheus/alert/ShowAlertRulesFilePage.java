package hestia.prometheus.alert;

import github.soltaufintel.amalia.base.FileService;
import hestia.HestiaWebapp;
import hestia.web.base.HPage;

public class ShowAlertRulesFilePage extends HPage {

    @Override
    protected void execute() {
        header(n("showAlertRulesFile"));
        put("alertRules", esc(FileService.loadPlainTextFile(HestiaWebapp.config.getAlertRulesFile())));
    }
}
