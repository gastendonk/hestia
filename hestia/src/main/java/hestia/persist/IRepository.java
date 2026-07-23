package hestia.persist;

import java.io.File;

public interface IRepository {
    
    File getFile(String file);
    
    void pull();

    String load(String file);

    void save(String file, String content, String commitMessage);
}
