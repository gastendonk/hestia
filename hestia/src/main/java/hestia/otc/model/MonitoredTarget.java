package hestia.otc.model;

import java.util.List;

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
    
    String getType2();
    
    String getInfo();
    
    List<Class<? extends MonitoredTarget>> CLASSES = List.of(Database.class, Server.class, Site.class,
            Definition.class);
    
    MonitoredTarget copy();
    
    String replace(String text);
}
