package hestia.grafana;

import java.util.List;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import hestia.HestiaWebapp;
import hestia.web.base.HPage;

public class GrafanasPage extends HPage {

    @Override
    protected void execute() {
        if (!HestiaWebapp.config.isGrafanaAllowed()) {
            throw new RuntimeException("Not allowed");
        }
        List<Grafana> grafanas = HestiaWebapp.config.grafanaDAO(b()).load();

        header("Grafana");
        put("branch", esc(b().getBranch()));
        displayConfig(grafanas);
        // see also htmx page GrafanaDashboardsPage
    }
    
    private void displayConfig(List<Grafana> grafanas) {
        var list = list("list");
        for (Grafana g : grafanas) {
            var m = list.add();
            m.put("id", esc(g.getId()));
            m.put("name", esc(g.getName()));
            m.put("host", esc(g.getHost()));
        }
        String link = "/{{branch}}/grafana/{{i.id}}/";
        Cols cols = Cols.of(
                Col.si("Name", "name"),
                new Col("Host", "<a href=\"{{i.host}}\" target=\"_blank\">{{i.host}}</a>").sortable("host"),
                new Col("", "<a href=\"" + link + "edit\" class=\"btn btn-xs btn-default\"><i class=\"fa fa-pencil\"></i> {{N.Edit}}</a>"
                        + " <a href=\"/" + link + "delete\" class=\"btn btn-xs btn-danger\""
                        + " onclick=\"return confirm('{{N.Delete}}?');\" title=\"{{N.Delete}}\"><i class=\"fa fa-trash-o\"></i></a>"));
        put("table", new TableComponent("wauto", cols, model, "list"));
    }
}
