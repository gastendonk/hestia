package hestia.otc;

public interface MonitoredTarget {

    String getTypeName();
    
    /**
     * @return job name
     */
    String getName();
    
    default String sort() {
        return getTypeName() + getName();
    }
}
