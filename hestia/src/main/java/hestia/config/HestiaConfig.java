package hestia.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.StringService;
import hestia.base.EnvVarAppConfig;
import hestia.base.IBranch;
import hestia.base.IConfig;
import hestia.environment.EnvironmentDAO;
import hestia.git.GitRepository;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.persist.FileRepository;
import hestia.persist.IRepository;
import hestia.prometheus.alert.AlertGroupDAO;
import hestia.prometheus.alert.rule.AlertRuleDAO;

public class HestiaConfig {
    /** built-in default version */
    public static final String OTELCOLVERSION = "0.137.0";
    public static IConfig configAccess = new EnvVarAppConfig();
    /** "1": instance is in cloud mode, value: address of cloud instance */
    private final String cloud;
    private final String otelcolContribDownloadUrl;
    private final File otelcolContrib;
    /** true: otelcol-contrib should be started; false: otelcol-contrib should only be started manually. */
    private final boolean run;
    private final String prometheusHost;
    private final String alertmanagerHost;
    private final List<String> customers;
    /** Prometheus alert rules file */
    private final File alertRulesFile;
    /** OTel Collector config file */
    private final File configYaml;
    private final File configYamlForValidate;
    private final String language;
    /** true: customer mode, false: manufacturer or cloud mode */
    private final boolean customer;
    
    public HestiaConfig() {
        cloud = get("CLOUD");
        otelcolContribDownloadUrl = readOtelcolContribDownloadUrl();
        otelcolContrib = new File(get("OTELCOL", "/work/otelcol-contrib-" + get("OTELCOLVERSION", OTELCOLVERSION)));
        run = "1".equals(get("RUN", "1"));
        prometheusHost = get("PROMETHEUS");     // http://prometheus:9090
        alertmanagerHost = get("ALERTMANAGER"); // http://alertmanager:9093
        language = get("LANGUAGE", "en");
        customer = !"0".equals(get("CUSTOMER", "1"));
        customers = Arrays.asList(get("CUSTOMERS", "").split(","));
        customers.sort((a, b) -> a.compareToIgnoreCase(b));
        
        // /work: working directory, exchange files with other containers
        alertRulesFile = new File(get("ALERTRULESFILE", "/work/rules/alert-rules.yml"));
        configYaml = new File(get("CONFIGYAML", "/work/config.yaml"));
        configYamlForValidate = new File(get("CONFIGYAML_VALIDATE", "/work/validate-config.yaml"));
    }
    
    private static String get(String key, String defaultValue) {
        return configAccess.get(key, defaultValue);
    }

    private static String get(String key) {
        return configAccess.get(key);
    }

    private static String readOtelcolContribDownloadUrl() {
        String url = get("OTELCOLURL", "https://github.com/open-telemetry/opentelemetry-collector-releases/releases/download/v{version}/otelcol-contrib_{version}_linux_amd64.tar.gz");
        if (StringService.isNullOrEmpty(url)) {
            throw new IllegalStateException("Please set env var OTELCOLURL.");
        }
        String version = get("OTELCOLVERSION", OTELCOLVERSION);
        if (StringService.isNullOrEmpty(version)) {
            throw new IllegalStateException("Please set env var OTELCOLVERSION.");
        } 
        Logger.info("otelcol-contrib version: " + version);
        return url.replace("{version}", version);
    }

    public String getOtelcolContribDownloadUrl() {
        return otelcolContribDownloadUrl;
    }

    public File getOtelcolContrib() {
        return otelcolContrib;
    }

    public boolean isCloud() {
        return "1".equals(cloud);
    }
    
    /**
     * @return null or address of cloud instance
     */
    public String getCloudInstance() {
        return StringService.isNullOrEmpty(cloud) || "1".equals(cloud) ? null : cloud;
    }

    public boolean isRun() {
        return run;
    }

    public String getPrometheusHost() {
        return prometheusHost;
    }

    public String getAlertmanagerHost() {
        return alertmanagerHost;
    }

    public List<String> getCustomers() {
        return customers;
    }

    public File getAlertRulesFile() {
        return alertRulesFile;
    }

    public File getConfigYaml() {
        return configYaml;
    }

    public File getConfigYamlForValidate() {
        return configYamlForValidate;
    }

    public String getLanguage() {
        return language;
    }
    
    public boolean isCustomer() {
        return customer;
    }
    
    public EnvironmentDAO environmentDAO(IBranch branch) {
        return new EnvironmentDAO(getRepository(branch));
    }

    public MonitoredTargetDAO mtDAO(IBranch branch) {
        return new MonitoredTargetDAO(getRepository(branch));
    }

    public AlertGroupDAO alertGroupDAO(IBranch branch) {
        return new AlertGroupDAO(getRepository(branch));
    }

    public AlertRuleDAO alertRuleDAO(IBranch branch) {
        return new AlertRuleDAO(alertGroupDAO(branch));
    }

    // TODO Ich sollte besser die Objekte vorhalten und nicht immer neu erstellen.
    public IRepository getRepository(IBranch branch) {
        var url = get("REPO");
        if (StringService.isNullOrEmpty(url)) {
            return new FileRepository(getBaseFolder());
        } else {
            var user = get("REPOUSER");
            check(user);
            return new GitRepository(url, user, get("REPOMAIL"), get("REPOPASSWORD"), getBaseFolder(), branch.getBranch());
        }
    }

    public File getBaseFolder() {
        var url = get("REPO");
        String folder;
        if (StringService.isNullOrEmpty(url)) {
            folder = get("DATAFOLDER");
            if (StringService.isNullOrEmpty(folder)) {
                throw new IllegalStateException("Please set env var DATAFOLDER (or REPO).");
            }
        } else {
            folder = get("REPOFOLDER");
            check(folder);
        }
        return new File(folder);
    }
    
    private void check(String c) {
        if (StringService.isNullOrEmpty(c)) {
            throw new IllegalStateException("Please set env vars REPOFOLDER, REPOUSER, REPOMAIL and REPOPASSWORD.");
        }
    }
}
