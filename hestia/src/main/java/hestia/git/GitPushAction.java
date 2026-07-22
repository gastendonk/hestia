package hestia.git;

import hestia.HestiaWebapp;
import hestia.base.HAction;

public class GitPushAction extends HAction {

    @Override
    protected void execute() {
        HestiaWebapp.config.getRepository(b()).push();

        ctx.redirect("/" + ctx.pathParam("branch"));
    }
}
