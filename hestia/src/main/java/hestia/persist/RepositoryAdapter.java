package hestia.persist;

import java.io.File;

public abstract class RepositoryAdapter implements IRepository {

    @Override
    public File getFile(String file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String load(String file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(String file, String content, String commitMessage) {
        throw new UnsupportedOperationException();
    }
}
