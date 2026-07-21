package hestia.exchange;

import hestia.base.HAction;

// POST
public class ReceiveAction extends HAction {

    @Override
    protected void execute() {
        String tag = ctx.pathParam("tag");
        
        new ExchangeService().receive(tag, ctx.body());
    }
}
