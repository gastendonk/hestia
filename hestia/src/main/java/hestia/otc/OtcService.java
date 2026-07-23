package hestia.otc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import hestia.HestiaWebapp;
import hestia.base.Downloader;
import hestia.base.IBranch;
import hestia.base.ShellScriptExecutor;
import hestia.otc.model.MonitoredTarget;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.otc.opts.OtcOptsDAO;

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
            Logger.info("config.yaml: " + yaml); // XXX
            validate(yaml);
            FileService.saveJsonFile(HestiaWebapp.config.getConfigYaml(), yaml);
            HestiaWebapp.otcProcess.kill();
            HestiaWebapp.otcProcess = new OtcProcess();
        }
    }

    private void validate(String yaml) {
        File configFile = HestiaWebapp.config.getConfigYamlForValidate();
        FileService.saveJsonFile(configFile, yaml);
        var sc = new ShellScriptExecutor();
        var exe = HestiaWebapp.config.getOtelcolContrib();
        var cmd = (ShellScriptExecutor.isWindows() ? "@" : "") + exe.getAbsolutePath() + " validate" //
                + " --config=" + configFile.getAbsolutePath();
        String out = sc.executeAndGetLog(cmd, exe.getParentFile());
        configFile.delete();
        if (sc.getExitValue() != 0) {
            throw new RuntimeException("Validate error:\n" + out);
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
}
