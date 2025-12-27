import java.util.ArrayList;

/**
 * Diese Klasse organisiert saemtliche Informationen zu einem Auftrag innerhalb der Fertigung.
 * Sie registriert die Mengen fuer Standard- sowie Luxusmodelle.
 * Weiterhin werden zentrale Parameter wie die Identitätsnummer, der Freigabestatus 
 * sowie die kalkulierte Dauer fuer den Materialeinkauf verwaltet. Jede Order besitzt
 * eine eindeutige Kennung. Zur strukturierten Ablage der Posten wird eine ArrayList verwendet.
 * * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */

public class Bestellung
{
    // Attribute zur Definition eines Fertigungsauftrags
    private ArrayList<Produkt> bestellteProdukte; // Verzeichnis der angeforderten Waren (beide Varianten)
    private boolean bestellBestaetigung; // Statusvariable, die angibt, ob die Order bereits freigegeben wurde
    private int bestellNr; // Exklusive Identifikationsnummer des Vorgangs
    private int beschaffungsZeit; // Benoetigtes Zeitintervall fuer den Materialeinkauf in Tagen
    private int anzahlStandardtueren; // Gesamtzahl der georderten Basismodelle
    private int anzahlPremiumtueren; // Gesamtzahl der georderten Edelmodelle
    private float lieferZeit; // Errechneter Zeitraum bis zur Fertigstellung und Zustellung
    // Kundenbezug
    private final int kundenNr;
    // Timestamps for KPI Durchlaufzeit (simulation time in millis)
    private long createdAt;
    private Long productionStartAt;
    private Long finishedAt;
    // Guard to prevent double material consumption per order
    private boolean materialVerbraucht;
    // Cancellation flag
    private boolean storniert;
    
    private Standardtuer standardtuer; // Temporaeres Objekt fuer eine Basistuer beim Befuellen
    private Premiumtuer premiumtuer; // Temporaeres Objekt fuer eine Luxustuer beim Befuellen
    private boolean alleProdukteProduziert; // Zeiger, ob die komplette Fertigung abgeschlossen ist
    private static int standardlieferzeit = 1; // Fixer Basiswert fuer die Zustellung in Tagen

    /**
     * Konstruiert eine neue Order mit einer ID und den festzulegenden Mengenwerten.
     * * @param bestellNr Die spezifische Identifikationsnummer
     * @param anzahlStandardtueren Gewuenschte Anzahl der Standardausfuehrungen
     * @param anzahlPremiumtueren Gewuenschte Anzahl der Premiumausfuehrungen
     * * Wichtig: Eine Bestellung ohne jeglichen Inhalt wird als ungueltig eingestuft.
     */
    public Bestellung(int bestellNr, int anzahlStandardtueren, int anzahlPremiumtueren) {
        this.bestellteProdukte = new ArrayList<>(); // Reserviert Speicher fuer das Produktverzeichnis
        this.bestellNr = bestellNr; // Festlegung der Auftrags-ID
        this.beschaffungsZeit = -1; // Initialwert vor der exakten Berechnung
        this.lieferZeit = -1; // Platzhalter fuer die Dauer der Auslieferung
        this.kundenNr = 0; // Default, wenn keine Kundennummer übergeben wurde
        
        // Der Status fuer die Freigabe ist initial negativ, da die Validierung noch folgt
        this.bestellBestaetigung = false; 
        this.alleProdukteProduziert = false; // Zu Projektbeginn ist die Fertigung noch offen
        this.anzahlStandardtueren = anzahlStandardtueren;
        this.anzahlPremiumtueren = anzahlPremiumtueren;
        this.createdAt = System.currentTimeMillis();
        this.materialVerbraucht = false;
        this.storniert = false;
        fuelleBestellteprodukte(anzahlStandardtueren, anzahlPremiumtueren);  // Bestueckt die Liste gemaess der Order 
    }

    /**
     * Erweiterter Konstruktor mit Kundennummer.
     */
    public Bestellung(int bestellNr, int kundenNr, int anzahlStandardtueren, int anzahlPremiumtueren) {
        this.bestellteProdukte = new ArrayList<>();
        this.bestellNr = bestellNr;
        this.beschaffungsZeit = -1;
        this.lieferZeit = -1;
        this.kundenNr = kundenNr;
        this.bestellBestaetigung = false;
        this.alleProdukteProduziert = false;
        this.anzahlStandardtueren = anzahlStandardtueren;
        this.anzahlPremiumtueren = anzahlPremiumtueren;
        this.createdAt = System.currentTimeMillis();
        this.materialVerbraucht = false;
        this.storniert = false;
        fuelleBestellteprodukte(anzahlStandardtueren, anzahlPremiumtueren);
    }

