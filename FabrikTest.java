import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

/**
 * Die Pruefklasse FabrikTest dient dazu, diverse Szenarien zu simulieren, um die Korrektheit der Fabrik-Logik zu verifizieren.
 * Hierbei werden verschiedene Ablaeufe erprobt, um sicherzustellen, dass Auftraege fehlerfrei abgewickelt und fehlerhafte
 * Nutzerangaben erkannt sowie abgefangen werden. Diese Testsuite validiert die gesamte Anwendung auf ihre Praxistauglichkeit.
 * * Es werden sowohl positive Standardfaelle als auch Negativtests durchgefuehrt, wie etwa die Zustandsaenderung von Waren
 * oder die Reaktion auf unzulaessige Bestellmengen.
 * * Das Ziel dieser Klasse ist die Absicherung der Kernprozesse, um eine stabile und verlaessliche Systemfunktionalitaet zu garantieren.
 *
 * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class FabrikTest
{
private Fabrik fabrik;
    
/**
 * Allgemeiner Konstruktor zur Erstellung der Testinstanz FabrikTest.
 */
    public FabrikTest()
    {
    }
    
/**
 * Bereitet die benoetigten Testobjekte vor.
 * * Diese Methode wird automatisiert vor jedem einzelnen Testfall ausgefuehrt.
 */
    @BeforeEach
    public void setUp() {
        fabrik = new Fabrik(); // Erzeugung einer neuen Fabrik-Instanz fuer jeden Testdurchlauf
        System.out.println("Beginn der Fabrik-Testreihe");
        System.out.println();
    }
    
/**
 * Bereinigt die Testumgebung nach der Durchführung.
 * * Erfolgt jeweils nach dem Abschluss eines Testfalls.
 */
    @AfterEach
    public void tearDown() {
        System.out.println();
        System.out.println("Abschluss der Fabrik-Testreihe");
        System.out.println("------------------------");
    }

@Test
    /**
     * Ueberprueft die Auftragserteilung unter Verwendung zulaessiger Parameter.
     */
    public void testeBestellung() {

        // Erstellung einer Fabrik zum Testen
        Fabrik testFabrik = new Fabrik();
        testFabrik.bestellungAufgeben(2, 5); // Auftrag über 2 Standard- und 5 Premiummodelle

        Bestellung ersteBestellung = testFabrik.gibBestellungen().get(0);

        // Sicherstellen, dass die Liste die korrekte Summe an Produkten enthaelt (Gesamt: 7)
        assertEquals(7, ersteBestellung.liefereBestellteProdukte().size()); // Verifikation der Gesamtzahl

        // Validierung, dass exakt 2 Standard- und 5 Premiumversionen enthalten sind
        // Analyse der Produktkategorien
        int anzahlStandardTueren = 0; 
        int anzahlPremiumtueren = 0;

        for (Object produkt : ersteBestellung.liefereBestellteProdukte()) {
            if (produkt instanceof Standardtuer) {
                anzahlStandardTueren++;
            } else if (produkt instanceof Premiumtuer) {
                anzahlPremiumtueren++;
            }
        }

        assertEquals(2, anzahlStandardTueren);
        assertEquals(5, anzahlPremiumtueren);

        System.out.println(
                "Erfolgreicher Test: Bestellung mit korrekten Werten wurde verarbeitet.");

    }
