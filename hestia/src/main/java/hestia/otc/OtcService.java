package hestia.otc;

import java.io.File;
import java.util.Collection;
import java.util.List;

import github.soltaufintel.amalia.base.FileService;
import hestia.HestiaWebapp;
import hestia.base.ShellScriptExecutor;
import hestia.otc.model.MonitoredTarget;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.otc.opts.OtcOptsDAO;

/**
 * Service for managing OTel Collector (otc) and its config.yaml file
 */
public class OtcService {
    private static final Object LOCK = new Object();

    public void deploy(Collection<String> environments) {
        synchronized (LOCK) {
            List<MonitoredTarget> list = MonitoredTargetDAO.loadAll(environments);
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
}
