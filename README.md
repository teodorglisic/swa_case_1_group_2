# Versandbeauftragungsprozess – External Task Worker Case 1, Group 2

## Inhaltsverzeichnis

1. [Überblick](#überblick)
2. [Geschäftsprozess](#geschäftsprozess)
3. [Architektur & Systemlandschaft](#architektur--systemlandschaft)
4. [Technische Umsetzung](#technische-umsetzung)
5. [Build & Start](#build--start)
6. [Projektstruktur](#projektstruktur)

---

## Überblick

Dieses Projekt implementiert einen **Camunda External Task Worker** für den Versandbeauftragungsprozess der fiktiven Firma **Accolaia AG**. Der Worker übernimmt die automatisierte Kommunikation mit der REST-API einer externen Spedition. Er legt Versandaufträge an und schreibt die Ergebnisse wie Tracking-Nummer, Abholdatum und Lieferdatum zurück in den BPMN-Prozess.

**Technologien:** Java 21 · Camunda External Task Client · JSON org.json

---

## Geschäftsprozess

Der BPMN-Prozess `Versandbeauftragungsprozess_V2_nach_Service_Task.bpmn` bildet den vollständigen Ablauf ab.

### Beteiligte Akteure

| Akteur                       | Rolle                                                  |
| ---------------------------- | ------------------------------------------------------ |
| **Kunden** | Lösen den Prozess durch eine Bestellung aus           |
| **Auftragsbearbeitungsabt.** | Prüft, gibt frei und leitet den Auftrag weiter        |
| **Versandabteilung** | Ergänzt Kundendaten und kommuniziert mit Spedition    |
| **Spedition extern** | Nimmt Versandaufträge entgegen, liefert Tracking-Infos|

### Der Service Task im Detail

Der BPMN-Prozess enthält einen **External Service Task** mit dem Topic `group2_requestAPI`. Camunda delegiert diesen Task an unseren Worker. Der Worker führt folgende Schritte aus:

1. Er liest die Prozessvariablen Adresse, Telefon, Gewicht und Kunden-ID aus.
2. Er sendet einen POST-Request an die Speditions-API.
3. Bei einem HTTP-Status 202 schreibt er Tracking-Nummer, Abholdatum und Lieferdatum zurück.

---

## Architektur & Systemlandschaft

```text
┌──────────────────┐       REST/HTTP        ┌──────────────────┐
│  Camunda Engine  │◄──────────────────────►│  External Task   │
│  192.168.111.3   │                        │  Worker Java     │
│  :8080           │                        │  Dieses Projekt  │
└──────────────────┘                        └────────┬─────────┘
                                                     │
                                                     │ POST /v1/consignment/request
                                                     ▼
                                            ┌──────────────────┐
                                            │  Speditions-API  │
                                            │  192.168.111.5   │
                                            │  :8080           │
                                            └──────────────────┘
```

### Kommunikationsfluss

1. **Long Polling**: Der Worker fragt die Camunda Engine nach offenen Tasks mit dem Topic group2_requestAPI. Das asyncResponseTimeout ist auf 1000 Millisekunden gesetzt.

2. **Task Lock**: Sobald ein Task verfügbar ist, wird er gesperrt. Das verhindert die gleichzeitige Bearbeitung durch andere Worker. Die lockDuration beträgt 1000 Millisekunden.

3. **API-Aufruf**: Der Worker sendet die Versanddaten per HTTP POST an die Speditions-API.

4. **Task Complete**: Bei Erfolg werden die Variablen gesetzt und der Task abgeschlossen.
---
## Technische Umsetzung
### Klassenübersicht

| Klasse              | Verantwortung                                                 |
| ------------------- | ------------------------------------------------------------- |
| `ServiceTaskWorker` | Camunda-Handler: liest Variablen, ruft API, schreibt Ergebnis |
| `ApiRequester`      | REST-Client: baut HTTP-Request, parst JSON-Antwort            |


### ServiceTaskWorker.java

- Dient als Einstiegspunkt der Anwendung über eine static main-Methode.

- Erstellt einen ExternalTaskClient mit der hartcodierten Base-URL inklusive Basic Auth Credentials.

- Abonniert das Topic group2_requestAPI.

- Liest beim Eintreffen eines Tasks die Prozessvariablen Strasse, Hausnummer, Postleitzahl, Stadt, Land, Telefon, E-Mail, Gewicht und Kunden-ID aus.

- Ruft die Methode ApiRequester.apiRequester auf.

- Schreibt bei einem HTTP-Status 202 die Felder trackingNumber, dateOfPickup und expectedDeliveryDate als Prozessvariablen in Form von Java Date-Objekten zurück.

- Schliesst den Task über externalTaskService.complete ab.

### ApiRequester.java

- Beinhaltet statische Methoden für den API-Aufruf.

- Baut einen JSON-Body mit destination, customerReference, recepientPhone und weight auf.

- Sendet den Body per HttpClient.send an die hartcodierte Speditions-API-URL.

- Gibt ein JSONObject mit dem HTTP-Statuscode und allen Feldern der API-Antwort zurück.

- Wirft Netzwerkfehler, ungültige URIs oder Interrupts als RuntimeException.

- Enthält eine eigene main-Methode für isolierte Tests der API-Anbindung.
---
## Build & Start
### Voraussetzungen

- Java 21 oder neuer

- Maven 3.9+

- Netzwerkzugang zur Camunda Engine `192.168.111.3:8080` und zur Speditions-API `192.168.111.5:8080`

### Befehle Terminal

    # Projekt bauen
    .\mvnw clean package 

    # Anwendung starten über Maven
    .\mvnw exec:java "-Dexec.mainClass=ch.fhnw.students.ServiceTaskWorker"

---
## Projektstruktur

```
swa_case_1_group_2/
├── pom.xml                          # Maven-Build mit Camunda-Abhaengigkeiten
├── README.md                        # Diese Dokumentation
├── mvnw
├── mvnw.cmd
├───.mvn
│   └───wrapper
│       └──maven-wrapper.properties
└── src/
    └── main/
        └── java/ch/fhnw/students/
            ├── ApiRequester.java      # REST-Client fuer die Speditions-API
            └── ServiceTaskWorker.java # Camunda External Task Handler
```
---
_FHNW – Software-Architektur, Case 1, Group 2_