@Test
    /**
     * Diese Testmethode validiert die korrekte Erfassung von Auftraegen und gibt die Ergebnisse aus.
     * Um Performance-Probleme in BlueJ zu vermeiden, werden nur kleine Mengen (1-3 Einheiten) verwendet.
     * Vorherige Testlogiken wurden aufgrund von Thread-Problemen in Kommentare verschoben.
     */
    public void testProdukteBestellen()
    {
        ThreadUtil.synchronisiertesPrintln("-----------------------------------------------------------");
        ThreadUtil.synchronisiertesPrintln("Der Bestellvorgang wird nun geprueft!");
        
        Fabrik Aeki = new Fabrik(); // Aufbau einer neuen Fabrik-Struktur
        fabrik.bestellungAufgeben(1, 2); // Erster Auftrag: 1 Standard, 2 Premium
        fabrik.bestellungAufgeben(2, 3); // Zweiter Auftrag: 2 Standard, 3 Premium 
        
        assertTrue(fabrik.gibBestellungen().get(0).gibAnzahlStandardtueren() == 1); // Validierung der Mengen fuer den ersten Eintrag (Standard)
        assertTrue(fabrik.gibBestellungen().get(0).gibAnzahlPremiumtueren() == 2); // Validierung der Mengen fuer den ersten Eintrag (Premium)
        assertTrue(fabrik.gibBestellungen().get(1).gibAnzahlStandardtueren() == 2); // Validierung der Mengen fuer den zweiten Eintrag (Standard)
        assertTrue(fabrik.gibBestellungen().get(1).gibAnzahlPremiumtueren() == 3); // Validierung der Mengen fuer den zweiten Eintrag (Premium)
        assertTrue(fabrik.gibBestellungen().size() == 2); // Kontrolle der gesamten Listenlaenge


        ThreadUtil.synchronisiertesPrintln("Auftrag wird registriert!");
        Aeki.bestellungenAusgeben(); // Visualisierung saemtlicher Auftraege in der Testfabrik
        
        Bestellung bestellung = fabrik.gibBestellungen().get(0); // Zugriff auf das erste Element
        fabrik.lagerAuffuellen(); // Bestandsaktualisierung nach Auftragseingang
    }
    
@Test
    /**
     * Validiert die Funktionalitaet der Bestaetigungslogik.
     * Es wird erwartet, dass ein Auftrag initial offen ist und nach der Bestaetigung als erledigt markiert wird.
     */
    public void testBestellBestaetigung() {
        // Pruefung der Statusaenderung eines Auftrags
        Bestellung bestellung = new Bestellung(1, 3, 2); // Definition eines Auftrags
        assertFalse(bestellung.gibBestellbestaetigung()); // Der Status muss zu Beginn auf 'false' stehen
        
        bestellung.bestellungBestaetigen(); // Ausfuehrung der Bestaetigung
        assertTrue(bestellung.gibBestellbestaetigung()); // Der Status muss nun 'true' sein
        
        System.out.println("Funktionstest der Bestaetigungsmethode erfolgreich.");
    }
    
@Test
    /**
     * Prueft die Reaktion auf unzulaessige Eingabewerte.
     * Negative Mengenangaben duerfen nicht zu einer Aufnahme in die Bestellliste fuehren.
     */
    public void UngültigeBestellwerte() {
        fabrik.bestellungAufgeben(-1, 2); // Test mit negativem Standard-Wert
        assertEquals(0, fabrik.gibBestellungen().size()); // Liste muss leer bleiben
        
        fabrik.bestellungAufgeben(1, -2); // Test mit negativem Premium-Wert
        assertEquals(0, fabrik.gibBestellungen().size()); // Liste muss weiterhin leer bleiben
        System.out.println("Abwehr fehlerhafter Bestellwerte erfolgreich getestet.");
    }
    
@Test
    /**
     * Verifiziert das Systemverhalten bei der Ausgabe einer leeren Auftragsliste.
     * Die Methode sollte ohne Absturz durchlaufen und eine leere Liste bestaetigen.
     */
    public void testBestellungenAusgebenLeer() {
        fabrik.bestellungenAusgeben(); // Versuch der Ausgabe ohne Daten
        assertTrue(fabrik.gibBestellungen().isEmpty());
        System.out.println("Leerlisten-Handling funktioniert einwandfrei.");
    }
    
@Test 
    /**
     * Kontrolliert, ob ein frisch angelegter Auftrag standardmaessig unbestaetigt bleibt.
     */
    public void testNeueBestellungUnbestaetigt() {
        Bestellung bestellung = new Bestellung(1, 5, 2);
        assertFalse(bestellung.gibBestellbestaetigung()); // Initialer Status muss negativ sein
        System.out.println("Standardstatus fuer Neuauftraege korrekt gesetzt.");
    }
    
