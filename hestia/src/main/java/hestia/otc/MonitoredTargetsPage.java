package hestia.otc;

import java.util.List;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import hestia.base.HPage;
import hestia.otc.model.Database;
import hestia.otc.model.MonitoredTarget;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.otc.model.Server;
import hestia.otc.model.Site;

public class MonitoredTargetsPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        List<MonitoredTarget> mtlist = MonitoredTargetDAO.load(id);
        
        header(n("MonitoredTargets"));
        put("id", esc(id));
        var list = list("list");
        for (MonitoredTarget mt : mtlist) {
            var map = list.add();
            map.put("id", esc(mt.getId()));
            map.put("name", esc(mt.getName()));
            map.put("active", mt.isActive());
            String type = "";
            if (mt instanceof Site) {
                type = "SITE";
            } else if (mt instanceof Server d) {
                type = "SERVER/" + d.getType().name();
            } else if (mt instanceof Database d) {
                type = "DATABASE/" + d.getType().name();
            }
            map.put("type", type);
        }
        list.sort((a, b) -> (a.get("type").toString() + a.get("name").toString())
                .compareToIgnoreCase(b.get("type").toString() + b.get("name").toString()));
        Cols cols = Cols.of(
                new Col(n("Name"), "<a href=\"/mt/{{id}}/{{i.id}}/edit\"{{if not i.active}}"
                        + " style=\"text-decoration: line-through;\"{{/if}}>{{i.name}}</a>").sortable("name"),
                Col.si(n("Type"), "type"),
                new Col("", "<a href=\"/mt/{{id}}/{{i.id}}/delete\" class=\"btn btn-xs btn-danger\" title=\"{{N.Delete}}\""
                        + " onclick=\"return confirm('{{N.Delete}}?');\"><i class=\"fa fa-trash-o\"></i></a>")
                );
        put("table", new TableComponent("wauto", cols, model, "list"));
    }
}
