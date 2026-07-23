package hestia.git;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.junit.Assert;
import org.junit.Test;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.git.Repository;
import github.soltaufintel.amalia.git.RepositoryDefinition;
import hestia.HestiaWebapp;
import hestia.config.HestiaConfig;
import hestia.environment.Environment;
import hestia.environment.EnvironmentDAO;

public class GitPersistenceTest {
    public static final String BASEDIR = "/dat/hestia-testdata";
    private static final String mail = "test@example.com";

    // Ich muss ein Remote Repo und ein lokales Repo anlegen
    // Dann testen: pull, add, commit, push
    @Test
    public void solo() throws Exception {
        // ICH WILL DEN TEST ORDNER SEHEN KÖNNEN!
        File mainTempFolder = new File(BASEDIR + "/1_solo");
        FileService.deleteFolder(mainTempFolder);
        mainTempFolder.mkdirs();
        
        // REMOTE GIT REPO
        File remoteDir = new File(mainTempFolder, "1-remote-dir");
        Git.init().setDirectory(remoteDir).setBare(true).call();

        // LOCAL GIT REPO
        File localDir = new File(mainTempFolder, "2-local-dir");
        var rd = new RepositoryDefinition() {
            @Override
            public String getUser() {
                return "me";
            }
            
            @Override
            public String getPassword() {
                return "";
            }
            
            @Override
            public String getUrl() {
                return remoteDir.toURI().toString();
            }
            
            @Override
            public File getLocalFolder() {
                return localDir;
            }
        };
        var repo = new Repository(rd);
        repo.pull();
        
        // DATEI ANLEGEN, COMMITTEN UND PUSHEN
        File file = new File(localDir, "a/b/c.txt");
        FileService.savePlainTextFile(file, "my content");
        repo.commit("ich speichere", "me", mail);
        repo.push("me", "");
        
        // ---- VERIFY ----
        final var localDir2 = new File(mainTempFolder, "3-local-dir-verify");
        var rd2 = new RepositoryDefinition() {
            @Override
            public String getUser() {
                return "me";
            }

            @Override
            public String getPassword() {
                return "";
            }

            @Override
            public String getUrl() {
                return remoteDir.toURI().toString();
            }

            @Override
            public File getLocalFolder() {
                return localDir2;
            }
        };
        repo = new Repository(rd2);
        repo.pull();
        String c = FileService.loadPlainTextFile(new File(localDir2, "a/b/c.txt"));
        Assert.assertEquals("my content", c);
    }

    @Test
    public void dao() throws Exception {
        // ICH WILL DEN TEST ORDNER SEHEN KÖNNEN!
        File mainTempFolder = new File(BASEDIR + "/2_dao");
        FileService.deleteFolder(mainTempFolder);
        mainTempFolder.mkdirs();
        
        // REMOTE GIT REPO
        File remoteDir = new File(mainTempFolder, "1-remote-dir");
        Git.init().setDirectory(remoteDir).setBare(true).call();

        // LOCAL GIT REPO
        File localDir = new File(mainTempFolder, "2-local-dir");
        
        // DATEI ANLEGEN, COMMITTEN UND PUSHEN
        Environment env = new Environment();
        env.setId(IdGenerator.createId25());
        env.setName("test1");
        env.setCustomer("CU1");
        var gitrepo = new GitRepository(remoteDir.toURI().toString(), "me", mail, "", localDir, "master");
        new EnvironmentDAO(gitrepo).insert(env);
        gitrepo.getRepo().push("me", "");
        
        // ---- VERIFY ----
        localDir = new File(mainTempFolder, "3-local-dir-verify");
        gitrepo = new GitRepository(remoteDir.toURI().toString(), "me", mail, "", localDir, "master");
        var envs = new EnvironmentDAO(gitrepo).load();
        Assert.assertEquals(1, envs.size());
        Assert.assertEquals("test1", envs.get(0).getName());
    }
    
    @Test
    public void config() throws Exception {
        // ICH WILL DEN TEST ORDNER SEHEN KÖNNEN!
        File mainTempFolder = new File(BASEDIR + "/3_config");
        FileService.deleteFolder(mainTempFolder);
        mainTempFolder.mkdirs();
        
        // REMOTE GIT REPO
        File remoteDir = new File(mainTempFolder, "1-remote-dir");
        Git.init().setDirectory(remoteDir).setBare(true).call();

        // LOCAL GIT REPO
        final File localDir = new File(mainTempFolder, "2-local-dir");
        
        // DATEI ANLEGEN, COMMITTEN UND PUSHEN
        Environment env = new Environment();
        env.setId(IdGenerator.createId25());
        env.setName("test1");
        env.setCustomer("CU1");
        
        setup(remoteDir, localDir);
        var dao = HestiaWebapp.config.environmentDAO(() -> "master");
        dao.insert(env);
        Assert.assertTrue("1/ master/environments Ordner muss da sein", new File(localDir, "master/environments").exists());
        Assert.assertFalse("1/ environments Ordner darf nicht da sein", new File(localDir, "environments").exists());
        dao.push("me", "");
        
        // ---- VERIFY ----
        final var localDir2 = new File(mainTempFolder, "3-local-dir-verify");
        setup(remoteDir, localDir2);
        dao = HestiaWebapp.config.environmentDAO(() -> "master");
        var envs = dao.load();
        Assert.assertEquals(1, envs.size());
        Assert.assertEquals("test1", envs.get(0).getName());
        Assert.assertTrue("master/environments Ordner muss da sein", new File(localDir2, "master/environments").exists());
        Assert.assertFalse("environments Ordner darf nicht da sein", new File(localDir2, "environments").exists());
    }
    
    private void setup(File remoteDir, File folder) {
        HestiaConfig.configAccess = (key, dv) -> {
            return switch (key) {
            case "REPO" -> remoteDir.toURI().toString();
            case "REPOFOLDER" -> folder.getAbsolutePath();
            case "REPOUSER" -> "me";
            case "REPOMAIL" -> mail;
            case "REPOPASSWORD" -> "";
            default -> dv;
            };
        };
        HestiaWebapp.config = new HestiaConfig();
    }
}
