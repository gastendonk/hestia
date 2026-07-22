package hestia.base;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import github.soltaufintel.amalia.base.StringService;
import hestia.environment.EnvironmentDAO;
import hestia.git.GitRepository;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.prometheus.alert.AlertGroupDAO;
import hestia.prometheus.alert.rule.AlertRuleDAO;

public class HestiaConfig {
    public static IConfig configAccess = new EnvVarAppConfig();
    private final File otelcolContrib;
    private final String prometheusHost;
    private final String alertmanagerHost;
    private final File baseFolder; // DATAFOLDER: persistent data
    private final List<String> customers;
    private final File monitoredTargetsFolder;
    private final File alertsFolder;
    /** Prometheus alert rules file */
    private final File alertRulesFile;
    /** OTel Collector config file */
    private final File configYaml;
    private final File configYamlForValidate;
    private final String language;
    private final boolean customer;
    
    public HestiaConfig() {
        otelcolContrib = new File(get("OTELCOL", "/app/otel/otelcol-contrib"));
        prometheusHost = get("PROMETHEUS", "http://prometheus:9090");
        alertmanagerHost = get("ALERTMANAGER", "http://alertmanager:9093");
        language = get("LANGUAGE", "en");
        customer = !"0".equals(get("CUSTOMER", "1"));
        customers = Arrays.asList(get("CUSTOMERS", "").split(","));
        customers.sort((a, b) -> a.compareToIgnoreCase(b));
        baseFolder = new File(get("DATAFOLDER", "data"));
        monitoredTargetsFolder = new File(baseFolder, "monitoredtargets");
        alertsFolder = new File(baseFolder, "alerts");
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

    public File getOtelcolContrib() {
        return otelcolContrib;
    }

    public String getPrometheusHost() {
        return prometheusHost;
    }

    public String getAlertmanagerHost() {
        return alertmanagerHost;
    }

    public File getBaseFolder() {
        return baseFolder;
    }

    public List<String> getCustomers() {
        return customers;
    }

    public File getMonitoredTargetsFolder() {
        return monitoredTargetsFolder;
    }

    public File getAlertsFolder() {
        return alertsFolder;
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

    private IRepository getRepository(IBranch branch) {
        var url = get("REPO");
        if (StringService.isNullOrEmpty(url)) {
            var folder = new File(get("DATAFOLDER"));
            return new FileRepository(folder);
        } else {
            var folder = new File(get("REPOFOLDER"));
            return new GitRepository(url, get("REPOUSER"), get("REPOMAIL"), get("REPOPASSWORD"), folder, branch.getBranch());
        }
    }
}
