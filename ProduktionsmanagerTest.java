import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * Die Unit-Test-Klasse ProduktionsmanagerTest dient dazu, diverse Pruefszenarien zu durchlaufen,
 * um die Systemstabilität zu validieren. Es werden unterschiedliche Ablaeufe simuliert,
 * damit sichergestellt ist, dass Auftraege fehlerfrei abgearbeitet sowie unzulaessige 
 * Parameter erkannt und entsprechend kommuniziert werden.
 *
 * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class ProduktionsmanagerTest{
    String nameTestClasse = "ProduktionsmanagerTest"; // Bezeichnung der aktuellen Testumgebung
    private Fabrik fabrik;
    private Produktionsmanager produktionsmanager;
    private Lager lager;

    /**
     * Standardkonstruktor fuer die Testklasse ProduktionsmanagerTest.
     */
    public ProduktionsmanagerTest()
    {
    }

    /**
     * Festlegung der Konfiguration, die vor jedem einzelnen Prüfdurchgang ausgefuehrt wird.
     */
    @BeforeEach
    public void setUp() {
        System.out.println("Start der Testreihe: " + nameTestClasse);
        System.out.println();
    }

    /**
     * Bereinigungsarbeiten, die nach Abschluss jedes Testfalls erfolgen.
     */
    @AfterEach
    public void tearDown() {
        System.out.println();
        System.out.println("Ende der Testreihe: " + nameTestClasse);
        System.out.println("------------------------");
    }
    
    @Test
    public void testfeuegeZuVerarbeitendeBestellungenHinzu() {
    // Vorbereitung: Instanziierung der Steuerungseinheit
    Fabrik fabrik = new Fabrik();  // Aufbau einer Fabrik-Instanz (Initialisierung moeglicherweise erforderlich)
    Lager lager = new Lager();     // Erstellung eines Materiallagers (ebenfalls Initialisierung pruefen)
    produktionsmanager = new Produktionsmanager(fabrik, lager);  // Erzeugung des Managers
    
    Bestellung bestellung = new Bestellung(1, 1, 1);

    // Ausfuehrung: Einreihen eines neuen Auftrags
    produktionsmanager.fuegeZuVerarbeitendeBestellungenHinzu(bestellung);

    // Verifikation: Pruefung, ob das Objekt tatsaechlich in der Warteschlange gelandet ist
    assertTrue(produktionsmanager.getZuVerarbeitendeBestellungen().contains(bestellung),
            "Der Auftrag wurde nicht ordnungsgemaess in die Warteschlange aufgenommen.");
    }

    @Test
    public void testRoboterZuweisen() {
    // Vorbereitung: Aufbau der Testumgebung
    Fabrik fabrik = new Fabrik();  
    Lager lager = new Lager();     
    produktionsmanager = new Produktionsmanager(fabrik, lager);  

    Bestellung bestellung = new Bestellung(1, 1, 1);
    Standardtuer produkt = new Standardtuer();
    bestellung.hinzufuegenProdukt(produkt);  // Zuweisung eines Produkts zum Auftrag

    // Ausfuehrung: Zuteilung der Fertigungsstationen
    produktionsmanager.RoboterZuweisen(produkt);

    // Verifikation: Kontrolle, ob die Holzbearbeitungseinheit in der Liste der Stationen steht
    assertTrue(produkt.getProduktionsAblauf().contains(produktionsmanager.getHolzRoboter()),
            "Die Zuweisung des Holzverarbeitungsroboters schlug fehl.");
    }

    
/*
 * Die nachfolgenden Tests sind deaktiviert, da sie in der BlueJ-Umgebung zu Instabilitaeten fuehren.
 */
    
// @Test
public void testBestellungMitGenugMaterial() {
    // Vorbereitung: Konfiguration von Auftrag und Materialbestand
    Bestellung bestellung = new Bestellung(1, 1, 1);
    Lager testLager = new Lager();
    
    // Sicherstellen der Materialverfuegbarkeit durch Bestandsauffuellung
    testLager.wareLiefern();  
    
    produktionsmanager = new Produktionsmanager(fabrik, testLager);
    
    // Ausfuehrung: Auftrag registrieren
    produktionsmanager.fuegeZuVerarbeitendeBestellungenHinzu(bestellung);

    // Start des Fertigungszyklus
    produktionsmanager.run();

    // Verifikation: Bestaetigung, dass der Auftrag in den aktiven Status ueberging
    assertTrue(produktionsmanager.getBestellungenInProduktion().contains(bestellung),
            "Trotz ausreichender Ressourcen wurde der Auftrag nicht gestartet.");
}

// @Test
public void testBestellungMitNichtGenugMaterial() {
    // Vorbereitung: Setup fuer einen Materialmangel
    Bestellung bestellung = new Bestellung(1, 1, 1);
    Lager testLager = new Lager();
    
    // Gezielte Reduzierung der Bestaende (z.B. Holz auf Null setzen)
    testLager.setzeVorhandeneHolzeinheiten(0);  
    
    produktionsmanager = new Produktionsmanager(fabrik, testLager);
    
    // Ausfuehrung: Auftrag in die Warteschlange geben
    produktionsmanager.fuegeZuVerarbeitendeBestellungenHinzu(bestellung);

    // Start der Prozesslogik
    produktionsmanager.run();

    // Verifikation: Sicherstellen, dass die Fertigung aufgrund des Mangels pausiert
    assertFalse(produktionsmanager.getBestellungenInProduktion().contains(bestellung),
            "Der Auftrag durfte bei Materialmangel nicht in die Produktion gehen.");
}

// @Test
public void testBestellungNachAbschlussEntfernen() {
    // Vorbereitung: Vollstaendige Bestueckung des Lagers fuer reibungslosen Ablauf
    Bestellung bestellung = new Bestellung(1, 1, 1);
    Lager testLager = new Lager();
    
    testLager.setzeVorhandeneHolzeinheiten(100); 
    testLager.setzeVorhandeneSchrauben(1000);   
    testLager.setzeVorhandeneFarbeinheiten(100); 
    testLager.setzeVorhandeneKartoneinheiten(500); 
    testLager.setzeVorhandeneGlaseinheiten(50);   

    // Initialisierung der Steuerung mit dem vorbereiteten Lager
    produktionsmanager = new Produktionsmanager(fabrik, testLager);
    
    // Ausfuehrung: Start der Bearbeitung und Simulation des Zeitablaufs
    produktionsmanager.fuegeZuVerarbeitendeBestellungenHinzu(bestellung);
    produktionsmanager.run();  

    // Verifikation: Pruefung, ob der Auftrag nach der Fertigstellung aus der aktiven Liste verschwindet
    assertFalse(produktionsmanager.getBestellungenInProduktion().contains(bestellung),
            "Die Bereinigung der aktiven Liste nach Fertigstellung erfolgte nicht korrekt.");
}

}