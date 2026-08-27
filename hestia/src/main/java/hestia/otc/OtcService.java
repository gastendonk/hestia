package hestia.otc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.web.image.IBinaryDataLoader;
import hestia.HestiaWebapp;
import hestia.base.IBranch;
import hestia.base.ShellScriptExecutor;
import hestia.otc.model.Database;
import hestia.otc.model.DatabaseType;
import hestia.otc.model.MonitoredTarget;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.otc.model.Site;
import hestia.otc.opts.OtcOptsDAO;
import hestia.prometheus.alert.AlertGroup;
import hestia.prometheus.alert.AlertGroupDAO;
import hestia.prometheus.alert.rule.AlertRule;
import hestia.prometheus.alert.rule.AlertRuleDAO;

/**
 * Service for managing OTel Collector (otc) and its config.yaml file
 */
public class OtcService {
    private static final Object LOCK = new Object();

    public void deploy(Collection<String> environments, IBranch branch) {
        synchronized (LOCK) {
            MonitoredTargetDAO dao = HestiaWebapp.config.mtDAO(branch);
            List<MonitoredTarget> list = dao.loadAll(environments);
            Logger.debug("[OtcService.deploy] " + environments + " => monitored targets: " + list.size());
            var yaml = new ConfigYamlBuilder(list, OtcOptsDAO.load()).build();
            Logger.debug("(1) config.yaml: " + yaml);
            validate(yaml); // (2)
            
            Logger.info("save OTC config to " + HestiaWebapp.config.getConfigYaml().getAbsolutePath());
            FileService.savePlainTextFile(HestiaWebapp.config.getConfigYaml(), yaml);
            Logger.debug("(3) config.yaml: " + FileService.loadPlainTextFile(HestiaWebapp.config.getConfigYaml()));

            if (HestiaWebapp.otcProcess != null) {
                HestiaWebapp.otcProcess.kill();
            }
            HestiaWebapp.otcProcess = new OtcProcess();
        }
    }

    private void validate(String yaml) {
        File configFile = HestiaWebapp.config.getConfigYamlForValidate(); // TODO Wieso nicht ein tempfile? vgl. PrometheusService
        FileService.savePlainTextFile(configFile, yaml);
        try {
            Logger.debug("(2) config.yaml: " + FileService.loadPlainTextFile(configFile));
            var sc = new ShellScriptExecutor();
            var exe = HestiaWebapp.config.getOtelcolContrib();
            var cmd = (ShellScriptExecutor.isWindows() ? "@" : "") + exe.getAbsolutePath() + " validate" //
                    + " --config=" + configFile.getAbsolutePath();
            String out = sc.executeAndGetLog(cmd, exe.getParentFile());
            if (sc.getExitValue() != 0) {
                throw new RuntimeException("Validate error:\n" + out);
            }
        } finally {
            if (Logger.getLevel() != Level.DEBUG) {
                configFile.delete();
            }
        }
    }
    
