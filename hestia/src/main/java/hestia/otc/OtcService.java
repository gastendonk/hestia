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
            throw new RuntimeException("Validate error\n" + out);
        }
    }
    
    public boolean deployOtelcolContrib() {
        try {
            // download
            var downloadFile = Files.createTempFile("", ".tar.gz").toFile();
            Logger.debug("deployOtelcolContrib | downloadFile: " + downloadFile);
            downloadFile.delete();
            var url = HestiaWebapp.config.getOtelcolContribDownloadUrl();
            Logger.info("deployOtelcolContrib | download: " + url);
            Downloader.download(url, Duration.ofMinutes(2), downloadFile);
            Logger.info("deployOtelcolContrib | download: " + downloadFile.getAbsolutePath() + ", " + downloadFile.isFile());

            // unzip
            Path tempDir = Files.createTempDirectory("extract");
            Logger.debug("deployOtelcolContrib | temp dir: " + tempDir.toFile().getAbsolutePath());
            Downloader.extractTarGz(downloadFile.toPath(), tempDir);

            // check if expected file is there
            File target = new File(tempDir.toFile(), "otelcol-contrib"); // TODO dn param.
            boolean exists = target.isFile();
            Logger.debug("deployOtelcolContrib | target file: " + target.getAbsolutePath() + ", " + exists);
            if (exists) {
                
                // deploy program
                var x = HestiaWebapp.config.getOtelcolContrib();
                FileService.copyFile(target, x.getParentFile());
                Logger.info("installed file: " + x.getAbsolutePath() + ", " + (x.isFile() ? "SUCCESS" : "ERROR: missing file"));
                return x.isFile();
            }
            return false;
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
    }
}