    /**
     * Instanziiert die entsprechenden Objekte fuer Basis- und Luxusmodelle und 
     * fuegt diese dem internen Verzeichnis hinzu.
     * * @param anzahlStandardTueren Zielmenge an Standardmodellen
     * @param anzahlPremiumTueren  Zielmenge an Premiummodellen
     */
    private void fuelleBestellteprodukte(int anzahlStandardTueren, int anzahlPremiumTueren) {

        int standardTueren = 0;
        int premiumTueren = 0;

        while (standardTueren < anzahlStandardTueren) { // Schleife bis zur Erreichung der Standard-Sollmenge
            bestellteProdukte.add(new Standardtuer());
            standardTueren++; // Zaehlvariable erhoehen
        }

        while (premiumTueren < anzahlPremiumTueren) { // Schleife bis zur Erreichung der Premium-Sollmenge
            bestellteProdukte.add(new Premiumtuer());
            premiumTueren++; // Zaehlvariable erhoehen
        }
    }
    
    /**
     * Gibt das Verzeichnis saemtlicher zum Auftrag gehoerenden Produkte aus.
     * * @return ArrayList mit Elementen des Typs Produkt.
     */     
    public ArrayList<Produkt> liefereBestellteProdukte()
    {
        return this.bestellteProdukte;   
    } 
    
    /**
     * Verifiziert intern, ob die Mengenangaben logisch sinnvoll sind.
     * * @param anzahlStandardtueren Quantitaet der Standardmodelle
     * @param anzahlPremiumtueren  Quantitaet der Premiummodelle
     * * @return true, wenn die Werte nicht negativ sind und mindestens ein Artikel gewaehlt wurde.
     */
    private boolean istGueltigeAnzahl(int anzahlStandardtueren, int anzahlPremiumtueren) {  
        // Prueft auf positive Werte und stellt sicher, dass die Order nicht leer ist
        return anzahlStandardtueren >= 0 && anzahlPremiumtueren >= 0 &&
               !(anzahlStandardtueren == 0 && anzahlPremiumtueren == 0);
    }

    /**
     * Markiert den Auftrag offiziell als "bestaetigt".
     */
    public void bestellungBestaetigen() {
        this.bestellBestaetigung = true;
    }
    
    /**
     * Gibt Auskunft ueber den aktuellen Bestaetigungsstatus der Order.
     * * @return Logikwert der Auftragsbestaetigung.
     */
    public boolean gibBestellbestaetigung() {
        return this.bestellBestaetigung;
    }
    
    /**
     * Fragt die hinterlegte Kennnummer des Auftrags ab.
     * * @return Die numerische Bestell-ID.
     */
    public int gibBestellNr() {
        return this.bestellNr;
    }

    /**
     * Liefert die Kundennummer dieser Bestellung (0 falls unbekannt).
     */
    public int gibKundenNr() { return this.kundenNr; }
    
    /**
     * Ruft den Zeitraum ab, der fuer den Materialeinkauf kalkuliert wurde.
     * * @return Anzahl der Tage fuer die Beschaffung.
     */
    public int gibBeschaffungsZeit() {
        return this.beschaffungsZeit; // Rueckgabe des berechneten Zeitraums
    }
    
    /**
     * Definiert die benoetigte Zeitspanne fuer den Materialeinkauf.
     * * @param beschaffungsZeit Dauer in Tagen.
     */
    public void setzeBeschaffungsZeit(int beschaffungsZeit) {
        this.beschaffungsZeit = beschaffungsZeit;
    }
    
    /**
     * Liefert die Menge der im Auftrag enthaltenen Premium-Modelle.
     * * @return Stuckzahl der Luxustueren.
     */
    public int gibAnzahlPremiumtueren() {
        return this.anzahlPremiumtueren; // Rueckgabe des Mengenwerts
    }
    
    /**
     * Liefert die Menge der im Auftrag enthaltenen Standard-Modelle.
     * * @return Stuckzahl der Basistueren.
     */
    public int gibAnzahlStandardtueren() {
        return this.anzahlStandardtueren; // Rueckgabe des Mengenwerts
    }
    
    /**
     * Visualisiert alle relevanten Auftragsdetails uebersichtlich im Ausgabefenster.
     * Enthaelt ID, Mengen, Freigabestatus sowie detaillierte Angaben zu jeder Position.
     */
    public void bestellungAusgeben() {
        System.out.println(this.toString());  // Ausgabe der Rahmendaten
        if (this.bestellBestaetigung) { // Statusabfrage zur Autorisierung
            System.out.println("Bestellung bestätigt: Ja");
        } else {
            System.out.println("Bestellung bestätigt: Nein");
        }

        System.out.println("#### Details zu den Artikeln ####"); // Trenner fuer die Einzelposten
        int zaehler = 0;
        System.out.println(this.bestellteProdukte);
        System.out.println(this.bestellteProdukte.size());
        
        // Iteriert durch das Verzeichnis und gibt den Status jeder einzelnen Tuer aus
        for (Produkt p : this.bestellteProdukte) {
            System.out.println("Tür-Index: " + zaehler);  // Position innerhalb der Liste
            String tuerTyp = p.getClass().getName(); // Ermittlung des spezifischen Typs
            System.out.print("[" + tuerTyp + "]: ");
            p.Zustandausgeben(); // Statusmeldung des jeweiligen Produkts
            zaehler++; // Index inkrementieren
        }
        System.out.println(); // Leerzeile fuer eine bessere Strukturierung
    }

