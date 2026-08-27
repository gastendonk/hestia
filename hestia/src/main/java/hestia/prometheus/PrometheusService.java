package hestia.prometheus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private static final Pattern LINE_NUMBER_PATTERN = 
            Pattern.compile("alert-rules.*?:\\s*yaml:\\s*line\\s+(\\d+):", Pattern.CASE_INSENSITIVE);

    public record LineEntry(int lineNumber, String content) {}

    public record ErrorContext(int targetLineNumber, List<LineEntry> contextLines) {}

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
                var msg = log.replace(dn, "alert-rules.yml");
                var lines = extractContext(msg, yaml); // extract faulty lines
                throw new RuntimeException("Alert rules validation failed!\n" + msg + "\n" +
                        lines.stream().map(i -> i.targetLineNumber + ":\n" +
                        i.contextLines.stream().map(j -> j.content).collect(Collectors.joining("\n"))
                        ).collect(Collectors.joining("\n")));
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
    
    private List<ErrorContext> extractContext(String validatorOutput, String yaml) {
        Set<Integer> lineNumbers = LINE_NUMBER_PATTERN.matcher(validatorOutput)
                .results()
                .map(match -> Integer.parseInt(match.group(1)))
                .collect(Collectors.toSet());

        String[] yamlLines = yaml.split("\\r?\\n");
        List<ErrorContext> contexts = new ArrayList<>();

        for (int targetLine : lineNumbers.stream().sorted().toList()) {
            int targetIndex = targetLine - 1;

            if (targetIndex < 0 || targetIndex >= yamlLines.length) {
                continue;
            }

            // 1. Start-Zeile finden: Rückwärts suchen bis zur Zeile, die mit "-" beginnt
            int startIndex = targetIndex;
            while (startIndex > 0 && !yamlLines[startIndex].trim().startsWith("-")) {
                startIndex--;
            }

            // 2. Ende-Zeile finden: Vorwärts lesen ab startIndex + 1, bis der nächste Listeneintrag ("-") kommt
            int endIndex = startIndex;
            while (endIndex + 1 < yamlLines.length) {
                String nextLine = yamlLines[endIndex + 1];
                
                // Stoppen, wenn die nächste Zeile ein neuer Listeneintrag ist
                if (nextLine.trim().startsWith("-")) {
                    break;
                }
                
                // Optional: Stoppen, wenn wir die Einrückungsebene komplett verlassen (z.B. nächste Gruppe)
                if (!nextLine.isBlank() && Character.isLetter(nextLine.charAt(0))) {
                    break;
                }
                
                endIndex++;
            }

            // 3. Zeilen von startIndex bis endIndex einsammeln
            List<LineEntry> extracted = new ArrayList<>();
            for (int i = startIndex; i <= endIndex; i++) {
                extracted.add(new LineEntry(i + 1, yamlLines[i]));
            }

            contexts.add(new ErrorContext(targetLine, extracted));
        }
        return contexts;
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
