package hestia.prometheus.alert;

import github.soltaufintel.amalia.base.StringService;
import hestia.base.HPage;

public class EditAlertGroupPage extends HPage {

    @Override
    protected void execute() {
        String env = ctx.pathParam("env");
        String id = ctx.pathParam("id");

        var g = AlertGroupDAO.load(env, id);
        
        if (isPOST()) {
            save(env, g);
        } else {
            header(n("EditGroup"));
            put("env", esc(env));
            put("name", esc(g.getName()));
            putInt("limit", g.getLimit());
            put("interval", esc(g.getInterval()));
        }
    }

    private void save(String env, AlertGroup g) {
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
        AlertGroupDAO.save(env, g);

        ctx.redirect("/alert/" + env);
    }
}
