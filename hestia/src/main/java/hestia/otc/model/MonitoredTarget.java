package hestia.otc.model;

/**
 * A monitored target can be a site, a server or a database.
 */
public interface MonitoredTarget {

    String getId();
    
    /**
     * @return job name
     */
    String getName();
    
    boolean isActive();
}
