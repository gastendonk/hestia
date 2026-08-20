package hestia.otc.opts;

import hestia.web.base.HPage;

public class EditOtcOptsPage extends HPage {

    @Override
    protected void execute() {
        var opts = OtcOptsDAO.load();
                
        if (isPOST()) {
            opts.setPrometheusPort(ctx.formParam("pp"));
            opts.setPrometheusremotewrite(ctx.formParam("prometheusremotewrite"));
            opts.setTempo(ctx.formParam("tempo"));
            opts.setLoki(ctx.formParam("loki"));
            opts.setOtc(ctx.formParam("otc"));
            opts.setDebug("on".equals(ctx.formParam("debug")));
            opts.setCustomer(ctx.formParam("customer"));
            OtcOptsDAO.save(opts);

            ctx.redirect("/");
        } else {
            header(n("Options"));
            put("pp", esc(opts.getPrometheusPort()));
            put("prometheusremotewrite", esc(opts.getPrometheusremotewrite()));
            put("tempo", esc(opts.getTempo()));
            put("loki", esc(opts.getLoki()));
            put("otc", esc(opts.getOtc()));
            put("debug", opts.isDebug());
            put("customer", esc(opts.getCustomer()));
            
            var tv = n("typicalValue") + ": ";
            put("hint1", tv + "http://prometheus:9090/api/v1/write");
            put("hint2", tv + "tempo:4317");
            put("hint3", tv + "http://loki:3100/otlp");
            put("hint4", ""); // URL of cloud instance not known yet
        }
    }
}
