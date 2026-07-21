package hestia.base;

import java.io.File;

public interface IRepository {
    
    File getFile(String file);

    String load(String file);

    void save(String file, String content, String commitMessage);
}
