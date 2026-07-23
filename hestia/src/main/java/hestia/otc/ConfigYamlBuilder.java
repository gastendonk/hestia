package hestia.otc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import github.soltaufintel.amalia.base.StringService;
import hestia.otc.model.Database;
import hestia.otc.model.DatabaseType;
import hestia.otc.model.MonitoredTarget;
import hestia.otc.model.Server;
import hestia.otc.model.Site;
import hestia.otc.opts.OtcOpts;

/**
 * Build content of OTel Collector config.yaml file
 */
public class ConfigYamlBuilder {
    private final List<String> receivers = new ArrayList<>();
    private final List<String> exporters = new ArrayList<>();
    private final List<MonitoredTarget> monitoredTargets;
    private final OtcOpts o;
    private String tracesExporters = "";
    
    public ConfigYamlBuilder(List<MonitoredTarget> monitoredTargets, OtcOpts o) {
        this.monitoredTargets = new ArrayList<>(monitoredTargets);
        this.o = o;
        this.monitoredTargets.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        receivers.add("otlp");
    }

    public String build() {
        return "receivers:\n" //
                + "  otlp:\n" //
                + "      protocols:\n" //
                + "        grpc: { endpoint: \"0.0.0.0:4317\" }\n" //
                + "        http: { endpoint: \"0.0.0.0:4318\" }\n" //
                + server() //
                + oracle() //
                + postgres() //
                + sites() //
                + processors() //
                + exporters() //
                + extensions() //
                + service();
    }

    private String server() {
        String ret = "";
        for (MonitoredTarget mt : monitoredTargets) {
            if (mt instanceof Server s) {
                ret += "          - job_name: " + mt.getName() + "\n" + //
                        "            static_configs: [{targets: [\"" + s.getHost() + "\"]}]\n";
                if (!StringService.isNullOrEmpty(s.getPath())) {
                    ret += "            metrics_path: \"" + s.getPath() + "\"\n";
                }
            }
        }
        if (!ret.isEmpty()) {
            ret = "  prometheus:\n" //
                    + "      config:\n" //
                    + "        global:\n" //
                    + "          scrape_interval: 60s\n" //
                    + "        scrape_configs:\n" //
                    + ret;
            receivers.add("prometheus");
        }
        return ret;
    }

    private String oracle() {
        String ret = "";
        boolean first = true;
        for (MonitoredTarget mt : monitoredTargets) {
            if (mt instanceof Database db && db.getType() == DatabaseType.ORACLE) {
                ret += "  oracledb/" + db.getName() + ":\n" + //
                        "    endpoint: \"" + db.getHost() + "\"\n" + //
                        "    service: \"" + db.getName() + "\"\n" + //
                        "    username: \"" + db.getUser() + "\"\n" + //
                        "    password: \"" + db.getPassword() + "\"\n" + //
                        "    collection_interval: 60s\n";
                if (first) {
                    ret += "    metrics: &oracle_metrics\n" //
                            + "      oracledb.logons: { enabled: true }\n"
                            + "      oracledb.physical_read_io_requests: { enabled: true }\n"
                            + "      oracledb.physical_write_io_requests: { enabled: true }\n"
                            + "      oracledb.physical_writes: { enabled: true }\n"
                            + "      oracledb.consistent_gets: { enabled: true }\n"
                            + "      oracledb.db_block_gets: { enabled: true }\n";
                    first = false;
                } else {
                    ret += "    metrics: *oracle_metrics\n";
                }
                receivers.add("oracledb/" + db.getName());
            }
        }
        return ret;
    }

    private String postgres() {
        String ret = "";
        boolean first = true;
        for (MonitoredTarget mt : monitoredTargets) {
            if (mt instanceof Database db && db.getType() == DatabaseType.POSTGRES) {
                ret += "  postgresql/" + db.getName() + ":\n" //
                        + "    databases: [" + db.getName() + "]\n" //
                        + "    endpoint: " + db.getHost() + "\n" //
                        + "    username: " + db.getUser() + "\n" //
                        + "    password: " + db.getPassword() + "\n" //
                        + "    tls: { insecure: true }\n";
                if (first) {
                    first = false;
                    ret += "    resource_attributes: &postgres_attrs\n" //
                            + "      postgresql.database.name: { enabled: true }\n\n";
                } else {
                    ret += "    resource_attributes: *postgres_attrs\n\n";
                }
                receivers.add("postgresql/" + db.getName());
            }
        }
        return ret;
    }

