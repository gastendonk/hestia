package hestia.exchange;

import hestia.base.HAction;

// POST
public class ReceiveAction extends HAction {

    @Override
    protected void execute() {
        String key = ctx.pathParam("key");
        
        new ExchangeService().receive(key, ctx.body());
    }
}
