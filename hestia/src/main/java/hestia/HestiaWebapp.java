package hestia;

import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.timer.Timer;
import github.soltaufintel.amalia.web.action.Escaper;
import github.soltaufintel.amalia.web.builder.LoggingInitializer;
import github.soltaufintel.amalia.web.builder.WebAppBuilder;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import github.soltaufintel.amalia.web.table.TableSortAction;
import hestia.base.EnvVarAppConfig;
import hestia.base.IBranch;
import hestia.base.IConfig;
import hestia.config.HestiaConfig;
import hestia.environment.AddEnvironmentPage;
import hestia.environment.DeleteEnvironmentAction;
import hestia.environment.EditEnvironmentPage;
import hestia.environment.EnvironmentsPage;
import hestia.exchange.DeliverPage;
import hestia.exchange.PullAction;
import hestia.exchange.ReceiveAction;
import hestia.exchange.ServeAction;
import hestia.git.GitPullAction;
import hestia.git.GitPushAction;
import hestia.git.GitTagAction;
import hestia.grafana.AddGrafanaPage;
import hestia.grafana.BackupDashboardAction;
import hestia.grafana.DeleteGrafanaAction;
import hestia.grafana.EditGrafanaPage;
import hestia.grafana.GrafanaDashboardJsonPage;
import hestia.grafana.GrafanaDashboardsPage;
import hestia.grafana.GrafanasPage;
import hestia.grafana.TransferDashboardPage;
import hestia.otc.InstallOtelcolContribAction;
import hestia.otc.OtcLogPage;
import hestia.otc.OtcProcess;
import hestia.otc.OtcService;
import hestia.otc.OtcStatusPage;
import hestia.otc.mt.AddMTPage;
import hestia.otc.mt.DeleteMTAction;
import hestia.otc.mt.DuplicateMTAction;
import hestia.otc.mt.EditMTPage;
import hestia.otc.mt.MonitoredTargetsPage;
import hestia.otc.opts.EditOtcOptsPage;
import hestia.otc.secret.AddSecretPage;
import hestia.otc.secret.DeleteSecretAction;
import hestia.otc.secret.SecretsPage;
import hestia.prometheus.alert.AddAlertGroupPage;
import hestia.prometheus.alert.AlertsPage;
import hestia.prometheus.alert.ApplyTemplatesAction;
import hestia.prometheus.alert.DeleteAlertGroupAction;
import hestia.prometheus.alert.EditAlertGroupPage;
import hestia.prometheus.alert.ShowAlertRulesFilePage;
import hestia.prometheus.alert.rule.AddAlertRulePage;
import hestia.prometheus.alert.rule.DeleteAlertRuleAction;
import hestia.prometheus.alert.rule.DuplicateAlertRuleAction;
import hestia.prometheus.alert.rule.EditAlertRulePage;
import hestia.prometheus.queryalerts.DoSilencePage;
import hestia.prometheus.queryalerts.QueryalertsAndSilencesPage;
import hestia.prometheus.silences.DeleteSilenceAction;
import hestia.special.jenkins.JenkinsTelemetry;
import hestia.special.jenkins.JenkinsTelemetry.JenkinsTelemetryTimer;
import hestia.web.DeployAction;
import hestia.web.IndexPage;
import hestia.web.KillAction;
import hestia.web.base.HestiaError404Page;
import hestia.web.base.HestiaErrorPage;
import hestia.web.base.HestiaPageInitializer;
import spark.Spark;

public class HestiaWebapp extends RouteDefinitions {
    public static final String VERSION = "0.6.0";
    public static HestiaConfig config;
    public static OtcProcess otcProcess;
    
    // TODO Grafana Dashboards sichern / transferieren
    
    @Override
    public void routes() {
        post("/x/receive/:customerkey/:tag", ReceiveAction.class);
        if (config.isCloud()) {
            Spark.get("/", (req, res) -> Escaper.esc(HestiaWebapp.config.getTitle() + " " + VERSION + " in cloud mode"));
            // TODO Auskunft (JSON?) welche Daten verwaltet werden
            get("/x/pull/:customerkey", ServeAction.class);
            Logger.info("cloud mode");
            return;
        }
        
        environments();
        monitoredTargets();
        alerts();
        grafana();

        get("/:branch/deploy", DeployAction.class);

        if (config.isCustomer()) {
            get("/x/pull", PullAction.class);
        } else { // only for manufacturer
            get("/:branch/push", GitPushAction.class);
            get("/:branch/pull", GitPullAction.class);
            get("/:branch/tag/:tag", GitTagAction.class);
            
            form("/x/push/:branch", DeliverPage.class);
        }

        get("/otc/:branch/kill", KillAction.class); // kill or restart OTC
        get("/otc/:branch/status", OtcStatusPage.class);
        get("/otc/:branch/install-otelcol-contrib", InstallOtelcolContribAction.class);
        get("/otc/log", OtcLogPage.class);
        form("/otc/options", EditOtcOptsPage.class);
        
        get("/qas", QueryalertsAndSilencesPage.class);
        form("/qas/silence/:alertname", DoSilencePage.class);
        get("/qas/expire-silence/:id", DeleteSilenceAction.class);

        form("/tablesort/:id/:col", TableSortAction.class);
        Spark.get("/config/customerkey", (req, res) -> HestiaWebapp.config.getCustomerKey());

        // at last
        form("/", IndexPage.class);
        form("/:branch", IndexPage.class);
        Logger.info("web mode");
    }

