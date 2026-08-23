package hestia.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.web.image.IBinaryDataLoader;

public class Downloader {

    private Downloader() {
    }
    
    public static void download(String fileUrl, Duration timeout, File destination) {
        IBinaryDataLoader.download(fileUrl, timeout, destination);
    }
    
    public static void extractTarGz(Path sourceTarGz, Path targetDir) throws IOException {
        try (InputStream fileIn = Files.newInputStream(sourceTarGz);
                BufferedInputStream buffIn = new BufferedInputStream(fileIn);
                GzipCompressorInputStream gzIn = new GzipCompressorInputStream(buffIn);
                TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn)) {
            TarArchiveEntry entry;
            while ((entry = tarIn.getNextEntry()) != null) {
                // Pfadtraversierung verhindern (Zip Slip Vulnerability Schutz)
                Path targetPath = targetDir.resolve(entry.getName()).normalize();
                if (!targetPath.startsWith(targetDir.normalize())) {
                    throw new IOException("Bad entry: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    // Falls Unterordner existieren, diese vorher anlegen
                    Files.createDirectories(targetPath.getParent());

                    // Datei direkt auf die Festplatte streamen
                    Files.copy(tarIn, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
    
    public static void makeExecutable(Path binaryPath) throws IOException {
        FileService.makeExecutable(binaryPath);
    }

    public static void copyFileToFile(File fromFile, File toFile) {
        FileService.copyFileToFile(fromFile, toFile);
    }
}
