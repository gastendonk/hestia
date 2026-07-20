package hestia.otc.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import github.soltaufintel.amalia.base.FileService;
import hestia.HestiaWebapp;
import hestia.base.RuntimeTypeAdapterFactory;

public class MonitoredTargetDAO {

    public static List<MonitoredTarget> load(String id) {
        String json = FileService.loadPlainTextFile(file(id));
        MonitoredTargets e = gson().fromJson(json, MonitoredTargets.class);
        return e == null || e.getList() == null ? new ArrayList<>() : e.getList();
    }

    public static void save(String id, List<MonitoredTarget> list) {
        list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        MonitoredTargets e = new MonitoredTargets();
        e.setList(list);
        FileService.savePlainTextFile(file(id), gson().toJson(e));
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

    private static Gson gson() {
        // Wir definieren ein Feld "targetType" im JSON, um die Klassen zu unterscheiden
        RuntimeTypeAdapterFactory<MonitoredTarget> adapterFactory = 
            RuntimeTypeAdapterFactory.of(MonitoredTarget.class, "targetType")
                .registerSubtype(Database.class, "database")
                .registerSubtype(Server.class, "server")
                .registerSubtype(Site.class, "site");
        return new GsonBuilder()
                .registerTypeAdapterFactory(adapterFactory)
                .setPrettyPrinting() // Optional: Für schöner lesbares JSON
                .create();
    }

    private static File file(String envId) {
        return new File(HestiaWebapp.config.getMonitoredTargetsFolder(), envId + ".json");
    }

    public static List<MonitoredTarget> loadAll(Collection<String> environmentIdList) {
        return environmentIdList.stream().map(e -> load(e)).flatMap(List::stream).collect(Collectors.toList());
    }
}
