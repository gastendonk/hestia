package hestia.git;

import hestia.HestiaWebapp;
import hestia.persist.IRepository;
import hestia.web.base.HAction;

public class GitTagAction extends HAction {

    @Override
    protected void execute() {
        String tag = ctx.pathParam("tag");
        
        IRepository irepo = HestiaWebapp.config.getRepository(b());
        if (irepo instanceof GitRepository repo) {
            repo.tag(tag);
        } else {
            throw new RuntimeException("Can't tag without Git repo.");
        }

        backToStartpage();
    }

}
