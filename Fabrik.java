import java.util.ArrayList;

/**
 * Die Fabrik-Steuerung koordiniert die Auftragsabwicklung sowie die Fertigung von Standard- und Premiummodellen.
 * * Diese Klasse verwaltet ein Verzeichnis saemtlicher eingegangener Auftraege und stellt Funktionen
 * zum Erfassen neuer sowie zum Einsehen bestehender Bestellungen bereit. Ergaenzend wird hierüber
 * die Materialversorgung des Lagers geregelt.
 * * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class Fabrik
{
    private ArrayList<Bestellung> bestellungen; // Dynamische Liste zur Aufbewahrung aller getaetigten Auftraege
    private int bestellNr; // Kontinuierlicher Zaehler zur eindeutigen Zuweisung von Bestellidentifikationen
    private Lager lager; // Die Lagerinstanz, welche die benoetigten Materialien bereitstellt
    private Produktionsmanager produktionsmanager; // Verantwortliche Einheit fuer die Steuerung der Fertigungsablaeufe
    
    private int lagerAuffuellungen; // Protokolliert die Haeufigkeit der Materialnachschuebe
    
/**
 * Initialisiert ein neues Fabrik-Objekt und legt eine leere Auftragsliste an.
 * * Setzt die Liste der Bestellungen auf, bereitet das Lager vor und startet die
 * Produktionseinheit sowie die Steuerungsschnittstellen.
 */
public Fabrik() {
    this.bestellungen = new ArrayList<>(); // Erstellung der Datenstruktur fuer die Auftragsverwaltung
    bestellNr = 0; 
    lager = new Lager();
    lagerAuffuellungen = 0;
        
    produktionsmanager = new Produktionsmanager(this, lager);
    produktionsmanager.setDaemon(true); // Daemon-Thread: blockiert das Programmende/Tests nicht trotz while(true)
    produktionsmanager.start();
}
    
/**
 * Liefert das Verzeichnis saemtlicher registrierter Auftraege.
 * * @return Eine Liste mit allen im System hinterlegten Bestellungen.
 */
public ArrayList<Bestellung> gibBestellungen () {
        return this.bestellungen; // Rueckgabe des Listenobjekts
    }
    
    /**
     * Führt den Prozess zur Aufgabe eines neuen Fertigungsauftrags aus.
     * * @param anzahlStandardtueren Menge der zu produzierenden Standardeinheiten
     * @param anzahlPremiumtueren  Menge der zu produzierenden Premiumeinheiten
     */
public void bestellungAufgeben(int anzahlStandardtueren, int anzahlPremiumtueren)
    {
        int beschaffungsZeit = -1;
        bestellNr++;
        float lieferzeit = -1;
        Bestellung bestellung = new Bestellung(bestellNr, anzahlStandardtueren, anzahlPremiumtueren);
        
        // Ermittlung der Zeitspanne fuer die Materialbeschaffung
        beschaffungsZeit = lager.gibBeschaffungsZeit(bestellung);
        
        // Ueberprüfung der Korrektheit der Bestellmengen
        if (anzahlStandardtueren > 20 || anzahlPremiumtueren > 100){ // Die Kapazitaetsgrenze wurde ueberschritten, Auftrag wird abgelehnt.
            System.out.println("Ihre Bestellung konnte nicht getätigt werden. Sie haben zu viele Produkte bestellt. ");
            bestellNr--; // Korrektur des Zaehlers, da der Auftrag nicht ins System aufgenommen wurde
        } else if (anzahlStandardtueren < 0 || anzahlPremiumtueren < 0){ // Fehlermeldung bei Eingabe negativer Mengenwerte
            System.out.println("Ihre Bestellung konnte nicht getätigt werden aufgrund negativer Werte. Bitte positive Werte eingeben. ");
            bestellNr--; // Korrektur des Zaehlers, da der Auftrag nicht ins System aufgenommen wurde
        } else if (anzahlStandardtueren == 0 && anzahlPremiumtueren == 0){
            System.out.println("Ihre Bestellung konnte nicht getätigt werden. Die Bestellmenge für mindestens ein Produkt muss positiv sein. ");
            bestellNr--; // Korrektur des Zaehlers, da der Auftrag nicht ins System aufgenommen wurde
        } else {
            // Pruefung der Materialverfuegbarkeit ohne Verbrauch
            if (!lager.hatGenugMaterial(bestellung)){
                System.out.println("Hat genug Material auf Lager. Lager wird aufgefüllt.");
                lagerAuffuellen(); // Bestandsaufstockung nur bei festgestelltem Mangel
            }
            bestellung.setzeBeschaffungsZeit(beschaffungsZeit);
            lieferzeit = gibLieferzeit(beschaffungsZeit, anzahlStandardtueren, anzahlPremiumtueren);
            bestellung.setzeLieferzeit(lieferzeit);
            bestellung.bestellungBestaetigen();
            bestellungen.add(bestellung); // Kontrollpunkt fuer Debugging
            System.out.println("Bestellung hinzugefügt: " + bestellung.gibBestellNr());
            produktionsmanager.fuegeZuVerarbeitendeBestellungenHinzu(bestellung);
            System.out.println("Bestellung aufgegeben!");
        }
        System.out.println("Aktuelle Anzahl Bestellungen: " + bestellungen.size());
} 
        
