package hestia.exchange;

import org.pmw.tinylog.Logger;

import hestia.web.base.HAction;
import spark.utils.StringUtils;

// POST
public class ReceiveAction extends HAction {

    @Override
    protected void execute() {
        Logger.info("ReceiveAction | " + ctx.fullPath() + " | windows-1252");
        String customerKey = ctx.pathParam("customerkey");
        String tag = ctx.pathParam("tag");
        String body = StringUtils.toString(ctx.req.bodyAsBytes(), "windows-1252");
        Logger.info("ReceiveAction | body: " + body);
        
        new ExchangeService().receive(customerKey, tag, body);
    }
}
