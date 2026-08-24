package hestia.prometheus.queryalerts;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import hestia.HestiaWebapp;
import hestia.prometheus.silences.PrometheusSilencesService;
import hestia.prometheus.silences.Silence.Matcher;
import hestia.web.base.HPage;

public class DoSilencePage extends HPage {

    @Override
    protected void execute() {
        String alertname = ctx.pathParam("alertname");
        
        List<PrometheusResult> queryalerts = new PrometheusQueryAlertsService(HestiaWebapp.config.getPrometheusHost()).queryAlerts();
        var ao = queryalerts.stream().filter(i -> i.getAlertName().equals(alertname)).findFirst();
        if (ao.isEmpty()) {
            throw new RuntimeException("Alert does not exist");
        }
        var a = ao.get();

        if (isPOST()) {
            try {
                long end = Long.valueOf(ctx.formParam("end"));
                if (end < 1) {
                    end = 1;
                }
                
                var sv = new PrometheusSilencesService(HestiaWebapp.config.getAlertmanagerHost());
                List<Matcher> matchers = List.of(new Matcher("alertname", a.getAlertName()));
                sv.createSilence(a.getAlertName(), "Silence created by Hestia", end, matchers);
                
                ctx.redirect("/qas");
            } catch (NumberFormatException e) {
                throw new RuntimeException("Please enter a number greater 0.");
            }
        } else {
            header(n("DoSilence"));
            put("end", LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("")));
        }
    }
}
