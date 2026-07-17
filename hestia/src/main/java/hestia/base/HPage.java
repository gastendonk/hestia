package hestia.base;

import github.soltaufintel.amalia.web.action.Page;
import hestia.HestiaWebapp;

public abstract class HPage extends Page {

    protected final String n(String key) {
        return NLS.get(HestiaWebapp.config.getLanguage(), key);
    }
    
    protected final void header(String title) {
        put("header", esc(title));
        put("title", esc(title) + " - Hestia");
    }
}
