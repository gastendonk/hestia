package hestia;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.builder.WebAppBuilder;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import github.soltaufintel.amalia.web.table.TableSortAction;
import hestia.base.EnvVarAppConfig;
import hestia.base.HestiaError404Page;
import hestia.base.HestiaErrorPage;
import hestia.base.HestiaPageInitializer;
import hestia.config.HestiaConfig;
import hestia.environment.AddEnvironmentPage;
import hestia.environment.DeleteEnvironmentAction;
import hestia.environment.EditEnvironmentPage;
import hestia.exchange.PullAction;
import hestia.exchange.PushAction;
import hestia.exchange.ReceiveAction;
import hestia.exchange.ServeAction;
import hestia.git.GitPullAction;
import hestia.git.GitPushAction;
import hestia.otc.AddMTPage;
import hestia.otc.DeleteMTAction;
import hestia.otc.DeployOtelcolcontribAction;
import hestia.otc.EditMTPage;
import hestia.otc.MonitoredTargetsPage;
import hestia.otc.OtcProcess;
import hestia.otc.OtcService;
import hestia.otc.opts.EditOtcOptsPage;
import hestia.prometheus.alert.AddAlertGroupPage;
import hestia.prometheus.alert.AlertsPage;
import hestia.prometheus.alert.DeleteAlertGroupAction;
import hestia.prometheus.alert.EditAlertGroupPage;
import hestia.prometheus.alert.rule.AddAlertRulePage;
import hestia.prometheus.alert.rule.DeleteAlertRuleAction;
import hestia.prometheus.alert.rule.EditAlertRulePage;
import hestia.web.DeployAction;
import hestia.web.IndexPage;
import hestia.web.KillAction;

public class HestiaWebapp extends RouteDefinitions {
    public static final String VERSION = "0.1.0";
    public static HestiaConfig config;
    public static OtcProcess otcProcess;
    
    @Override
    public void routes() {
        form("/:branch/environment/add", AddEnvironmentPage.class);
        get("/:branch/environment/:id/delete", DeleteEnvironmentAction.class);
        form("/:branch/environment/:id", EditEnvironmentPage.class);
        get("/:branch/deploy", DeployAction.class);
        get("/:branch/push", GitPushAction.class);
        get("/:branch/pull", GitPullAction.class);

        form("/:branch/mt/:id/add", AddMTPage.class);
        form("/:branch/mt/:id/:id2/edit", EditMTPage.class);
        get("/:branch/mt/:id/:id2/delete", DeleteMTAction.class);
        get("/:branch/mt/:id", MonitoredTargetsPage.class);
        
        form("/options", EditOtcOptsPage.class);

        get("/:branch/alert/:env", AlertsPage.class); // Alle Gruppen und Rules zu einer Umgebung
        form("/:branch/alert-group/:env/add", AddAlertGroupPage.class);
        form("/:branch/alert-group/:env/:id/edit", EditAlertGroupPage.class);
        get("/:branch/alert-group/:env/:id/delete", DeleteAlertGroupAction.class);
        form("/:branch/alert-rule/:env/:g/add", AddAlertRulePage.class);
        form("/:branch/alert-rule/:env/:g/:id/edit", EditAlertRulePage.class);
        get("/:branch/alert-rule/:env/:g/:id/delete", DeleteAlertRuleAction.class);
        
        get("/otc/deploy-otelcol-contrib", DeployOtelcolcontribAction.class);
        get("/otc/kill", KillAction.class);
        form("/tablesort/:id/:col", TableSortAction.class);
        
        get("/x/push/:tag", PushAction.class);
        post("/x/receive/:tag", ReceiveAction.class);
        get("/x/pull", PullAction.class);
        get("/x/serve/:branch/:key", ServeAction.class);

        // at last
        form("/", IndexPage.class);
        form("/:branch", IndexPage.class);
    }

    public static void main(String[] args) {
        new WebAppBuilder(VERSION)
                .withConfig(new EnvVarAppConfig())
                .withTemplatesFolders(HestiaWebapp.class, "/templates")
                .withErrorPage(HestiaErrorPage.class, HestiaError404Page.class)
                .withPageInitializer(new HestiaPageInitializer())
                .withInitializer(c -> config = new HestiaConfig())
                .withRoutes(new HestiaWebapp())
                .build()
                .boot();
        Logger.info("data folder: " + config.getBaseFolder().getAbsolutePath());
        try {
            if (!config.getOtelcolContrib().isFile()) {
                new OtcService().deployOtelcolContrib();
            }
            if (config.getOtelcolContrib().isFile()) {
                otcProcess = new OtcProcess();
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
