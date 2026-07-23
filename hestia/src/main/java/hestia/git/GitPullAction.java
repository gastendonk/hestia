package hestia.git;

import hestia.HestiaWebapp;
import hestia.web.base.HAction;

public class GitPullAction extends HAction {

    @Override
    protected void execute() {
        HestiaWebapp.config.getRepository(b()).pull();

        backToStartpage();
    }
}