    private void environments() {
        form("/:branch/environment/add", AddEnvironmentPage.class);
        get("/:branch/environment/:id/delete", DeleteEnvironmentAction.class);
        form("/:branch/environment/:id", EditEnvironmentPage.class);
        get("/:branch/environment", EnvironmentsPage.class);
    }

    private void monitoredTargets() {
        form("/:branch/mt/:id/add", AddMTPage.class);
        form("/:branch/mt/:id/:id2/edit", EditMTPage.class);
        get("/:branch/mt/:id/:id2/delete", DeleteMTAction.class);
        get("/:branch/mt/:id/:id2/duplicate", DuplicateMTAction.class);
        get("/:branch/mt/:id", MonitoredTargetsPage.class);

        form("/:branch/secret/:id/add", AddSecretPage.class);
        get("/:branch/secret/:id/:id2/delete", DeleteSecretAction.class);
        get("/:branch/secret/:id", SecretsPage.class);
    }

    private void alerts() {
        get("/:branch/alert/:env/apply-templates", ApplyTemplatesAction.class);
        get("/:branch/alert/:env", AlertsPage.class); // Alle Gruppen und Rules zu einer Umgebung
        form("/:branch/alert-group/:env/add", AddAlertGroupPage.class);
        form("/:branch/alert-group/:env/:id/edit", EditAlertGroupPage.class);
        get("/:branch/alert-group/:env/:id/delete", DeleteAlertGroupAction.class);
        form("/:branch/alert-rule/:env/:g/add", AddAlertRulePage.class);
        form("/:branch/alert-rule/:env/:g/:id/edit", EditAlertRulePage.class);
        get("/:branch/alert-rule/:env/:g/:id/duplicate", DuplicateAlertRuleAction.class);
        get("/:branch/alert-rule/:env/:g/:id/delete", DeleteAlertRuleAction.class);
        get("/alert-rules-file", ShowAlertRulesFilePage.class);
    }
    
    private void grafana() {
        get("/:branch/grafana", GrafanasPage.class);
        get("/:branch/grafana-dashboards/:id", GrafanaDashboardsPage.class);
        form("/:branch/grafana/add", AddGrafanaPage.class);
        form("/:branch/grafana/:id/edit", EditGrafanaPage.class);
        get("/:branch/grafana/:id/delete", DeleteGrafanaAction.class);
        get("/:branch/grafana/:g/dashboard/:d/backup", BackupDashboardAction.class);
        form("/:branch/grafana/:g/dashboard/:d/transfer", TransferDashboardPage.class);
        get("/:branch/grafana/:g/dashboard/:d/json", GrafanaDashboardJsonPage.class);
    }

    public static void main(String[] args) {
        new WebAppBuilder(VERSION)
                .withConfig(new EnvVarAppConfig())
                .withLogging(new LoggingInitializer(Level.INFO, "{date} {level}  {message}"))
                .withTemplatesFolders(HestiaWebapp.class, "/templates")
                .withErrorPage(HestiaErrorPage.class, HestiaError404Page.class)
                .withPageInitializer(new HestiaPageInitializer())
                .withInitializer(c -> config = new HestiaConfig((IConfig) c))
                .withInitializer(c -> Timer.create(c))
                .withRoutes(new HestiaWebapp())
                .build()
                .boot();
        Logger.info("data folder: " + config.getBaseFolder().getAbsolutePath());
        if (config.isDeployOnReceive()) {
            deployOnReceipt();
        } else if (!config.isCloud()) {
            startOTC();
        }
        if (config.getSpecial().contains(",JENKINS,")) {
            JenkinsTelemetry.init("hestia", "http://localhost:4317");
            JenkinsTelemetryTimer.jenkinsUrl = "https://jenkins.intern.x-map.de";
            Timer.INSTANCE.createTimer(JenkinsTelemetryTimer.class, "0 * * * * ?"); // alle 60 Sekunden
            Logger.info("Jenkins metrics activated");
        }
    }

    private static void deployOnReceipt() {
        try {
            Logger.info("trying to deploy on startup...");
            if (!config.getOtelcolContrib().isFile()) {
                new OtcService().installOtelcolContrib(); // auto-install
            }
            IBranch b = () -> "master";
            DeployAction.deploy(b, config.environmentDAO(b));
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private static void startOTC() {
        try {
            if (!config.getOtelcolContrib().isFile()) {
                new OtcService().installOtelcolContrib(); // auto-install
            }
            if (config.getOtelcolContrib().isFile()) {
                otcProcess = new OtcProcess(() -> "master");
            } else {
                Logger.warn("File \"" + config.getOtelcolContrib().getAbsolutePath()
                        + "\" does not exist. Do not start otcProcess at startup.");
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
