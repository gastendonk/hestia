package hestia.prometheus.silences;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import hestia.prometheus.silences.Silence.Matcher;

/**
 * Alertmanager from setup1 must be running
 */
public class PrometheusSilencesServiceTest {

    @Test
    public void loadSilences() {
        var sv = new PrometheusSilencesService("http://localhost:9093");
        
        String id = sv.createSilence("testcase", "--", 1, List.of(new Matcher("kat", "K-9")));
        List<Silence> silences = sv.getActiveSilences();
        sv.expireSilence(id);
        
        Assert.assertEquals(1, silences.size());
        Assert.assertEquals(0, sv.getActiveSilences().size());
    }
}
