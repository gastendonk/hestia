package hestia.otc.opts;

/**
 * Some options for the OTel Collector config.yaml file
 */
public class OtcOpts {
    /** host of metrics database (Prometheus) */
    private String prometheusremotewrite;
    /** host of traces database (Tempo) */
    private String tempo;
    /** host of logs database (Loki) */
    private String loki;
    /** host of other OTel Collector to send all data to */
    private String otc;
    /** true: log debug messages */
    private boolean debug = true;

    public String getPrometheusremotewrite() {
        return prometheusremotewrite;
    }

    public void setPrometheusremotewrite(String prometheusremotewrite) {
        this.prometheusremotewrite = prometheusremotewrite;
    }

    public String getTempo() {
        return tempo;
    }

    public void setTempo(String tempo) {
        this.tempo = tempo;
    }

    public String getLoki() {
        return loki;
    }

    public void setLoki(String loki) {
        this.loki = loki;
    }

    public String getOtc() {
        return otc;
    }

    public void setOtc(String otc) {
        this.otc = otc;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
