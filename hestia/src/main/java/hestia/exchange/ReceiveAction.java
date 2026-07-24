package hestia.exchange;

import org.pmw.tinylog.Logger;

import hestia.web.base.HAction;
import spark.utils.StringUtils;

// POST
public class ReceiveAction extends HAction {

    @Override
    protected void execute() {
        Logger.info("ReceiveAction | " + ctx.fullPath());
        
        String customerKey = ctx.pathParam("customerkey");
        String tag = ctx.pathParam("tag");
        boolean windowsEncoding = "win".equals(ctx.queryParam("enc")); // If caller is Windows use ?enc=win, otherwise not.
        String body;
        if (windowsEncoding) {
            body = StringUtils.toString(ctx.req.bodyAsBytes(), "windows-1252");
        } else {
            body = ctx.body();
        }
        
        new ExchangeService().receive(customerKey, tag, body);
    }
}
