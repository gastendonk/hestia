package hestia.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;
import org.pmw.tinylog.Logger;

/**
 * Service for creating a temporary folder. This class also ensures that old temp folders will be deleted.
 */
public class TempDirService {
    private final String prefix;
    private int dayModifier = 0;
    
    /**
     * The temp dir must have a prefix. This is used for finding old temp folders.
     * @param prefix e.g. "foo_"
     */
    public TempDirService(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("prefix should not be empty!");
        }
        this.prefix = prefix;
    }
    
    public void setDayModifier(int dayModifier) {
        this.dayModifier = dayModifier;
    }
    
    public File createTempDir() {
        try {
            String currentPrefix = prefix + TempDirService.timestamp(dayModifier);
            File tempDir = Files.createTempDirectory(currentPrefix).toFile();
            cleanup(tempDir, currentPrefix);
            return tempDir;
        } catch (IOException e) {
            throw new RuntimeException("Error creating temporary directory", e);
        }
    }
    
    public static String timestamp() {
        return timestamp(0);
    }
    
    public static String timestamp(int dayModifier) {
        return LocalDateTime.now().plusDays(dayModifier).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_";
    }
    
    private void cleanup(File tempDir, String currentPrefix) {
        for (File d : tempDir.getParentFile().listFiles()) {
            if (d.isDirectory() && d.getName().startsWith(prefix) && !d.getName().startsWith(currentPrefix) && d.getName().compareTo(tempDir.getName()) < 0) {
                try {
                    FileUtils.deleteDirectory(d);
                } catch (IOException e) {
                    Logger.warn(e);
                }
            }
        }
    }
    
    public static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            try {
                FileUtils.deleteDirectory(folder);
            } catch (IOException e) {
                throw new RuntimeException("Error deleting folder!\n" + folder.getAbsolutePath(), e);
            }
        }
    }
}
