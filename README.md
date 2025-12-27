# Fabrik GUI

Java/BlueJ Projekt mit Swing-Oberfläche zur Verwaltung von Bestellungen und Produktion.

## Build & Start

Windows PowerShell:

```powershell
javac -encoding UTF-8 *.java
java FabrikGUI
```

## Hinweise
- Kompilierte Dateien (`*.class`) sind aus dem Repo ausgeschlossen.
- Tests nutzen JUnit lokal; sie sind nicht Teil des Builds in diesem Repo.

## Neu: Kundenverwaltung + GUI-Integration

- Neuer Menüpunkt `Kunden → Übersicht` öffnet die Kundenverwaltung.
- Übersicht zeigt alle Kunden (Spalten: Kundennummer, Name, Firma, Position).
- Bearbeitung direkt in der Tabelle möglich; mit "Speichern" werden Änderungen ins Backend geschrieben.
- "Neuer Kunde…" legt einen neuen Kunden an und aktualisiert die Tabelle.
- Tab "Historie" zeigt alle Bestellungen des ausgewählten Kunden.

## Auftragserfassung mit Kundennummer

- Links im Auftragseingabe-Bereich ist ein neues Feld `Kundennummer` hinzugekommen.
- Beim Klick auf `Bestellung aufgeben` wird die Kundennummer validiert und die Bestellung dem Kunden zugeordnet.

## Backend-Erweiterungen

- `Kunde.java`, `KundenDatenbank.java`: In-Memory-Verwaltung mit auto. Kundennummern.
- `Fabrik`: Feld `kundenDb`, Methoden `gibKundenDb()`, `kundeAnlegen(...)`, `findeKunde(...)`, `gibBestellungenFuerKunde(kundenNr)` und überladene `bestellungAufgeben(int kundenNr, int std, int prem)`.
- `Bestellung`: neues Feld `kundenNr` (immutable) und entsprechender Konstruktor.

## Tests

- `FabrikGUITest`: 
	- Prüft, dass der Bestell-Button `bestellungAufgeben(kundenNr, std, prem)` aufruft.
	- Prüft, dass "Speichern" in der Kundenverwaltung das Backend aktualisiert.

## Schnellzugriff in der GUI

- Kundenverwaltung: Menü `Kunden → Übersicht`.
- Auftragserfassung: Felder `Standardtüren`, `Premiumtüren`, `Kundennummer` ausfüllen und `Bestellung aufgeben` klicken.
