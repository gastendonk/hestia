package hestia.exchange;

import hestia.base.HAction;

public class PushAction extends HAction {

    @Override
    protected void execute() {
        String tag = ctx.pathParam("tag");
        
        new ExchangeService().push(tag);
        
        ctx.redirect("/");
    }
}
