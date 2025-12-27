import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Diese Testumgebung dient der Ueberpruefung saemtlicher Prozessablaeufe innerhalb der Standardmodelle. 
 * Es werden diverse Szenarien durchlaufen, um die korrekte Verarbeitung von Auftraegen sicherzustellen 
 * und eine angemessene Handhabung sowie Rueckmeldung bei fehlerhaften Eingaben zu garantieren.
 * * Die Pruefverfahren decken sowohl reguläre Erfolgsfaelle als auch kritische Fehlerszenarien ab.
 * * Primär dient diese Klasse der Absicherung der Basisfunktionalitaeten, um ein stabiles 
 * und fehlerfreies Systemverhalten zu gewaehrleisten.
 *
 * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class StandardtuerTest {
    String nameTestClasse = "StandardtuerTest"; // Bezeichnung der vorliegenden Testeinheit

    /**
     * Initialer Konstruktor fuer die Testklasse StandardtuerTest.
     */
    public StandardtuerTest() {
    }

    /**
     * Vorbereitungsschritte, die unmittelbar vor jedem einzelnen Pruefdurchgang ausgefuehrt werden.
     */
    @BeforeEach
    public void setUp() {
        System.out.println("Start des Testlaufs: " + nameTestClasse);
        System.out.println();
    }

    /**
     * Abschlussroutinen, die nach Beendigung jedes Testdurchgangs erfolgen.
     */
    @AfterEach
    public void tearDown() {
        System.out.println();
        System.out.println("Ende des Testlaufs: " + nameTestClasse);
        System.out.println("------------------------");
    }

    @Test
    /**
     * Validiert die statischen Zugriffsmethoden fuer Materialwerte und Produktionszeiten.
     */
    public void testeStandardtuerGetters() {
        assertEquals(Standardtuer.getHolzeinheiten(), 2);
        assertEquals(Standardtuer.getSchrauben(), 10);
        assertEquals(Standardtuer.getFarbeinheiten(), 2);
        assertEquals(Standardtuer.getKartoneinheiten(), 1);
        assertEquals(Standardtuer.getProduktionszeit(), 10);

        System.out.println("Abfrage der Standardtuer-Parameter erfolgreich abgeschlossen");
    }

    @Test
    /**
     * Verifiziert die korrekte Instanziierung sowie die Datenabfrage der Oberklasse Produkt.
     */
    public void testeProduktGetter() {

        Standardtuer testStandardtuer = new Standardtuer();
        assertEquals(testStandardtuer.aktuellerZustand(), 0);

        System.out.println("Bestaetigung: Datenabfrage und Status-Initialisierung des Produkts arbeiten fehlerfrei.");

    }

    @Test
    /**
     * Kontrolliert die Funktionalitaet der Statusaenderung innerhalb der Oberklasse Produkt.
     */
    public void testeProduktSetter() {

        Standardtuer testStandardtuer = new Standardtuer();
        testStandardtuer.zustandAendern(2);
        assertEquals(testStandardtuer.aktuellerZustand(), 2);

        System.out.println("Bestaetigung: Die Statusaktualisierung des Produkts ist funktionsfaehig.");

    }
}