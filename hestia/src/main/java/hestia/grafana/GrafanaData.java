package hestia.grafana;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Grafana JSON
public class GrafanaData {
    private List<DashboardName> dashboards;
    private List<Datasource> datasources;

    public List<DashboardName> getDashboards() {
        return dashboards;
    }

    public void setDashboards(List<DashboardName> dashboards) {
        this.dashboards = dashboards;
    }

    public List<Datasource> getDatasources() {
        return datasources;
    }

    public void setDatasources(List<Datasource> datasources) {
        this.datasources = datasources;
    }

    public static class DashboardName {
        private String name;
        private String id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class Datasource {
        private int id;      // Interne Datenbank-ID
        private String uid;  // Eindeutige UID (wichtiger für API-Links)
        private String name; // Anzeigename (z.B. "Prometheus")
        private String type; // Typ (z.B. "prometheus", "influxdb")

        public int getId() {
            return id;
        }

        public String getUid() {
            return uid;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }

    public class DashboardList {
        private List<Dashboard> items;

        public List<Dashboard> getItems() {
            return items;
        }
    }

    public static class Dashboard {
        private Metadata metadata;
        private Map<String, Object> spec; // Das komplette Dashboard JSON

        public Metadata getMetadata() {
            return metadata;
        }

        // Quasi das Dashboard JSON
        public Map<String, Object> getSpec() {
            return spec;
        }

        /**
         * Hilfsmethode, um den lesbaren Titel aus der Spec zu extrahieren
         */
        public String getDashboardTitle() {
            if (spec != null && spec.containsKey("title")) {
                return spec.get("title").toString();
            }
            return "Unbenannt";
        }
        
        public int replaceDatasourceUid(String oldUid, String newUid) {
            return _replaceDatasourceUid(spec, oldUid, newUid);
        }
        
        @SuppressWarnings("unchecked")
        public static int _replaceDatasourceUid(Map<String, Object> spec, String oldUid, String newUid) {
            int changes = 0;
            for (Map.Entry<String, Object> entry : spec.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Map) {
                    Map<String, Object> childMap = (Map<String, Object>) value;
                    if ("datasource".equals(entry.getKey())) { // Prüfen, ob wir in einem "datasource" Objekt sind
                        if (oldUid.equals(childMap.get("uid"))) {
                            childMap.put("uid", newUid);
                            changes++;
                        }
                    } else {
                        changes += _replaceDatasourceUid(childMap, oldUid, newUid); // recursive
                    }
                } else if (value instanceof List) {
                    for (Object item : (List<?>) value) {
                        if (item instanceof Map) {
                            changes += _replaceDatasourceUid((Map<String, Object>) item, oldUid, newUid); // recursive
                        }
                    }
                }
            }
            return changes;
        }
        
        public Set<Map<String, String>> collectDatasources() {
            Set<Map<String, String>> ret = new HashSet<>();
            _collectDatasources(spec, ret);
            return ret;
        }

        @SuppressWarnings("unchecked")
        private static void _collectDatasources(Map<String, Object> spec, Set<Map<String, String>> found) {
            for (Map.Entry<String, Object> entry : spec.entrySet()) {
                Object value = entry.getValue();

                if (value instanceof Map) {
                    Map<String, Object> childMap = (Map<String, Object>) value;

                    // Prüfen, ob der Key "datasource" ist
                    if ("datasource".equals(entry.getKey())) {
                        Object uid = childMap.get("uid");
                        Object type = childMap.get("type");

                        if (uid instanceof String && !((String) uid).isEmpty()) {
                            Map<String, String> ds = new HashMap<>();
                            ds.put("uid", (String) uid);
                            // In Grafana-JSONs ist 'type' oft der sprechende Name der Quelle (z.B. prometheus)
                            ds.put("name", type instanceof String ? (String) type : "unknown");
                            found.add(ds);
                        }
                    } else {
                        // Weiter rekursiv in die Map abtauchen
                        _collectDatasources(childMap, found);
                    }

                } else if (value instanceof List) {
                    // Listen (wie 'panels' oder 'targets') durchlaufen
                    for (Object item : (List<?>) value) {
                        if (item instanceof Map) {
                            _collectDatasources((Map<String, Object>) item, found);
                        }
                    }
                }
            }
        }
    } // Dashboard

    public static class Metadata {
        private String name; // Der technische Name/ID
        private String uid; // Die Grafana UID

        public String getName() {
            return name;
        }

        public String getUid() {
            return uid;
        }
    }
}