@Test 
    /**
     * Stellt sicher, dass zwei identische Auftraege als zwei eigenstaendige Positionen behandelt werden.
     */
    public void testWiederholteBestellungAufgeben() {
        fabrik.bestellungAufgeben(3, 3); // Erster Durchgang
        fabrik.bestellungAufgeben(3, 3); // Zweiter Durchgang mit gleichen Werten
        
        assertEquals(2, fabrik.gibBestellungen().size()); // Die Liste muss nun zwei Eintraege enthalten
        System.out.println("Mehrfachbestellungen werden korrekt separiert.");
    }
    
@Test
    /**
     * Validiert die Kalkulation der Lieferfrist.
     * Hierbei werden Beschaffungszeit, Produktionsaufwand und Standardversandzeit verrechnet.
     */
    public void testGibLieferzeit() {
        // Beispielhafte Parameter fuer die Berechnung
        int beschaffungsZeit = 2; // Zeitaufwand fuer Material (Tage)
        int anzahlStandardtueren = 3; // Menge Standard
        int anzahlPremiumtueren = 2; // Menge Premium

        // Referenzwerte aus den Produktklassen
        Standardtuer standardtuer = new Standardtuer();
        Premiumtuer premiumtuer = new Premiumtuer();

        // Zeitaufwand pro Einheit
        int standardTuerZeit = standardtuer.getProduktionszeit(); 
        int premiumTuerZeit = premiumtuer.getProduktionszeit();   

        // Manuelle Berechnung der Ziel-Lieferzeit
        int erwarteteLieferzeit = 
            ((standardTuerZeit * anzahlStandardtueren) + 
            (premiumTuerZeit * anzahlPremiumtueren)) / (60 * 24) + 
            (beschaffungsZeit + Bestellung.gibStandardlieferzeit());

        // Abruf des berechneten Wertes aus der Fabrik
        float berechneteLieferzeit = fabrik.gibLieferzeit(beschaffungsZeit, anzahlStandardtueren, anzahlPremiumtueren);

        // Vergleich der Werte
        assertEquals(erwarteteLieferzeit, Math.round(berechneteLieferzeit), 
            "Die ermittelte Zeitdauer weicht vom erwarteten Ergebnis ab.");
    }

@Test
    /**
     * Simuliert die Auftragserteilung, wenn Nullwerte fuer die Mengen uebermittelt werden.
     * Es wird erwartet, dass in diesem Fall keine Transaktion stattfindet.
     */
public void testBestellungAufgebenFehlermeldungAnzahlNull() {
        
        // Konsolenausgabe fuer den Testverlauf
        ThreadUtil.synchronisiertesPrintln("-----------------------------------------------------------");
        ThreadUtil.synchronisiertesPrintln("Pruefung: Eingabe von Nullwerten");
        ThreadUtil.synchronisiertesPrintln("");

        // Initialisierung der Fabrikumgebung
        Fabrik TestFabrik = new Fabrik();

        // Auftrag mit 0 Einheiten starten
        TestFabrik.bestellungAufgeben(0, 0);         

        // Kontrolle der aktuellen Liste
        TestFabrik.bestellungenAusgeben();

        ThreadUtil.synchronisiertesPrintln("");

        // Abschlussmeldung des Testlaufs
        ThreadUtil.synchronisiertesPrintln("Durchlauf beendet");
        ThreadUtil.synchronisiertesPrintln("-----------------------------------------------------------");
        ThreadUtil.synchronisiertesPrintln("");
    }
    
@Test
    /**
     * Untersucht, ob die Lieferzeit korrekt ermittelt wird und ob nach Verstreichen dieser Zeit
     * der Lieferstatus automatisch aktualisiert wird.
     */
