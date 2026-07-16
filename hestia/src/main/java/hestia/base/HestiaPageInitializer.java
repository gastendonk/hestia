package hestia.base;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Page;
import github.soltaufintel.amalia.web.action.PageInitializer;

public class HestiaPageInitializer extends PageInitializer {

    @Override
    public void initPage(Context ctx, Page page) {
        page.put("title", "Hestia");
    }
}
