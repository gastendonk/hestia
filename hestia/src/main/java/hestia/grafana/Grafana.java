package hestia.grafana;

import java.util.List;

import hestia.grafana.GrafanaData.DashboardName;
import hestia.persist.Identifiable;

public class Grafana implements Identifiable {
    private String id;
    private String name;
    private String host;
    private String token;
    private transient List<DashboardName> dashboardNames;
    private transient String error;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<DashboardName> getDashboardNames() {
        return dashboardNames;
    }

    public void setDashboardNames(List<DashboardName> dashboardNames) {
        this.dashboardNames = dashboardNames;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
