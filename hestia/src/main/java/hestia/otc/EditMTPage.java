package hestia.otc;

import github.soltaufintel.amalia.base.StringService;
import hestia.HestiaWebapp;
import hestia.base.HPage;

public class EditMTPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id"); // environment
        String id2 = ctx.pathParam("id2"); // MonitoredTarget
        String m = ctx.queryParam("m");

        var p = HestiaWebapp.persistenceFactory.monitoredTarget();
        var mt = p.loadOne(id, null, id2);
        
        if (isPOST()) {
            setFields(id, m, mt);
            p.save(id, mt, false);
            
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
            header(n("oracle".equals(m) ? "EditMTOracleDB" : "EditMTPostgresDB"));
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

    private void setFields(String id, String m, MonitoredTarget mt) {
        String name = ctx.formParam("f1");
        if (StringService.isNullOrEmpty(name)) {
            throw new RuntimeException("Please enter name");
        }
        
        if (mt instanceof Server s) {
            s.setName(name);
            s.setHost(ctx.formParam("f2"));
            s.setPath(ctx.formParam("f3"));
        } else if (mt instanceof Site s) {
            s.setName(name);
            s.setUrl(ctx.formParam("f2"));
        } else if (mt instanceof Database s) {
            s.setName(name);
            s.setHost(ctx.formParam("f2"));
            s.setUser(ctx.formParam("f3"));
            s.setPassword(ctx.formParam("f4"));
        } else {
            throw new RuntimeException("Unsupported MonitoredTarget type");
        }
    }
}
