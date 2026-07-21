package hestia.git;

import hestia.HestiaWebapp;
import hestia.base.HAction;

public class GitPushAction extends HAction {

    @Override
    protected void execute() {
        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }

        HestiaWebapp.config.push();

        ctx.redirect("/" + ctx.pathParam("branch"));
    }
}
