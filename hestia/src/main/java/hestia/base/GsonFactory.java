package hestia.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import hestia.otc.model.MonitoredTarget;
import hestia.otc.model.MonitoredTargetTypeAdapter;

/**
 * Creates the Gson instance used for repository persistence.
 */
public final class GsonFactory {

    private GsonFactory() {
    }

    /**
     * Creates a configured Gson instance.
     *
     * @return the configured Gson instance
     */
    public static Gson create() {
        return new GsonBuilder()
                .registerTypeAdapter(
                        MonitoredTarget.class,
                        new MonitoredTargetTypeAdapter()
                )
                .setPrettyPrinting()
                .create();
    }
}
