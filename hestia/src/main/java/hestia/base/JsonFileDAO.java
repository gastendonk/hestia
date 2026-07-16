package hestia.base;

import java.io.File;
import java.util.List;

// TODO -> Amalia
public abstract class JsonFileDAO<T> {

    public List<T> load(File file) {
        return new JsonFile<T>(file).getList();
    }
    
    public void save(List<T> list, File file) {
        new JsonFile<T>(list).save(file);
    }
}
