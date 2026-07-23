package hestia.persist;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import hestia.git.GitRepository;
import hestia.git.RepositoryPersistenceException;

/**
 * Base class for DAOs that store a complete list of objects in one JSON file.
 *
 * <p>Each modifying operation writes the complete JSON file and immediately
 * creates a local Git commit. Pushing commits is intentionally not handled by
 * this class.</p>
 *
 * @param <T> the stored object type
 */
public abstract class AbstractJsonListDAO<T extends Identifiable> {
    private final IRepository gitRepository;
    private final Gson gson = GsonFactory.create();
    private final Type listType;

    /**
     * Creates a new DAO.
     *
     * @param gitRepository the Git repository access
     * @param gson the configured Gson instance
     * @param branch the branch used by this DAO
     * @param elementType the stored element type
     */
    protected AbstractJsonListDAO(IRepository gitRepository, Class<T> elementType) {
        this.gitRepository = Objects.requireNonNull(gitRepository);
        this.listType = TypeToken.getParameterized(List.class, elementType).getType();
    }

    /**
     * Returns the file path for the given environment.
     *
     * @param environmentId the environment identifier
     * @return the repository-relative file path
     */
    public abstract String getPath(String environmentId);
    
    public File getFile(String environmentId) {
        return gitRepository.getFile(getPath(environmentId));
    }

    protected abstract String getItemNameForCommitMessage();

    public List<T> loadAll(Collection<String> environmentIdList) {
        List<T> ret = new ArrayList<>();
        for (String id : environmentIdList) {
            ret.addAll(load(id));
        }
        return ret;
    }
    
    /**
     * Loads all objects belonging to an environment.
     *
     * @param environmentId the environment identifier
     * @return an immutable list of stored objects
     */
    public List<T> load(String environmentId) {
        String path = getPath(environmentId);
        String json = gitRepository.load(path);
        if (json == null) {
            return List.of();
        }
        List<T> values = gson.fromJson(json, listType);
        if (values == null) {
            return List.of();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    /**
     * Loads one object by its identifier.
     *
     * @param environmentId the environment identifier
     * @param id the object identifier
     * @return the matching object
     * @throws RepositoryPersistenceException if no matching object exists
     */
    public T loadOne(String environmentId, String id) {
        return load(environmentId).stream()
                .filter(object -> Objects.equals(object.getId(), id))
                .findFirst().orElse(null);
    }

    /**
     * Inserts a new object and creates a local Git commit.
     *
     * @param environmentId the environment identifier
     * @param object the object to insert
     * @throws RepositoryPersistenceException if the identifier already exists
     */
    public void insert(String environmentId, T object) {
        Objects.requireNonNull(object);

        List<T> values = new ArrayList<>(load(environmentId));

        boolean alreadyExists = values.stream()
                .anyMatch(existing -> Objects.equals(existing.getId(), object.getId()));

        if (alreadyExists) {
            throw new RepositoryPersistenceException(
                    "Object with ID '%s' already exists in environment '%s'"
                            .formatted(object.getId(), environmentId)
            );
        }

        values.add(object);
        insertExtras(object, values);

        saveAndCommit(
                environmentId,
                values,
                "add " + getItemNameForCommitMessage()
        );
    }
    
    protected void insertExtras(T object, List<T> values) {
    }

    /**
     * Updates an existing object and creates a local Git commit.
     *
     * @param environmentId the environment identifier
     * @param object the replacement object
     * @throws RepositoryPersistenceException if the identifier does not exist
     */
    public void update(String environmentId, T object) {
        Objects.requireNonNull(object);

        List<T> values = new ArrayList<>(load(environmentId));

        for (int index = 0; index < values.size(); index++) {
            T existing = values.get(index);

            if (Objects.equals(existing.getId(), object.getId())) {
                values.set(index, object);

                saveAndCommit(
                        environmentId,
                        values,
                        "update " + getItemNameForCommitMessage()
                );

                return;
            }
        }

        throw new RepositoryPersistenceException(
                "Object with ID '%s' does not exist in environment '%s'"
                        .formatted(object.getId(), environmentId)
        );
    }

    /**
     * Deletes an object and creates a local Git commit.
     *
     * @param environmentId the environment identifier
     * @param id the identifier of the object to delete
     * @throws RepositoryPersistenceException if the identifier does not exist
     */
    public void delete(String environmentId, String id) {
        List<T> values = new ArrayList<>(load(environmentId));

        boolean removed = values.removeIf(
                object -> Objects.equals(object.getId(), id)
        );

        if (!removed) {
            throw new RepositoryPersistenceException(
                    "Object with ID '%s' does not exist in environment '%s'"
                            .formatted(id, environmentId)
            );
        }

        saveAndCommit(
                environmentId,
                values,
                "delete " + getItemNameForCommitMessage()
        );
    }

    /**
     * Serializes the given objects, writes the JSON file and creates a commit.
     *
     * @param environmentId the environment identifier
     * @param values the complete file content
     * @param commitMessage the commit message
     */
    private void saveAndCommit(String environmentId, List<T> values, String commitMessage) {
        String path = getPath(environmentId);

        try {
            String json = gson.toJson(values, listType);
            gitRepository.save(path, json, commitMessage);
        } catch (RuntimeException e) {
            throw new RepositoryPersistenceException("Could not write and commit JSON file: " + path, e);
        }
    }
    
    public void push(String user, String password) {
        if (gitRepository instanceof GitRepository g) {
            g.getRepo().push(user, password);
        } else {
            throw new RuntimeException("Can't call push() because it's a " + gitRepository.getClass().getName());
        }
    }
}
