package hestia.exchange;

import hestia.web.base.HAction;

public class ServeAction extends HAction {
    private String r;
    
    @Override
    protected void execute() {
        String key = ctx.pathParam("key");
    
        r = new ExchangeService().serve(key, () -> ctx.pathParam("branch"));
    }
    
    @Override
    protected String render() {
        return r;
    }
}
