package hestia.base;

import org.junit.Assert;
import org.junit.Test;

import hestia.HestiaWebapp;
import hestia.environment.EnvironmentDAO;
import hestia.exchange.ExchangeService;
import hestia.otc.model.MonitoredTargetDAO;
import hestia.prometheus.alert.AlertGroupDAO;

public class FoldersTest {

    @Test
    public void burg() {
        HestiaConfig.configAccess = (key, dv) -> {
            return switch (key) {
            case "REPO" -> "http://git/bla";
            case "REPOFOLDER" -> "data/repo";
            case "PULLREPO" -> "0";
            default -> dv;
            };
        };
        HestiaWebapp.config = new HestiaConfig();

        Assert.assertEquals("data/repo/environments/environments.json", EnvironmentDAO.file().toString().replace("\\", "/"));
        Assert.assertEquals("data/repo/monitoredtargets/abcd.json", MonitoredTargetDAO.file("abcd").toString().replace("\\", "/"));
        Assert.assertEquals("data/repo/alerts/abcd.json", AlertGroupDAO.file("abcd").toString().replace("\\", "/"));
    }

    @Test
    public void cloud() {
        initConfig2();

        var customerKey = "c1234";
        var tag = "k5678";
        Assert.assertEquals("data/c1234/k5678.json", ExchangeService.file(customerKey, tag).toString().replace("\\", "/"));
    }

    @Test
    public void customer() {
        initConfig2();

        Assert.assertEquals("data/environments/environments.json", EnvironmentDAO.file().toString().replace("\\", "/"));
        Assert.assertEquals("data/monitoredtargets/abcd.json", MonitoredTargetDAO.file("abcd").toString().replace("\\", "/"));
        Assert.assertEquals("data/alerts/abcd.json", AlertGroupDAO.file("abcd").toString().replace("\\", "/"));
    }

    private void initConfig2() {
        HestiaConfig.configAccess = (key, dv) -> {
            return switch (key) {
            case "DATAFOLDER" -> "data";
            default -> dv;
            };
        };
        HestiaWebapp.config = new HestiaConfig();
    }
}
