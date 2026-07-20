package hestia.otc;

import github.soltaufintel.amalia.base.StringService;
import hestia.base.HPage;
import hestia.otc.model.Database;
import hestia.otc.model.MonitoredTarget;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.otc.model.Server;
import hestia.otc.model.Site;

public class EditMTPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id"); // environment
        String id2 = ctx.pathParam("id2"); // MonitoredTarget
        String m = ctx.queryParam("m");

        var list = MonitoredTargetDAO.load(id);
        MonitoredTarget mt = list.stream().filter(i -> i.getId().equals(id2)).findFirst().orElseThrow();
        
        if (isPOST()) {
            save(id, m, mt);
            MonitoredTargetDAO.save(id, list);
            ctx.redirect("/mt/" + id);
        } else {
            display(id, m, mt);
        }
    }

    private void display(String id, String m, MonitoredTarget mt) {
        put("id", esc(id));
        put("id2", esc(mt.getId()));
        put("m", esc(m));
        put("f1", esc(mt.getName()));
        if (mt instanceof Server s) {
            header(n("EditMTLinuxServer"));
            put("f2label", n("Host"));
            put("f3label", n("PathOptional"));
            put("f4label", "");
            put("f2", esc(s.getHost()));
            put("f3", esc(s.getPath()));
        } else if (mt instanceof Site s) {
            header(n("EditMTSite"));
            put("f2label", "URL");
            put("f3label", "");
            put("f4label", "");
            put("f2", esc(s.getUrl()));
        } else if (mt instanceof Database s) {
            if ("oracle".equals(m)) {
                header(n("EditMTOracleDB"));
            } else {
                header(n("EditMTPostgresDB"));
            }
            put("f2label", n("Host"));
            put("f3label", n("User"));
            put("f4label", n("Password"));
            put("f2", esc(s.getHost()));
            put("f3", esc(s.getUser()));
            put("f4", esc(s.getPassword()));
        } else {
            throw new RuntimeException("Unsupported MonitoredTarget type");
        }
    }

    private void save(String id, String m, MonitoredTarget mt) {
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
        } else {
            throw new RuntimeException("Unsupported MonitoredTarget type");
        }
    }
}
