package hestia.base;

import java.io.File;

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
    private final Repository repo;
    private final String repoAuthor;
    private final String repoMail;
    
    public HestiaConfig() {
        otelcolContrib = new File(get("OTELCOL", "/app/otel/otelcol-contrib"));
        prometheusHost = get("PROMETHEUS", "http://prometheus:9090");
        alertmanagerHost = get("ALERTMANAGER", "http://alertmanager:9093");
        // DATAFOLDER: persistent data
        // /work: working directory, exchange files with other containers
        File base = new File(get("DATAFOLDER", "/data"));
        environmentsFolder = new File(base, "environments");
        monitoredTargetsFolder = new File(base, "monitoredtargets");
        alertsFolder = new File(base, "alerts");
        alertRulesFile = new File(get("ALERTRULESFILE", "/work/rules/alert-rules.yml"));
        configYaml = new File(get("CONFIGYAML", "/work/config.yaml"));
        configYamlForValidate = new File(get("CONFIGYAML_VALIDATE", "/work/validate-config.yaml"));
        language = get("LANGUAGE", "en");
        customer = !"0".equals(get("CUSTOMER", "1"));
        if (StringService.isNullOrEmpty(get("REPO", null))) {
            repoAuthor = null;
            repoMail = null;
            repo = null;
        } else {
            repoAuthor = get("REPOUSER", null);
            repoMail = get("REPOMAIL", null);
            repo = new Repository(new RepositoryDefinition() {
                @Override
                public String getUser() {
                    return repoAuthor;
                }
                
                @Override
                public String getPassword() {
                    return get("REPOPASS", null);
                }
                
                @Override
                public String getUrl() {
                    return get("REPO", null);
                }
                
                @Override
                public File getLocalFolder() {
                    return new File(get("REPOFOLDER", null));
                }
            });
        }
    }

    public static String get(String key, String defaultValue) {
        String value = System.getenv(key);
        return StringService.isNullOrEmpty(value) ? defaultValue : value;
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
    
    public Repository getRepo() {
        return repo;
    }
    
    public void commit(String commitMessage) {
        if (repo != null) {
            repo.commit(commitMessage, repoAuthor, repoMail);
        }
    }
}
