package hestia.environment;

import java.util.List;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import hestia.HestiaWebapp;
import hestia.web.base.HPage;

public class EnvironmentsPage extends HPage {

    @Override
    protected void execute() {
        List<Environment> envs = environmentDAO().load();
        
        var list = list("envs");
        for (Environment env : envs) {
            var m = list.add();
            m.put("id", esc(env.getId()));
            String name = env.getName();
            if (!HestiaWebapp.config.isCustomer()) {
                name = env.getCustomer() + " " + name;
            }
            m.put("name", esc(name));
            m.put("nr1", env.isActive() ? "" + mtDAO().count(env.getId()) : "&ndash;");
            m.put("nr2", env.isActive() ? "" + alertGroupDAO().count(env.getId()) : "&ndash;");
            m.put("active", env.isActive());
        }
        var delete = " <a href=\"/{{branch}}/environment/{{i.id}}/delete\" onclick=\"return confirm('{{N.Delete}}?');\""
                + " class=\"btn btn-xs btn-danger\" title=\"{{N.Delete}}\"><i class=\"fa fa-trash-o\"></i></a>";
        if (HestiaWebapp.config.isCustomer()) {
            delete = "";
        }
        Cols cols = Cols.of( //
                new Col(n("Environment"), "{{if not i.active}}<span class=\"not-active\">{{/if}}{{i.name}}"
                        + "{{if not i.active}}</span>{{/if}}").sortable("name"), //
                new Col("", "<a href=\"/{{branch}}/environment/{{i.id}}\" class=\"btn btn-xs btn-default\" title=\"{{N.Edit}}\"><i"
                        + " class=\"fa fa-pencil\"></i></a>"
                        + delete
                        + " <a href=\"/{{branch}}/mt/{{i.id}}\" class=\"btn btn-xs btn-default mw1\">" + n("MonitoredTargets") + " ({{i.nr1}})</a>"
                        + " <a href=\"/{{branch}}/alert/{{i.id}}\" class=\"btn btn-xs btn-default mw2\">" + n("Alerts") + " ({{i.nr2}})</a>"
                        ));
        put("table", new TableComponent("wauto", cols, model, "envs").sort(0));
        header(n("Environments"));
    }
}
