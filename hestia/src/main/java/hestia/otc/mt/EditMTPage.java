package hestia.otc.mt;

import github.soltaufintel.amalia.base.StringService;
import hestia.HestiaWebapp;
import hestia.otc.model.AbstractMonitoredTarget;
import hestia.otc.model.Database;
import hestia.otc.model.DatabaseType;
import hestia.otc.model.Definition;
import hestia.otc.model.MonitoredTarget;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.otc.model.Server;
import hestia.otc.model.Site;
import hestia.web.base.HPage;

public class EditMTPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id"); // environment
        String id2 = ctx.pathParam("id2"); // MonitoredTarget

        var dao = mtDAO();
        MonitoredTarget mt = dao.loadOne(id, id2);
        
        if (isPOST()) {
            save(id, mt, dao);
        } else {
            display(id, mt);
        }
    }

    private void display(String id, MonitoredTarget mt) {
        put("id", esc(id));
        put("id2", esc(mt.getId()));
        put("f1", esc(mt.getName()));
        put("f2textarea", false);
        String h;
        if (mt instanceof Server s) {
            h = "MTLinuxServer";
            put("f2label", n("Host"));
            put("f3label", n("PathOptional"));
            put("f4label", "");
            put("f2", esc(s.getHost()));
            put("f3", esc(s.getPath()));
        } else if (mt instanceof Site s) {
            h = "MTSite";
            put("f2label", "URL");
            put("f3label", "");
            put("f4label", "");
            put("f2", esc(s.getUrl()));
        } else if (mt instanceof Database s) {
            h = s.getType() == DatabaseType.ORACLE ? "MTOracleDB" : "MTPostgresDB";
            put("f2label", n("Host"));
            put("f3label", n("User"));
            put("f4label", n("Password"));
            put("f2", esc(s.getHost()));
            put("f3", esc(s.getUser()));
            put("f4", esc(s.getPassword()));
        } else if (mt instanceof Definition s) {
            h = "MTDefinition";
            put("f2label", "Definition");
            put("f3label", "");
            put("f4label", "");
            put("f2", esc(s.getDefinition()));
            put("f2textarea", true);
        } else {
            throw new RuntimeException("Unsupported MonitoredTarget type");
        }
        if (!HestiaWebapp.config.isCustomer()) {
            h = "Edit" + h;
        }
        header(n(h));
        put("active", mt.isActive());
    }

    private void save(String envId, MonitoredTarget mt, MonitoredTargetDAO dao) {
        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        if (StringService.isNullOrEmpty(ctx.formParam("f1"))) {
            throw new RuntimeException("Please enter name");
        }
        
        if (mt instanceof Server s) {
            s.setName(ctx.formParam("f1"));
            s.setHost(ctx.formParam("f2"));
            s.setPath(ctx.formParam("f3"));
        } else if (mt instanceof Site s) {
            s.setName(ctx.formParam("f1"));
            s.setUrl(ctx.formParam("f2"));
        } else if (mt instanceof Database s) {
            s.setName(ctx.formParam("f1"));
            s.setHost(ctx.formParam("f2"));
            s.setUser(ctx.formParam("f3"));
            s.setPassword(ctx.formParam("f4"));
        } else if (mt instanceof Definition s) {
            s.setName(ctx.formParam("f1"));
            s.setDefinition(ctx.formParam("f2"));
        } else {
            throw new RuntimeException("Unsupported MonitoredTarget type");
        }
        if (mt instanceof AbstractMonitoredTarget a) {
            a.setActive("on".equals(ctx.formParam("active")));
        }
        dao.update(envId, mt);
        
        ctx.redirect("/" + ctx.pathParam("branch") + "/mt/" + envId);
    }
}
