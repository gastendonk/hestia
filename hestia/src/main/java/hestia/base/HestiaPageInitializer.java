package hestia.base;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Page;
import github.soltaufintel.amalia.web.action.PageInitializer;
import hestia.HestiaWebapp;

public class HestiaPageInitializer extends PageInitializer {

    @Override
    public void initPage(Context ctx, Page page) {
        page.put("title", "Hestia");
        page.put("VERSION", HestiaWebapp.VERSION);
        page.put("sortableJS", false);
        page.put("N", "en".equals(HestiaWebapp.config.getLanguage()) ? NLS.dataMap_en : NLS.dataMap_de);
        page.put("customer", true);
    }
}
