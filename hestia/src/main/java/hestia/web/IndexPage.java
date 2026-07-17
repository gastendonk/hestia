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
            m.putInt("nr", env.isActive() ? nr(env) : 0);
            m.put("active", env.isActive());
        }
        Cols cols = Cols.of( //
                new Col(n("Environment"), "<a href=\"/environment/{{i.id}}\"{{if not i.active}}"
                        + " class=\"not-active\"{{/if}}>{{i.name}}</a>").sortable("name"), //
                new Col("", "<a href=\"/alert/{{i.id}}\" class=\"btn btn-xs btn-default\">" + n("Alerts") + " ({{i.nr}})</a>"));
        put("table", new TableComponent("wauto", cols, model, "envs").sort(0));
    }

    private int nr(Environment env) {
        int ret = 0;
        var groups = AlertGroupDAO.load(env.getId());
        for (AlertGroup g : groups) {
            ret += g.getRules().stream().filter(i -> i.isActive()).count();
        }
        return ret;
    }
}
