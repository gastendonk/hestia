package hestia.base;

import github.soltaufintel.amalia.web.action.Page;
import hestia.HestiaWebapp;
import hestia.environment.EnvironmentDAO;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.prometheus.alert.AlertGroupDAO;
import hestia.prometheus.alert.rule.AlertRuleDAO;

public abstract class HPage extends Page {

    protected final String n(String key) {
        return NLS.get(HestiaWebapp.config.getLanguage(), key);
    }
    
    protected final void header(String title) {
        put("header", esc(title));
        put("title", esc(title) + " - Hestia");
    }
    
    protected final EnvironmentDAO environmentDAO() {
        return HestiaWebapp.config.environmentDAO(b());
    }
    
    protected final MonitoredTargetDAO mtDAO() {
        return HestiaWebapp.config.mtDAO(b());
    }
    
    protected final AlertGroupDAO alertGroupDAO() {
        return HestiaWebapp.config.alertGroupDAO(b());
    }
    
    protected final AlertRuleDAO alertRuleDAO() {
        return HestiaWebapp.config.alertRuleDAO(b());
    }
    
    protected IBranch b() {
        return () -> ctx.pathParam("branch");
    }

}
