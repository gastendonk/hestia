# Hestia Konzept

Im Monitoring-Umfeld unterscheidet man zwischen den drei Säulen der Observability: **Metriken, Traces und Logs**.
Der **OpenTelemetry Collector (OTC)** dient als zentraler Baustein, um diese Daten zu sammeln (per Pull- or Push-Verfahren).

Der OTC leitet die Daten anschließend an die jeweiligen spezialisierten Backends weiter:
- Metriken an die **Prometheus**-Datenbank
- Traces an die Grafana **Tempo**-Datenbank
- Logs an die Grafana **Loki**-Datenbank *(aktuell nicht angedacht)*

Die Visualisierung der aggregierten Daten erfolgt mittels **Grafana**. Obwohl Grafana die Möglichkeit bietet, Alarmregeln über die
Benutzeroberfläche zu erstellen, wird dieser Ansatz aus architektonischen Gründen bewusst nicht gewählt.

## Alarmregeln in Prometheus
Alarmregeln werden konsequent direkt in **Prometheus** definiert. Dies bietet signifikante Vorteile:
- **Ressourceneffizienz und Performance:** Prometheus hält die relevanten Metriken im Arbeitsspeicher (RAM). Die Evaluierung der Regeln findet direkt auf diesen lokalen Daten statt. Grafana muss somit nicht kontinuierlich Abfragen über das Netzwerk an Prometheus senden.
- **Ausfallsicherheit:** Die Alarmierung bleibt selbst dann funktionsfähig, wenn die Visualisierungsschicht (Grafana) temporär nicht verfügbar ist.
- **Trennung von Zuständigkeiten:** Prometheus wertet die Regeln aus und übergibt aktive Alarme (Firing Alerts) an den Prometheus Alertmanager. Dieser fasst doppelte oder zusammengehörige Meldungen sinnvoll zusammen, unterdrückt wiederholtes Fluten von Nachrichten und übernimmt das eigentliche Versenden der Benachrichtigungen.

## Die Evolution zu Hestia
Um eine zuverlässige Überwachung zu gewährleisten, müssen OTC, Prometheus, Alertmanager und die Speicher-Backends konsistent konfiguriert werden. Zudem müssen Grafana-Dashboards gesichert und portierbar sein. *(Wobei nicht jeder Kunde neben dem OTC Prometheus & Co betreiben muss.)*

Der klassische GitOps-Ansatz (z. B. mittels doco-cd) hat sich in der Praxis jedoch als zu schwergewichtig und fehleranfällig erwiesen: Das manuelle Anpassen unübersichtlicher Konfigurationsdateien mit anschließendem Push führte oft zu unzuverlässigen Neustarts der Container, deren Erfolg zudem schwer zu validieren war.

Da der OTC im laufenden Betrieb komplex zu verwalten ist, wurde die Idee eines Sidecars geboren. Hestia geht als Java 17 Webapp jedoch einen Schritt weiter: Es agiert nicht nur als einfaches Sidecar, sondern übernimmt als übergeordnete Instanz das gesamte Prozessmanagement des OTC-Containers.

## Kernkomponenten und Features von Hestia

### 1. OpenTelemetry Collector Management
Hestia automatisiert den gesamten Lifecycle des OTC innerhalb des Containers:

- **Lifecycle-Management:** Automatischer Download, Installation, Start und kontinuierliche Überwachung des OTC-Prozesses via Healthchecks.

- **Safe Configuration:** Vor dem Einspielen neuer Konfigurationen validiert Hestia die Dateien syntaktisch und semantisch, um ungeplante Betriebsunterbrechungen des Monitoring-Systems effektiv zu verhindern.

### 2. Datenmodell und Hierarchie
Die Konfiguration in Hestia ist logisch strukturiert:

- **Kundenspezifische Umgebungen:** Die oberste Organisationsebene. Ganze Umgebungen können bei Bedarf temporär deaktiviert werden, woraufhin Hestia die nachgelagerten Monitoring-Tools automatisch anpasst.

- **Monitored Targets:** Innerhalb einer Umgebung werden die zu überwachenden Zielsysteme (Server, Websites, Datenbanken) über die Hestia-GUI gepflegt.

- **Alarmregeln:** Die Definition von Alert-Rules, strukturiert in logische Gruppen.

### 3. Git-Backend und Multi-Customer-Release-Prozess
Die Versionsverwaltung und Verteilung erfolgt über eine dreistufige Hestia-Architektur:

1. Zentrales **Git-Repository** (unsere Seite): Alle Umgebungen und Konfigurationen werden versioniert in Git verwaltet (Einsatz von Branches, tags für dedizierte Release-Stände).

2. Die Hestia-Relay-Architektur: Unser internes **Burg-Hestia** pusht die vorbereiteten und getaggten Konfigurationen an ein zentrales **Cloud-Hestia** (Relay).

3. Das beim Kunden laufende **Customer-Hestia** holt sich die für ihn bestimmten Daten vom Cloud-Hestia ab.

### 4. 1-Klick-Update
Beim Kunden ermöglicht Hestia ein echtes "Schlüsselfertig"-Prinzip: Mit nur einem Klick in der GUI lädt das Customer-Hestia die aktuellen Konfigurationen (Umgebungen, Monitored Targets, Alert Rules) vom Cloud-Hestia und aktualisiert bzw. startet den OTC nahtlos neu.

### 5. Operatives Dashboard (Alarme und Silences)
Hestia dient als zentrales Betriebs-Dashboard für den Kunden:

- **Status-Anzeige:** Direkte Visualisierung von aktiven Alarmen (Firing Alerts), die von Prometheus via Alertmanager registriert wurden.

- **Silence-Management:** Ermöglicht das temporäre Stummschalten (Unterdrücken) von Alarmen direkt aus der Hestia-Oberfläche heraus, um beispielsweise während geplanter Wartungsfenster Fehlalarme zu vermeiden.
