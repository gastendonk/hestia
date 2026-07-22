package hestia.environment;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.base.StringService;
import hestia.base.FileRepository;
import hestia.base.IRepository;
import hestia.git.GitPersistenceTest;

public class EnvironmentsTest {
    private static final String f = GitPersistenceTest.BASEDIR;
    
    @Test
    public void insert() {
        // Prepare
        var folder = new File(f + "/insert");
        FileService.deleteFolder(folder);
        IRepository repo = new FileRepository(folder);
        
        // Test
        Environment env = new Environment();
        env.setId(IdGenerator.createId25());
        env.setName("dummy");
        env.setCustomer("C1");
        new EnvironmentDAO(repo).insert(env);
        
        // Verify
        repo = new FileRepository(folder);
        Assert.assertTrue(new File(folder, "environments/environments.json").isFile());
        var k = new EnvironmentDAO(repo).loadOne(env.getId());
        Assert.assertFalse(StringService.isNullOrEmpty(k.getCustomerKey()));
    }

    @Test
    public void update() {
        // Prepare
        var folder = new File(f + "/update");
        FileService.deleteFolder(folder);
        IRepository repo = new FileRepository(folder);
        Environment env = new Environment();
        env.setId(IdGenerator.createId25());
        env.setName("dummy");
        env.setCustomer("C1");
        new EnvironmentDAO(repo).insert(env);

        // Test
        var k = new EnvironmentDAO(repo).loadOne(env.getId());
        k.setCustomer("C2");
        new EnvironmentDAO(repo).update(k);

        // Verify
        var k2 = new EnvironmentDAO(repo).loadOne(env.getId());
        Assert.assertEquals("C2", k2.getCustomer());
    }
    
    @Test
    public void delete() {
        // Prepare
        var folder = new File(f + "/delete");
        FileService.deleteFolder(folder);
        IRepository repo = new FileRepository(folder);
        Environment env = new Environment();
        env.setId(IdGenerator.createId25());
        env.setName("dummy");
        env.setCustomer("C1");
        new EnvironmentDAO(repo).insert(env);

        // Test
        new EnvironmentDAO(repo).delete(env.getId());

        // Verify
        var k = new EnvironmentDAO(repo).loadOne(env.getId());
        Assert.assertNull(k);
    }
}
