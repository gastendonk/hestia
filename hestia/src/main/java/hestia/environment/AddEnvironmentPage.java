package hestia.environment;

import github.soltaufintel.amalia.base.IdGenerator;
import hestia.base.HPage;

public class AddEnvironmentPage extends HPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String name = ctx.formParam("name").toLowerCase();
            if (name.isBlank()) {
                throw new RuntimeException("Please enter name");
            }
            
            Environment env = new Environment();
            env.setId(IdGenerator.createId25());
            env.setName(name);
            EnvironmentDAO.save(env);
            
            ctx.redirect("/");
        } else {
            header(n("NewEnvironment"));
        }
    }
}