    public boolean installOtelcolContrib() {
        try {
            // download
            var downloadFile = Files.createTempFile("", ".tar.gz").toFile();
            downloadFile.delete();
            var url = HestiaWebapp.config.getOtelcolContribDownloadUrl();
            Logger.info("deployOtelcolContrib | URL: " + url);
            IBinaryDataLoader.download(url, Duration.ofMinutes(2), downloadFile);
            Logger.info("deployOtelcolContrib | download file: " + downloadFile.getAbsolutePath() + ", " + downloadFile.isFile());

            // unzip
            Path tempDir = Files.createTempDirectory("extract");
            Logger.debug("deployOtelcolContrib | temp folder: " + tempDir.toFile().getAbsolutePath());
            extractTarGz(downloadFile.toPath(), tempDir);
            downloadFile.delete();

            // check if expected file is there
            String dn = HestiaWebapp.config.getOtelcolContrib().getName();
            dn = dn.substring(0, dn.lastIndexOf("-")); // remove version
            File target = new File(tempDir.toFile(), dn); // expected file after unzip
            boolean exists = target.isFile();
            var msg = "deployOtelcolContrib | target file: " + target.getAbsolutePath() + ", " + exists;
            if (exists) {
                Logger.debug(msg);
                
                // install program
                var otelcolContrib = HestiaWebapp.config.getOtelcolContrib();
                FileService.copyFileToFile(target, otelcolContrib);
                exists = otelcolContrib.isFile();
                Logger.info("deployOtelcolContrib | installed file: " + otelcolContrib.getAbsolutePath() +
                        ", " + (exists ? "SUCCESS" : "ERROR: missing file"));
                if (exists) {
                    target.delete();
                    FileService.makeExecutable(otelcolContrib.toPath());
                }
            } else {
                Logger.error(msg);
            }
            return exists;
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
    }
    
    private static void extractTarGz(Path sourceTarGz, Path targetDir) throws IOException {
        try (InputStream fileIn = Files.newInputStream(sourceTarGz);
                BufferedInputStream buffIn = new BufferedInputStream(fileIn);
                GzipCompressorInputStream gzIn = new GzipCompressorInputStream(buffIn);
                TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn)) {
            TarArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                // Pfadtraversierung verhindern (Zip Slip Vulnerability Schutz)
                Path targetPath = targetDir.resolve(entry.getName()).normalize();
                if (!targetPath.startsWith(targetDir.normalize())) {
                    throw new IOException("Bad entry: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    // Falls Unterordner existieren, diese vorher anlegen
                    Files.createDirectories(targetPath.getParent());

                    // Datei direkt auf die Festplatte streamen
                    Files.copy(tarIn, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Fuer jede Site einen Alarm anlegen.
     * @param branch -
     * @param id environment ID
     * @param istDown "ist down"
     */
    public void siteAlerts(IBranch branch, String id, String istDown) {
        MonitoredTargetDAO dao = HestiaWebapp.config.mtDAO(branch);
        AlertRuleDAO ruleDAO = HestiaWebapp.config.alertRuleDAO(branch);

        List<AlertRule> allRules = new ArrayList<>();
        AlertGroup g = group("Sites", branch, id, allRules);
        
        List<MonitoredTarget> mtlist = dao.load(id);
        int created = 0, updated = 0;
        for (MonitoredTarget mt : mtlist) {
            if (mt instanceof Site s) {
                String name = s.getName().replace(" ", "_");
                AlertRule rule = find(name, allRules);
                boolean found = (rule != null);
                if (!found) {
                    rule = new AlertRule();
                    rule.setId(IdGenerator.createId25());
                    rule.setAlert(name);
                    rule.setDescription(s.getUrl());
                    rule.setDurationFor("");
                    rule.setSummary(s.getName() + " " + istDown);
                }
                rule.setExpr("(sum(httpcheck_status{http_status_class=\"2xx\", http_url=\"" + s.getUrl()
                        + "\"}) or vector(0)) == 0");
                if (found) {
                    ruleDAO.update(id, g.getId(), rule);
                    updated++;
                } else {
                    ruleDAO.insert(id, g.getId(), rule);
                    created++;
                }
            }
        }
        Logger.info("[siteAlerts] alert rules created: " + created + ", updated: " + updated);
    }

    /**
     * Fuer jede Oracle DB einen Alarm anlegen.
     * @param branch -
     * @param id environment ID
     * @param istDown "ist down"
     */
    public void oracleAlerts(IBranch branch, String id, String istDown) {
        MonitoredTargetDAO dao = HestiaWebapp.config.mtDAO(branch);
        AlertRuleDAO ruleDAO = HestiaWebapp.config.alertRuleDAO(branch);

        List<AlertRule> allRules = new ArrayList<>();
        AlertGroup g = group("Oracle", branch, id, allRules);
        
        List<MonitoredTarget> mtlist = dao.load(id);
        int created = 0, updated = 0;
        for (MonitoredTarget mt : mtlist) {
            if (mt instanceof Database s && s.getType() == DatabaseType.ORACLE) {
                String name = s.getName().replace(" ", "_");
                AlertRule rule = find(name, allRules);
                boolean found = (rule != null);
                if (!found) {
                    rule = new AlertRule();
                    rule.setId(IdGenerator.createId25());
                    rule.setAlert(name);
                    rule.setDescription("Host: " + s.getHost());
                    rule.setDurationFor("");
                    rule.setSummary("Oracle DB " + s.getName() + " " + istDown);
                }
                rule.setExpr("absent(oracledb_user_commits_total{instance=\"" + s.getHost() + "/" + s.getName() + "\"})");
                if (found) {
                    ruleDAO.update(id, g.getId(), rule);
                    updated++;
                } else {
                    ruleDAO.insert(id, g.getId(), rule);
                    created++;
                }
            }
        }
        Logger.info("[oracleAlerts] alert rules created: " + created + ", updated: " + updated);
    }
    
    private AlertGroup group(String name, IBranch branch, String id, List<AlertRule> allRules) {
        AlertGroupDAO alertGroupDAO = HestiaWebapp.config.alertGroupDAO(branch);
        List<AlertGroup> groups = alertGroupDAO.load(id);
        if (groups.isEmpty()) {
            AlertGroup g = new AlertGroup();
            g.setId(IdGenerator.createId25());
            g.setName(name);
            alertGroupDAO.insert(id, g);
            return g;
        } else {
            for (AlertGroup i : groups) {
                allRules.addAll(i.getRules());
            }
            for (AlertGroup i : groups) {
                if (i.getName().toLowerCase().contains(name.toLowerCase())) {
                    return i;
                }
            }
            return groups.get(0);
        }
    }

    private AlertRule find(String name, List<AlertRule> rules) {
        for (AlertRule r : rules) {
            if (r.getAlert().equals(name)) {
                return r;
            }
        }
        return null;
    }

    public String duplicate(IBranch branch, String environmentId, String mtId) {
        var dao = HestiaWebapp.config.mtDAO(branch);
        var mt = dao.loadOne(environmentId, mtId);
        var neu = mt.copy();
        dao.insert(environmentId, neu);
        return neu.getId();
    }
}
