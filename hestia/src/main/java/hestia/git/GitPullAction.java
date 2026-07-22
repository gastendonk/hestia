package hestia.git;

import hestia.HestiaWebapp;
import hestia.base.HAction;

public class GitPullAction extends HAction {

    @Override
    protected void execute() {
        HestiaWebapp.config.getRepository(b()).pull();

        ctx.redirect("/" + ctx.pathParam("branch"));
    }
}
