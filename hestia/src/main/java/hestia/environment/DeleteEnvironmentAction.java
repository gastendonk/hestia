package hestia.environment;

import hestia.base.HAction;

public class DeleteEnvironmentAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        if (mtDAO().count(id) > 0 || alertGroupDAO().count(id) > 0) {
            throw new RuntimeException(n("CantDeleteEnvironment"));
        }
        environmentDAO().delete(id);
        
        backToStartpage();
    }
}
