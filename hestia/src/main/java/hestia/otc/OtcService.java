package hestia.otc;

import java.io.File;
import java.util.List;

import github.soltaufintel.amalia.base.FileService;

/**
 * Service for managing OTel Collector config file. Service for managing
 * monitored targets for each Hestia environment.
 */
public class OtcService {

    // config.yaml erzeugen
    public void saveConfigFile(List<MonitoredTarget> allMonitoredTargets, OtcOpts opts, File file) {
        String content = new OtcConfigBuilder(allMonitoredTargets, opts).build();
        FileService.savePlainTextFile(file, content);
    }
}
