package hestia.web;

import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import hestia.HestiaWebapp;
import hestia.base.IBranch;
import hestia.environment.Environment;
import hestia.git.GitRepository;
import hestia.otc.OtcProcess;
import hestia.persist.IRepository;
import hestia.web.base.HPage;

public class IndexPage extends HPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String branch = ctx.formParam("branch2");
            ctx.redirect("/" + branch);
            return;
        }
        
        var b = b();
        IRepository repo = HestiaWebapp.config.getRepository(b);
        List<Environment> envs = environmentDAO().load();
        OtcProcess otc = HestiaWebapp.otcProcess;
        boolean otcAlive = otc != null && otc.alive();
        String tag = getTag();
        
        displayGit(repo, b);
        displayEnvironments(envs);
        put("alive", otcAlive);
        put("tag", esc(tag));
        put("DeployBtn", "Deploy" + (DeployAction.isBURG() ? " (BURG)" : ""));
        put("dor", HestiaWebapp.config.isDeployOnReceive());
        put("pam", !StringService.isNullOrEmpty(HestiaWebapp.config.getPrometheusHost())
                || !StringService.isNullOrEmpty(HestiaWebapp.config.getAlertmanagerHost()));
        put("prgname", esc(HestiaWebapp.config.getTitle()));
    }
    
    private void displayGit(IRepository repo, IBranch b) {
        if (ctx.pathParam("branch") == null) {
            put("branch", "master");
        }
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
    }
    
    // see same code in EnvironmentsPage
    private void displayEnvironments(List<Environment> envs) {
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
        list.sort((a, b) -> a.get("name").toString().compareToIgnoreCase(b.get("name").toString()));
        var delete = " <a href=\"/{{branch}}/environment/{{i.id}}/delete\" onclick=\"return confirm('{{N.Delete}}?');\""
                + " class=\"btn btn-xs btn-danger\" title=\"{{N.Delete}}\"><i class=\"fa fa-trash-o\"></i></a>";
        if (HestiaWebapp.config.isCustomer()) {
            delete = "";
        }
        Cols cols = Cols.of( //
                new Col(n("Environment"), "{{if not i.active}}<span class=\"not-active\">{{/if}}{{i.name}}"
                        + "{{if not i.active}}</span>{{/if}}").sortable("name"), //
                new Col("<a href=\"/{{branch}}/environment/add\" class=\"btn btn-xs btn-primary\">{{N.NewEnvironment}}</a>"
                        + " <a href=\"/{{branch}}/alert/templates\" class=\"btn btn-xs btn-default\">Alert Rule Templates</a>",
                        "<a href=\"/{{branch}}/environment/{{i.id}}\" class=\"btn btn-xs btn-default\" title=\"{{N.Edit}}\"><i"
                                + " class=\"fa fa-pencil\"></i></a>"
                        + " <a href=\"/{{branch}}/alert/{{i.id}}/apply-templates\" class=\"btn btn-xs btn-default\""
                                + " title=\"{{N.ApplyTemplates}}...\"><i class=\"fa fa-exclamation\"></i></a>"
                        + delete
                        + " <a href=\"/{{branch}}/mt/{{i.id}}\" class=\"btn btn-xs btn-default mw1\">" + n("MonitoredTargets") + " ({{i.nr1}})</a>"
                        + " <a href=\"/{{branch}}/alert/{{i.id}}\" class=\"btn btn-xs btn-default mw2\">" + n("Alerts") + " ({{i.nr2}})</a>"
                        ).right());
        put("table", new TableComponent("!wauto table", cols, model, "envs"));
    }
    
    private String getTag() {
        IRepository irepo = HestiaWebapp.config.getRepository(b());
        if (irepo instanceof GitRepository repo) {
            return repo.calculateNextTag(1);
        }
        return null;
    }

    @Override
    protected IBranch b() {
        return () -> {
            String b = ctx.pathParam("branch");
            return StringService.isNullOrEmpty(b) ? "master" : b;
        };
    }
}
