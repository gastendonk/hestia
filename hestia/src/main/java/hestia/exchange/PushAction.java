package hestia.exchange;

import hestia.base.HAction;

public class PushAction extends HAction {

    @Override
    protected void execute() {
        new ExchangeService().push();
        
        ctx.redirect("/");
    }
}
