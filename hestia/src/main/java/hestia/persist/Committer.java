package hestia.persist;

public interface Committer {

    void commit(String message);
}