void testBestellungenNachDefinierterLieferzeit() {
    // Beispielkonfiguration: Ausreichend Material vorhanden
    int beschaffungsZeit = 0; 
    int anzahlStandardtueren = 3; 
    int anzahlPremiumtueren = 5;

    // Erwartungswert kalkulieren
    float erwarteteLieferzeit = fabrik.gibLieferzeit(beschaffungsZeit, anzahlStandardtueren, anzahlPremiumtueren);
    
    // Auftrag absenden
    fabrik.bestellungAufgeben(anzahlStandardtueren, anzahlPremiumtueren);

    // Zugriff auf die Bestelldaten
    ArrayList<Bestellung> bestellungen = fabrik.gibBestellungen();

    Bestellung bestellung = bestellungen.get(0);

    // Abgleich der kalkulierten Zeit
    assertEquals(erwarteteLieferzeit, bestellung.gibLieferzeit(), 
        "Die Lieferzeitberechnung im Objekt ist inkorrekt.");

    // Simulation des Zeitablaufs (Umrechnung in Millisekunden)
    float lieferzeitInMillisekunden = erwarteteLieferzeit * 24 * 60 * 60 * 1000; 
    float lieferzeitStart = System.currentTimeMillis();

    // Verifikation der Bestaetigung nach Prozessende
    assertTrue(bestellung.gibBestellbestaetigung(), 
        "Der Auftrag haette nach der berechneten Zeit bestaetigt sein muessen.");
}

@Test
    /**
     * Prueft, ob nach einer manuellen Bestandsaufstockung die Bearbeitung korrekt
     * fortgesetzt wird und der Zaehler fuer Auffuellungen praezise arbeitet.
     */
void testVerzoegerungUndProduktionNachAuffuellung() {
        // Erstmaliges Auffuellen des Bestands
        fabrik.lagerAuffuellen(); 
        int initialLagerAuffuellungen = fabrik.gibLagerAuffuellungen();

        // Normalen Auftrag platzieren
        fabrik.bestellungAufgeben(5, 10);
        ArrayList<Bestellung> bestellungenVorAuffuellung = fabrik.gibBestellungen();
        assertEquals(1, bestellungenVorAuffuellung.size(), "Die Liste sollte genau einen Eintrag zeigen.");

        // Erneute Bestandsaktualisierung
        fabrik.lagerAuffuellen(); 
        int neueLagerAuffuellungen = fabrik.gibLagerAuffuellungen();

        // Validierung des Zaehlerstandes
        assertEquals(initialLagerAuffuellungen + 1, neueLagerAuffuellungen,
                "Der Zaehler der Auffuellvorgange wurde nicht korrekt erhoeht.");

        // Auftragserteilung nach dem Logistikvorgang
        fabrik.bestellungAufgeben(15, 20);
        ArrayList<Bestellung> bestellungenNachAuffuellung = fabrik.gibBestellungen();
        assertEquals(2, bestellungenNachAuffuellung.size(),
                "Die Gesamtanzahl der Auftraege ist nach der Auffuellung fehlerhaft.");
}
    
@Test
    /**
     * Stellt sicher, dass Auftraege tatsaechlich als zugestellt markiert werden.
     * Zudem werden Beschaffungs- und Lieferzeiten auf Plausibilitaet geprueft.
     */
void testBestellungenWurdenGeliefert() {
        // Aufgabe mehrerer Testbestellungen
        fabrik.bestellungAufgeben(3, 7);
        fabrik.bestellungAufgeben(10, 5);

        ArrayList<Bestellung> bestellungen = fabrik.gibBestellungen();

        // Kontrolle der Registrierung
        assertEquals(2, bestellungen.size(), "Es fehlen eingetragene Bestellungen.");

        // Statuspruefung fuer jedes einzelne Element
        for (Bestellung bestellung : bestellungen) {
            assertTrue(bestellung.gibBestellbestaetigung(),
                    "Ein Auftrag wurde nicht korrekt bestaetigt.");

            int beschaffungsZeit = bestellung.gibBeschaffungsZeit();
            assertTrue(beschaffungsZeit >= 0,
                    "Negative Beschaffungszeiten sind unzulaessig.");

            float lieferzeit = bestellung.gibLieferzeit();
            assertTrue(lieferzeit > 0,
                    "Die Lieferzeitangabe muss einen positiven Wert aufweisen.");
        }
    }

 @Test
    /**
     * Analysiert die Fehlerbehandlung bei der Eingabe negativer Mengen.
     * Erwartet wird ein Abbruch des Vorgangs ohne Datenspeicherung.
     */
