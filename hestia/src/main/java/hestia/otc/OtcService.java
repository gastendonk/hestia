package hestia.otc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.base.IdGenerator;
import hestia.HestiaWebapp;
import hestia.base.Downloader;
import hestia.base.IBranch;
import hestia.base.ShellScriptExecutor;
import hestia.otc.model.Database;
import hestia.otc.model.Definition;
import hestia.otc.model.MonitoredTarget;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.otc.model.Server;
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
            var yaml = new ConfigYamlBuilder(list, OtcOptsDAO.load()).build();
            Logger.debug("(1) config.yaml: " + yaml);
            validate(yaml);
            
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
            configFile.delete();
        }
    }
    
    public boolean installOtelcolContrib() {
        try {
            // download
            var downloadFile = Files.createTempFile("", ".tar.gz").toFile();
            downloadFile.delete();
            var url = HestiaWebapp.config.getOtelcolContribDownloadUrl();
            Logger.info("deployOtelcolContrib | URL: " + url);
            Downloader.download(url, Duration.ofMinutes(2), downloadFile);
            Logger.info("deployOtelcolContrib | download file: " + downloadFile.getAbsolutePath() + ", " + downloadFile.isFile());

            // unzip
            Path tempDir = Files.createTempDirectory("extract");
            Logger.debug("deployOtelcolContrib | temp folder: " + tempDir.toFile().getAbsolutePath());
            Downloader.extractTarGz(downloadFile.toPath(), tempDir);
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
                Downloader.copyFileToFile(target, otelcolContrib);
                exists = otelcolContrib.isFile();
                Logger.info("deployOtelcolContrib | installed file: " + otelcolContrib.getAbsolutePath() +
                        ", " + (exists ? "SUCCESS" : "ERROR: missing file"));
                if (exists) {
                    target.delete();
                    Downloader.makeExecutable(otelcolContrib.toPath());
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
    
    /**
     * F�r jede Site einen Alarm anlegen.
     * @param branch -
     * @param id environment ID
     * @param istDown "ist down"
     */
    public void siteAlerts(IBranch branch, String id, String istDown) {
        MonitoredTargetDAO dao = HestiaWebapp.config.mtDAO(branch);
        AlertRuleDAO ruleDAO = HestiaWebapp.config.alertRuleDAO(branch);

        List<AlertRule> allRules = new ArrayList<>();
        AlertGroup g = group(branch, id, allRules);
        
        List<MonitoredTarget> mtlist = dao.load(id);
        int n = 0;
        for (MonitoredTarget mt : mtlist) {
            if (mt instanceof Site s && !exist(s.getName().replace(" ", "_"), allRules)) {
                AlertRule rule = new AlertRule();
                rule.setId(IdGenerator.createId25());
                rule.setAlert(s.getName().replace(" ", "_"));
                rule.setSummary(s.getName() + " " + istDown);
                rule.setDescription(s.getUrl());
                rule.setExpr("httpcheck_status{http_url=\"" + s.getUrl() + "\"} == 0");
                rule.setDurationFor("");
                ruleDAO.insert(id, g.getId(), rule);
                n++;
            }
        }
        Logger.info("alert rules created: " + n);
    }
    
    private AlertGroup group(IBranch branch, String id, List<AlertRule> allRules) {
        AlertGroupDAO alertGroupDAO = HestiaWebapp.config.alertGroupDAO(branch);
        List<AlertGroup> groups = alertGroupDAO.load(id);
        if (groups.isEmpty()) {
            AlertGroup g = new AlertGroup();
            g.setId(IdGenerator.createId25());
            g.setName("Sites");
            alertGroupDAO.insert(id, g);
            return g;
        } else {
            for (AlertGroup i : groups) {
                allRules.addAll(i.getRules());
            }
            for (AlertGroup i : groups) {
                if (i.getName().toLowerCase().contains("sites")) {
                    return i;
                }
            }
            return groups.get(0);
        }
    }

    private boolean exist(String name, List<AlertRule> rules) {
        for (AlertRule r : rules) {
            if (r.getAlert().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public String duplicate(IBranch branch, String environmentId, String mtId) {
        var dao = HestiaWebapp.config.mtDAO(branch);
        MonitoredTarget m = dao.loadOne(environmentId, mtId);
        String idNeu = "_";
        if (m instanceof Server s) {
            var n = new Server();
            n.setHost(s.getHost());
            n.setName(s.getName());
            n.setPath(s.getPath());
            n.setType(s.getType());
            n.setId(IdGenerator.createId25());
            n.setActive(s.isActive());
            dao.insert(environmentId, n);
            idNeu = n.getId();
        } else if (m instanceof Site s) {
            var n = new Site();
            n.setName(s.getName());
            n.setUrl(s.getUrl());
            n.setId(IdGenerator.createId25());
            n.setActive(s.isActive());
            dao.insert(environmentId, n);
            idNeu = n.getId();
        } else if (m instanceof Database s) {
            var n = new Database();
            n.setHost(s.getHost());
            n.setName(s.getName());
            n.setUser(s.getUser());
            n.setPassword(s.getPassword());
            n.setId(IdGenerator.createId25());
            n.setActive(s.isActive());
            dao.insert(environmentId, n);
            idNeu = n.getId();
        } else if (m instanceof Definition s) {
            var n = new Definition();
            n.setDefinition(s.getDefinition());
            n.setName(s.getName());
            n.setId(IdGenerator.createId25());
            n.setActive(s.isActive());
            dao.insert(environmentId, n);
            idNeu = n.getId();
        } else {
            throw new UnsupportedOperationException(m.getClass().getName());
        }
        return idNeu;
    }
}
