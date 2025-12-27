/**
 * Dieses Objekt verwaltet das Depot für die Rohmaterialien innerhalb der Fertigungsstätte.
 * * Es steuert die Bestandsmengen von Holz, Eisenwaren, Farbmitteln, Verpackungen und Glaskomponenten,
 * welche für die Herstellung der Standard- und Premiumpodelle essenziell sind. Die Klasse
 * überwacht die Materialverfügbarkeit, initiiert Nachbestellungen bei Bedarf und garantiert 
 * so einen reibungslosen Ablauf der Produktionskette.
 *
 * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class Lager {
// Deklaration und Initialisierung der globalen Konstanten
public static final int MAXHOLZEINHEITEN = 1000; // Maximale Kapazität für Holzressourcen
public static final int MAXSCHRAUBEN = 5000; // Obergrenze für den Schraubenvorrat
public static final int MAXFARBEINHEITEN = 1000; // Maximalbestand an Farbmitteln
public static final int MAXKARTONEINHEITEN = 1000; // Kapazitätslimit für Verpackungsmaterial
public static final int MAXGLASEINHEITEN = 100; // Höchstmenge der lagerbaren Glaselemente
// Warnschwellen (Fraction of MAX) for GUI alerts
public static final double WARN_LEVEL = 0.20;
public static final double CRIT_LEVEL = 0.10;

private int vorhandeneHolzeinheiten; // Aktueller Vorrat an Holz
private int vorhandeneSchrauben; // Momentane Anzahl an verfügbaren Schrauben
private int vorhandeneFarbeinheiten; // Gegenwärtiger Bestand an Farbe
private int vorhandeneKartoneinheiten; // Aktuell gelagerte Kartonagen
private int vorhandeneGlaseinheiten; // Derzeit verfügbarer Glasbestand

private Lieferant lieferant; // Verbindung zum externen Zulieferer
protected boolean istBestellt = false; // Statusanzeige für laufende Liefervorgänge
// Historie und Lieferinformationen
private java.util.List<RestockEntry> historie = new java.util.ArrayList<>();
private RestockEntry aktiveLieferung = null;
private long letzteLieferungStartAt = 0L;
private float letzteLieferzeitMs = 0f;

/** Eintrag für Nachbestellungen (manuell/automatisch). */
public static class RestockEntry {
    public final long orderedAt;
    public final String typ; // "AUTO" oder "MANUAL"
    public final int holz, schrauben, farbe, karton, glas;
    public final float lieferzeitMs;
    private Long deliveredAt; // null bis Lieferung erfolgt

    public RestockEntry(String typ, int holz, int schrauben, int farbe, int karton, int glas, float lieferzeitMs) {
        this.orderedAt = System.currentTimeMillis();
        this.typ = typ;
        this.holz = holz; this.schrauben = schrauben; this.farbe = farbe; this.karton = karton; this.glas = glas;
        this.lieferzeitMs = lieferzeitMs;
        this.deliveredAt = null;
    }

