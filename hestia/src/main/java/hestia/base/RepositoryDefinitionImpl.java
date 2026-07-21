package hestia.base;

import java.io.File;

import github.soltaufintel.amalia.git.RepositoryDefinition;

public class RepositoryDefinitionImpl implements RepositoryDefinition {
    private final String user;
    private final String password;
    private final String url;
    private final File folder;
    
    public RepositoryDefinitionImpl(String user, String password, String url, File folder) {
        this.user = user;
        this.password = password;
        this.url = url;
        this.folder = folder;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public File getLocalFolder() {
        return folder;
    }
}
