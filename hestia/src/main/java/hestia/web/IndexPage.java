package hestia.web;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import hestia.HestiaWebapp;
import hestia.base.HPage;
import hestia.base.IBranch;
import hestia.base.IRepository;
import hestia.environment.Environment;
import hestia.git.GitRepository;
import hestia.otc.OtcProcess;
import hestia.prometheus.alert.AlertGroup;

public class IndexPage extends HPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String branch = ctx.formParam("branch2");
            ctx.redirect("/" + branch);
            return;
        }
        OtcProcess otc = HestiaWebapp.otcProcess;
        List<Environment> envs = environmentDAO().load();

        put("info", esc(System.getenv("INFO")));
        put("pid", otc == null || otc.pid() <= 0 ? "" : "" + otc.pid());
        put("alive", otc != null && otc.alive());
        if (otc == null) {
            put("status", "--");
        } else {
            putInt("status", otc.checkHealth());
        }
        File otcFile = HestiaWebapp.config.getOtelcolContrib();
        put("otcFileInfo", otcFile.getName() + (otcFile.isFile() ? " exists." : " doesn't exist."));
        put("datetime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        put("info1", otc == null ? "" : esc(otc.info1));
        put("info2", otc == null ? "" : esc(otc.info2));
        var config = FileService.loadPlainTextFile(new File("/work/config.yaml"));
        put("config", config == null ? "File config.yaml not found" : esc(config));
        if (ctx.pathParam("branch") == null) {
            put("branch", "master");
        }
        var b = b();
        IRepository repo = HestiaWebapp.config.getRepository(b);
        put("hasRepo", repo instanceof GitRepository && !HestiaWebapp.config.isCustomer());
        if (repo instanceof GitRepository g) {
            var branch = b.getBranch();
            combobox("branchs", g.getRepo().getBranchNames(), branch, false);
            put("git", esc(g.getUrl()));
            int r = 99;
            try {
                r = g.getRepo().hasUnpushedCommits("refs/heads/" + branch, "refs/remotes/origin/" + branch);
            } catch (Exception e) {
                if (!e.getMessage().contains("Local branch does not exist")) {
                    Logger.warn("hasUnpushedCommits: " + e.getMessage());
                }
            }
            put("unpushed", r > 0);
        } else {
            list("branchs");
            put("git", "#");
            put("unpushed", false);
        }

        var list = list("envs");
        for (Environment env : envs) {
            var m = list.add();
            m.put("id", esc(env.getId()));
            String name = env.getName();
            if (!HestiaWebapp.config.isCustomer()) {
                name = env.getCustomer() + " " + name;
            }
            m.put("name", esc(name));
            m.putInt("nr1", env.isActive() ? nr1(env) : 0);
            m.putInt("nr2", env.isActive() ? nr2(env) : 0);
            m.put("active", env.isActive());
        }
        Cols cols = Cols.of( //
                new Col(n("Environment"), "{{if not i.active}}<span class=\"not-active\">{{/if}}{{i.name}}"
                        + "{{if not i.active}}</span>{{/if}}").sortable("name"), //
                new Col("", "<a href=\"/{{branch}}/environment/{{i.id}}\" class=\"btn btn-xs btn-default\" title=\"{{N.Edit}}\"><i"
                        + " class=\"fa fa-pencil\"></i></a>"
                        + " <a href=\"/{{branch}}/environment/{{i.id}}/delete\" onclick=\"return confirm('{{N.Delete}}?');\""
                        + " class=\"btn btn-xs btn-danger\" title=\"{{N.Delete}}\"><i class=\"fa fa-trash-o\"></i></a>"
                        + " <a href=\"/{{branch}}/mt/{{i.id}}\" class=\"btn btn-xs btn-default\">" + n("MonitoredTargets") + " ({{i.nr1}})</a>"
                        + " <a href=\"/{{branch}}/alert/{{i.id}}\" class=\"btn btn-xs btn-default\">" + n("Alerts") + " ({{i.nr2}})</a>"
                        ));
        put("table", new TableComponent("wauto", cols, model, "envs").sort(0));
    }

    private int nr1(Environment env) {
        return mtDAO().count(env.getId());
    }

    private int nr2(Environment env) {
        int ret = 0;
        var groups = alertGroupDAO().load(env.getId());
        for (AlertGroup g : groups) {
            ret += g.getRules().stream().filter(i -> i.isActive()).count();
        }
        return ret;
    }
    
    @Override
    protected IBranch b() {
        return () -> {
            String b = ctx.pathParam("branch");
            return StringService.isNullOrEmpty(b) ? "master" : b;
        };
    }
}
