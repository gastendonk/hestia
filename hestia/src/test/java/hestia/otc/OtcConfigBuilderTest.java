package hestia.otc;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import hestia.otc.model.Database;
import hestia.otc.model.DatabaseType;
import hestia.otc.model.MonitoredTarget;
import hestia.otc.model.Server;
import hestia.otc.model.Site;
import hestia.otc.opts.OtcOpts;

public class OtcConfigBuilderTest {

    @Test
    public void build() {
        OtcOpts o = new OtcOpts();
        o.setCustomer("COMPANY");
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
                    collection_interval: 30s
                    targets:
                      - endpoint: "http://server"

                processors:
                  batch: {}
                  attributes:
                    actions:
                      - key: customer
                        value: "COMPANY"
                        action: insert
                      - key: process.command_line
                        action: delete
                      - key: process.pid
                        action: delete
                      - key: process.executable.path
                        action: delete
                      - key: host.name
                        action: delete
                  transform/remove_scope_labels:
                    metric_statements:
                      - context: datapoint
                        statements:
                          - delete_key(attributes, "otel_scope_name")
                          - delete_key(attributes, "otel_scope_version")
                  transform/make_labels:
                    metric_statements:
                      - context: datapoint
                        statements:
                          - set(attributes["database"], resource.attributes["postgresql.database.name"]) where resource.attributes["postgresql.database.name"] != nil
                          - set(attributes["server"], resource.attributes["server.address"]) where resource.attributes["server.address"] != nil
                          - set(attributes["port"], resource.attributes["server.port"]) where resource.attributes["server.port"] != nil
                          - set(attributes["instance"], resource.attributes["service.instance.id"]) where resource.attributes["service.instance.id"] != nil
                          - set(attributes["deployment.environment"], resource.attributes["deployment.environment"]) where resource.attributes["deployment.environment"] != nil

                exporters:
                  prometheus_remote_write:
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
                  extensions: [health_check]
                  pipelines:
                    metrics:
                      receivers:
                        - otlp
                        - prometheus
                        - oracledb/DLHTEST
                        - httpcheck
                      processors: [attributes, transform/remove_scope_labels, transform/make_labels, batch]
                      exporters:  [prometheus_remote_write, debug]
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
                  batch: {}
                  attributes:
                    actions:
                      - key: customer
                        value: "unspecified"
                        action: insert
                      - key: process.command_line
                        action: delete
                      - key: process.pid
                        action: delete
                      - key: process.executable.path
                        action: delete
                      - key: host.name
                        action: delete
                  transform/remove_scope_labels:
                    metric_statements:
                      - context: datapoint
                        statements:
                          - delete_key(attributes, "otel_scope_name")
                          - delete_key(attributes, "otel_scope_version")
                  transform/make_labels:
                    metric_statements:
                      - context: datapoint
                        statements:
                          - set(attributes["database"], resource.attributes["postgresql.database.name"]) where resource.attributes["postgresql.database.name"] != nil
                          - set(attributes["server"], resource.attributes["server.address"]) where resource.attributes["server.address"] != nil
                          - set(attributes["port"], resource.attributes["server.port"]) where resource.attributes["server.port"] != nil
                          - set(attributes["instance"], resource.attributes["service.instance.id"]) where resource.attributes["service.instance.id"] != nil
                          - set(attributes["deployment.environment"], resource.attributes["deployment.environment"]) where resource.attributes["deployment.environment"] != nil

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
                  extensions: [health_check]
                  pipelines:
                    metrics:
                      receivers:
                        - otlp
                      processors: [attributes, transform/remove_scope_labels, transform/make_labels, batch]
                      exporters:  [debug, otlphttp/otc]
                                """;
        Assert.assertEquals(expectation, out);
    }
    
    @Test
    public void debugOff() {
        OtcOpts o = new OtcOpts();
        o.setOtc("http://cloud");
        o.setDebug(false);
        o.setTempo("tempo:4317");

        String out = new ConfigYamlBuilder(List.of(), o).build();

        String expectation = """
                receivers:
                  otlp:
                      protocols:
                        grpc: { endpoint: "0.0.0.0:4317" }
                        http: { endpoint: "0.0.0.0:4318" }
                
                processors:
                  batch: {}
                  attributes:
                    actions:
                      - key: customer
                        value: "unspecified"
                        action: insert
                      - key: process.command_line
                        action: delete
                      - key: process.pid
                        action: delete
                      - key: process.executable.path
                        action: delete
                      - key: host.name
                        action: delete
                  transform/remove_scope_labels:
                    metric_statements:
                      - context: datapoint
                        statements:
                          - delete_key(attributes, "otel_scope_name")
                          - delete_key(attributes, "otel_scope_version")
                  transform/make_labels:
                    metric_statements:
                      - context: datapoint
                        statements:
                          - set(attributes["database"], resource.attributes["postgresql.database.name"]) where resource.attributes["postgresql.database.name"] != nil
                          - set(attributes["server"], resource.attributes["server.address"]) where resource.attributes["server.address"] != nil
                          - set(attributes["port"], resource.attributes["server.port"]) where resource.attributes["server.port"] != nil
                          - set(attributes["instance"], resource.attributes["service.instance.id"]) where resource.attributes["service.instance.id"] != nil
                          - set(attributes["deployment.environment"], resource.attributes["deployment.environment"]) where resource.attributes["deployment.environment"] != nil

                exporters:
                  otlp/tempo:
                    endpoint: "tempo:4317"
                    tls: { insecure: true }
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

                extensions:
                  health_check:

                service:
                  extensions: [health_check]
                  pipelines:
                    metrics:
                      receivers:
                        - otlp
                      processors: [attributes, transform/remove_scope_labels, transform/make_labels, batch]
                      exporters:  [otlphttp/otc]
                    traces:
                      receivers:  [otlp]
                      processors: [batch]
                      exporters:  [otlp/tempo]
                                """;
        Assert.assertEquals(expectation, out);
    }
}
