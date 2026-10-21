package hestia.otc.secret;

import hestia.web.base.HAction;

public class DeleteSecretAction extends HAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id"); // environment
        String id2 = ctx.pathParam("id2"); // Secret.name

        secretDAO().delete(id, id2);
     
        ctx.redirect("/" + ctx.pathParam("branch") + "/secret/" + id);
    }
}
