package hestia.otc;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class OtcConfigBuilderTest {

    @Test
    public void build() {
        OtcOpts o = new OtcOpts();
        o.setPrometheusremotewrite("http://prometheus:9090");
        o.setTempo("tempo:4317");
        o.setLoki("http://loki:3100/otlp");

        String out = new ConfigYamlBuilder(mt(), o).build();

        String expectation = """
                receivers:
                  otlp:
                      protocols:
                        grpc: { endpoint: "0.0.0.0:4317" }
                        http: { endpoint: "0.0.0.0:4318" }
                  prometheus:
                      config:
                        global:
                          scrape_interval: 60s
                        scrape_configs:
                          - job_name: db03
                            static_configs: [{targets: ["db03:9100"]}]
                  oracledb/DLHTEST:
                    endpoint: "db03:1521"
                    service: "DLHTEST"
                    username: "metrics"
                    password: "secret"
                    collection_interval: 60s
                    metrics: &oracle_metrics
                      oracledb.logons: { enabled: true }
                      oracledb.physical_read_io_requests: { enabled: true }
                      oracledb.physical_write_io_requests: { enabled: true }
                      oracledb.physical_writes: { enabled: true }
                      oracledb.consistent_gets: { enabled: true }
                      oracledb.db_block_gets: { enabled: true }
                  httpcheck:
                    collection_interval: 5m
                    targets:
                      - endpoint: "http://server"

                processors:
                  batch:
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

                exporters:
                  prometheusremotewrite:
                    endpoint: "http://prometheus:9090"
                  otlp/tempo:
                    endpoint: "tempo:4317"
                    tls: { insecure: true }
                  otlphttp/loki:
                    endpoint: "http://loki:3100/otlp"
                  debug:
                    verbosity: detailed

                extensions:
                  health_check:

                service:
                  telemetry:
                    metrics:
                      readers:
                        - pull:
                            exporter:
                              prometheus:
                                host: "0.0.0.0"
                                port: 8888
                  extensions: [health_check]
                  pipelines:
                    metrics:
                      receivers:
                        - otlp
                        - prometheus
                        - oracledb/DLHTEST
                        - httpcheck
                      processors: [attributes, transform/make_labels, batch]
                      exporters:  [prometheusremotewrite, debug]
                    traces:
                      receivers:  [otlp]
                      processors: [batch]
                      exporters:  [otlp/tempo, debug]
                    logs:
                      receivers:  [otlp]
                      processors: [batch]
                      exporters:  [otlphttp/loki]
                                """;
        Assert.assertEquals(expectation, out);
    }

    private List<MonitoredTarget> mt() {
        List<MonitoredTarget> mt = new ArrayList<>();

        Server s = new Server();
        s.setHost("db03:9100");
        s.setName("db03");
        mt.add(s);

        Database d = new Database();
        d.setHost("db03:1521");
        d.setName("DLHTEST");
        d.setUser("metrics");
        d.setPassword("secret");
        d.setType(DatabaseType.ORACLE);
        mt.add(d);

        Site h = new Site();
        h.setName("server");
        h.setUrl("http://server");
        mt.add(h);
        return mt;
    }

    @Test
    public void otc() {
        OtcOpts o = new OtcOpts();
        o.setOtc("http://cloud");

        String out = new ConfigYamlBuilder(List.of(), o).build();

        String expectation = """
                receivers:
                  otlp:
                      protocols:
                        grpc: { endpoint: "0.0.0.0:4317" }
                        http: { endpoint: "0.0.0.0:4318" }
                
                processors:
                  batch:
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

                exporters:
                  otlphttp/otc:
                    endpoint: "http://cloud"
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
                  debug:
                    verbosity: detailed

                extensions:
                  health_check:

                service:
                  telemetry:
                    metrics:
                      readers:
                        - pull:
                            exporter:
                              prometheus:
                                host: "0.0.0.0"
                                port: 8888
                  extensions: [health_check]
                  pipelines:
                    metrics:
                      receivers:
                        - otlp
                      processors: [attributes, transform/make_labels, batch]
                      exporters:  [debug, otlphttp/otc]
                                """;
        Assert.assertEquals(expectation, out);
    }
}