    public Long getDeliveredAt() { return deliveredAt; }
    void markDelivered() { this.deliveredAt = System.currentTimeMillis(); }
}

    /**
     * Erzeugt ein Lager-Objekt. Dabei werden alle Bestände auf das Maximum gesetzt 
     * und die Verbindung zum Lieferanten hergestellt.
     */
    public Lager() {
// Zuweisung der Startwerte für alle Materialressourcen
vorhandeneHolzeinheiten = MAXHOLZEINHEITEN;
vorhandeneSchrauben = MAXSCHRAUBEN;
vorhandeneFarbeinheiten = MAXFARBEINHEITEN;
vorhandeneKartoneinheiten = MAXKARTONEINHEITEN;
vorhandeneGlaseinheiten = MAXGLASEINHEITEN;
lieferant = new Lieferant(this);
    }

    /**
     * Ermittelt die benötigte Zeitdauer für die Materialbeschaffung in Tagen:
     * - 0 Tage, falls alle Ressourcen für den Auftrag sofort abrufbar sind.
     * - 2 Tage (oder mehr bei grossem Überhang), falls eine Nachbestellung nötig ist.
     * * Innerhalb der Logik wird geprüft, welche Mengen für die spezifische Bestellung 
     * kalkuliert werden müssen. Reicht der Bestand nicht aus, erhöht sich die Wartezeit.
     *
     * @param kundenBestellung Das aktuelle Auftragsobjekt mit der Liste der Produkte.
     * @return Zeitaufwand für die Beschaffung in Tagen.
     */
    public int gibBeschaffungsZeit(Bestellung kundenBestellung) {
        int benoetigtesHolz = 0;
        int benoetigteSchrauben = 0;
        int benoetigteFarbe = 0;
        int benoetigterKarton = 0;
        int benoetigtesGlas = 0;
        
        Standardtuer standardtuer = new Standardtuer();
        Premiumtuer premiumtuer = new Premiumtuer();
        
    // Ermittlung des kumulierten Materialbedarfs aller bestellten Einheiten
    for (Produkt produkt : kundenBestellung.liefereBestellteProdukte()) {
        if (produkt instanceof Standardtuer) {
                benoetigtesHolz += standardtuer.getHolzeinheiten();
                benoetigteSchrauben += standardtuer.getSchrauben();
                benoetigteFarbe += standardtuer.getFarbeinheiten();
                benoetigterKarton += standardtuer.getKartoneinheiten();
        } else if (produkt instanceof Premiumtuer) {
                // Typumwandlung, um auf spezifische Attribute der Premiumvariante zuzugreifen
                benoetigtesHolz += premiumtuer.getHolzeinheiten();
                benoetigteSchrauben += premiumtuer.getSchrauben();
                benoetigteFarbe += premiumtuer.getFarbeinheiten();
                benoetigterKarton += premiumtuer.getKartoneinheiten();
                benoetigtesGlas += premiumtuer.getGlaseinheiten();
        }
    }

    // Abgleich: Ist die benötigte Menge kleiner oder gleich dem Vorrat?
    if (vorhandeneHolzeinheiten >= benoetigtesHolz && vorhandeneSchrauben >= benoetigteSchrauben &&
        vorhandeneGlaseinheiten >= benoetigtesGlas && vorhandeneFarbeinheiten >= benoetigteFarbe &&
        vorhandeneKartoneinheiten >= benoetigterKarton) {
        return 0; // Bestand deckt den Bedarf komplett
    } else {
        return 2; // Bestückung des Lagers durch Nachbestellung erforderlich
    }
}

/**
 * Füllt die Materialbestände wieder auf das jeweilige Maximum auf.
 * * Es wird berechnet, wie viel Differenz zum Maximalbestand besteht. Diese Mengen 
 * werden beim Zulieferer angefordert. Dies sichert eine konstante Produktionsbereitschaft 
 * und ermöglicht es, flexibel auf Nachfragespitzen zu reagieren, ohne Ineffizienzen 
 * durch Materialmangel zu riskieren.
 */
public void lagerAuffuellen() {
    // Bestimmung der Differenzmengen zum Soll-Zustand
    int zuBestellendeHolzeinheiten = MAXHOLZEINHEITEN - vorhandeneHolzeinheiten;
    int zuBestellendeSchrauben = MAXSCHRAUBEN - vorhandeneSchrauben;
    int zuBestellendeFarbeinheiten = MAXFARBEINHEITEN - vorhandeneFarbeinheiten;
    int zuBestellendeKartoneinheiten = MAXKARTONEINHEITEN - vorhandeneKartoneinheiten;
    int zuBestellendesGlas = MAXGLASEINHEITEN - vorhandeneGlaseinheiten;

    // Abbruch, falls das Depot bereits vollständig bestückt ist
    if (zuBestellendeHolzeinheiten == 0 && zuBestellendeSchrauben == 0 &&
        zuBestellendeFarbeinheiten == 0 && zuBestellendeKartoneinheiten == 0 &&
        zuBestellendesGlas == 0) {
        System.out.println("Lager ist bereits voll. Keine Bestellung notwendig.");
        return;
    }

    // Sicherstellung, dass nicht mehrere Bestellungen gleichzeitig laufen
    if (!istBestellt) {
        System.out.println("Lager: Bestand zu niedrig -> Nachbestellung ausgelöst (Lieferung in ~48s).");
        Lieferant l = new Lieferant(this);
        // Historie-Eintrag und Liefer-Infos setzen
        RestockEntry entry = new RestockEntry("AUTO", zuBestellendeHolzeinheiten, zuBestellendeSchrauben,
                zuBestellendeFarbeinheiten, zuBestellendeKartoneinheiten, zuBestellendesGlas, l.gibLieferzeit());
        aktiveLieferung = entry;
        historie.add(entry);
        istBestellt = true;
        letzteLieferungStartAt = entry.orderedAt;
        letzteLieferzeitMs = entry.lieferzeitMs;
        l.wareBestellen(zuBestellendeHolzeinheiten, zuBestellendeSchrauben, zuBestellendeFarbeinheiten,
                        zuBestellendeKartoneinheiten, zuBestellendesGlas, entry);
    } else {
        System.out.println("Lager: Ware bereits auf dem Weg!");
    }
}

