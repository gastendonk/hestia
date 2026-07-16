package hestia.base;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HestiaBaseService {

    private HestiaBaseService() {
    }
    
    public static List<File> getFiles(File folder, String suffix) {
        List<File> ret = new ArrayList<>();
        if (folder != null && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(suffix)) {
                        ret.add(file);
                    }
                }
            }
        }
        return ret;
    }
}
