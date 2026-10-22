package hestia.otc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.web.image.IBinaryDataLoader;
import hestia.HestiaWebapp;
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
            var secrets = HestiaWebapp.config.secretDAO(branch).loadAllAsMap(environments);
            Logger.debug("[OtcService.deploy] " + environments + " => monitored targets: " + list.size());
            var yaml = new ConfigYamlBuilder(list, OtcOptsDAO.load()).build();
            Logger.debug("(1) config.yaml: " + yaml);
            validate(yaml, secrets); // (2)
            
            Logger.info("save OTC config to " + HestiaWebapp.config.getConfigYaml().getAbsolutePath());
            FileService.savePlainTextFile(HestiaWebapp.config.getConfigYaml(), yaml);
            Logger.debug("(3) config.yaml: " + FileService.loadPlainTextFile(HestiaWebapp.config.getConfigYaml()));

            if (HestiaWebapp.otcProcess != null) {
                HestiaWebapp.otcProcess.kill();
            }
            HestiaWebapp.otcProcess = new OtcProcess(branch);
        }
    }

    private void validate(String yaml, Map<String, String> secrets) {
        File configFile = HestiaWebapp.config.getConfigYamlForValidate(); // TODO Wieso nicht ein tempfile? vgl. PrometheusService
        FileService.savePlainTextFile(configFile, yaml);
        try {
            Logger.debug("(2) config.yaml: " + FileService.loadPlainTextFile(configFile));
            var sc = new ShellScriptExecutor();
            var exe = HestiaWebapp.config.getOtelcolContrib();
            var win = ShellScriptExecutor.isWindows();
            var cmd = envvars(secrets, win) //
                    + (win ? "@" : "") + exe.getAbsolutePath() + " validate" //
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
    
    private String envvars(Map<String, String> secrets, boolean win) {
        var lf = win ? "\r\n" : "\n";
        return secrets.entrySet().stream()
                .map(entry -> {
                    String ret;
                    String name = entry.getKey();
                    String secret = entry.getValue();
                    if (win) {
                        ret = "SET " + name + "=" + secret;
                    } else {
                        ret = "export " + name + "='" + secret.replace("'", "'\\''") + "'";
                    }
                    return ret + lf;
                })
                .collect(Collectors.joining());
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

    public String duplicate(IBranch branch, String environmentId, String mtId) {
        var dao = HestiaWebapp.config.mtDAO(branch);
        var mt = dao.loadOne(environmentId, mtId);
        var neu = mt.copy();
        dao.insert(environmentId, neu);
        return neu.getId();
    }

    public static List<String> envs(IBranch branch) {
        var rawEnvs = HestiaWebapp.config.environmentDAO(branch).load();
        if (HestiaWebapp.config.isBURG()) {
            return rawEnvs.stream().filter(i -> i.isActive() && i.getCustomer().equals("BURG")).map(i -> i.getId()).toList();
        } else {
            return rawEnvs.stream().filter(i -> i.isActive()).map(i -> i.getId()).toList();
        }
    }
}
