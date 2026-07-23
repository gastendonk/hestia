package hestia.git;

import hestia.HestiaWebapp;
import hestia.web.base.HAction;

public class GitPushAction extends HAction {

    @Override
    protected void execute() {
        if (HestiaWebapp.config.getRepository(b()) instanceof GitRepository git) {
            git.push();
        }

        backToStartpage();
    }
}
