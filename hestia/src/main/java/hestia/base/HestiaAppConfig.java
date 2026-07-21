package hestia.base;

import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.web.config.AppConfig;

public class HestiaAppConfig extends AppConfig { // TODO rename

    @Override
    protected String load(String dn) {
        return "";
    }

    @Override
    public String get(String key, String pDefault) {
        return switch (key) {
        case "app.name" -> getenv("APPNAME", "Hestia");
        case "port" -> getenv("PORT", "8080");
        case "development" -> getenv("DEVELOPMENT", "false");
        default -> pDefault;
        };
    }

    public static String getenv(String key, String pDefault) {
        String value = System.getenv(key);
        return StringService.isNullOrEmpty(value) ? pDefault : value;
    }
}
