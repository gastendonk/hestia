package hestia.otc.mt;

import java.util.List;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import hestia.HestiaWebapp;
import hestia.otc.model.MonitoredTarget;
import hestia.web.base.HPage;

public class MonitoredTargetsPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        List<MonitoredTarget> mtlist = mtDAO().load(id);
        
        header(n("MonitoredTargets"));
        put("id", esc(id));
        var list = list("list");
        for (MonitoredTarget mt : mtlist) {
            var map = list.add();
            map.put("id", esc(mt.getId()));
            map.put("name", esc(mt.getName()));
            map.put("info", esc(mt.getInfo()));
            map.put("active", mt.isActive());
            map.put("type", mt.getType2());
        }
        list.sort((a, b) -> (a.get("type").toString() + a.get("name").toString())
                .compareToIgnoreCase(b.get("type").toString() + b.get("name").toString()));
        Cols cols = Cols.of(
                new Col(n("Name"), "<a href=\"/{{branch}}/mt/{{id}}/{{i.id}}/edit\"{{if not i.active}}"
                        + " style=\"text-decoration: line-through;\"{{/if}}>{{i.name}}</a>").sortable("name"),
                Col.si("Info", "info"),
                Col.si(n("Type"), "type")
                );
        if (!HestiaWebapp.config.isCustomer()) {
            cols.add(new Col("", "<a href=\"/{{branch}}/mt/{{id}}/{{i.id}}/duplicate\" class=\"btn btn-xs btn-default\""
                    + " title=\"{{N.Duplicate}}\"><i class=\"fa fa-copy\"></i></a>"
                    + " <a href=\"/{{branch}}/mt/{{id}}/{{i.id}}/delete\" class=\"btn btn-xs btn-danger\""
                    + " title=\"{{N.Delete}}\" onclick=\"return confirm('{{N.Delete}}?');\"><i class=\"fa fa-trash-o\"></i></a>"));
        }
        put("table", new TableComponent("wauto", cols, model, "list"));
    }
}
