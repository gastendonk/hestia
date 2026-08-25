package hestia.otc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.rest.REST;
import hestia.HestiaWebapp;

/**
 * Start and stop otelcol-contrib application
 */
public class OtcProcess {
    private static final Object LOCK = new Object();
    private Process p;
    private boolean checkpoint1;
    private boolean checkpoint2;
    private Thread logReader;
    private String log = "";
    
    public OtcProcess() {
        synchronized (LOCK) {
            if (!HestiaWebapp.config.isRun()) {
                Logger.info("!!  otc process not started because env var RUN is not 1");
                return;
            }
            var program = HestiaWebapp.config.getOtelcolContrib();
            if (!program.isFile()) {
                Logger.error("OTC can't be started! Program file not found: " + program.getAbsolutePath());
                return;
            }
            var configYaml = HestiaWebapp.config.getConfigYaml();
            if (!configYaml.isFile()) {
                Logger.error("OTC can't be started! Config file not found: " + configYaml.getAbsolutePath());
                // TODO Gaebe es denn Umgebungen?
                return;
            }
            String c = "--config=" + configYaml.getAbsolutePath();
            Logger.info("starting otc process... | " + program.getAbsolutePath() + " " + c);
            try {
                ProcessBuilder pb = new ProcessBuilder(program.getAbsolutePath(), c);
                pb.redirectErrorStream(true);
                p = pb.start();
                Logger.info("OTC process has pid " + p.pid() + ".");
                logs();
            } catch (IOException e) {
                Logger.error(e);
            }
        }
    }
    
    private void logs() {
        checkpoint1 = false;
        checkpoint2 = false;
//        OtcLog.init();
        logReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
//                    OtcLog.info(line);
                    
                    // Checkpoints prüfen
                    if (!checkpoint1 && line.contains("Everything is ready. Begin running and processing data")) {
                        checkpoint1 = true; // Erfolg: Der otc ist vollstaendig einsatzbereit!
                    }
                    if (!checkpoint2 && line.contains("health_check") && line.contains("ready")) {
                        checkpoint2 = true; // Erfolg: otc Health-Check-Erweiterung ist aktiv.
                    }
                }
            } catch (IOException e) {
                if (e.getMessage() == null || !e.getMessage().contains("Stream closed")) {
                    Logger.error(e, "Error loading otelcol-contrib log");
                }
            }
        });
        logReader.setName("OTC-LogReader");
        logReader.setDaemon(true);
        logReader.start();
    }

    public String getLog() {
        return log;
    }

    public boolean isCheckpoint1() {
        return checkpoint1;
    }

    public boolean isCheckpoint2() {
        return checkpoint2;
    }

    public long pid() {
        synchronized (LOCK) {
            return p == null ? -1 : p.pid();
        }
    }

    public boolean alive() {
        synchronized (LOCK) {
            boolean ret = p != null && p.isAlive();
            if (p != null && !p.isAlive()) {
                Logger.warn("exit value: " + p.exitValue());
            }
            return ret;
        }
    }

    /**
     * @return success: 200, otherwise error (-1: exception, -2: no process)
     */
    public int checkHealth() {
        synchronized (LOCK) {
            if (p == null) {
                return -2;
            }
            try {
                var r = new REST("http://localhost:13133/").get();
                int status = r.status();
                Logger.debug("health_check: [" + status + "] " + r.response());
                return status;
            } catch (Exception e) {
                Logger.error("health_check: " + e.getMessage());
                return -1;
            }
        }
    }

    public void kill() {
        if (p == null) {
            Logger.error("no process, can't kill");
            return;
        }
        synchronized (LOCK) {
            Logger.info("killing...");
            try {
                try {
                    logReader.interrupt();
                } catch (Exception ex) {
                    Logger.error(ex);
                }
                p.destroy();
                checkpoint1 = false;
                checkpoint2 = false;
                try {
                    // Wait up to 3 seconds for normal termination.
                    if (!p.waitFor(3, TimeUnit.SECONDS)) {
                        Logger.warn("OTC is not responding to SIGTERM. Forcing termination (SIGKILL)...");
                        p.destroyForcibly(); // Force hard termination (SIGKILL)
                        p.waitFor(); // Wait indefinitely for the process to be cleaned up by the OS.
                    }
                    Logger.info("OTC process successfully killed.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }
}
