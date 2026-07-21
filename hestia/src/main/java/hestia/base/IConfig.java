package hestia.base;

public interface IConfig {

    String get(String key, String defaultValue);
    
    default String get(String key) {
        return get(key, null);
    }
}
