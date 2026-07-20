package hestia.exchange;

import hestia.base.HAction;

public class PullAction extends HAction {

    @Override
    protected void execute() {
        new ExchangeService().pull();
        
        ctx.redirect("/");
    }
}
