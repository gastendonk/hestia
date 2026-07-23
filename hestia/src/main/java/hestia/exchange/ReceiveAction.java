package hestia.exchange;

import hestia.web.base.HAction;

// POST
public class ReceiveAction extends HAction {

    @Override
    protected void execute() {
        String customerKey = ctx.pathParam("customerkey");
        String tag = ctx.pathParam("tag");
        
        new ExchangeService().receive(customerKey, tag, ctx.body());
    }
}
