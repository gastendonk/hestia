package hestia.otc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import hestia.persist.Persistence;

public class MTCustomerPersistence implements Persistence<MonitoredTarget> {
    private List<MonitoredTarget> customer;
    
    @Override
    public List<MonitoredTarget> load(String envId, String unused) {
        List<MonitoredTarget> loaded = MonitoredTargetDAO.load(envId); // readonly Daten vom Hersteller
        customer = MonitoredTargetDAO.load(file2(envId)); // Kundendaten
        return mixin(loaded, customer); // Kundendaten einmischen
    }
    
    static List<MonitoredTarget> mixin(List<MonitoredTarget> loaded, List<MonitoredTarget> pCustomer) {
        // 1. Map aller Customer-Eintraege erstellen (ID -> Target)
        Map<String, MonitoredTarget> customerMap = pCustomer.stream().collect(
                Collectors.toMap(MonitoredTarget::getId, target -> target, (existing, replacement) -> replacement));

        // 2. Wir merken uns, welche Customer-IDs wir waehrend der Verarbeitung von 'loaded' sehen
        Set<String> processedCustomerIds = new HashSet<>();
        List<MonitoredTarget> result = new ArrayList<>();

        // 3. Bestehende Eintraege verarbeiten / aktualisieren / loeschen
        for (MonitoredTarget loadedTarget : loaded) {
            String id = loadedTarget.getId();
            if (customerMap.containsKey(id)) {
                MonitoredTarget customerTarget = customerMap.get(id);
                processedCustomerIds.add(id); // Als verarbeitet markieren
                // Nur hinzufuegen, wenn es im Customer-Datensatz aktiv ist (sonst: Loeschen)
                if (customerTarget.isActive()) {
                    result.add(customerTarget);
                }
            } else {
                // Nicht in Customer vorhanden -> unveraendert aus loaded uebernehmen
                result.add(loadedTarget);
            }
        }

        // 4. Neue Eintraege hinzufuegen (die nur in customer existieren und aktiv sind)
        for (MonitoredTarget customerTarget : pCustomer) {
            if (!processedCustomerIds.contains(customerTarget.getId()) && customerTarget.isActive()) {
                result.add(customerTarget);
            }
        }
        result.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return result;
    }

    @Override
    public MonitoredTarget loadOne(String envId, String unused, String id) {
        return load(envId, unused).stream().filter(i -> i.getId().equals(id)).findFirst().orElseThrow();
    }

    @Override
    public void save(String envId, String unused, MonitoredTarget mt, boolean add) {
        if (add) {
            load(envId, unused);
        }
        customer.removeIf(i -> i.getId().equals(mt.getId()));
        customer.add(mt);
        MonitoredTargetDAO.save(file2(envId), customer);
    }

    @Override
    public void delete(String envId, String unused, String id) {
        customer = MonitoredTargetDAO.load(file2(envId));
        var exist = MonitoredTargetDAO.load(envId).stream().filter(i -> i.getId().equals(id)).findFirst();
        if (exist.isPresent()) { // Es gibt das Objekt herstellerseitig.
            Site mt = new Site();
            mt.setId(id);
            mt.setActive(false);
            mt.setName("");
            customer.removeIf(i -> i.getId().equals(id));
            customer.add(mt);
        } else { // Es gibt das Objekt nur kundenseitig.
            customer.removeIf(i -> i.getId().equals(id));
        }
        MonitoredTargetDAO.save(file2(envId), customer);
    }

    private String file2(String envId) {
        return "customer/" + envId;
    }
}
