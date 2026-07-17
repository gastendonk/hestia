package hestia.otc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.rest.REST;

/**
 * Start and stop otelcol-contrib application
 */
public class OtcProcess {
    private static final Object LOCK = new Object();
    private Process p;
    public String info1 = "--";
    public String info2 = "--";

    public OtcProcess() {
        synchronized (LOCK) {
            if (!"1".equals(System.getenv("RUN"))) {
                Logger.warn("otc process not started because env var RUN is not 1");
                return;
            }
            String x = "/app/otel/otelcol-contrib";
            String c = "--config=/work/config.yaml";
            Logger.info("starting otc process... | " + x + " " + c);
            try {
                ProcessBuilder pb = new ProcessBuilder(x, c);
                pb.redirectErrorStream(true);
                p = pb.start();
                Logger.info("new process style. pid " + p.pid());
                logs();
            } catch (IOException e) {
                Logger.error(e);
            }
        }
    }

    private void logs() {
        // Den Output in einem separaten Thread asynchron auslesen
        Thread logReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Option A: Das Log weiterhin in die Docker-Konsole schreiben, damit man es von
                    // aussen sieht
                    Logger.info("[OTel-Collector] " + line);

                    // Option B: Nach der Erfolgsmeldung suchen
                    if (line.contains("Everything is ready. Begin running and processing data")) {
                        info1 = ">>> ERFOLG: OpenTelemetry Collector ist vollständig einsatzbereit! (PID: "
                                + p.pid() + ")";
                        Logger.info(info1);
                        // Hier kannst du ein Flag setzen oder ein Event triggern
                    }

                    if (line.contains("health_check") && line.contains("ready")) {
                        info2 = ">>> ERFOLG: OTel Health-Check-Erweiterung ist aktiv.";
                        Logger.info(info2);
                    }
                }
            } catch (IOException e) {
                Logger.error("Fehler beim Lesen der Collector-Logs: ", e);
            }
        });
        logReader.setDaemon(true); // Thread als Daemon starten, damit er die JVM nicht am Beenden hindert
        logReader.start();
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
     * @return success: 200, otherwise error
     */
    public int checkHealth() {
        synchronized (LOCK) {
            try {
                var r = new REST("http://localhost:13133/").get();
                int status = r.getHttpResponse().getStatusLine().getStatusCode(); // TODO Amalia
                Logger.info("health_check: [" + status + "] " + r.response());
                return status;
            } catch (Exception e) {
                Logger.error("health_check: " + e.getMessage());
                return -1;
            }
        }
    }

    public void kill() {
        synchronized (LOCK) {
            Logger.info("destroy...");
            try {
                p.destroy();
                info1 = "//";
                info2 = "//";
                try {
                    // Warte bis zu 3 Sekunden auf das normale Beenden
                    if (!p.waitFor(3, TimeUnit.SECONDS)) {
                        Logger.warn("OTel reagiert nicht auf SIGTERM. Erzwinge Abbruch (SIGKILL)...");
                        p.destroyForcibly(); // Erzwinge harten Abbruch (SIGKILL)
                        p.waitFor(); // Warte unbegrenzt, bis der Prozess im OS abgeräumt wurde
                    }
                    Logger.info("OTel-Prozess erfolgreich beendet.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                Logger.error(e);
            }
            Logger.info("destroy done");
        }
    }
}