/**
 * Visualisiert den aktuellen Status der Materialbestände auf der Standardausgabe.
 */
public void lagerBestandAusgeben() {
    System.out.println("Aktueller Lagerbestand: ");
    System.out.println("Vorhandene Holzeinheiten: " + vorhandeneHolzeinheiten +
    " Vorhandene Schrauben: " + vorhandeneSchrauben +
    " Vorhandene Farbeeinheiten: " + vorhandeneFarbeinheiten +
    " Vorhandene Kartoneinheiten: " + vorhandeneKartoneinheiten +
    " Vorhandene Glaseinheiten: " + vorhandeneGlaseinheiten + "\n\n");
    System.out.println();
    System.out.println(); // Erzeugung von Abständen für bessere Lesbarkeit
}

// Zugriffsmethoden für die Bestandsabfrage (vorrangig für Unit-Tests)

/** @return Die aktuelle Menge an Holz im Bestand. */
public int getVorhandeneHolzeinheiten() { 
    return vorhandeneHolzeinheiten;
}

/** @return Die aktuelle Menge an Schrauben im Bestand. */
public int getVorhandeneSchrauben() { 
    return vorhandeneSchrauben;
}

/** @return Die aktuelle Menge an Farbmitteln im Bestand. */
public int getVorhandeneFarbeinheiten() { 
    return vorhandeneFarbeinheiten;
}

/** @return Die aktuelle Menge an Kartonagen im Bestand. */
public int getVorhandeneKartoneinheiten() { 
    return vorhandeneKartoneinheiten;
}

/** @return Die aktuelle Menge an Glaselementen im Bestand. */
public int getVorhandeneGlaseinheiten() { 
    return vorhandeneGlaseinheiten;
}

/**
 * Validiert, ob für eine spezifische Bestellung ausreichend Ressourcen lagern.
 * * Diese Funktion dient lediglich der Prüfung und führt KEINE Bestandsänderung durch.
 * Sie kalkuliert den Gesamtbedarf pro Produkttyp und vergleicht diesen mit dem Ist-Zustand.
 * * @return true bei ausreichender Deckung, false bei Materialmangel.
 */
public boolean hatGenugMaterial(Bestellung kundenBestellung) {
    int benoetigtesHolz = 0;
    int benoetigteSchrauben = 0;
    int benoetigteFarbe = 0;
    int benoetigterKarton = 0;
    int benoetigtesGlas = 0;

    for (Produkt produkt : kundenBestellung.liefereBestellteProdukte()) {
        if (produkt instanceof Standardtuer) {
            benoetigtesHolz += Standardtuer.getHolzeinheiten();
            benoetigteSchrauben += Standardtuer.getSchrauben();
            benoetigteFarbe += Standardtuer.getFarbeinheiten();
            benoetigterKarton += Standardtuer.getKartoneinheiten();
        } else if (produkt instanceof Premiumtuer) {
            benoetigtesHolz += Premiumtuer.getHolzeinheiten();
            benoetigteSchrauben += Premiumtuer.getSchrauben();
            benoetigteFarbe += Premiumtuer.getFarbeinheiten();
            benoetigterKarton += Premiumtuer.getKartoneinheiten();
            benoetigtesGlas += Premiumtuer.getGlaseinheiten();
        }
    }

    return (vorhandeneHolzeinheiten >= benoetigtesHolz &&
            vorhandeneSchrauben >= benoetigteSchrauben &&
            vorhandeneFarbeinheiten >= benoetigteFarbe &&
            vorhandeneKartoneinheiten >= benoetigterKarton &&
            vorhandeneGlaseinheiten >= benoetigtesGlas);
}

