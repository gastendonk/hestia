package hestia.otc;

import java.io.File;
import java.util.List;

import github.soltaufintel.amalia.base.FileService;
import hestia.base.ShellScriptExecutor;

/**
 * Service for managing OTel Collector (otc) and its config.yaml file
 */
public class OtcService {

    /**
     * @param program otelcol-contrib binary
     * @param windows false: Linux
     * @param configFile otc config.yaml
     * @return "": success, otherwise error message
     */
    public String validate(File program, boolean windows, File configFile) {
        var sc = new ShellScriptExecutor();
        var cmd = (windows ? "@" : "") + program.getAbsolutePath() + " validate --config=" + configFile.getAbsolutePath();
        String out = sc.executeAndGetLog(cmd, program.getParentFile());
        return sc.getExitValue() == 0 ? "" : out;
    }
    
    public void start(File program, boolean windows, File configFile) {
        var sc = new ShellScriptExecutor();
        sc.wait = false;
        var cmd = program.getAbsolutePath() + " --config=" + configFile.getAbsolutePath();
        sc.execute(cmd, program.getParentFile());
    }

//    public synchronized void stop(Process process) throws InterruptedException {
//        if (process == null || !process.isAlive()) {
//            return;
//        }
//
//        process.destroy(); // SIGTERM unter Linux
//
//        if (!process.waitFor(10, TimeUnit.SECONDS)) {
//            process.destroyForcibly();
//
//            if (!process.waitFor(5, TimeUnit.SECONDS)) {
//                throw new IllegalStateException(
//                        "OTel Collector could not be stopped"
//                );
//            }
//        }
//    }
    
    /**
     * Build config.yaml content and save file.
     * @param allMonitoredTargets all monitored targets from all active environments
     * @param opts -
     * @param file config.yaml
     */
    public void saveConfigFile(List<MonitoredTarget> allMonitoredTargets, OtcOpts opts, File file) {
        String content = new OtcConfigBuilder(allMonitoredTargets, opts).build();
        FileService.savePlainTextFile(file, content);
    }
}
