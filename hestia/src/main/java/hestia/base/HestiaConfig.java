package hestia.base;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.git.Repository;
import github.soltaufintel.amalia.git.RepositoryDefinition;
import hestia.environment.EnvironmentDAO;
import hestia.git.GitRepository;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.prometheus.alert.AlertGroupDAO;
import hestia.prometheus.alert.rule.AlertRuleDAO;

public class HestiaConfig {
    static IConfig configAccess = new HestiaAppConfig();
    private final File otelcolContrib;
    private final String prometheusHost;
    private final String alertmanagerHost;
    private final File baseFolder; // DATAFOLDER: persistent data
    private final File customersFolder;
    private final List<String> customers;
    private final File environmentsFolder;
    private final File monitoredTargetsFolder;
    private final File alertsFolder;
    /** Prometheus alert rules file */
    private final File alertRulesFile;
    /** OTel Collector config file */
    private final File configYaml;
    private final File configYamlForValidate;
    private final String language;
    private final boolean customer;
    private final RepositoryDefinition repodefinition;
    private final Repository repo;
    private final String repoAuthor;
    private final String repoMail;
    
    public HestiaConfig() {
        otelcolContrib = new File(get("OTELCOL", "/app/otel/otelcol-contrib"));
        prometheusHost = get("PROMETHEUS", "http://prometheus:9090");
        alertmanagerHost = get("ALERTMANAGER", "http://alertmanager:9093");
        language = get("LANGUAGE", "en");
        customer = !"0".equals(get("CUSTOMER", "1"));
        if (StringService.isNullOrEmpty(get("REPO"))) {
            Logger.info("not REPO mode");
            repoAuthor = null;
            repoMail = null;
            repodefinition = null;
            repo = null;
            baseFolder = new File(get("DATAFOLDER", "/data"));
        } else {
            repoAuthor = get("REPOUSER");
            repoMail = get("REPOMAIL");
            repodefinition = new RepositoryDefinitionImpl(repoAuthor, get("REPOPASSWORD"), get("REPO"),
                    new File(get("REPOFOLDER")));
            repo = new Repository(repodefinition);
            if ("1".equals(get("PULLREPO", "1"))) { // needed for test
                Logger.info("Git repository folder: " + repodefinition.getLocalFolder().getAbsolutePath()
                        + ", " + repodefinition.getLocalFolder().isDirectory());
                repo.pull();
            }
            baseFolder = repodefinition.getLocalFolder();
        }
        customersFolder = baseFolder;
        customers = Arrays.asList(get("CUSTOMERS", "").split(","));
        customers.sort((a, b) -> a.compareToIgnoreCase(b));
        environmentsFolder = new File(baseFolder, "environments");
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

    public File getCustomersFolder() {
        return customersFolder;
    }

    public List<String> getCustomers() {
        return customers;
    }

    public File getEnvironmentsFolder() {
        return environmentsFolder;
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
    
    public RepositoryDefinition getRepoDefinition() {
        return repodefinition;
    }

    public Repository getRepo() {
        return repo;
    }
    
    public void commit(String commitMessage) {
        if (repo != null) {
            repo.commit(commitMessage, repoAuthor, repoMail);
        }
    }

    public void pull() {
        if (repodefinition != null && repo != null) {
            repo.pull();
        }
    }
    
    public void push() {
        if (repodefinition != null && repo != null) {
            repo.push(repodefinition.getUser(), repodefinition.getPassword());
        }
    }
    
    public EnvironmentDAO environmentDAO(IBranch branch) {
        return new EnvironmentDAO(repo(branch));
    }

    public MonitoredTargetDAO mtDAO(IBranch branch) {
        return new MonitoredTargetDAO(repo(branch));
    }

    public AlertGroupDAO alertGroupDAO(IBranch branch) {
        return new AlertGroupDAO(repo(branch));
    }

    public AlertRuleDAO alertRuleDAO(IBranch branch) {
        return new AlertRuleDAO(alertGroupDAO(branch));
    }

    private IRepository repo(IBranch b) {
        var url = get("REPO");
        if (StringService.isNullOrEmpty(url)) {
            var folder = new File(get("DATAFOLDER"));
            return new FileRepository(folder);
        } else {
            var folder = new File(get("REPOFOLDER"));
            return new GitRepository(url, get("REPOUSER"), get("REPOMAIL"), get("REPOPASSWORD"), folder, b.getBranch());
        }
    }
}
