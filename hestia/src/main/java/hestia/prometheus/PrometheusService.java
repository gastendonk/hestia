package hestia.prometheus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.rest.REST;
import hestia.HestiaWebapp;
import hestia.base.IBranch;
import hestia.base.ShellScriptExecutor;
import hestia.prometheus.alert.AlertGroup;
import hestia.prometheus.alert.AlertRulesYamlBuilder;

/**
 * General service for managing Prometheus and the Prometheus Alertmanager
 */
public class PrometheusService {

    public void deploy(Collection<String> environments, IBranch branch) {
        if (StringService.isNullOrEmpty(HestiaWebapp.config.getPrometheusHost())) {
            Logger.info("[PrometheusService] no deployment because PROMETHEUS is not set");
            return;
        }
        var dao = HestiaWebapp.config.alertGroupDAO(branch);
        List<AlertGroup> groups = dao.loadAll(environments);
        var yaml = new AlertRulesYamlBuilder(groups).build();
        try {
            validate(yaml);
            var file = HestiaWebapp.config.getAlertRulesFile();
            write(file.toPath(), yaml);
            if (file.isFile()) {
                Logger.info("file written: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        reloadPrometheus();
    }
    
    private void validate(String yaml) throws IOException {
        var promtool = HestiaWebapp.config.getPromtool();
        Logger.debug("promtool: " + promtool);
        if (StringService.isNullOrEmpty(promtool) || !promtool.contains("promtool") || !new File(promtool).isFile()) {
            Logger.info("Alert rules cannot be validated because there is no promtool.");
            return;
        }
        var tempFile = Files.createTempFile("validate-alerts", ".yml");
        Logger.debug(yaml);
        write(tempFile, yaml);
        try {
            var sc = new ShellScriptExecutor();
            var dn = tempFile.toFile().getAbsolutePath();
            var cmd = promtool + " check rules " + dn;
            Logger.debug(cmd);
            String log = sc.executeAndGetLog(cmd, tempFile.toFile().getParentFile());
            Logger.debug(log);
            if (sc.getExitValue() == 0) {
                Logger.info("Alert rules validation ok");
            } else {
                throw new RuntimeException("Alert rules validation failed!\n" + log.replace(dn, "alert-rules.yml"));
            }
        } finally {
            if (Logger.getLevel() != Level.DEBUG) {
                tempFile.toFile().delete();
            }
        }
    }
    
    private void write(Path file, String yaml) throws IOException {
        file.toFile().getParentFile().mkdirs();
        Files.writeString(file, yaml, StandardCharsets.UTF_8);
    }

    public void reloadPrometheus() {
        if (!StringService.isNullOrEmpty(HestiaWebapp.config.getPrometheusHost())) {
            var url = HestiaWebapp.config.getPrometheusHost() + "/-/reload";
            Logger.info("reloadPrometheus: " + url);
            REST.post(url, "");
            // Status 500 ist ein Indiz dafuer, dass die alert-rules nicht valide sind.
        }
    }
    
    public void reloadAlertmanager() {
        if (!StringService.isNullOrEmpty(HestiaWebapp.config.getAlertmanagerHost())) {
            var url = HestiaWebapp.config.getAlertmanagerHost() + "/-/reload";
            Logger.info("reloadAlertmanager: " + url);
            REST.post(url, "");
        }
    }
}
