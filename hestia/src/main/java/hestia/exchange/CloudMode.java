package hestia.exchange;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.route.RouteDefinitions;

public class CloudMode extends RouteDefinitions {

    @Override
    public void routes() {
        post("/x/receive/:tag", ReceiveAction.class);
        get("/x/serve/:branch/:key", ServeAction.class);
        Logger.info("cloud mode");
    }
}
