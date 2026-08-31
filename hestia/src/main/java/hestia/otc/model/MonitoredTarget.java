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

    List<String> MTTYPES = List.of("SERVER/LINUX", "DATABASE/ORACLE", "DATABASE/POSTGRES", "SITE");
    
    default boolean equalMTTYPE(String mttype) {
        return getType2().equals(mttype);
    }
    
    MonitoredTarget copy();
    
    /**
     * Replace variables
     * @param text any field content of a MonitoredTarget
     * @return text with inserted variable contents
     */
    String replace(String text);
}