/**
 * Entnimmt die für einen Auftrag benötigten Materialien aus dem Bestand.
 * * Falls die Mengen nicht ausreichen, findet keine Entnahme statt und es wird ein 
 * negativer Status zurückgegeben. Die eigentliche Nachbestellung wird extern 
 * gesteuert.
 *
 * @param kundenBestellung Der zu bearbeitende Auftrag.
 * @return true, falls die Materialien erfolgreich abgezogen wurden, sonst false.
 */
public synchronized boolean genugMaterial(Bestellung kundenBestellung) {
    if (kundenBestellung.istMaterialVerbraucht()) {
        return true; // already consumed for this order, avoid double-deduction
    }
    int benoetigtesHolz = 0;
    int benoetigteSchrauben = 0;
    int benoetigteFarbe = 0;
    int benoetigterKarton = 0;
    int benoetigtesGlas = 0;
    boolean materialVorhanden = true;

    for (Produkt produkt : kundenBestellung.liefereBestellteProdukte()) {
        if (produkt instanceof Standardtuer) {
                benoetigtesHolz += Standardtuer.getHolzeinheiten();
                benoetigteSchrauben += Standardtuer.getSchrauben();
                benoetigteFarbe += Standardtuer.getFarbeinheiten();
                benoetigterKarton += Standardtuer.getKartoneinheiten();
        } else if (produkt instanceof Premiumtuer) {
                benoetigtesHolz += Premiumtuer.getHolzeinheiten();
                benoetigteSchrauben += Premiumtuer.getSchrauben();
                benoetigteFarbe += Premiumtuer.getFarbeinheiten();
                benoetigterKarton += Premiumtuer.getKartoneinheiten();
                benoetigtesGlas += Premiumtuer.getGlaseinheiten();
        }
    }

    // Prüfung der Verfügbarkeit vor der Bestandsminderung
    if (vorhandeneHolzeinheiten < benoetigtesHolz || vorhandeneSchrauben < benoetigteSchrauben ||
        vorhandeneGlaseinheiten < benoetigtesGlas || vorhandeneFarbeinheiten < benoetigteFarbe ||
        vorhandeneKartoneinheiten < benoetigterKarton) {
        materialVorhanden = false; 
    } else {
        // Reduktion der Bestände um die verbrauchten Einheiten
        vorhandeneHolzeinheiten -= benoetigtesHolz;
        vorhandeneSchrauben -= benoetigteSchrauben;
        vorhandeneFarbeinheiten -= benoetigteFarbe;
        vorhandeneKartoneinheiten -= benoetigterKarton;
        vorhandeneGlaseinheiten -= benoetigtesGlas;
        kundenBestellung.markiereMaterialVerbraucht();
    }
    return materialVorhanden;
}

/**
 * Schliesst den Lieferprozess ab und setzt alle Bestände auf ihre Maximalwerte zurück.
 */
public void wareLiefern() {
    vorhandeneHolzeinheiten = MAXHOLZEINHEITEN;
    vorhandeneSchrauben = MAXSCHRAUBEN;
    vorhandeneFarbeinheiten = MAXFARBEINHEITEN;
    vorhandeneKartoneinheiten = MAXKARTONEINHEITEN;
    vorhandeneGlaseinheiten = MAXGLASEINHEITEN;

    System.out.println("Lager: Ware wurde geliefert!");
    lagerBestandAusgeben();

    istBestellt = false;
}

