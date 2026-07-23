package hestia.persist;

import java.io.File;

import github.soltaufintel.amalia.base.FileService;

public class FileRepository implements IRepository {
    private final File folder;
    
    public FileRepository(File folder) {
        this.folder = folder;
    }

    @Override
    public File getFile(String file) {
        return new File(folder, file);
    }

    @Override
    public String load(String file) {
        return FileService.loadPlainTextFile(getFile(file));
    }

    @Override
    public void save(String file, String content, String commitMessage) {
        FileService.savePlainTextFile(getFile(file), content);
    }

    @Override
    public void pull() {
    }
}
