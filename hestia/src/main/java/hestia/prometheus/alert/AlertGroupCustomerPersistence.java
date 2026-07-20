package hestia.prometheus.alert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import hestia.persist.Persistence;

public class AlertGroupCustomerPersistence implements Persistence<AlertGroup> {
    private List<AlertGroup> customer;
    
    @Override
    public List<AlertGroup> load(String envId, String unused) {
        List<AlertGroup> loaded = AlertGroupDAO.load(envId); // readonly Daten vom Hersteller
        customer = AlertGroupDAO.load(file2(envId)); // Kundendaten
        return mixin(loaded, customer); // Kundendaten einmischen
    }

    // TODO ungetestet
    private List<AlertGroup> mixin(List<AlertGroup> loaded, List<AlertGroup> pCustomer) {
        // 1. Map aller Customer-Eintraege erstellen (ID -> Target)
        Map<String, AlertGroup> customerMap = pCustomer.stream().collect(
                Collectors.toMap(AlertGroup::getId, target -> target, (existing, replacement) -> replacement));

        // 2. Wir merken uns, welche Customer-IDs wir waehrend der Verarbeitung von 'loaded' sehen
        Set<String> processedCustomerIds = new HashSet<>();
        List<AlertGroup> result = new ArrayList<>();

        // 3. Bestehende Eintraege verarbeiten / aktualisieren / loeschen
        for (AlertGroup loadedTarget : loaded) {
            String id = loadedTarget.getId();
            if (customerMap.containsKey(id)) {
                AlertGroup customerTarget = customerMap.get(id);
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
        for (AlertGroup customerTarget : pCustomer) {
            if (!processedCustomerIds.contains(customerTarget.getId()) && customerTarget.isActive()) {
                result.add(customerTarget);
            }
        }
        return result;
    }
    
    @Override
    public AlertGroup loadOne(String envId, String unused, String id) {
        return load(envId, unused).stream().filter(i -> i.getId().equals(id)).findFirst().orElseThrow();
    }

    @Override
    public void save(String envId, String unused, AlertGroup group, boolean add) {
        if (add) {
            load(envId, unused);
        }
        customer.removeIf(i -> i.getId().equals(group.getId()));
        customer.add(group);
        AlertGroupDAO.save(file2(envId), customer);
    }

    @Override
    public void delete(String envId, String unused, String id) {
        var exist = AlertGroupDAO.load(envId).stream().filter(i -> i.getId().equals(id)).findFirst();
        if (exist.isPresent()) { // Es gibt das Objekt herstellerseitig.
            AlertGroup group = new AlertGroup();
            group.setId(id);
            group.setActive(false);
            customer.removeIf(i -> i.getId().equals(id));
            customer.add(group);
        } else { // Es gibt das Objekt nur kundenseitig.
            customer.removeIf(i -> i.getId().equals(id));
        }
        AlertGroupDAO.save(file2(envId), customer);
    }

    private String file2(String envId) {
        return "customer/" + envId;
    }
}
