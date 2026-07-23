package hestia.exchange;

import org.pmw.tinylog.Logger;

import hestia.HestiaWebapp;
import hestia.git.GitRepository;
import hestia.persist.IRepository;
import hestia.web.base.HPage;

public class PushPage extends HPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String tag = ctx.formParam("tag");
            
            Logger.info("tag = " + tag);
            // TODO
//            new ExchangeService().push(tag);

            
            backToStartpage();
        } else {
            IRepository irepo = HestiaWebapp.config.getRepository(b());
            if (irepo instanceof GitRepository repo) {
                var tags = repo.getRepo().getTagNames();
                var tag = repo.calculateNextTag(0);
                if ("k0".equals(tag)) {
                    tag = null;
                }
                combobox("tags", tags, tag, false);
                header(n("Auslieferung"));
            }
        }
    }
}
