package hestia.prometheus;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import hestia.prometheus.rules.AlertRulesFile;
import hestia.prometheus.rules.PrometheusRulesService;

public class PrometheusServiceTest {

    @Test
    public void loadAlertRulesFiles() {
        var rulesFolder = new File("development/setup1/prometheus/rules");

        var alertRulesFiles = new PrometheusRulesService().loadAlertRulesFiles(rulesFolder);

        // Verify
        Assert.assertEquals(1, alertRulesFiles.size());
        AlertRulesFile f = alertRulesFiles.get(0);
        Assert.assertEquals("alert-rules.yml", f.getName());
        Assert.assertEquals(1, f.getGroups().size());
        Assert.assertEquals("infrastruktur_alarme", f.getGroups().get(0).getName());
        Assert.assertEquals(1, f.getGroups().get(0).getRules().size());
    }
}
