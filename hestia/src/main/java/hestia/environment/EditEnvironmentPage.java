package hestia.environment;

import hestia.base.HPage;

public class EditEnvironmentPage extends HPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        var env = EnvironmentDAO.load(id);
        
        if (isPOST()) {
            String name = ctx.formParam("name").toLowerCase();
            if (name.isBlank()) {
                throw new RuntimeException("Please enter name");
            }
            
            env.setName(name);
            env.setActive("on".equals(ctx.formParam("active")));
            EnvironmentDAO.save(env);
            
            ctx.redirect("/");
        } else {
            header(n("EditEnvironment"));
            put("name", esc(env.getName()));
            put("active", env.isActive());
        }
    }
}
