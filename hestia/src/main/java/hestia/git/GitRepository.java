package hestia.git;

import java.io.File;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.git.Repository;
import github.soltaufintel.amalia.git.RepositoryDefinition;
import hestia.persist.IRepository;

public class GitRepository implements IRepository {
    private final String mail;
    private final RepositoryDefinition rd;
    private final Repository repo;
    
    public GitRepository(String url, String user, String mail, String password, File baseFolder, String branch) {
        this.mail = mail;
        rd = new RepositoryDefinition() {
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
                return new File(baseFolder, branch);
            }
        };
        repo = new Repository(rd);
        repo.switchToBranch(branch);
    }

    @Override
    public File getFile(String file) {
        return new File(rd.getLocalFolder(), file);
    }

    @Override
    public String load(String file) {
        var folder = rd.getLocalFolder();
        if (!folder.isDirectory()) {
            // TODO Wie oft soll ich pullen?
            folder.getParentFile().mkdirs(); // TODO amalia-git
            repo.pull();
        }
        return FileService.loadPlainTextFile(getFile(file));
    }

    @Override
    public void save(String filename, String content, String commitMessage) {
        File file = getFile(filename);
        FileService.savePlainTextFile(file, content);
        repo.commit(commitMessage, rd.getUser(), mail);
    }

    public Repository getRepo() {
        return repo;
    }
    
    @Override
    public void pull() {
        Logger.info("pull | " + rd.getLocalFolder());
        try {
            repo.pull();
        } catch (Exception e) {
            Logger.error(e);
        }
    }
    
    @Override
    public void push() {
        Logger.info("push | " + rd.getLocalFolder());
        repo.push(rd.getUser(), rd.getPassword());
    }
    
    public String getUrl() {
        return rd.getUrl();
    }
}
