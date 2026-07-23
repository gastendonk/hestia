package hestia.git;

import github.soltaufintel.amalia.base.StringService;
import hestia.HestiaWebapp;
import hestia.persist.IRepository;
import hestia.web.base.HPage;

public class GitTagPage extends HPage {

    @Override
    protected void execute() {
        String tag = ctx.queryParam("tag");
        
        IRepository irepo = HestiaWebapp.config.getRepository(b());
        if (irepo instanceof GitRepository repo) {
            if (StringService.isNullOrEmpty(tag)) {
                tag = repo.calculateNextTag();
                put("tag", esc(tag));
                put("label", esc(n("taggen").replace("$t", tag)));
            } else {
                render = false;
                repo.tag(tag);
                backToStartpage();
            }
        } else {
            throw new RuntimeException("Can't tag without Git repo.");
        }
    }
}
