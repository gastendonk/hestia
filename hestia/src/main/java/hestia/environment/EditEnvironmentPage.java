package hestia.environment;

import hestia.HestiaWebapp;
import hestia.base.HPage;

public class EditEnvironmentPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        var env = EnvironmentDAO.load(id);
        
        if (isPOST()) {
            String name = ctx.formParam("name").toLowerCase().replace(" ", "");
            if (name.isBlank()) {
                throw new RuntimeException("Please enter name");
            }
            
            env.setName(name);
            env.setCustomer(ctx.formParam("customer"));
            env.setCustomerKey(ctx.formParam("customerKey"));
            env.setActive("on".equals(ctx.formParam("active")));
            EnvironmentDAO.save(env, false);
            
            ctx.redirect("/");
        } else {
            header(n("EditEnvironment"));
            put("name", esc(env.getName()));
            combobox("customers", HestiaWebapp.config.getCustomers(), env.getCustomer(), false);
            put("customerKey", esc(env.getCustomerKey()));
            put("active", env.isActive());
        }
    }
}
