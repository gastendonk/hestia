package hestia.persist;

import java.util.List;

public abstract class AbstractJsonListSimpleDAO<T extends Identifiable> extends AbstractJsonListDAO<T> {
    private static final String UNUSED_ID = "";

    public AbstractJsonListSimpleDAO(IRepository gitRepository, Class<T> clazz) {
        super(gitRepository, clazz);
    }

    public List<T> load() {
        return super.load(UNUSED_ID);
    }

    public T loadOne(String id) {
        return super.loadOne(UNUSED_ID, id);
    }

    public void insert(T t) {
        super.insert(UNUSED_ID, t);
    }
    
    public void update(T environment) {
        super.update(UNUSED_ID, environment);
    }

    public void delete(String id) {
        super.delete(UNUSED_ID, id);
    }
}
