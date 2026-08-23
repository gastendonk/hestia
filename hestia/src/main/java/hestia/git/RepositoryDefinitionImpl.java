package hestia.git;

import java.io.File;

import github.soltaufintel.amalia.git.RepositoryDefinition;

public class RepositoryDefinitionImpl implements RepositoryDefinition {
    private final String url;
    private final String user;
    private final String password;
    private final File localFolder;
    
    public RepositoryDefinitionImpl(String url, String user, String password, File localFolder) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.localFolder = localFolder;
    }

    @Override
    public String getUrl() {
        return url;
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
    public File getLocalFolder() {
        return localFolder;
    }
}
