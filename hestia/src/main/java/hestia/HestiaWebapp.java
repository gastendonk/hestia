package hestia;

import github.soltaufintel.amalia.web.builder.WebAppBuilder;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import github.soltaufintel.amalia.web.table.TableSortAction;
import hestia.base.HestiaConfig;
import hestia.base.HestiaPageInitializer;
import hestia.environment.AddEnvironmentPage;
import hestia.environment.EditEnvironmentPage;
import hestia.otc.AddMTPage;
import hestia.otc.DeleteMTAction;
import hestia.otc.EditMTPage;
import hestia.otc.EditOtcOptsPage;
import hestia.otc.MonitoredTargetsPage;
import hestia.otc.OtcProcess;
import hestia.prometheus.alert.AddAlertGroupPage;
import hestia.prometheus.alert.AddAlertRulePage;
import hestia.prometheus.alert.AlertsPage;
import hestia.prometheus.alert.DeleteAlertGroupAction;
import hestia.prometheus.alert.DeleteAlertRuleAction;
import hestia.prometheus.alert.EditAlertGroupPage;
import hestia.prometheus.alert.EditAlertRulePage;
import hestia.web.IndexPage;
import hestia.web.KillAction;

public class HestiaWebapp extends RouteDefinitions {
    public static final String VERSION = "0.1.0";
    public static HestiaConfig config;
    public static OtcProcess otcProcess;
    
    @Override
    public void routes() {
        get("/", IndexPage.class);
        get("/otc/kill", KillAction.class);
        form("/environment/add", AddEnvironmentPage.class);
        form("/environment/:id", EditEnvironmentPage.class);

        form("/mt/:id/add", AddMTPage.class);
        form("/mt/:id/:id2/edit", EditMTPage.class);
        get("/mt/:id/:id2/delete", DeleteMTAction.class);
        get("/mt/:id", MonitoredTargetsPage.class);
        form("/options", EditOtcOptsPage.class);

        get("/alert/:env", AlertsPage.class); // Alle Gruppen und Rules zu einer Umgebung
        form("/alert-group/:env/add", AddAlertGroupPage.class);
        form("/alert-group/:env/:id/edit", EditAlertGroupPage.class);
        get("/alert-group/:env/:id/delete", DeleteAlertGroupAction.class);
        form("/alert-rule/:env/:g/add", AddAlertRulePage.class);
        form("/alert-rule/:env/:g/:id/edit", EditAlertRulePage.class);
        get("/alert-rule/:env/:g/:id/delete", DeleteAlertRuleAction.class);
        
        form("/tablesort/:id/:col", TableSortAction.class);
    }

    public static void main(String[] args) {
        new WebAppBuilder(VERSION)
                .withTemplatesFolders(HestiaWebapp.class, "/templates")
                .withPageInitializer(new HestiaPageInitializer())
                .withInitializer(c -> config = new HestiaConfig())
                .withRoutes(new HestiaWebapp())
                .build()
                .boot();
        otcProcess = new OtcProcess();
    }
}
