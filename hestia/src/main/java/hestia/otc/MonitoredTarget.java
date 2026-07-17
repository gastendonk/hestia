package hestia.otc;

/**
 * A monitored target can be a site, a server or a database.
 */
public interface MonitoredTarget {

    /**
     * @return job name
     */
    String getName();
    
    boolean isActive();
}
