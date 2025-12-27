import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Diese Pruefklasse validiert die verschiedenen Leistungsmerkmale der Premiumtuer. 
 * Anhand gezielter Ablaeufe wird sichergestellt, dass die Fertigungsprozesse 
 * einwandfrei funktionieren und das System stabil auf unterschiedliche Szenarien reagiert.
 * * Das Pruefspektrum umfasst sowohl reguläre Ablaeufe als auch die Untersuchung 
 * von Fehlermoeglichkeiten. 
 * * Im Kern dient die Klasse der Qualitaetssicherung der Basisfunktionen, um 
 * eine kontinuierliche Zuverlaessigkeit der Anwendung zu garantieren.
 *
 * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class PremiumtuerTest {
    String nameTestClasse = "PremiumtuerTest"; // Name der vorliegenden Testkonfiguration

    /**
     * Allgemeiner Konstruktor fuer diese Testeinheit.
     */
    public PremiumtuerTest() {
    }

    /**
     * Einrichtung der Testbasis und Protokollierung vor jedem Pruefschritt.
     */
    @BeforeEach
    public void setUp() {
        System.out.println("Start des Testlaufs: " + nameTestClasse);
        System.out.println();
    }

    /**
     * Aufraeumarbeiten und Formatierung der Konsole nach Abschluss eines Laufs.
     */
    @AfterEach
    public void tearDown() {
        System.out.println();
        System.out.println("Abschluss des Testlaufs: " + nameTestClasse);
        System.out.println("------------------------");
    }

    @Test
    /**
     * Validierung der festen Materialvorgaben. 
     * Hierbei wird abgeglichen, ob die hinterlegten Konstanten fuer Holz, Eisenwaren, 
     * Glas, Farbmittel, Verpackungsmaterial und die Fertigungsdauer korrekt sind.
     */
    public void testePremiumtuerGetters() {
        assertEquals(Premiumtuer.getHolzeinheiten(), 4);
        assertEquals(Premiumtuer.getSchrauben(), 5);
        assertEquals(Premiumtuer.getGlaseinheiten(), 5);
        assertEquals(Premiumtuer.getFarbeinheiten(), 1);
        assertEquals(Premiumtuer.getKartoneinheiten(), 5);
        assertEquals(Premiumtuer.getProduktionszeit(), 30);

        System.out.println("Datenabfrage der Premiumtuer-Parameter erfolgreich.");
    }

    @Test
    /**
     * Kontrolle der Standardwerte, welche durch Vererbung von der Klasse Produkt stammen. 
     * Das Ziel ist die Bestaetigung des ordnungsgemassen Ausgangszustands bei der Instanziierung.
     */
    public void testeProduktGetter() {

        Premiumtuer testPremiumtuer = new Premiumtuer();
        // Erwartet wird der Initialzustand 0 (Bestellt)
        assertEquals(testPremiumtuer.aktuellerZustand(), 0);

        System.out.println("Validierung der ererbten Getter und der Status-Initialisierung erfolgreich.");

    }

    @Test
    /**
     * Untersuchung der Methoden zur Zustandsmodifikation (geerbt von Produkt). 
     * Es wird verifiziert, ob sich der Bearbeitungsstatus einer Tuer wunschgemaess anpassen laesst.
     */
    public void testeProduktSetter() {

        Premiumtuer testPremiumtuer = new Premiumtuer();
        testPremiumtuer.zustandAendern(2); // Setze Status auf "Fertiggestellt"
        assertEquals(testPremiumtuer.aktuellerZustand(), 2);

        System.out.println("Funktionstest der Status-Aktualisierung (Setter) erfolgreich.");

    }
}