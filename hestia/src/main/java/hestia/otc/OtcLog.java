package hestia.otc;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import github.soltaufintel.amalia.base.FileService;

public class OtcLog {
    private static final int QUEUE_SIZE = 10_000;
    private static final BlockingQueue<String> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private static File file;
    private static Logger logger;
    private static FileHandler fileHandler;
    private static Thread writerThread;
    
    private OtcLog() {
    }
    
    public static synchronized void init() {
        final int mb = 2;
        final int bytesLimit = mb * 1024 * 1024;
        
        file = new File(System.getProperty("java.io.tmpdir"), "otc.log");
        stopWriter();

        logger = Logger.getLogger("OTelCollectorFileLogger");
        logger.setUseParentHandlers(false); // VERHINDERT Konsolen- / Docker-Ausgabe!

        for (var handler : logger.getHandlers()) {
            logger.removeHandler(handler);
            handler.close();
        }
        queue.clear();
        
        try {
            // FileHandler(path, limitBytes, count, append)
            // count = 1 -> Es existiert STRENG GENAU 1 Datei
            fileHandler = new FileHandler(file.getAbsolutePath(), bytesLimit, 1, false);

            // Schlankes Format: Nur Uhrzeit + Nachricht
            fileHandler.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(java.util.logging.LogRecord record) {
                    return record.getMessage() + System.lineSeparator();
                }
            });

            logger.addHandler(fileHandler);
        } catch (IOException e) {
            org.pmw.tinylog.Logger.error(e, "Could not init OTC log file");
        }
        writerThread = new Thread(OtcLog::writeLoop);
        writerThread.setName("OTC-LogWriter");
        writerThread.setDaemon(true);
        writerThread.start();
    }

    public static void info(String line) {
        queue.offer(line);
    }

    private static void writeLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String line = queue.take();
                logger.info(line);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void stopWriter() {
        if (writerThread != null) {
            writerThread.interrupt();
            writerThread = null;
        }
        if (fileHandler != null) {
            fileHandler.flush();
            fileHandler.close();
            fileHandler = null;
        }
    }

    public static String load() {
        // TODO amalia: wenn in loadBinaryFile file null ist soll null geliefert werden
        return file == null ? "N/A" : FileService.loadPlainTextFile(file);
    }
}
