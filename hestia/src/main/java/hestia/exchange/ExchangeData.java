package hestia.exchange;

import java.io.File;
import java.util.Map;

import github.soltaufintel.amalia.base.FileService;

public class ExchangeData {
    private String tag;
    /** key: filename, value: file content as JSON */
    private Map<String, String> files;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }
    
    public void put(File file) {
        var content = FileService.loadPlainTextFile(file);
        put(file, content);
    }
    
    public void put(File file, String content) {
        var d = dn(file);
        if (content == null) {
            throw new RuntimeException("File not found or is empty: " + d);
        }
        files.put(d, content);
    }

    private String dn(File file) {
        return file.getParentFile().getName() + "/" + file.getName();
    }
}
