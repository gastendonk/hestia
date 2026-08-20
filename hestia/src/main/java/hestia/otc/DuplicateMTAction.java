package hestia.otc;

import github.soltaufintel.amalia.base.IdGenerator;
import hestia.HestiaWebapp;
import hestia.otc.model.Database;
import hestia.otc.model.MonitoredTarget;
import hestia.otc.model.Server;
import hestia.otc.model.Site;
import hestia.web.base.HAction;

public class DuplicateMTAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id"); // environment
        String id2 = ctx.pathParam("id2"); // MonitoredTarget

        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        var dao = mtDAO();
        MonitoredTarget m = dao.loadOne(id, id2);
        String idNeu = "_";
        if (m instanceof Server s) {
            var n = new Server();
            n.setHost(s.getHost());
            n.setName(s.getName());
            n.setPath(s.getPath());
            n.setType(s.getType());
            n.setId(IdGenerator.createId25());
            n.setActive(s.isActive());
            dao.insert(id, n);
            idNeu = n.getId();
        } else if (m instanceof Site s) {
            var n = new Site();
            n.setName(s.getName());
            n.setUrl(s.getUrl());
            n.setId(IdGenerator.createId25());
            n.setActive(s.isActive());
            dao.insert(id, n);
            idNeu = n.getId();
        } else if (m instanceof Database s) {
            var n = new Database();
            n.setHost(s.getHost());
            n.setName(s.getName());
            n.setUser(s.getUser());
            n.setPassword(s.getPassword());
            n.setId(IdGenerator.createId25());
            n.setActive(s.isActive());
            dao.insert(id, n);
            idNeu = n.getId();
        } else {
            throw new UnsupportedOperationException(m.getClass().getName());
        }

        ctx.redirect("/" + ctx.pathParam("branch") + "/mt/" + id + "/" + idNeu + "/edit");
    }
}
