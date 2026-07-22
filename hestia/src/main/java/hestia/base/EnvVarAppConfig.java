package hestia.base;

import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.web.config.AppConfig;

public class EnvVarAppConfig extends AppConfig implements IConfig {

    @Override
    protected String load(String dn) {
        return "";
    }

    @Override
    public String get(String key, String defaultValue) {
        return switch (key) {
        case "app.name" -> getenv("APPNAME", "Hestia");
        case "port" -> getenv("PORT", "8080");
        case "development" -> getenv("DEVELOPMENT", "false");
        default -> getenv(key, defaultValue);
        };
    }

    public static String getenv(String key, String defaultValue) {
        String value = System.getenv(key);
        return StringService.isNullOrEmpty(value) ? defaultValue : value;
    }
}
