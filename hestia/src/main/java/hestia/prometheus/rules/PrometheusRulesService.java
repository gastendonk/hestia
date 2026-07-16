package hestia.prometheus.rules;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import hestia.base.HestiaBaseService;

/**
 * Service for loading and saving Prometheus alert rules files
 */
public class PrometheusRulesService {

    public List<AlertRulesFile> loadAlertRulesFiles(File folder) {
        var reader = new AlertRulesFileReader();
        return HestiaBaseService.getFiles(folder, ".yml").stream() //
                .map(file -> {
                    try {
                        return reader.read(file);
                    } catch (IOException e) {
                        Logger.error(e, "Error loading alert rules file " + file.getAbsolutePath());
                        return null;
                    }
                }) //
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void saveAlertRulesFiles(List<AlertRulesFile> files, File folder) {
        for (AlertRulesFile file : files) {
            FileService.savePlainTextFile(new File(folder, file.getName()), file.yaml());
        }
    }
}
