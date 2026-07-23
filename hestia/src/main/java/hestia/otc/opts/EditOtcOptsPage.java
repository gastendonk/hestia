package hestia.otc.opts;

import hestia.base.HPage;

public class EditOtcOptsPage extends HPage {

    @Override
    protected void execute() {
        var opts = OtcOptsDAO.load();
                
        if (isPOST()) {
            opts.setPrometheusremotewrite(ctx.formParam("prometheusremotewrite"));
            opts.setTempo(ctx.formParam("tempo"));
            opts.setLoki(ctx.formParam("loki"));
            opts.setOtc(ctx.formParam("otc"));
            opts.setDebug("on".equals(ctx.formParam("debug")));
            OtcOptsDAO.save(opts);

            ctx.redirect("/");
        } else {
            header(n("Options"));
            put("prometheusremotewrite", esc(opts.getPrometheusremotewrite()));
            put("tempo", esc(opts.getTempo()));
            put("loki", esc(opts.getLoki()));
            put("otc", esc(opts.getOtc()));
            put("debug", opts.isDebug());
            
            var tv = n("typicalValue") + ": ";
            put("hint1", tv + "http://prometheus:9090/api/v1/write");
            put("hint2", tv + "tempo:4317");
            put("hint3", tv + "http://loki:3100/otlp");
            put("hint4", ""); // URL of cloud instance not known yet
        }
    }
}