    /** * Instanziiert eine neue Standardtuer und fuegt sie dem Auftrag hinzu. 
     */ 
    private void standardtuerHinzufuegen()
    {
         standardtuer = new Standardtuer();
         bestellteProdukte.add(standardtuer);        
    }  
    
    /** * Instanziiert eine neue Premiumtuer und fuegt sie dem Auftrag hinzu.
     */
    private void PremiumtuerHinzufuegen()
    {
        premiumtuer = new Premiumtuer();
        bestellteProdukte.add(premiumtuer);          
    }
    
    @Override
    public String toString() { // Erstellt eine kompakte Textreprasentation der Order
        String kundeStr = (kundenNr > 0 ? (" | Kunde #" + kundenNr) : "");
        return "Bestellung: " + bestellNr + " | " + anzahlStandardtueren + " Standard, " + anzahlPremiumtueren + " Premium" + " | Beschaffungsdauer: "
                + beschaffungsZeit + " Tage" + kundeStr;
    }
    
    /**
     * Setzt die spezifische Zeitspanne fuer die Zustellung fest, die je nach Auftrag
     * variieren kann (beispielsweise durch Verzoegerungen im Einkauf). 
     * * @param lieferZeit Zeitwert fuer die Auslieferung.
     */
    public void setzeLieferzeit(float lieferZeit)
    {
        this.lieferZeit = lieferZeit;
    }
    
    /**
     * Ruft die kalkulierte Zustellungsdauer ab.
     * * @return Dauer der Lieferung.
     */
    public float gibLieferzeit()
    {
        return this.lieferZeit;
    }
    
    /**
     * Markiert saemtliche Artikel dieses Auftrags als fertig produziert und meldet 
     * die Versandbereitschaft der Ware.
     */
    public void setzeAlleProdukteProduziert() { // Abschlussmeldung der Produktion
        System.out.println("Auftrag " + bestellNr + ": Die Fertigung ist abgeschlossen. Versandbereit.");
        this.alleProdukteProduziert = true; // Status auf "produziert" aktualisieren
        this.finishedAt = System.currentTimeMillis();
    }

    /**
     * Liefert eine kompakte Statusanzeige fuer die Bestellung basierend auf Produktzustaenden.
     * Mögliche Werte: "neu", "in Produktion", "fertig".
     */
    public String gibStatusString() {
        if (storniert) return "storniert";
        if (alleProdukteProduziert) return "fertig";
        boolean anyInProd = false;
        boolean allDone = true;
        for (Produkt p : bestellteProdukte) {
            int z = p.aktuellerZustand();
            if (z == 1) anyInProd = true;
            if (z != 2) allDone = false;
        }
        if (allDone) return "fertig";
        if (anyInProd) return "in Produktion";
        return "neu";
    }

    /** Returns true if all products are produced. */
    public boolean sindAlleProdukteProduziert() {
        if (alleProdukteProduziert) return true;
        for (Produkt p : bestellteProdukte) {
            if (p.aktuellerZustand() != 2) return false;
        }
        return true;
    }

    /** Mark production start timestamp if not set. */
    public void markProductionStartedIfNeeded() {
        if (this.productionStartAt == null) {
            this.productionStartAt = System.currentTimeMillis();
        }
    }

    public long gibCreatedAt() { return createdAt; }
    public Long gibProductionStartAt() { return productionStartAt; }
    public Long gibFinishedAt() { return finishedAt; }
    /** Returns true if materials were already consumed for this order. */
    public boolean istMaterialVerbraucht() { return materialVerbraucht; }
    /** Mark materials consumed to avoid double-deduction. */
    public void markiereMaterialVerbraucht() { this.materialVerbraucht = true; }

    /** Markiert die Bestellung als storniert. */
    public void storniere() { this.storniert = true; }
    /** Gibt an, ob die Bestellung storniert ist. */
    public boolean istStorniert() { return storniert; }
    
    /**
     * Ruft den derzeit gueltigen Standardwert fuer Lieferfristen ab.
     *
     * @return Die allgemeine Dauer einer Standardzustellung.
     */
    public static int gibStandardlieferzeit()
    {
        return standardlieferzeit;
    }

    // Erforderliche Methode zur Einbindung in automatisierte Modultests
    public void hinzufuegenProdukt(Produkt produkt) {
        // Ermoeglicht das manuelle Einfuegen eines Objekts in die Liste
        bestellteProdukte.add(produkt);
    }
}