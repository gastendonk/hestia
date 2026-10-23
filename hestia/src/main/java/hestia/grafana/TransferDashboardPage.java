package hestia.grafana;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.base.StringService;
import hestia.grafana.GrafanaData.Dashboard;
import hestia.grafana.GrafanaData.Datasource;
import hestia.web.base.HPage;

public class TransferDashboardPage extends HPage {
    private String base;
    private List<Grafana> configs;

    @Override
    protected void execute() {
        String grafanaId = ctx.pathParam("g");
        String dashboardId = ctx.pathParam("d");
        String target = ctx.queryParam("target");

        base = "/" + esc(b().getBranch() + "grafana/" + ctx.pathParam("g") + "/dashboard/" + ctx.pathParam("d")) + "/transfer";
        configs = grafanaDAO().load();
        var gc = get(grafanaId);
        GrafanaService sv = new GrafanaService(gc);
        Dashboard d = sv.loadDashboard(dashboardId);

        if (StringService.isNullOrEmpty(target)) {
            selectTargetGrafana(gc, d);
        } else {
            put("target", u(target));
            var gc2 = get(target);
            if (isPOST()) {
                action(gc, d, gc2);
            } else {
                selectDatasource(gc, d, gc2);
            }
        }
        header(n("TransferDashboard"));
        put("srcG", esc(gc.getName()));
        put("srcD", esc(d.getDashboardTitle()));
        put("json", esc(FileService.prettyJSON(d.getSpec())));
    }

    private Grafana get(String id) {
        return configs.stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
    }

    private void selectTargetGrafana(Grafana gc, Dashboard d) {
        var list = list("grafanaList");
        for (Grafana c : configs) {
            list.add().put("name", esc(c.getName())).put("link", base + "?target=" + u(c.getId()));
        }
        put("modus1", true);
    }

    private void selectDatasource(Grafana src, Dashboard d, Grafana target) {
        var datasources = new GrafanaService(target).loadDatasources();
        var list = list("datasources");
        Set<Map<String, String>> j = d.collectDatasources();
        int i = 0;
        for (Map<String, String> jj : j) {
            var name = jj.get("name");
            var uid = jj.get("uid");
            if ("grafana".equals(name)) {
                continue;
            }
            var map = list.add();
            map.put("name", esc(name));
            map.put("uid", esc(uid));
            map.putInt("i", i++);
            var list2 = map.list("targets");
            for (Datasource ds : datasources) {
                if ("grafana-postgresql-datasource".equals(ds.getName())) {
                    continue;
                }
                var map2 = list2.add();
                map2.put("name", esc(ds.getName() + " [" + ds.getUid() + "]"));
                map2.put("id", esc(ds.getUid()));
                map2.put("selected", false);
            }
        }
        put("modus1", false);
    }

    private void action(Grafana src, Dashboard d, Grafana target) {
        Map<String, String> change = getChangeMap(d);
        
        // Änderungen am Dashboard vornehmen
        int changes = 0;
        for (Map.Entry<String, String> kv : change.entrySet()) {
            changes += d.replaceDatasourceUid(kv.getKey(), kv.getValue());
        }
        Logger.info("change map (changes: " + changes + "): " + change);

        // Dashboard ins Ziel Grafana speichern
        new GrafanaService(target).saveDashboard(d);
        
        ctx.redirect("/samira/grafana/" + ctx.pathParam("g"));
    }

    private Map<String, String> getChangeMap(Dashboard d) {
        Map<String, String> change = new HashMap<>();
        Set<Map<String, String>> j = d.collectDatasources();
        int i = 0;
        for (Map<String, String> jj : j) {
            var name = jj.get("name");
            var uid = jj.get("uid");
            if ("grafana".equals(name)) {
                continue;
            }
            var auswahl = ctx.formParam("ds" + i);
            change.put(uid, auswahl);
            i++;
        }
        return change;
    }
}
