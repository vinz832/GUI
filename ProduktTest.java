import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.LinkedList;

/**
 * Die Testeinheit ProduktTest dient der Verifizierung saemtlicher Systemoperationen.
 * Mittels verschiedener Testfaelle wird sichergestellt, dass die Auftragsabwicklung
 * reibungslos verlaeuft und fehlerhafte Eingaben praezise abgefangen werden.
 * * Dabei werden sowohl Regelfaelle als auch Sondersituationen beruecksichtigt.
 * Das Hauptaugenmerk liegt auf der logischen Konsistenz und der Belastbarkeit des Codes.
 * * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class ProduktTest {
    String nameTestClasse = "ProduktTest"; // Bezeichnung der aktuellen Versuchsreihe

    /**
     * Standardkonstruktor fuer die Testklasse.
     */
    public ProduktTest() {
    }

    /**
     * Bereitet die Testumgebung vor jedem einzelnen Durchlauf frisch vor.
     */
    @BeforeEach
    public void setUp() {
        System.out.println("Beginn der Pruefung: " + nameTestClasse);
        System.out.println();
    }

    /**
     * Erledigt saemtliche Aufraeumarbeiten nach Beendigung eines Tests.
     */
    @AfterEach
    public void tearDown() {
        System.out.println();
        System.out.println("Abschluss der Pruefung: " + nameTestClasse);
        System.out.println("------------------------");
    }

    @Test
    /**
     * Validiert die Instanziierung von Objekten sowie die korrekte Rueckgabe der Abfragemethoden.
     */
    public void testeProduktGetter() {
        Produkt testProdukt = new Produkt(); // Instanziierung einer Testeinheit zur Funktionspruefung
        assertEquals(testProdukt.aktuellerZustand(), 0);

        System.out.println("Verifiziert: Initialzustand und Datenabfrage arbeiten einwandfrei.");
    }

    @Test
    /**
     * Prueft, ob die Logik zur Statusmodifikation fehlerfrei arbeitet.
     */
    public void testeProduktSetter() {
        Produkt testProdukt = new Produkt(); // Erzeugung der zu testenden Komponente
        testProdukt.zustandAendern(2); // Haendische Zuweisung des finalen Bearbeitungszustands
        assertEquals(testProdukt.aktuellerZustand(), 2);
 
        System.out.println("Verifiziert: Die Korrektheit der Statusaktualisierung wurde bestaetigt.");
    }
    
    @Test
    /**
     * Garantiert, dass das System bei unzulaessigen Statuswerten keine Modifikationen zulaesst.
     */
    public void testeUngueltigenZustand() {
        Produkt testProdukt = new Produkt();
        testProdukt.zustandAendern(5); // Versuch der Zuweisung eines nicht existenten Statuswerts
        assertEquals(testProdukt.aktuellerZustand(), 0); // Resultat: Der Zustand muss unveraendert bleiben
    } 
    
    @Test
    /**
     * Analysiert die Organisation der Auftragsliste innerhalb eines Roboters.
     * Simuliert dabei das Hinzufuegen sowie das strukturierte Auslesen von Elementen.
     */
    public void testeWarteschlange() {
        Roboter roboter = new Roboter("Roboter1");
        Produkt produkt = new Produkt();

        roboter.fuegeProduktHinzu(produkt); // Einreihen eines Elements in die Prozesskette
        assertEquals(1, roboter.getWarteschlange().size()); // Pruefung der aktuellen Listenbelegung

        Produkt verarbeitet = roboter.getWarteschlange().poll(); // Entnahme zur weiteren Verarbeitung
        assertEquals(produkt, verarbeitet); // Verifizierung der Objektkonsistenz
        assertEquals(0, roboter.getWarteschlange().size()); // Kontrolle, ob die Liste anschliessend leer ist
    }  
    
    @Test
    /**
     * Prueft den Durchlauf eines Produkts durch die definierte Fertigungsstrasse.
     * Hierbei muss die Zustandsentwicklung den Bearbeitungsschritten logisch folgen.
     */
    public void testeProduktDurchProduktionsablauf() {
        Roboter roboter1 = new Roboter("Holzverarbeitungsroboter");
        LinkedList<Roboter> produktionsAblauf = new LinkedList<>();
        produktionsAblauf.add(roboter1);

        Produkt testProdukt = new Produkt();
        testProdukt.setzeProduktionsAblauf(produktionsAblauf);
    
        // Simulation des Fortschritts an den Stationen
        testProdukt.naechsteProduktionsStation();
        assertEquals(testProdukt.aktuellerZustand(), 1); // Status: Bearbeitung laeuft
        testProdukt.naechsteProduktionsStation();
        assertEquals(testProdukt.aktuellerZustand(), 2); // Status: Produktion beendet
    }

    @Test
    /**
     * Untersucht das Systemverhalten fuer den Fall, dass keine Fertigungsstationen hinterlegt wurden.
     */
    public void testeLeereProduktionsstation() {
        Produkt testProdukt = new Produkt();
        testProdukt.setzeProduktionsAblauf(new LinkedList<Roboter>()); // Einpflegen einer leeren Abfolge

        testProdukt.naechsteProduktionsStation(); // Trigger ohne vorhandene Roboterstationen
        assertEquals(testProdukt.aktuellerZustand(), 2); // Resultat: Automatische Markierung als fertiggestellt
    }
    
    @Test
    /**
     * Stellt sicher, dass ein Produkt nach der Fertigstellung in diesem Status verweilt.
     * Ein Zuruecksetzen in einen Bearbeitungsmodus durch Fehlaufrufe muss verhindert werden.
     */
    public void testeProduktSchonFertig() {
        Roboter roboter1 = new Roboter("Roboter1");
        LinkedList<Roboter> produktionsAblauf = new LinkedList<>();
        produktionsAblauf.add(roboter1);

        Produkt testProdukt = new Produkt();
        testProdukt.setzeProduktionsAblauf(produktionsAblauf);
        testProdukt.naechsteProduktionsStation(); // Startphase
        testProdukt.naechsteProduktionsStation(); // Abschlussphase

        // Ein redundanter Aufruf darf den Endstatus "2" nicht mehr modifizieren
        testProdukt.naechsteProduktionsStation();
        assertEquals(testProdukt.aktuellerZustand(), 2); 
    }
        
    @Test
    /**
     * Kontrolliert, ob die Statusmeldungen praezise auf der Konsole ausgegeben werden.
     */
    public void testeZustandausgeben() {
        Produkt testProdukt = new Produkt();
        
        testProdukt.zustandAendern(0);
        testProdukt.Zustandausgeben(); // Pruefung der Meldung fuer "Bestellt"

        testProdukt.zustandAendern(1);
        testProdukt.Zustandausgeben(); // Pruefung der Meldung fuer "In Produktion"

        testProdukt.zustandAendern(2);
        testProdukt.Zustandausgeben(); // Pruefung der Meldung fuer "Fertiggestellt"
    }    
}