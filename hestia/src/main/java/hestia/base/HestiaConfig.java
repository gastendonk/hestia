package hestia.base;

import java.io.File;

import github.soltaufintel.amalia.base.StringService;
import hestia.otc.OtcOpts;

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
    private final OtcOpts otcOpts;
    
    public HestiaConfig() {
        otelcolContrib = new File(get("OTELCOL", "/app/otel/otelcol-contrib"));
        prometheusHost = get("PROMETHEUS", "http://prometheus:9090");
        alertmanagerHost = get("ALERTMANAGER", "http://alertmanager:9093");
        File base = new File(get("DATAFOLDER", "/data"));
        environmentsFolder = new File(base, "environments");
        monitoredTargetsFolder = new File(base, "monitoredtargets");
        alertsFolder = new File(base, "alerts");
        alertRulesFile = new File(get("ALERTRULESFILE", "/work/rules/alert-rules.yml"));
        configYaml = new File(get("CONFIGYAML", "/work/config.yaml"));
        configYamlForValidate = new File(get("CONFIGYAML_VALIDATE", "/tmp/config.yaml"));
        otcOpts = new OtcOpts();
        otcOpts.prometheusremotewrite = prometheusHost;
        otcOpts.otc = get("TARGET_OTC", null);
        otcOpts.tempo = get("TEMPO", null);
        otcOpts.loki = get("LOKI", null);
        otcOpts.debug = "1".equals(get("DEBUG", "1"));
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

    public OtcOpts getOtcOpts() {
        return otcOpts;
    }
}
