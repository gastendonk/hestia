package hestia.grafana;

import java.lang.reflect.Type;
import java.util.List;

import org.pmw.tinylog.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.rest.REST;
import hestia.grafana.GrafanaData.Dashboard;
import hestia.grafana.GrafanaData.DashboardList;
import hestia.grafana.GrafanaData.DashboardName;
import hestia.grafana.GrafanaData.Datasource;
import hestia.persist.IRepository;

public class GrafanaService {
    private final String name;
    private final String host;
    private final String token;

    /**
     * GrafanaService beschaffen über: HestiaWebapp.config.byGrafanaId
     */
    public GrafanaService(Grafana config) {
        name = config.getName();
        host = config.getHost();
        token = config.getToken();
    }
    
    public List<DashboardName> loadDashboardNames() {
        var list = loadDashboardList();
        if (list.getItems() == null) {
            return List.of();
        }
        return list.getItems().stream()
                .map(i -> {
                    DashboardName n = new DashboardName();
                    n.setId(i.getMetadata().getName());
                    n.setName(i.getDashboardTitle());
                    return n;
                })
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
    }
    
    public DashboardList loadDashboardList() {
        // Streng genommen fehlt hier das Paging.
        var json = get("/apis/dashboard.grafana.app/v1beta1/namespaces/default/dashboards");
        return new Gson().fromJson(json, DashboardList.class);
    }
    
    public List<Datasource> loadDatasources() {
        var json = get("/api/datasources");
        Type listType = new TypeToken<List<Datasource>>() {}.getType();
        return new Gson().fromJson(json, listType);
    }
    
    public Dashboard loadDashboard(String id) {
        var list = loadDashboardList();
        if (list.getItems() == null) {
            return null;
        }
        return list.getItems().stream() //
                .filter(i -> i.getMetadata().getName().equals(id)) //
                .findFirst().orElse(null);
    }
    
    public void backup(String id, IRepository repo) {
        Dashboard d = loadDashboard(id);
        String title = d.getDashboardTitle();
        String filename = title;
        if (StringService.isNullOrEmpty(filename) || filename.contains("/") || filename.contains("\\")) {
            throw new RuntimeException("Illegal filename: " + filename);
        }
        filename = "grafana-dashboards/" + safeName(name) + "/" + safeName(filename) + ".json";
        Logger.info("backup dashboard into file: " + filename);
        
/* TODO Umstellen auf repo
         * 
        if (!withUser(getRepoDef(), user).commitAndPushFile_ifChanged( //
                filename, //
                FileService.prettyJSON(d.getSpec()), //
                user.getRealName(), //
                user.getMailAddress(), //
                "backup dashboard: " + title)) {
            Logger.info("  " + filename + " has no changes");
        }
         */
    }
    
    public static String safeName(String filename) {
        return FileService.getSafeName(filename.replace(" ", "_").toLowerCase());
    }
    
    public void saveDashboard(Dashboard d) {
        boolean found = false;
        List<DashboardName> ex = loadDashboardNames();
        for (DashboardName dn : ex) {
            if (dn.getName().equals(d.getDashboardTitle())) {
                found = true;
                break;
            }
        }
        if (found) {
            updateDashboard(d);
        } else {
            createDashboard(d);
        }
    }

    public void createDashboard(Dashboard d) {
        post("/apis/dashboard.grafana.app/v1beta1/namespaces/default/dashboards", new Gson().toJson(d));
        Logger.info("createDashboard ok");
    }

    public void updateDashboard(Dashboard d) {
        String uid = d.getMetadata().getName();
        Logger.info("update dashboard: uid=" + uid);
        put("/apis/dashboard.grafana.app/v1beta1/namespaces/default/dashboards/" + uid,
                new Gson().toJson(d));
        Logger.info("updateDashboard ok");
    }

    private String get(String path) {
        return REST.utf8_get(host + path, token);
    }

    private String post(String path, String body) {
        return REST.utf8_post(host + path, token, body);
    }

    private String put(String path, String body) {
        return REST.utf8_put(host + path, token, body);
    }
}
