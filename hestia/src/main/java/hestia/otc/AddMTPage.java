package hestia.otc;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.base.StringService;
import hestia.base.HPage;

public class AddMTPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        String m = ctx.queryParam("m");

        if (isPOST()) {
            save(id, m);
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
        } else { // DB
            if ("oracle".equals(m)) {
                header(n("AddMTOracleDB"));
            } else {
                header(n("AddMTPostgresDB"));
            }
            put("f2label", n("Host"));
            put("f3label", n("User"));
            put("f4label", n("Password"));
        }
    }

    private void save(String id, String m) {
        var list = MonitoredTargetDAO.load(id);
        if (StringService.isNullOrEmpty(ctx.formParam("f1"))) {
            throw new RuntimeException("Please enter name");
        }
        if ("linux".equals(m)) {
            Server s = new Server();
            s.setType(ServerType.LINUX);
            s.setId(IdGenerator.createId25());
            s.setName(ctx.formParam("f1"));
            s.setHost(ctx.formParam("f2"));
            s.setPath(ctx.formParam("f3"));
            list.add(s);
        } else if ("site".equals(m)) {
            Site s = new Site();
            s.setId(IdGenerator.createId25());
            s.setName(ctx.formParam("f1"));
            s.setUrl(ctx.formParam("f2"));
            list.add(s);
        } else { // DB
            Database s = new Database();
            if ("oracle".equals(m)) {
                s.setType(DatabaseType.ORACLE);
            } else {
                s.setType(DatabaseType.POSTGRES);
            }
            s.setId(IdGenerator.createId25());
            s.setName(ctx.formParam("f1"));
            s.setHost(ctx.formParam("f2"));
            s.setUser(ctx.formParam("f3"));
            s.setPassword(ctx.formParam("f4"));
            list.add(s);
        }
        MonitoredTargetDAO.save(id, list);
        ctx.redirect("/mt/" + id);
    }
}
