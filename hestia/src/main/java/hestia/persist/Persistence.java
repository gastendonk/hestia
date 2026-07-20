package hestia.persist;

import java.util.List;

public interface Persistence<T> {

    default List<T> load(String envId) {
        return load(envId, null);
    }
    
    List<T> load(String envId, String groupId);
    
    default T loadOne(String envId, String id) {
        return loadOne(envId, null, id);
    }
    
    T loadOne(String envId, String groupId, String id);
    
    default void save(String envId, T obj, boolean add) {
        save(envId, null, obj, add);
    }
    
    void save(String envId, String groupId, T obj, boolean add);

    default void delete(String envId, String id) {
        delete(envId, null, id);
    }
    
    void delete(String envId, String groupId, String id);
}
