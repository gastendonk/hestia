package hestia.exchange;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    // Regex matcht ein 'k' gefolgt von einer reinen Zahl (z. B. "k1", "k42")
    private static final Pattern K_TAG_PATTERN = Pattern.compile("^k(\\d+)$");

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
            sortTags(tags);
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
    
    private void sortTags(List<String> tags) {
        tags.sort(Comparator.comparing((String tag) -> {
            if (tag != null) {
                Matcher matcher = K_TAG_PATTERN.matcher(tag);
                if (matcher.matches()) {
                    // Falls k+Zahl -> sortiere primaer nach dem Praefix "k" (Kategorie 0) 
                    // und sekundaer nach der Zahl als Long
                    return new Key(0, tag, Long.parseLong(matcher.group(1)));
                }
            }
            // Alle anderen Tags -> Kategorie 1, sortiert nach dem String selbst
            return new Key(1, tag, null);
        }));
    }

    // Hilfs-Record (Java 17 Feature) fuer den mehrstufigen Vergleich
    private record Key(int category, String rawTag, Long number) implements Comparable<Key> {
        @Override
        public int compareTo(Key other) {
            // 1. Nach Kategorie vergleichen ("k"-Tags zuerst)
            int catComp = Integer.compare(this.category, other.category);
            if (catComp != 0) {
                return catComp;
            }

            // 2. Innerhalb der "k"-Tags: Nach Zahl sortieren
            if (this.number != null && other.number != null) {
                return Long.compare(this.number, other.number);
            }

            // 3. Sonst: Normale alphabetische Sortierung
            if (this.rawTag == null) return -1;
            if (other.rawTag == null) return 1;
            return this.rawTag.compareToIgnoreCase(other.rawTag);
        }
    }
}
