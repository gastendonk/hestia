package hestia.prometheus.alert;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import github.soltaufintel.amalia.base.FileService;
import hestia.HestiaWebapp;

public class AlertGroupDAO {

    public static List<AlertGroup> load(String environment) {
        AlertGroups e = FileService.loadJsonFile(file(environment), AlertGroups.class);
        return e == null || e.getList() == null ? new ArrayList<>() : e.getList();
    }

    public static void save(List<AlertGroup> list, String environment) {
        list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        AlertGroups e = new AlertGroups();
        e.setList(list);
        FileService.saveJsonFile(file(environment), e);
    }

    public static class AlertGroups {
        private List<AlertGroup> list;

        public List<AlertGroup> getList() {
            return list;
        }

        public void setList(List<AlertGroup> list) {
            this.list = list;
        }
    }

    private static File file(String environment) {
        return new File(HestiaWebapp.config.getAlertsFolder(), environment + ".json");
    }

    public static List<AlertGroup> loadAll(Collection<String> environments) {
        return environments.stream().map(e -> load(e)).flatMap(List::stream).collect(Collectors.toList());
    }
}
