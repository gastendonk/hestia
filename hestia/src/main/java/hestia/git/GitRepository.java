package hestia.git;

import java.io.File;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.git.Repository;
import github.soltaufintel.amalia.git.RepositoryDefinition;
import hestia.base.IRepository;

public class GitRepository implements IRepository {
    private final File folder;
    private final String author;
    private final String mail;
    private final Repository repo;

    public GitRepository(String url, String user, String mail, String password, File baseFolder, String branch) {
        author = user;
        this.mail = mail;
        folder = new File(baseFolder, branch);
        var rd = new RepositoryDefinition() {
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
        };
        repo = new Repository(rd);
    }

    @Override
    public File getFile(String file) {
        var f = new File(folder, file);
        Logger.info("getFile: " + f.getAbsolutePath());
        return f;
    }

    @Override
    public String load(String file) {
        if (!folder.isDirectory()) {
            // TODO Wie oft soll ich pullen?
            folder.getParentFile().mkdirs(); // TODO amalia-git
            repo.pull();
        }
        return FileService.loadPlainTextFile(getFile(file));
    }

    @Override
    public void save(String file, String content, String commitMessage) {
        var f = getFile(file);
        FileService.savePlainTextFile(f, content);
        repo.commit(commitMessage, author, mail);
        Logger.info(f.getAbsolutePath() + " -> commit: " + commitMessage); // XXX DEBUG
    }

    public Repository getRepo() {
        return repo;
    }
}
