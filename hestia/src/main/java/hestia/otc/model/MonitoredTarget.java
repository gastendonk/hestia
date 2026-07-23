package hestia.otc.model;

import hestia.persist.Identifiable;

/**
 * A monitored target can be a site, a server or a database.
 */
public interface MonitoredTarget extends Identifiable {

    /**
     * @return job name
     */
    String getName();
    
    boolean isActive();
}
