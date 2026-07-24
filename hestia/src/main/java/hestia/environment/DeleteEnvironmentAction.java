package hestia.environment;

import hestia.HestiaWebapp;
import hestia.web.base.HAction;

public class DeleteEnvironmentAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException("Delete not allowed");
        }
        if (mtDAO().count(id) > 0 || alertGroupDAO().count(id) > 0) {
            throw new RuntimeException(n("CantDeleteEnvironment"));
        }
        environmentDAO().delete(id);
        
        if ("e".equals(ctx.queryParam("r"))) {
            ctx.redirect("/" + b().getBranch() + "/environments");
        } else {
            backToStartpage();
        }
    }
}
