package hestia.base;

import static github.soltaufintel.amalia.web.action.NLSService.loadDataMap;
import static github.soltaufintel.amalia.web.action.NLSService.loadRB;

import java.util.Map;

import com.github.template72.data.DataMap;

/**
 * National language support
 */
public class NLS {
    public static final Map<String, String> de = loadRB("de", "/de.rb", NLS.class);
    public static final Map<String, String> en = loadRB("en", "/en.rb", NLS.class);
    public static DataMap dataMap_de = loadDataMap(de);
    public static DataMap dataMap_en = loadDataMap(en);
    
    private NLS() {
    }

    public static Map<String, String> getProperties(String lang) {
        return "en".equals(lang) ? en : de;
    }
    
    public static String get(String lang, String key) {
        String text = getProperties(lang).get(key);
        return text == null ? key : text;
    }
}
