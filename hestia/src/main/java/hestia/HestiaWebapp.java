package hestia;

import github.soltaufintel.amalia.web.builder.WebAppBuilder;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import hestia.base.HestiaPageInitializer;
import hestia.otc.process.OtcProcess;
import hestia.web.IndexPage;
import hestia.web.KillAction;

public class HestiaWebapp extends RouteDefinitions {
    public static final String VERSION = "0.1.0";
    public static OtcProcess otcProcess;
    
    @Override
    public void routes() {
        get("/", IndexPage.class);
        get("/otc/kill", KillAction.class);
    }

    public static void main(String[] args) {
        new WebAppBuilder(VERSION)
                .withTemplatesFolders(HestiaWebapp.class, "/templates")
                .withPageInitializer(new HestiaPageInitializer())
                .withRoutes(new HestiaWebapp())
                .build()
                .boot();
        otcProcess = new OtcProcess();
    }
}
