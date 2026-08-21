package hestia.otc;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import github.soltaufintel.amalia.base.FileService;

public class OtcLog {
    private static File file;
    private static Logger logger;

    private OtcLog() {
    }
    
    public static void init() {
        final int mb = 2;
        file = new File(System.getProperty("java.io.tmpdir"), "otc.log");

        logger = Logger.getLogger("OTelCollectorFileLogger");
        logger.setUseParentHandlers(false); // VERHINDERT Konsolen- / Docker-Ausgabe!

        try {
            int bytesLimit = mb * 1024 * 1024;

            // FileHandler(path, limitBytes, count, append)
            // count = 1 -> Es existiert STRENG GENAU 1 Datei
            FileHandler fileHandler = new FileHandler(file.getAbsolutePath(), bytesLimit, 1, false);

            // Schlankes Format: Nur Uhrzeit + Nachricht
            fileHandler.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(java.util.logging.LogRecord record) {
                    return record.getMessage() + System.lineSeparator();
                }
            });

            logger.addHandler(fileHandler);
        } catch (IOException e) {
            org.pmw.tinylog.Logger.error(e, "Konnte OTel FileHandler nicht initialisieren");
        }
    }

    public static void info(String line) {
        logger.info(line);
    }
    
    public static String load() {
        return FileService.loadPlainTextFile(file);
    }
}
