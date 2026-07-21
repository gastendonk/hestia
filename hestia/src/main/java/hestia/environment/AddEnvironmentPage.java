package hestia.environment;

import github.soltaufintel.amalia.base.IdGenerator;
import hestia.HestiaWebapp;
import hestia.base.HPage;

public class AddEnvironmentPage extends HPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String name = ctx.formParam("name").toLowerCase().replace(" ", "");
            if (name.isBlank()) {
                throw new RuntimeException("Please enter name");
            }
            
            Environment env = new Environment();
            env.setId(IdGenerator.createId25());
            env.setName(name);
            env.setCustomer(ctx.formParam("customer"));
            EnvironmentDAO.save(env, true);
            
            ctx.redirect("/");
        } else {
            header(n("NewEnvironment"));
            combobox("customers", HestiaWebapp.config.getCustomers(), (String) null, false);
        }
    }
}
