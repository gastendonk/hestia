package hestia.special.jenkins;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.timer.AbstractTimer;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;

public class JenkinsTelemetry {
    private static Meter meter;
    private static final AtomicInteger workerCountCache = new AtomicInteger(0);

    public static void init(String serviceName, String otlpEndpoint) {
        Resource resource = Resource.getDefault();
        OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.builder()
                .setEndpoint(otlpEndpoint) // z.B. "http://localhost:4317"
                .build();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(PeriodicMetricReader.builder(metricExporter)
                        .setInterval(Duration.ofSeconds(60))
                        .build())
                .build();
        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .buildAndRegisterGlobal();
        meter = openTelemetrySdk.getMeter("hestia.jenkins");
        meter.gaugeBuilder("jenkins.workers.active")
                .setDescription("Anzahl der laufenden Jenkins-Worker")
                .setUnit("{worker}")
                .buildWithCallback(measurement -> measurement.record(workerCountCache.get()));
    }

    private static void updateWorkerCount(int count) {
        workerCountCache.set(count);
    }
    
    public static class JenkinsTelemetryTimer extends AbstractTimer {
        public static String jenkinsUrl;

        @Override
        protected void timerEvent() {
            if (!StringService.isNullOrEmpty(jenkinsUrl)) {
                var workers = new JenkinsService().loadWorkers(jenkinsUrl);
                workers.removeIf(i -> !i.ok());
                Logger.info("workers: " + workers.size()); // XXX raus
                updateWorkerCount(workers.size());
            }
        }
    }
}
