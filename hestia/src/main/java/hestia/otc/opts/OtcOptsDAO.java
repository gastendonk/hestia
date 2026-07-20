package hestia.otc;

import java.io.File;

import github.soltaufintel.amalia.base.FileService;
import hestia.HestiaWebapp;

public class OtcOptsDAO {

    public static OtcOpts load() {
        var o = FileService.loadJsonFile(file(), OtcOpts.class);
        return o == null ? new OtcOpts() : o;
    }

    public static void save(OtcOpts o) {
        FileService.saveJsonFile(file(), o);
    }

    private static File file() {
        return new File(HestiaWebapp.config.getMonitoredTargetsFolder(), "OtcOpts.json");
    }
}
