package hestia.git;

import hestia.HestiaWebapp;
import hestia.base.HAction;

public class GitPullAction extends HAction {

    @Override
    protected void execute() {
        if (HestiaWebapp.config.isCustomer()) {
            throw new RuntimeException();
        }

// TODO        HestiaWebapp.config.pull();

        ctx.redirect("/" + ctx.pathParam("branch"));
    }
}
