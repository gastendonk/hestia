package hestia.exchange;

import java.util.TreeSet;

import hestia.HestiaWebapp;
import hestia.environment.Environment;
import hestia.environment.EnvironmentDAO;
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
            String customerKey = ctx.formParam("customerKey");
            String tag = ctx.formParam("tag");
            if (customerKey == null || customerKey.indexOf(": ") < 0) {
                throw new RuntimeException("Please select customer key");
            }

            new ExchangeService().push(b(), customerKey.substring(customerKey.indexOf(": ") + 2), tag);
            
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
            var customerKeys = getCustomerKeys(irepo);
            combobox("customerKeys", customerKeys, customerKeys.iterator().next(), false);
            combobox("tags", tags, tag, false);
            header(n("Auslieferung"));
        } else {
            throw new RuntimeException("Not possible without a Git repo."); // TODO doch es ist moeglich!
        }
    }
    
    private TreeSet<String> getCustomerKeys(IRepository repo) {
        var ret = new TreeSet<String>();
        for (Environment env : new EnvironmentDAO(repo).load()) {
            ret.add(env.getCustomer() + ": " + env.getCustomerKey());
        }
        if (ret.isEmpty()) {
            throw new RuntimeException("There are no customer keys.");
        }
        return ret;
    }
}