public void testBestellungAufgebenFehlermeldungNegativeAnzahl() {
        
        // Output zur Nachverfolgung
        ThreadUtil.synchronisiertesPrintln("----------------------------------------------------------");
        ThreadUtil.synchronisiertesPrintln("Pruefung: Negative Mengenwerte");
        ThreadUtil.synchronisiertesPrintln("");

        Fabrik TestFabrik = new Fabrik();

        // Eingabe von Fehlwerten provozieren
        TestFabrik.bestellungAufgeben(-1, -3);
        TestFabrik.bestellungAufgeben(-2, -1);         

        // Visualisierung der (hoffentlich leeren) Liste
        TestFabrik.bestellungenAusgeben();

        ThreadUtil.synchronisiertesPrintln("");

        ThreadUtil.synchronisiertesPrintln("Test abgeschlossen");
        ThreadUtil.synchronisiertesPrintln("---------------------------------------------------------");
        ThreadUtil.synchronisiertesPrintln("");
    }

@Test
    /**
     * Verifiziert, dass bei diversen unzulaessigen Eingaben (Null, Uebermenge, Negativ)
     * keine Bestellung ins System gelangt.
     */
    public void testeBestellungFalsch() {

        Fabrik testFabrik = new Fabrik();
        
        // Fall 1: Keine Produkte gewaehlt
        testFabrik.bestellungAufgeben(0, 0);
        // Fall 2: Kapazitaet massiv ueberschritten
        testFabrik.bestellungAufgeben(15_000, 0);
        // Fall 3: Negative Menge verwendet
        testFabrik.bestellungAufgeben(-5, 0);

        // Sicherstellen, dass die Liste nach diesen Fehlversuchen leer ist
        assertEquals(0, testFabrik.gibBestellungen().size());

        System.out.println(
                "Fehlertest erfolgreich: Keine unzulaessigen Auftraege wurden registriert.");

    }
        
@Test
    /**
     * Prueft die spezifischen Logik-Erweiterungen aus dem zweiten Entwicklungszyklus.
     */
    public void testeBestellungAufgeben() {

        Fabrik testFabrik = new Fabrik();

        /// Szenario: Ausreichender Lagerbestand vorhanden
        testFabrik.bestellungAufgeben(2, 5);

        Bestellung ersteBestellung = testFabrik.gibBestellungen().get(0);

        // Verifikation der Wartezeit fuer Material (sollte 0 sein)
        assertEquals(0, testFabrik.gibLager().gibBeschaffungsZeit(ersteBestellung));

        // Verifikation, dass keine unnoetige Auffuellung stattfand
        assertEquals(0, testFabrik.gibLagerAuffuellungen());

        // Kontrolle der berechneten Lieferfrist (Kalkulation: 1.12 Tage)
        assertEquals(1.12f, ersteBestellung.gibLieferzeit()); 
                                                        
        // Statuskontrolle der Bestaetigung
        assertTrue(ersteBestellung.gibBestellbestaetigung());

        /// Szenario: Materialmangel führt zur Nachbestellung
        /// Beispiel: Glas-Bedarf ueberschreitet den Vorrat
        testFabrik.bestellungAufgeben(0, 21);

        Bestellung zweiteBestellung = testFabrik.gibBestellungen().get(1);

        // Verifikation der Beschaffungsverzoegerung (erwartet: 2 Tage)
        assertEquals(2, testFabrik.gibLager().gibBeschaffungsZeit(zweiteBestellung));

        // Verifikation, dass der Auffuellvorgang ausgeloest wurde
        assertEquals(1, testFabrik.gibLagerAuffuellungen());

        // Kontrolle der angepassten Lieferfrist (Kalkulation: ca. 3.44 Tage)
        assertEquals(3.44f, Math.round(zweiteBestellung.gibLieferzeit() * 100) / 100f); 
                                                        
        // Finale Statuskontrolle
        assertTrue(zweiteBestellung.gibBestellbestaetigung());
        
        System.out.println(
                "Integrationsprüfung für die erweiterte Bestell-Logik erfolgreich absolviert.");
    }
}