/**
 * Listet alle aktuell im System befindlichen Auftraege auf.
 * Gibt Details wie ID, Mengen, notwendige Beschaffungsdauer, kalkulierte 
 * Lieferzeit und Status der Bestaetigung aus.
 */
    public void bestellungenAusgeben() {
        System.out.println("In der Fabrik gibt es gerade folgende Bestellungen:");
        for (Bestellung bestellung : bestellungen) {
            System.out.println("Bestellung Nummer " + bestellung.gibBestellNr()
                    + " Standardtüren: " + bestellung.gibAnzahlStandardtueren()
                    + " Premiumtüren: " + bestellung.gibAnzahlPremiumtueren()
                    + " Beschaffungszeit: " + Math.round(bestellung.gibBeschaffungsZeit()) + " Tage "
                    + " Lieferzeit: " + Math.round(bestellung.gibLieferzeit()) + " Tagen "
                    + " Bestellbestätigung: " + bestellung.gibBestellbestaetigung());
        }
    }

/**
 * Kalkuliert die voraussichtliche Lieferzeit unter Einbeziehung von Materialbeschaffung,
 * Fertigungsdauer der einzelnen Tueren und der Basis-Lieferfrist.
 *
 * @param beschaffungsZeit Dauer fuer den Zukauf fehlender Rohstoffe 
 * @param anzahlStandardtueren Summe der bestellten Standardobjekte
 * @param anzahlPremiumtueren Summe der bestellten Premiumobjekte
 *
 * @return lieferzeit Der Zeitpunkt, zu dem die Ware voraussichtlich beim Kunden eintrifft
 */
    public float gibLieferzeit(int beschaffungsZeit, int anzahlStandardtueren, int anzahlPremiumtueren)
    {
        Standardtuer standardtuer = new Standardtuer();
        Premiumtuer premiumtuer = new Premiumtuer();
        
        // Berechnung des Lieferdatums aus Herstellungs- und Logistikzeiten
        float lieferzeit;
        lieferzeit = ((standardtuer.getProduktionszeit() * anzahlStandardtueren) + 
                      (premiumtuer.getProduktionszeit() * anzahlPremiumtueren)) / (60*24f) + 
                      (beschaffungsZeit + Bestellung.gibStandardlieferzeit());
        return Math.round(lieferzeit*100.0)/100f;
    }
    
    /**
     * Stockt die Materialvorraete im Lager fuer die Produktion auf.
     * Inkrementiert den Zaehler fuer Nachfuellvorgaenge und zeigt den neuen Status an.
     */
    public void lagerAuffuellen() {
        lager.lagerAuffuellen();  // Vorrat ergaenzen
        lager.lagerBestandAusgeben(); // Aktuellen Bestand visualisieren
        this.lagerAuffuellungen++; // Zaehler fuer Logistikvorgaenge erhoehen
    }
    
    /**
     * Gewaehrt Zugriff auf das Lager-Objekt der Fabrik.
     * Hauptsaechlich fuer automatisierte Pruefungen in FabrikTest vorgesehen.
     * * @return Das zugehoerige Lager-Objekt.
     */
    public Lager gibLager() {
        return this.lager;
    }

    /** Optional getter for monitoring in GUI. */
    public Produktionsmanager gibProduktionsmanager() {
        return this.produktionsmanager;
    }
    
    /**
     * Ermoeglicht das Abrufen der Information, wie oft eine Materialnachfuellung erfolgte.
     * Dient der Verifizierung innerhalb der Testumgebung FabrikTest.
     * * @return Die Gesamtzahl der durchgefuehrten Lagerauffuellungen.
     */
    public int gibLagerAuffuellungen() {
        return this.lagerAuffuellungen;
    }
    
    public static void main(String[] args) {
        // Entry point unused in GUI context.
    }
}