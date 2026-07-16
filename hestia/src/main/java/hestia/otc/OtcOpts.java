package hestia.otc;

/**
 * Some options for the OTel Collector config.yaml file
 */
public class OtcOpts {
    /** host of metrics database (Prometheus) */
    public String prometheusremotewrite;
    /** host of traces database (Tempo) */
    public String tempo;
    /** host of logs database (Loki) */
    public String loki;
    /** host of other OTel Collector to send all data to */
    public String otc;
    /** true: log debug messages */
    public boolean debug = true;
}
