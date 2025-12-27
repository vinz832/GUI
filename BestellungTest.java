import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

/**
 * Die Pruefinstanz BestellungTest dient der Kontrolle verschiedener Ablaeufe innerhalb der Auftragsverwaltung.
 * In dieser Klasse wird verifiziert, ob Auftraege fehlerfrei abgearbeitet werden und ob die Systemlogik
 * bei unzulaessigen Werten mit den vorgesehenen Routinen reagiert.
 * * Dabei werden sowohl reguläre Standardfaelle als auch kritische Randbedingungen untersucht.
 * * Die Intention dieser Testklasse ist die Absicherung der zentralen Programmlogik fuer einen verlaesslichen Betrieb.
 *
 * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class BestellungTest {
    String nameTestClasse = "BestellungTest"; // Identifikationsname der Testumgebung

    /**
     * Allgemeiner Konstruktor fuer die Testklasse BestellungTest.
     */
    public BestellungTest() {
    }

    /**
     * Vorbereitende Massnahmen, die unmittelbar vor jedem einzelnen Testdurchlauf stattfinden.
     */
    @BeforeEach
    public void setUp() {
        System.out.println("Beginn der Testreihe: " + nameTestClasse);
        System.out.println();
    }

    /**
     * Schlussarbeiten und Dokumentationsschritte nach der Beendigung eines Testfalls.
     */
    @AfterEach
    public void tearDown() {
        System.out.println();
        System.out.println("Ende der Testreihe: " + nameTestClasse);
        System.out.println("------------------------");
    }

    @Test
    /**
     * Prüft die Funktionalitaet der Methode bestellungBestaetigen(). 
     * Hierbei wird kontrolliert, ob der Status der Freigabe von 'false' auf 'true' springt, 
     * sobald die Aktivierung erfolgt.
     */
    public void testeBestellungBestaetigen() {
    
        // Instanziierung eines Objekts zu Testzwecken
        Bestellung testBestellung = new Bestellung(5, 7, 2);

        assertEquals(testBestellung.gibBestellbestaetigung(), false);
        testBestellung.bestellungBestaetigen();
        assertEquals(testBestellung.gibBestellbestaetigung(), true);

        System.out.println("Die Verifizierung der Freigabefunktion verlief erfolgreich.");

    }
    
    @Test
    /**
     * Kontrolliert die fehlerfreie Erstellung eines neuen Auftrags.
     * Stellt sicher, dass die im Konstruktor angegebenen Daten exakt in den Feldern hinterlegt werden.
     */
    public void testeBestellung() {

        // Erzeugung einer Test-Order
        Bestellung testBestellung = new Bestellung(2, 5, 7);

        assertEquals(testBestellung.gibBestellNr(), 2);
        assertEquals(testBestellung.gibAnzahlStandardtueren(), 5);
        assertEquals(testBestellung.gibAnzahlPremiumtueren(), 7);
        
        // Ueberpruefung der voreingestellten Initialwerte bei der Objekterzeugung
        assertEquals(testBestellung.gibBestellbestaetigung(), false);
        assertEquals(testBestellung.gibBeschaffungsZeit(), -1);

        System.out.println(
                "Der Test der Initialisierung mit Vorgabewerten und Parametern war erfolgreich.");

    }
    
    @Test
    /**
     * Untersucht die Schreib- und Leseoperationen fuer das Zeitintervall der Materialbeschaffung.
     * Garantiert, dass eingegebene Zeitspannen korrekt vom System registriert werden.
     */
    public void testeSetzeBeschaffungsZeit() {

        // Erzeugung einer Test-Order
        Bestellung testBestellung = new Bestellung(5, 7, 2);

        assertEquals(testBestellung.gibBeschaffungsZeit(), -1);
        testBestellung.setzeBeschaffungsZeit(2);
        assertEquals(testBestellung.gibBeschaffungsZeit(), 2);

        System.out.println("Erfolgreicher Funktionstest fuer das Setzen der Beschaffungsdauer.");

    }
    
    @Test
    /**
     * Verifiziert das Abrufen des aktuellen Bestaetigungsgrades.
     * Prueft, ob die Statusänderung nach erfolgter Freigabe korrekt im System abgebildet wird.
     */
    public void testegibBestellbestaetigung() {

        // Erzeugung einer Test-Order
        Bestellung testBestellung = new Bestellung(5, 7, 2);

        assertEquals(testBestellung.gibBestellbestaetigung(), false);
        testBestellung.bestellungBestaetigen();
        assertEquals(testBestellung.gibBestellbestaetigung(), true);

        System.out.println(
                "Statuskontrolle der Auftragsbestaetigung erfolgreich abgeschlossen.");

    }
    
    @Test
    /**
     * Kontrolliert die ordnungsgemaesse Zuweisung der Lieferfrist mittels der entsprechenden Systemmethode.
     */
    public void testeSetzeLieferzeit() {

        // Erzeugung einer Test-Order
        Bestellung testBestellung = new Bestellung(5, 7, 2);

        assertEquals(testBestellung.gibLieferzeit(), -1);
        testBestellung.setzeLieferzeit(2);
        assertEquals(testBestellung.gibLieferzeit(), 2);

        System.out.println("Testdurchlauf zur Festlegung der Lieferfrist erfolgreich.");

    }
    
    @Test
    /**
     * Analysiert die automatische Bestueckung des Produktverzeichnisses.
     * Prueft die Gesamtmenge sowie die korrekten Datentypen innerhalb der Liste.
     */
    public void testeFuelleBestellteProdukte() {
        Bestellung testBestellung = new Bestellung(1, 3, 2);
        ArrayList<Produkt> produkte = testBestellung.liefereBestellteProdukte();
        
        assertEquals(5, produkte.size());
        assertTrue(produkte.get(0) instanceof Standardtuer);
        assertTrue(produkte.get(3) instanceof Premiumtuer);
    }
    
    @Test
    /**
     * Untersucht den Abschluss der Fertigungsphase fuer saemtliche Positionen eines Auftrags.
     */
    public void testeSetzeAlleProdukteProduziert() {
        Bestellung testBestellung = new Bestellung(1, 2, 3);
        testBestellung.setzeAlleProdukteProduziert();
        // Die Bestaetigung erfolgt hier primär durch den fehlerfreien Ablauf der Methode.
    }
    
    @Test
    /**
     * Kombinierte Pruefung der Zugriffsmethoden fuer die Dauer der Materialbeschaffung.
     */
    public void testeBeschaffungsZeitGetterUndSetter() {
        Bestellung testBestellung = new Bestellung(1, 2, 3);
        
        // Kontrolle des Initialwertes
        assertEquals(-1, testBestellung.gibBeschaffungsZeit());

        // Modifikation und erneute Pruefung
        testBestellung.setzeBeschaffungsZeit(5);
        assertEquals(5, testBestellung.gibBeschaffungsZeit());
    }
    
    @Test
    /**
     * Validiert die korrekte Verarbeitung von Dezimalzahlen im Kontext der Lieferdauer.
     */
    public void testeLieferzeitGetterUndSetter() {
        Bestellung testBestellung = new Bestellung(1, 2, 3);
        
        // Ausgangswert verifizieren
        assertEquals(-1, testBestellung.gibLieferzeit());

        // Neuen Wert zuweisen und Korrektheit prüfen
        testBestellung.setzeLieferzeit(3.5f);
        assertEquals(3.5f, testBestellung.gibLieferzeit());
    }
    
    @Test
    /**
     * Garantiert, dass die Abfragen der Stückzahlen fuer die jeweiligen Tuerkategorien praezise Daten liefern.
     */
    public void testeGetterAnzahlTueren() {
        Bestellung testBestellung = new Bestellung(1, 4, 6);
        
        // Abgleich der Soll-Mengen mit den Ist-Werten
        assertEquals(4, testBestellung.gibAnzahlStandardtueren());
        assertEquals(6, testBestellung.gibAnzahlPremiumtueren());
    }
    
    @Test
    /**
     * Prueft die logische Abfolge zwischen der Statusänderung und der Abfrage der Auftragsfreigabe.
     */
    public void testeBestellbestaetigungGetterUndSetter() {
        Bestellung testBestellung = new Bestellung(1, 2, 3);

        // Der Status muss anfangs negativ sein
        assertFalse(testBestellung.gibBestellbestaetigung());

        // Nach der Bestätigung muss der Status positiv sein
        testBestellung.bestellungBestaetigen();
        assertTrue(testBestellung.gibBestellbestaetigung());
    }
    
    @Test
    /**
     * Stellt sicher, dass die Funktion zur Bereitstellung der Produktliste eine integre Struktur zurueckgibt.
     */
    public void testeLiefereBestellteProdukte() {
        Bestellung testBestellung = new Bestellung(1, 2, 3);
        
        ArrayList<Produkt> produkte = testBestellung.liefereBestellteProdukte();
        
        // Validierung der Listengroesse und Inhalte
        assertEquals(5, produkte.size());
        assertTrue(produkte.get(0) instanceof Standardtuer);
        assertTrue(produkte.get(3) instanceof Premiumtuer);
    }
    
    @Test
    /**
     * Kontrolliert den statischen Abruf der globalen Standardlieferfrist.
     */
    public void testeStandardlieferzeitGetter() {
        // Der hinterlegte Wert von 1 Tag wird hier verifiziert.
        assertEquals(1, Bestellung.gibStandardlieferzeit());
    }    
}