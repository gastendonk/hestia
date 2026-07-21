package hestia.base;

import java.io.File;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.git.Repository;
import github.soltaufintel.amalia.git.RepositoryDefinition;

public class HestiaConfig {
    private final File otelcolContrib;
    private final String prometheusHost;
    private final String alertmanagerHost;
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
        File base; // DATAFOLDER: persistent data
        if (StringService.isNullOrEmpty(get("REPO"))) {
            repoAuthor = null;
            repoMail = null;
            repodefinition = null;
            repo = null;
            base = new File(get("DATAFOLDER", "/data"));
        } else {
            repoAuthor = get("REPOUSER");
            repoMail = get("REPOMAIL");
            repodefinition = new RepositoryDefinitionImpl(repoAuthor, get("REPOPASSWORD"), get("REPO"),
                    new File(get("REPOFOLDER")));
            repo = new Repository(repodefinition);
            Logger.info("Git repository folder: " + repodefinition.getLocalFolder().getAbsolutePath()
                    + ", " + repodefinition.getLocalFolder().isDirectory());   
            repo.pull();
            base = repodefinition.getLocalFolder();
        }
        // /work: working directory, exchange files with other containers
        environmentsFolder = new File(base, "environments");
        monitoredTargetsFolder = new File(base, "monitoredtargets");
        alertsFolder = new File(base, "alerts");
        alertRulesFile = new File(get("ALERTRULESFILE", "/work/rules/alert-rules.yml"));
        configYaml = new File(get("CONFIGYAML", "/work/config.yaml"));
        configYamlForValidate = new File(get("CONFIGYAML_VALIDATE", "/work/validate-config.yaml"));
    }
    
    private static String get(String key, String defaultValue) {
        return HestiaAppConfig.getenv(key, defaultValue);
    }

    private static String get(String key) {
        return HestiaAppConfig.getenv(key, null);
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
}
