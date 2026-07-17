package hestia.otc.process;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.rest.REST;

public class OtcProcess {
    private Process p;
    // TODO Lock -> zu einer Zeit darf hier nur 1 Aktion erfolgen

    public OtcProcess() {
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
            //pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); // Gibt OTEL-Logs in die Docker-Konsole aus
            p = pb.start();
            Logger.info("new process style. pid " + p.pid());
            logs();
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public String info1 = "--", info2 = "--";
    
    private void logs() {
        // 2. Den Output in einem separaten Thread asynchron auslesen
        Thread logReader = new Thread(() -> {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    // Option A: Das Log weiterhin in die Docker-Konsole schreiben, damit man es von
                    // außen sieht
                    Logger.info("[OTel-Collector] " + line);

                    // Option B: Nach der Erfolgsmeldung suchen
                    if (line.contains("Everything is ready. Begin running and processing data")) {
                        info1=">>> ERFOLG: OpenTelemetry Collector ist vollständig einsatzbereit! (PID: "
                                + p.pid() + ")";
                        Logger.info(info1);
                        // Hier kannst du ein Flag setzen oder ein Event triggern
                    }

                    if (line.contains("health_check") && line.contains("ready")) {
                        info2=">>> ERFOLG: OTel Health-Check-Erweiterung ist aktiv.";
                        Logger.info(info2);
                    }
                }
            } catch (IOException e) {
                Logger.error("Fehler beim Lesen der Collector-Logs: ", e);
            }
        });

        // Thread als Daemon starten, damit er die JVM nicht am Beenden hindert
        logReader.setDaemon(true);
        logReader.start();
    }

    public long pid() {
        return p == null ? -1 : p.pid();
    }

    public boolean alive() {
        boolean ret = p != null && p.isAlive();
        if (p != null && !p.isAlive()) {
            Logger.warn("exit value: " + p.exitValue());
        }
        return ret;
    }

    public int check() {
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

    public void kill() {
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
