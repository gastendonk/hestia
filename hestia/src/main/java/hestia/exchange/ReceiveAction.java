package hestia.exchange;

import org.pmw.tinylog.Logger;

import hestia.web.base.HAction;

// POST
public class ReceiveAction extends HAction {

    @Override
    protected void execute() {
        Logger.info("ReceiveAction | " + ctx.fullPath());
        String customerKey = ctx.pathParam("customerkey");
        String tag = ctx.pathParam("tag");
        
        new ExchangeService().receive(customerKey, tag, ctx.body());
    }
}
