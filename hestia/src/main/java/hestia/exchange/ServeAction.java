package hestia.exchange;

import hestia.base.HAction;

public class ServeAction extends HAction {
    private String r;
    
    @Override
    protected void execute() {
        String key = ctx.pathParam("key");
    
        r = new ExchangeService().serve(key);
    }
    
    @Override
    protected String render() {
        return r;
    }
}
