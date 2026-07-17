package hestia.otc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import github.soltaufintel.amalia.base.FileService;
import hestia.HestiaWebapp;

public class MonitoredTargetDAO {

    public static List<MonitoredTarget> load(String environment) {
        MonitoredTargets e = FileService.loadJsonFile(file(environment), MonitoredTargets.class);
        return e == null || e.getList() == null ? new ArrayList<>() : e.getList();
    }

    public static void save(List<MonitoredTarget> list, String environment) {
        list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        MonitoredTargets e = new MonitoredTargets();
        e.setList(list);
        FileService.saveJsonFile(file(environment), e);
    }

    public static class MonitoredTargets {
        private List<MonitoredTarget> list;

        public List<MonitoredTarget> getList() {
            return list;
        }

        public void setList(List<MonitoredTarget> list) {
            this.list = list;
        }
    }

    private static File file(String environment) {
        return new File(HestiaWebapp.config.getMonitoredTargetsFolder(), environment + ".json");
    }

    public static List<MonitoredTarget> loadAll(Collection<String> environments) {
        return environments.stream().map(e -> load(e)).flatMap(List::stream).collect(Collectors.toList());
    }
}
