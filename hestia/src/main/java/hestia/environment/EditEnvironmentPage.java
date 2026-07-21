package hestia.environment;

import hestia.HestiaWebapp;
import hestia.base.HPage;

public class EditEnvironmentPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        var dao = environmentDAO();
        var env = dao.loadOne(id);
        
        if (isPOST()) {
            String name = ctx.formParam("name").toLowerCase().replace(" ", "");
            if (name.isBlank()) {
                throw new RuntimeException("Please enter name");
            }
            
            env.setName(name);
            env.setCustomer(ctx.formParam("customer"));
            env.setCustomerKey(ctx.formParam("customerKey"));
            env.setActive("on".equals(ctx.formParam("active")));
            dao.update(env);
            
            ctx.redirect("/" + ctx.pathParam("branch"));
        } else {
            header(n("EditEnvironment"));
            put("name", esc(env.getName()));
            combobox("customers", HestiaWebapp.config.getCustomers(), env.getCustomer(), false);
            put("customerKey", esc(env.getCustomerKey()));
            put("active", env.isActive());
        }
    }
}
