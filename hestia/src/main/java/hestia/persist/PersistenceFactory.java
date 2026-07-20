package hestia.persist;

import hestia.otc.MTCustomerPersistence;
import hestia.otc.MTGitPersistence;
import hestia.otc.MonitoredTarget;
import hestia.prometheus.alert.AlertGroup;
import hestia.prometheus.alert.AlertGroupCustomerPersistence;
import hestia.prometheus.alert.AlertGroupGitPersistence;
import hestia.prometheus.alert.AlertRule;
import hestia.prometheus.alert.AlertRuleCustomerPersistence;
import hestia.prometheus.alert.AlertRuleGitPersistence;

public class PersistenceFactory {
    private final Committer committer;

    public PersistenceFactory() {
        committer = null;
//        committer = message -> {
//            // TODO
//        };
    }

    // Persistence objects must be created at every call!
    
    public Persistence<MonitoredTarget> monitoredTarget() {
        return committer != null ? new MTGitPersistence(committer) : new MTCustomerPersistence();
    }

    public Persistence<AlertGroup> alertGroup() {
        return committer != null ? new AlertGroupGitPersistence(committer) : new AlertGroupCustomerPersistence();
    }

    public Persistence<AlertRule> alertRule() {
        return committer != null ? new AlertRuleGitPersistence(committer) : new AlertRuleCustomerPersistence();
    }
}
