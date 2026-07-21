package hestia.prometheus.alert;

import github.soltaufintel.amalia.base.StringService;
import hestia.HestiaWebapp;
import hestia.base.HPage;

public class EditAlertGroupPage extends HPage {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        String id = ctx.pathParam("id");

        var dao = alertGroupDAO();
        var g = dao.loadOne(env, id);
        
        if (isPOST()) {
            save(env, g, dao);
        } else {
            header(n("EditGroup"));
            put("env", esc(env));
            put("name", esc(g.getName()));
            putInt("limit", g.getLimit());
            put("interval", esc(g.getInterval()));
        }
    }

    private void save(String env, AlertGroup g, AlertGroupDAO dao) {
        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }
        String name = ctx.queryParam("name").trim();
        if (name.isBlank()) {
            throw new RuntimeException("Please enter name");
        }

        g.setName(name);
        g.setInterval(ctx.queryParam("interval"));
        try {
            var limit = ctx.queryParam("limit");
            if (StringService.isNullOrEmpty(limit)) {
                g.setLimit(0);
            } else {
                g.setLimit(Integer.parseInt(limit));
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Limit must be a number");
        }
        dao.update(env, g);

        ctx.redirect("/" + ctx.pathParam("branch") + "/alert/" + env);
    }
}
