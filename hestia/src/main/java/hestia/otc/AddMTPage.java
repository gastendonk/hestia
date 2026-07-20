package hestia.otc;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.base.StringService;
import hestia.HestiaWebapp;
import hestia.base.HPage;

public class AddMTPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        String m = ctx.queryParam("m");

        if (isPOST()) {
            var mt = createMT(id, m);
            HestiaWebapp.persistenceFactory.monitoredTarget().save(id, mt, true);
            
            ctx.redirect("/mt/" + id);
        } else {
            display(id, m);
        }
    }

    private void display(String id, String m) {
        put("id", esc(id));
        put("m", esc(m));
        if ("linux".equals(m)) {
            header(n("AddMTLinuxServer"));
            put("f2label", n("Host"));
            put("f3label", n("PathOptional"));
            put("f4label", "");
        } else if ("site".equals(m)) {
            header(n("AddMTSite"));
            put("f2label", "URL");
            put("f3label", "");
            put("f4label", "");
        } else if ("oracle".equals(m) || "postgres".equals(m)) {
            header("oracle".equals(m) ? n("AddMTOracleDB") : n("AddMTPostgresDB"));
            put("f2label", n("Host"));
            put("f3label", n("User"));
            put("f4label", n("Password"));
        } else {
            throw new RuntimeException("Unsupported mode " + m);
        }
    }

    private MonitoredTarget createMT(String id, String m) {
        String name = ctx.formParam("f1");
        if (StringService.isNullOrEmpty(name)) {
            throw new RuntimeException("Please enter name");
        }
        
        if ("linux".equals(m)) {
            Server s = new Server();
            s.setType(ServerType.LINUX);
            s.setId(IdGenerator.createId25());
            s.setName(name);
            s.setHost(ctx.formParam("f2"));
            s.setPath(ctx.formParam("f3"));
            return s;
        } else if ("site".equals(m)) {
            Site s = new Site();
            s.setId(IdGenerator.createId25());
            s.setName(name);
            s.setUrl(ctx.formParam("f2"));
            return s;
        } else if ("oracle".equals(m) || "postgres".equals(m)) {
            Database s = new Database();
            s.setType("oracle".equals(m) ? DatabaseType.ORACLE : DatabaseType.POSTGRES);
            s.setId(IdGenerator.createId25());
            s.setName(name);
            s.setHost(ctx.formParam("f2"));
            s.setUser(ctx.formParam("f3"));
            s.setPassword(ctx.formParam("f4"));
            return s;
        } else {
            throw new RuntimeException("Unsupported mode " + m);
        }
    }
}
