package hestia.web;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import hestia.HestiaWebapp;
import hestia.base.HPage;
import hestia.environment.Environment;
import hestia.environment.EnvironmentDAO;
import hestia.otc.OtcProcess;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.prometheus.alert.AlertGroup;
import hestia.prometheus.alert.AlertGroupDAO;

public class IndexPage extends HPage {

    @Override
    protected void execute() {
        OtcProcess otc = HestiaWebapp.otcProcess;
        List<Environment> envs = EnvironmentDAO.load();

        put("info", esc(System.getenv("INFO")));
        put("pid", "" + otc.pid());
        put("alive", otc.alive());
        putInt("status", otc.checkHealth());
        put("datetime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        put("info1", esc(otc.info1));
        put("info2", esc(otc.info2));
        put("config", esc(FileService.loadPlainTextFile(new File("/work/config.yaml"))));

        var list = list("envs");
        for (Environment env : envs) {
            var m = list.add();
            m.put("id", esc(env.getId()));
            m.put("name", esc(env.getName()));
            m.putInt("nr1", env.isActive() ? nr1(env) : 0);
            m.putInt("nr2", env.isActive() ? nr2(env) : 0);
            m.put("active", env.isActive());
        }
        Cols cols = Cols.of( //
                new Col(n("Environment"), "{{if not i.active}}<span class=\"not-active\">{{/if}}{{i.name}}"
                        + "{{if not i.active}}</span>{{/if}}").sortable("name"), //
                new Col("", "<a href=\"/environment/{{i.id}}\" class=\"btn btn-xs btn-default\" title=\"Bearbeiten\"><i"
                        + " class=\"fa fa-pencil\"></i></a>"
                        + " <a href=\"/mt/{{i.id}}\" class=\"btn btn-xs btn-default\">" + n("MonitoredTargets") + " ({{i.nr1}})</a>"
                        + " <a href=\"/alert/{{i.id}}\" class=\"btn btn-xs btn-default\">" + n("Alerts") + " ({{i.nr2}})</a>"
                        ));
        put("table", new TableComponent("wauto", cols, model, "envs").sort(0));
    }

    private int nr1(Environment env) {
        return (int) MonitoredTargetDAO.load(env.getId()).stream().filter(i -> i.isActive()).count();
    }

    private int nr2(Environment env) {
        int ret = 0;
        var groups = AlertGroupDAO.load(env.getId());
        for (AlertGroup g : groups) {
            ret += g.getRules().stream().filter(i -> i.isActive()).count();
        }
        return ret;
    }
}
