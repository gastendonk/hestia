package hestia.exchange;

import hestia.HestiaWebapp;
import hestia.git.GitRepository;
import hestia.persist.IRepository;
import hestia.web.base.HPage;

/**
 * Delivery: Push data from Burg to cloud instance
 */
public class DeliverPage extends HPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String tag = ctx.formParam("tag");
            
            new ExchangeService().push(b(), tag);
            
            backToStartpage();
        } else {
            display();
        }
    }

    private void display() {
        IRepository irepo = HestiaWebapp.config.getRepository(b());
        if (irepo instanceof GitRepository repo) {
            if (HestiaWebapp.config.getCloudInstance() == null) {
                throw new RuntimeException("Not possible because there is not cloud instance defined.");
            }
            var tags = repo.getRepo().getTagNames();
            var tag = repo.calculateNextTag(0);
            if ("k0".equals(tag)) {
                tag = null;
            }
            combobox("tags", tags, tag, false);
            header(n("Auslieferung"));
        } else {
            throw new RuntimeException("Not possible without a Git repo.");
        }
    }
}
