# OTel Collector binaries
source: https://github.com/open-telemetry/opentelemetry-collector-releases/releases/tag/v0.137.0

## Windows
filename: otelcol-contrib_0.137.0_windows_amd64.tar.gz

## Linux/Docker
filename: otelcol-contrib_0.137.0_linux_amd64.tar.gz

copy to hestia/otelcol-contrib/linux

size: 343.601.336 bytes

### Dockerfile

RUN addgroup -S otel && adduser -S otel -G otel
COPY --chmod=755 otelcol-contrib /usr/local/bin/otelcol-contrib
USER otel
EXPOSE 4317 4318 8889
ENTRYPOINT ["/usr/local/bin/otelcol-contrib"]
CMD ["--config=/etc/otelcol-contrib/config.yaml"]
