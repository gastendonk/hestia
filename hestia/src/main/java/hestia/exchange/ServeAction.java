package hestia.exchange;

import org.pmw.tinylog.Logger;

import hestia.web.base.HAction;

public class ServeAction extends HAction {
    private String r;
    
    @Override
    protected void execute() {
        Logger.info("ServeAction | " + ctx.fullPath());
        String customerKey = ctx.pathParam("customerkey");
    
        r = new ExchangeService().serve(customerKey);
        if (r == null) {
            ctx.status(404);
        }
    }
    
    @Override
    protected String render() {
        return r;
    }
}
