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
        List<Grafana> grafanas = grafanaDAO().load();

        header("Grafana");
        put("branch", esc(b().getBranch()));
        var list = list("list");
        for (Grafana g : grafanas) {
            var m = list.add();
            m.put("id", esc(g.getId()));
            m.put("name", esc(g.getName()));
            m.put("host", esc(g.getHost()));
        }
        Cols cols = Cols.of(
                Col.si("Name", "name", "/{{branch}}/grafana/{{i.id}}/edit"),
                Col.si("Host", "host"), // TODO Link um Grafana aufzurufen
                new Col("", "<a href=\"/{{branch}}/grafana/{{i.id}}/delete\" class=\"btn btn-xs btn-danger\""
                        + " onclick=\"return confirm('{{N.Delete}}?');\" title=\"{{N.Delete}}\"><i class=\"fa fa-trash-o\"></i></a>"));
        put("table", new TableComponent("wauto", cols, model, "list"));
    }
}
