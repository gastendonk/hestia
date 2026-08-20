package hestia.otc;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.base.StringService;
import hestia.HestiaWebapp;
import hestia.otc.model.Database;
import hestia.otc.model.DatabaseType;
import hestia.otc.model.Definition;
import hestia.otc.model.MonitoredTarget;
import hestia.otc.model.Server;
import hestia.otc.model.ServerType;
import hestia.otc.model.Site;
import hestia.web.base.HPage;

public class AddMTPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        String m = ctx.queryParam("m");

        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        if (isPOST()) {
            save(id, m);
        } else {
            display(id, m);
        }
    }

    private void display(String id, String m) {
        put("id", esc(id));
        put("m", esc(m));
        put("f3value", "");
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
                put("f3value", "METRICS"); // Eingabeerleichterung
            } else {
                header(n("AddMTPostgresDB"));
                put("f3value", "metrics"); // Eingabeerleichterung
            }
            put("f2label", n("Host"));
            put("f3label", n("User"));
            put("f4label", n("Password"));
        }
    }

    private void save(String id, String m) {
        String f1 = ctx.formParam("f1");
        String f2 = ctx.formParam("f2");
        if (StringService.isNullOrEmpty(f1) && "site".equals(m)) {
            f1 = f2.replace("http://", "").replace("https://", ""); // Eingabeerleichterung
        }
        if (StringService.isNullOrEmpty(f1)) {
            throw new RuntimeException("Please enter name");
        }
        MonitoredTarget mt;
        if ("linux".equals(m)) {
            Server s = new Server();
            s.setType(ServerType.LINUX);
            s.setId(IdGenerator.createId25());
            s.setName(f1);
            s.setHost(f2);
            s.setPath(ctx.formParam("f3"));
            if (StringService.isNullOrEmpty(s.getHost()) && !StringService.isNullOrEmpty(s.getName())) {
                s.setHost(s.getName() + ":9100"); // Eingabeerleichterung
            }
            mt = s;
        } else if ("site".equals(m)) {
            Site s = new Site();
            s.setId(IdGenerator.createId25());
            s.setName(f1);
            s.setUrl(f2);
            mt = s;
        } else if ("definition".equals(m)) {
            Definition s = new Definition();
            s.setId(IdGenerator.createId25());
            s.setName(f1);
            s.setDefinition(f2);
            mt = s;
        } else { // DB
            Database s = new Database();
            if ("oracle".equals(m)) {
                s.setType(DatabaseType.ORACLE);
            } else {
                s.setType(DatabaseType.POSTGRES);
            }
            s.setId(IdGenerator.createId25());
            s.setName(f1);
            s.setHost(f2);
            s.setUser(ctx.formParam("f3"));
            s.setPassword(ctx.formParam("f4"));
            mt = s;
        }
        mtDAO().insert(id, mt);
        ctx.redirect("/" + ctx.pathParam("branch") + "/mt/" + id);
    }
}
