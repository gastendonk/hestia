package hestia.git;

/**
 * Indicates that repository data could not be read or written.
 */
public class RepositoryPersistenceException extends RuntimeException {

    public RepositoryPersistenceException(String message) {
        super(message);
    }

    public RepositoryPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
