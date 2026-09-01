package hestia.prometheus.alert.rule;

import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import hestia.HestiaWebapp;
import hestia.base.IBranch;
import hestia.environment.Environment;
import hestia.otc.model.MonitoredTarget;
import hestia.prometheus.alert.AlertGroup;
import hestia.prometheus.alert.AlertGroupDAO;

public class AlertRuleTemplateService {
    private int created;
    private int updated;
    private Environment env;
    
    public void applyAlertRuleTemplates(IBranch branch, String environmentId) {
        created = 0;
        updated = 0;
        var envDAO = HestiaWebapp.config.environmentDAO(branch);
        var mtDAO = HestiaWebapp.config.mtDAO(branch);
        AlertGroupDAO dao = HestiaWebapp.config.alertGroupDAO(branch);

        env = envDAO.loadOne(environmentId);
        List<AlertGroup> sources = dao.load("templates");
        List<AlertGroup> targets = new ArrayList<>(dao.load(environmentId));
        List<MonitoredTarget> mtlist = mtDAO.load(environmentId);
        
        for (AlertGroup s : sources) {
            String name = r(s.getName()).replace(" ", "_");
            AlertGroup t = findGroup(targets, name);
            if (t == null) {
                t = new AlertGroup();
                t.setId(IdGenerator.createId25());
                t.setName(name);
                targets.add(t);
            }
            copyRules(s, t, mtlist);
        }
        dao.saveAndCommit(environmentId, targets, "Alert rule templates applied");
        Logger.info("created rules: " + created + ", updated rules: " + updated);
    }
    
    private AlertGroup findGroup(List<AlertGroup> list, String name) {
        for (AlertGroup g : list) {
            if (g.getName().equalsIgnoreCase(name)) {
                return g;
            }
        }
        return null;
    }
    
    private void copyRules(AlertGroup s, AlertGroup t, List<MonitoredTarget> mtlist) {
        for (AlertRule sr : s.getRules()) {
            for (MonitoredTarget mt : mtlist) {
                if (mt.equalMTTYPE(sr.getMttype())) {
                    String id = mt.replace(sr.getAlert());
                    AlertRule x = findRule(t.getRules(), id);
                    if (x == null) {
                        AlertRule tr = sr.copy();
                        updateFields(mt, tr, tr);
                        tr.setAlert(id);
                        t.getRules().add(tr);
                        created++;
                    } else {
                        updateFields(mt, sr, x);
                        updated++;
                    }
                }
            }
        }
    }
    
    private AlertRule findRule(List<AlertRule> rules, String alert) {
        for (AlertRule r : rules) {
            if (r.getAlert().equalsIgnoreCase(alert)) {
                return r;
            }
        }
        return null;
    }
    
    private void updateFields(MonitoredTarget mt, AlertRule source, AlertRule target) {
        target.setSummary(r(mt.replace(source.getSummary())));
        target.setDescription(r(mt.replace(source.getDescription())));
        target.setExpr(r(mt.replace(source.getExpr())));
        target.setChannel(r(mt.replace(source.getChannel())));
        target.setEscalationChannel(r(mt.replace(source.getEscalationChannel())));
    }
    
    private String r(String text) {
        return text
                .replace("{customer}", env.getCustomer().toLowerCase())
                .replace("{CUSTOMER}", env.getCustomer().toUpperCase())
                .replace("{environment}", env.getName());
    }
}
