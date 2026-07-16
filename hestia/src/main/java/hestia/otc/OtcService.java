package hestia.otc;

import java.io.File;
import java.util.List;

import github.soltaufintel.amalia.base.FileService;

/**
 * Service for managing OTel Collector config file
 */
public class OtcService {

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
