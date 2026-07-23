package hestia.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.pmw.tinylog.Logger;

public class Downloader {

    private Downloader() {
    }
    
    // TODO amalia-http
    public static void download(String fileUrl, Duration timeout, File destination) {
        try {
            long start = System.currentTimeMillis();
            destination.getParentFile().mkdirs();
            HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(fileUrl)).timeout(timeout).GET()
                    .build();
            HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(destination.toPath(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
            if (response.statusCode() != 200) {
                throw new IOException("Download error! HTTP status: " + response.statusCode());
            } else {
                Logger.info("download time: " + (System.currentTimeMillis() - start) + "ms");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    
    public static void makeExecutable(Path binaryPath) throws IOException { // 755
        Set<PosixFilePermission> permissions = Set.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ,
                PosixFilePermission.OTHERS_EXECUTE
        );
        Files.setPosixFilePermissions(binaryPath, permissions);
    }
}
