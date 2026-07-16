package hestia;

import github.soltaufintel.amalia.web.builder.WebAppBuilder;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import hestia.base.HestiaPageInitializer;

public class HestiaWebapp extends RouteDefinitions {
    public static final String VERSION = "0.1.0";
    
    @Override
    public void routes() {
        // TODO
    }

    public static void main(String[] args) {
        new WebAppBuilder(VERSION)
                .withTemplatesFolders(HestiaWebapp.class, "/templates")
                .withPageInitializer(new HestiaPageInitializer())
                .withRoutes(new HestiaWebapp())
                .build()
                .boot();
    }
}
