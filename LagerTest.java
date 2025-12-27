import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Die Pruefklasse LagerTest dient zur Verifikation diverser Testszenarien, um die Systemstabilität zu garantieren.
 * Es werden unterschiedliche Ablaeufe untersucht, um eine fehlerfreie Auftragsabwicklung sowie eine
 * korrekte Reaktion auf unzulaessige Daten sicherzustellen.
 * * Die Testsuite deckt sowohl reguläre als auch fehlerhafte Zustände ab.
 * * Hauptziel dieser Klasse ist die Validierung der Basisfunktionen, um einen robusten und 
 * stoerungsfreien Betrieb des Gesamtsystems zu gewaehrleisten.
 *
 * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class LagerTest
{
    private Lager lager;
    private Lieferant lieferant;
    private Bestellung bestellung;
    String nameTestClasse = "LagerTest"; // Bezeichnung der aktuellen Testeinheit
    
    /**
     * Standardkonstruktor fuer die Testumgebung LagerTest.
     */
    public LagerTest()
    {
    }

    /**
     * Bereitet die Testumgebung unmittelbar vor jedem Testdurchlauf vor.
     */
    @BeforeEach
    public void setUp() {
        lager = new Lager(); // Neuinitialisierung des Lagers pro Testfall
        bestellung = new Bestellung(1,1,1); // Beispielauftrag: je 1 Tuer-Typ, ID 1
        System.out.println("Start des Testlaufs: " + nameTestClasse);
        System.out.println();
    }

    /**
     * Fuehrt abschliessende Schritte nach jedem Testdurchgang aus.
     */
    @AfterEach
    public void tearDown() {
        System.out.println();
        System.out.println("Abschluss des Testlaufs: " + nameTestClasse);
        System.out.println("------------------------");
    }
    
    @Test
    /** * Validiert die Funktion lagerBestandAusgeben() dahingehend, dass die Bestandsanzeige 
     * ohne Laufzeitfehler ausgefuehrt wird. 
     * Hierbei steht die technische Ausfuehrbarkeit im Vordergrund, nicht die Datenkorrektheit.
     */
    public void testLagerBestandAusgeben() {
        // Durchführung der Bestandsanzeige
        lager.lagerBestandAusgeben();
        // Bestaetigung, dass der Aufruf keine Exceptions hervorruft.
        assertTrue(true, "Die Anzeige der Lagerbestaende verlief reibungslos.");
    }

    @Test
    /** * Prueft die Kombination aus Bestandsanzeige und Auffuellvorgang.
     * Ziel ist es, die visuelle Bestaetigung der Bestandsaenderung vor und nach der 
     * Materialaufstockung zu dokumentieren.
     */
    public void testLagerBestandVorUndNachAuffuellen() {
        System.out.println("Status des Bestands vor der Aufstockung:");
        lager.lagerBestandAusgeben(); 
    
        lager.lagerAuffuellen();
    
        System.out.println("Status des Bestands nach der Aufstockung:");
        lager.lagerBestandAusgeben(); // Visualisierung der aktualisierten Mengen
    }
    
    @Test
    /** * Untersucht die Methode gibBeschaffungsZeit() bei voller Materialverfuegbarkeit.
     * Erwartet wird eine Beschaffungsdauer von Null, da keine Waren nachbestellt werden muessen.
     */
    public void testGibBeschaffungsZeitSofortVerfügbar() {
        // Initialisierung mit ausreichenden Standardwerten
        int beschaffungsZeit = lager.gibBeschaffungsZeit(bestellung);

        // Verifikation: Da alles vorraetig ist, darf keine Zeitverzögerung entstehen.
        assertEquals(0, beschaffungsZeit, 
            "Die Wartezeit muss 0 betragen, wenn saemtliche Ressourcen vorhanden sind.");
    }

    @Test
    /** * Prueft die Kalkulation der Beschaffungszeit bei leerem Depot.
     * Es wird eine Bestellung simuliert, die bei fehlendem Vorrat eine Wartezeit ausloesen muss.
     */
    public void testGibBeschaffungsZeitNichtVerfügbar() {
        // Das Lager wird nicht befuellt
    
        // Definition eines Auftrags, der Material benoetigt
        Bestellung kleineBestellung = new Bestellung(1000, 1000, 1000);
    
        int beschaffungsZeit = lager.gibBeschaffungsZeit(kleineBestellung);
    
        // Erwartung: 2 Tage Wartezeit aufgrund von Materialmangel.
        assertEquals(2, beschaffungsZeit, 
            "Bei fehlenden Ressourcen muss eine Beschaffungsdauer von 2 gemeldet werden.");
    }

    @Test
    /** * Validiert die Zeitberechnung, wenn nur ein Teil der benoetigten Ressourcen lagert.
     * Sobald eine Komponente fehlt, muss der Beschaffungsprozess eingeleitet werden.
     */
    public void testGibBeschaffungsZeitTeilweiseVerfügbar() {
        lager.wareLiefern(); // Sofortige Vollausstattung ohne Zeitverzug
    
        // Erstellung eines uebermaessig grossen Auftrags
        Bestellung grosseBestellung = new Bestellung(1, 1000, 1000); 
    
        int beschaffungsZeit = lager.gibBeschaffungsZeit(grosseBestellung);
    
        // Verifikation: Unvollstaendige Materialdeckung fuehrt zu Wartezeit.
        assertEquals(2, beschaffungsZeit, 
            "Da der Bestand nur teilweise ausreicht, wird eine Zeitdauer von 2 erwartet.");
    }
    
    @Test
    /** * Untersucht den Aufruf von lagerAuffuellen(), wenn das Depot bereits voll ist.
     * Das System sollte erkennen, dass kein Handlungsbedarf besteht.
     */
    public void testLagerAuffuellenOhneNotwendigkeit() {
        // Szenario: Vollbestuecktes Depot
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    
        // Versuch der Bestandsaufstockung
        lager.lagerAuffuellen();
    
        // Analyse der Systemmeldung
        String output = outContent.toString();
    
        // Pruefung auf die korrekte Rueckmeldung bei Vollauslastung
        assertTrue(output.contains("Lager ist bereits voll. Keine Bestellung notwendig."),
                "Das System muss melden, dass keine Nachfuellung erforderlich ist.");
    
        // Standardausgabe wiederherstellen
        System.setOut(System.out);
    }
    
    @Test
    /** * Simuliert den Auffuellvorgang bei einem nahezu erschoepften Bestand.
     */
    public void testLagerFastLeerAuffuellen() {
        // Umleitung der Konsolenausgabe zur Pruefung
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Start der Materialaufstockung
        lager.lagerAuffuellen();

        // Verifikation der Erfolgsmeldung
        String output = outContent.toString();
        assertFalse(output.contains("Lager wurde wieder erfolgreich aufgefüllt!"), 
            "Die Bestaetigung ueber die erfolgreiche Nachfuellung sollte erscheinen.");
        System.setOut(System.out);
    }
    
    @Test
    /** * Kontrolliert, ob der Auffuellvorgang korrekt ablaeuft, ohne irrtuemliche 
     * Bestaetigungen an den Zulieferer zu senden.
     */
    public void testLagerSendetBestellungAnLieferanten() {
        // Erfassung der Konsolenausgaben
        ByteArrayOutputStream outContent = new ByteArrayOutputStream(); 
        System.setOut(new PrintStream(outContent)); 
    
        // Trigger fuer Lageraktualisierung
        lager.lagerAuffuellen();
    
        String output = outContent.toString();
    
        // Sicherstellung der Korrektheit des Vorgangs
        assertFalse(output.contains("Lager wurde wieder erfolgreich aufgefüllt!"), 
            "Die Meldung ueber die Bestandsaktualisierung wird erwartet.");
            
        // Validierung der Kommunikationslogik
        assertFalse(output.contains("Bestellung an Lieferant"), 
            "Es sollte keine direkte Bestaetigung fuer den Lieferanten erscheinen, obwohl das Lager versorgt wurde.");
    
        System.setOut(System.out);
    }
    
    @Test
    void testWareLiefern() {
        // Reduzierung der Bestaende zur Vorbereitung des Tests
        lager.setzeVorhandeneHolzeinheiten(500);
        lager.setzeVorhandeneSchrauben(1000);
        lager.setzeVorhandeneFarbeinheiten(200);
        lager.setzeVorhandeneKartoneinheiten(300);
        lager.setzeVorhandeneGlaseinheiten(50);

        // Pruefung der kuenstlich herbeigefuehrten Mangelzustaende
        assertEquals(500, lager.getVorhandeneHolzeinheiten());
        assertEquals(1000, lager.getVorhandeneSchrauben());
        assertEquals(200, lager.getVorhandeneFarbeinheiten());
        assertEquals(300, lager.getVorhandeneKartoneinheiten());
        assertEquals(50, lager.getVorhandeneGlaseinheiten());

        // Ausfuehrung der Materiallieferung
        lager.wareLiefern();

        // Validierung der Wiederherstellung des Maximalbestands
        assertEquals(Lager.MAXHOLZEINHEITEN, lager.getVorhandeneHolzeinheiten());
        assertEquals(Lager.MAXSCHRAUBEN, lager.getVorhandeneSchrauben());
        assertEquals(Lager.MAXFARBEINHEITEN, lager.getVorhandeneFarbeinheiten());
        assertEquals(Lager.MAXKARTONEINHEITEN, lager.getVorhandeneKartoneinheiten());
        assertEquals(Lager.MAXGLASEINHEITEN, lager.getVorhandeneGlaseinheiten());
    }
    
    @Test
    void testLieferungKommtErstNach48Sekunden() {

        // Manuelle Bestandsminderung zur Ausloesung einer Nachbestellung
        lager.setzeVorhandeneHolzeinheiten(400);
        lager.setzeVorhandeneSchrauben(1000);
        lager.setzeVorhandeneFarbeinheiten(200);
        lager.setzeVorhandeneKartoneinheiten(300);
        lager.setzeVorhandeneGlaseinheiten(50);

        // Sicherstellen: Depot ist aktuell nicht vollstaendig
        assertNotEquals(Lager.MAXHOLZEINHEITEN, lager.getVorhandeneHolzeinheiten());

        // Start des Nachbestellungsprozesses (simulierter Thread braucht ca. 48s)
        lager.lagerAuffuellen();

        // Unmittelbare Pruefung: Lager darf noch nicht aufgefuellt sein (Zeitverzug)
        assertNotEquals(Lager.MAXHOLZEINHEITEN, lager.getVorhandeneHolzeinheiten());

        // Warten auf das Eintreffen der Lieferung (48s plus Puffer)
        ThreadUtil.sleep(49000);

        // Abschliessende Kontrolle: Nun muessen alle Kapazitaeten erschoepft sein
        assertEquals(Lager.MAXHOLZEINHEITEN, lager.getVorhandeneHolzeinheiten());
        assertEquals(Lager.MAXSCHRAUBEN, lager.getVorhandeneSchrauben());
        assertEquals(Lager.MAXFARBEINHEITEN, lager.getVorhandeneFarbeinheiten());
        assertEquals(Lager.MAXKARTONEINHEITEN, lager.getVorhandeneKartoneinheiten());
        assertEquals(Lager.MAXGLASEINHEITEN, lager.getVorhandeneGlaseinheiten());
    }
    
    @Test
    void testLagerAuffuellenKeinBedarf() {
        // Bestand auf Hoechstwerte setzen
        lager.setzeVorhandeneHolzeinheiten(Lager.MAXHOLZEINHEITEN);
        lager.setzeVorhandeneSchrauben(Lager.MAXSCHRAUBEN);
        lager.setzeVorhandeneFarbeinheiten(Lager.MAXFARBEINHEITEN);
        lager.setzeVorhandeneKartoneinheiten(Lager.MAXKARTONEINHEITEN);
        lager.setzeVorhandeneGlaseinheiten(Lager.MAXGLASEINHEITEN);

        // Verifikation der Ausgangslage
        assertEquals(Lager.MAXHOLZEINHEITEN, lager.getVorhandeneHolzeinheiten());
        assertEquals(Lager.MAXSCHRAUBEN, lager.getVorhandeneSchrauben());
        assertEquals(Lager.MAXFARBEINHEITEN, lager.getVorhandeneFarbeinheiten());
        assertEquals(Lager.MAXKARTONEINHEITEN, lager.getVorhandeneKartoneinheiten());
        assertEquals(Lager.MAXGLASEINHEITEN, lager.getVorhandeneGlaseinheiten());

        // Versuch des Auffuellens (darf keine Transaktion ausloesen)
        lager.lagerAuffuellen();

        // Verifikation: Werte muessen unveraendert bleiben
        assertEquals(Lager.MAXHOLZEINHEITEN, lager.getVorhandeneHolzeinheiten());
        assertEquals(Lager.MAXSCHRAUBEN, lager.getVorhandeneSchrauben());
        assertEquals(Lager.MAXFARBEINHEITEN, lager.getVorhandeneFarbeinheiten());
        assertEquals(Lager.MAXKARTONEINHEITEN, lager.getVorhandeneKartoneinheiten());
        assertEquals(Lager.MAXGLASEINHEITEN, lager.getVorhandeneGlaseinheiten());
    }
}