/**
 * Lieferung mit konkreten Nachbestellmengen (für manuelle/gezielte Auffüllungen).
 * Summiert die gelieferten Mengen zum aktuellen Bestand und begrenzt auf MAX.
 * Markiert Historie-Eintrag als geliefert.
 */
public synchronized void wareLiefern(int holz, int schrauben, int farbe, int karton, int glas, RestockEntry entry) {
    vorhandeneHolzeinheiten = Math.min(MAXHOLZEINHEITEN, vorhandeneHolzeinheiten + holz);
    vorhandeneSchrauben = Math.min(MAXSCHRAUBEN, vorhandeneSchrauben + schrauben);
    vorhandeneFarbeinheiten = Math.min(MAXFARBEINHEITEN, vorhandeneFarbeinheiten + farbe);
    vorhandeneKartoneinheiten = Math.min(MAXKARTONEINHEITEN, vorhandeneKartoneinheiten + karton);
    vorhandeneGlaseinheiten = Math.min(MAXGLASEINHEITEN, vorhandeneGlaseinheiten + glas);

    System.out.println("Lager: Zielgerichtete Lieferung eingetroffen (manuell/auto).");
    lagerBestandAusgeben();

    istBestellt = false;
    if (entry != null) {
        entry.markDelivered();
        if (aktiveLieferung == entry) aktiveLieferung = null;
    }
}

/** @return true wenn eine Lieferung unterwegs ist. */
public boolean istLieferungUnterwegs() {
    return istBestellt;
}

/** @return Zeitstempel (ms) des Starts der letzten Lieferung, 0 wenn keine. */
public long gibLetzteLieferungStartAt() { return letzteLieferungStartAt; }
/** @return Dauer (ms) der letzten Lieferung. */
public float gibLetzteLieferzeitMs() { return letzteLieferzeitMs; }

/** @return Kopie der Nachbestell-Historie. */
public java.util.List<RestockEntry> gibNachbestellHistorie() { return new java.util.ArrayList<>(historie); }

/**
 * Triggert eine manuelle Nachbestellung über den Lieferanten mit Verzögerung.
 * Erst nach Ablauf der Lieferzeit werden die angeforderten Mengen verbucht.
 */
public void manuelleNachbestellung(int holz, int schrauben, int farbe, int karton, int glas) {
    if (holz <= 0 && schrauben <= 0 && farbe <= 0 && karton <= 0 && glas <= 0) {
        System.out.println("Lager: Keine Mengen angegeben – Nachbestellung abgebrochen.");
        return;
    }
    if (istBestellt) {
        System.out.println("Lager: Es läuft bereits eine Lieferung – bitte warten.");
        return;
    }
    Lieferant l = new Lieferant(this);
    RestockEntry entry = new RestockEntry("MANUAL", holz, schrauben, farbe, karton, glas, l.gibLieferzeit());
    aktiveLieferung = entry;
    historie.add(entry);
    istBestellt = true;
    letzteLieferungStartAt = entry.orderedAt;
    letzteLieferzeitMs = entry.lieferzeitMs;
    System.out.println("Lager: Manuelle Nachbestellung ausgelöst (Lieferung in ~48s).");
    l.wareBestellen(holz, schrauben, farbe, karton, glas, entry);
}

/** @return Warnlevel fraction (e.g., 0.20). */
public static double getWarnLevel() { return WARN_LEVEL; }
/** @return Critical level fraction (e.g., 0.10). */
public static double getCritLevel() { return CRIT_LEVEL; }

// Hilfsmethoden zur gezielten Bestandsmanipulation (primär für automatisierte Tests)

public void setzeVorhandeneHolzeinheiten(int menge) {
    this.vorhandeneHolzeinheiten = menge;
}

public void setzeVorhandeneSchrauben(int menge) {
    this.vorhandeneSchrauben = menge;
}

public void setzeVorhandeneFarbeinheiten(int menge) {
    this.vorhandeneFarbeinheiten = menge;
}

public void setzeVorhandeneKartoneinheiten(int menge) {
    this.vorhandeneKartoneinheiten = menge;
}

public void setzeVorhandeneGlaseinheiten(int menge) {
    this.vorhandeneGlaseinheiten = menge;
}

}