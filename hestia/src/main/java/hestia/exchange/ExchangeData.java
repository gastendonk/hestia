package hestia.exchange;

import java.io.File;
import java.util.Map;

import github.soltaufintel.amalia.base.FileService;

public class ExchangeData {
    /** key: filename, value: file content as JSON */
    private Map<String, String> files;

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }
    
    public void put(File file) {
        files.put(dn(file), FileService.loadPlainTextFile(file));
    }

    private String dn(File file) {
        return file.getParentFile().getName() + "/" + file.getName();
    }
}