    private String sites() {
        String ret = "";
        for (MonitoredTarget mt : monitoredTargets) {
            if (mt instanceof Site s) {
                ret += "      - endpoint: \"" + s.getUrl() + "\"\n";
            }
        }
        if (!ret.isEmpty()) {
            ret = "  httpcheck:\n" //
                    + "    collection_interval: 5m\n" //
                    + "    targets:\n" //
                    + ret;
            receivers.add("httpcheck");
        }
        return ret;
    }

    private String processors() {
        return """
                
                processors:
                  batch: {}
                  attributes:
                    actions:
                      - key: process.command_line
                        action: delete
                      - key: process.pid
                        action: delete
                      - key: process.executable.path
                        action: delete
                      - key: host.name
                        action: delete
                  transform/make_labels:
                    metric_statements:
                      - context: datapoint
                        statements:
                          - set(attributes["database"], resource.attributes["postgresql.database.name"])
                          - set(attributes["deployment.environment"], resource.attributes["deployment.environment"])

                                """;
    }

    private String exporters() {
        String ret = "exporters:\n";
        if (!StringService.isNullOrEmpty(o.getPrometheusremotewrite())) { // write metrics to Prometheus
            ret += "  prometheusremotewrite:\n    endpoint: \"" + o.getPrometheusremotewrite() + "\"\n";
            exporters.add("prometheusremotewrite");
        }
        if (o.isDebug()) {
            exporters.add("debug");
        }
        if (exporters.isEmpty()) {
            throw new RuntimeException("exporters must not be empty");
        }
        if (!StringService.isNullOrEmpty(o.getTempo())) { // write traces to Tempo
            ret += "  otlp/tempo:\n    endpoint: \"" + o.getTempo() + "\"\n    tls: { insecure: true }\n";
            tracesExporters = "otlp/tempo, ";
        }
        if (!StringService.isNullOrEmpty(o.getLoki())) { // write logs to Loki
            ret += "  otlphttp/loki:\n" + "    endpoint: \"" + o.getLoki() + "\"\n";
        }
        if (!StringService.isNullOrEmpty(o.getOtc())) {
            ret += """
                    $indent
                      otlphttp/otc:
                        endpoint: "$e"
                        compression: gzip
                        sending_queue:
                          enabled: true
                          num_consumers: 4
                          queue_size: 8192
                        retry_on_failure:
                          enabled: true
                          initial_interval: 1s
                          max_interval: 30s
                          max_elapsed_time: 5m
                                        """ //
                    .replace("$indent\n", "") // trick
                    .replace("$e", o.getOtc());
            exporters.add("otlphttp/otc");
        }
        if (o.isDebug()) {
            ret += "  debug:\n    verbosity: detailed\n";
        }
        return ret + "\n";
    }

    private String extensions() {
        return """
                extensions:
                  health_check:

                                """;
    }

    private String service() {
        /* Das darf nur rein, wenn es prometheus dingens gibt.
        String prometheus = """
                  telemetry:
                    metrics:
                      readers:
                        - pull:
                            exporter:
                              prometheus:
                                host: "0.0.0.0"
                                port: 8888
                """;*/
        String ret = """
                service:
                  extensions: [health_check]
                  pipelines:
                    metrics:
                      receivers:{{m_receivers}}
                      processors: [attributes, transform/make_labels, batch]
                      exporters:  [{{m_exporters}}]
                                """ //
                .replace("{{m_receivers}}",
                        receivers.stream().map(i -> "\n        - " + i).collect(Collectors.joining())) //
                .replace("{{m_exporters}}", exporters.stream().collect(Collectors.joining(", ")));
        if (!StringService.isNullOrEmpty(o.getTempo())) {
            ret += "    traces:\n      receivers:  [otlp]\n      processors: [batch]\n      exporters:  ["
                    + tracesExporters + "debug]\n";
        }
        if (!StringService.isNullOrEmpty(o.getLoki())) {
            ret += "    logs:\n      receivers:  [otlp]\n      processors: [batch]\n      exporters:  [otlphttp/loki]\n";
        }
        return ret;
    }
}
