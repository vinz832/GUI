import java.util.LinkedList;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Diese Klasse fungiert als Steuerungseinheit fuer die Fertigung. Der Produktionsmanager 
 * leitet die Bearbeitung der eingegangenen Auftraege aus der Fabrik und koordiniert 
 * die Arbeitsablaeufe mit den Robotersystemen (in dieser Ausbaustufe primär mit dem 
 * Roboter fuer die Holzbearbeitung).
 * * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class Produktionsmanager extends Thread
{
     private Holzverarbeitungsroboter holzRoboter;
    private java.util.List<Roboter> maschinenpark = new java.util.ArrayList<>();
     // Weitere Robotersysteme (Platzhalter fuer kuenftige Modifikationen)
     // private Montageroboter montageRoboter; Hinweis: Integration moeglich, da Klasse existiert, laut Anforderung jedoch noch nicht aktiv
     // private Lackierroboter lackierRoboter; Hinweis: Integration moeglich, da Klasse existiert, laut Anforderung jedoch noch nicht aktiv
     // private Verpackungsroboter verpackungsRoboter; Hinweis: Integration moeglich, da Klasse existiert, laut Anforderung jedoch noch nicht aktiv
     
     private Fabrik meineFabrik;
     private Lager meinLager;
    private volatile boolean notfallStop = false;
     
    // Strukturen zur Organisation der Auftragsablaeufe
     private LinkedList<Bestellung> zuVerarbeitendeBestellungen; // Schlange fuer anstehende Auftraege
     private LinkedList<Bestellung> bestellungenInProduktion; // Verzeichnis aktuell bearbeiteter Auftraege
     
     // Indikator, ob bereits ein Auftrag das System durchlaufen hat
    private boolean hatteSchonBestellungen = false;
    // Statusvariable zur Kontrolle der Terminierungsmeldung. 
    // Sie stellt sicher, dass der Hinweis auf abgeschlossene Arbeiten erst erscheint, 
    // wenn tatsaechlich Produkte gefertigt wurden, um eine Meldung direkt beim Start zu unterbinden.

    /**
     * Erzeugt einen neuen Produktionsmanager.
     * Richtet die Auftragslisten ein, aktiviert die Roboter-Prozesse und verknuepft 
     * die Instanzen von Fabrik und Materiallager.
     * * @param fabrik Referenz auf die ubergeordnete Fertigungsstaette.
     * @param lager Referenz auf das Depot fuer Rohmaterialien.
     */
    public Produktionsmanager(Fabrik fabrik , Lager lager)
    {        
         // Vorbereitung der Datenstrukturen
        zuVerarbeitendeBestellungen = new LinkedList<Bestellung>();
        bestellungenInProduktion = new LinkedList<Bestellung>(); 
        
        // Verknuepfung der Systemkomponenten
        meineFabrik = fabrik;
        meinLager = lager;
        
        holzRoboter = new Holzverarbeitungsroboter("WoodPecker"); // Instanziierung der Holzverarbeitungseinheit
        holzRoboter.setDaemon(true); // Daemon-Thread: blockiert das Programmende/Tests nicht trotz while(true)
        holzRoboter.start(); // Aktivierung des Holzroboter-Threads
        // Default-Metadaten
        holzRoboter.setzeProduktionsJahr(2015);
        maschinenpark.add(holzRoboter);
    }
    
     /**
      * Registriert ein neues Auftragsobjekt in der Warteschlange fuer die Fertigung.
      * * @param bestellung Das Objekt, das in die Produktion aufgenommen werden soll.
      */   
    public void fuegeZuVerarbeitendeBestellungenHinzu(Bestellung bestellung){
        zuVerarbeitendeBestellungen.add(bestellung); // Einreihen in die Bearbeitungskette
        hatteSchonBestellungen = true;  // System hat nun mindestens einen Auftrag registriert.
                                        // Dieser Status ermoeglicht es, am Ende eines Zyklus 
                                        // eine korrekte Erfolgsmeldung auszugeben.
        System.out.println("Ein neuer Fertigungsauftrag wurde an den Manager uebergeben.");
    }
    
    // Zugriffsmethode fuer automatisierte Tests
    public LinkedList<Bestellung> getZuVerarbeitendeBestellungen() {
        return zuVerarbeitendeBestellungen;
    }
    
    // Zugriffsmethode fuer automatisierte Tests
    public LinkedList<Bestellung> getBestellungenInProduktion() {
        return bestellungenInProduktion;
    }

    /**
     * Kernprozess des Managers.
     * * Entnimmt Auftraege aus der Schlange, validiert die Materialbestaende, 
     * initiiert die Fertigungsschritte und kontrolliert den Fortschritt 
     * bis zum Abschluss.
     */
    public void run(){
        ThreadUtil.synchronisiertesPrintln("Der Produktionsmanager nimmt die Arbeit auf.");
            while(true){
                if (notfallStop) {
                    try { Thread.sleep(300); } catch (InterruptedException ignore) {}
                    continue;
                }
                // Pruefen, ob ein neuer Auftrag zur Bearbeitung ansteht
            Bestellung naechsteBestellung = zuVerarbeitendeBestellungen.peek();
                if (naechsteBestellung != null) {
                    if (meinLager.genugMaterial(naechsteBestellung)) {
                        zuVerarbeitendeBestellungen.poll(); // Auftrag nun final aus der Schlange entnehmen
                        bestellungenInProduktion.add(naechsteBestellung);
                        starteProduktion(naechsteBestellung);
                    } else {
                        ThreadUtil.synchronisiertesPrintln("Produktionsmanager: Ressourcenmangel -> Warte auf Materialnachschub.");
                        meinLager.lagerAuffuellen();
                    }
                }
            
            // Abfrage des Fortschritts fuer alle aktiven Fertigungsprozesse
            ArrayList<Bestellung> fertigeBestellungen = new ArrayList<Bestellung>();
            for (Bestellung bestellung : bestellungenInProduktion) {
                boolean alleProdukteProduziert = true;

                for (Produkt produkt : bestellung.liefereBestellteProdukte()) {
                    if (produkt.aktuellerZustand() != 2) {
                        alleProdukteProduziert = false;
                        break;
                    }
                }

                if (alleProdukteProduziert) {
                    fertigeBestellungen.add(bestellung);
                }
            }

            // Bereinigung der Liste nach Abschluss der Iteration
            for (Bestellung bestellung : fertigeBestellungen) {
                bestellungenInProduktion.remove(bestellung);
                bestellung.setzeAlleProdukteProduziert();
                System.out.println("Auftrag " + bestellung.gibBestellNr() + " wurde erfolgreich beendet.");
            }

            if (hatteSchonBestellungen 
                && bestellungenInProduktion.isEmpty() 
                && zuVerarbeitendeBestellungen.isEmpty()) {

                System.out.println("Saemtliche anstehenden Auftraege wurden prozessiert.");
                hatteSchonBestellungen = false; // Zuruecksetzen des Indikators fuer den naechsten Zyklus.
                                                // Verhindert mehrfache Meldungen ohne neue Auftraege.
            }      
             // Taktung des Prozesses zur Schonung der Systemressourcen
                try{
                    Thread.sleep(1000);
                }
                catch(InterruptedException ie)
                {
                    ie.printStackTrace();
                }
            }
    }
    /** Aktiviert den globalen Notfall-Stop: pausiert alle Roboter und stoppt die Bearbeitung. */
    public synchronized void aktiviereNotfallStop() {
        notfallStop = true;
        setzePauseFuerAlleRoboter(true);
    }
    /** Hebt den globalen Notfall-Stop auf: setzt alle Roboter fort. */
    public synchronized void hebeNotfallStopAuf() {
        notfallStop = false;
        setzePauseFuerAlleRoboter(false);
    }
    /** Liefert den aktuellen Notfall-Status. */
    public boolean istNotfallStopAktiv() { return notfallStop; }
    /** Pausiert oder startet alle registrierten Roboter. */
    private void setzePauseFuerAlleRoboter(boolean pausiert) {
        try { holzRoboter.setzePausiert(pausiert); } catch (Exception ignore) {}
        for (Roboter r : maschinenpark) {
            try { r.setzePausiert(pausiert); } catch (Exception ignore) {}
        }
    }

    /**
     * Setzt die Fertigungsschritte fuer einen spezifischen Auftrag in Gang.
     * * @param bestellung Der zu produzierende Auftrag.
     */
    private void starteProduktion(Bestellung bestellung){
        ThreadUtil.synchronisiertesPrintln("Produktionsmanager: Fertigungsstart fuer Auftragsnummer: " + bestellung.gibBestellNr());
        bestellung.markProductionStartedIfNeeded();
        for(Produkt produkt : bestellung.liefereBestellteProdukte()){
            RoboterZuweisen(produkt); // Festlegen der Roboter-Route fuer das Einzelprodukt
            produkt.naechsteProduktionsStation();
        }          
    }
    
    /**
     * Definiert fuer ein Produkt die erforderliche Kette an Bearbeitungsstationen.
     * Beruecksichtigt die theoretische Einbindung verschiedener Robotertypen.
     * * @param produkt Das zu bearbeitende Objekt (Standard- oder Premiumvariante).
     */
    protected void RoboterZuweisen(Produkt produkt){
        LinkedList<Roboter> bearbeitungsReihenfolge = new LinkedList<Roboter>();
        if(produkt instanceof Standardtuer){
            // Ablaufplan fuer Standardtueren: geeigneten Holzroboter mit geringster Last waehlen
            bearbeitungsReihenfolge.add(waehleHolzroboter());
            produkt.setzeProduktionsAblauf(bearbeitungsReihenfolge); 
        }
        else if(produkt instanceof Premiumtuer){
            // Ablaufplan fuer Premiumtueren: geeigneten Holzroboter mit geringster Last waehlen
            bearbeitungsReihenfolge.add(waehleHolzroboter());
            produkt.setzeProduktionsAblauf(bearbeitungsReihenfolge); 
        }        
    }
    
    // Zugriffsmethode fuer automatisierte Tests
    public Holzverarbeitungsroboter getHolzRoboter() {
        return holzRoboter;
    }

    /** Zugriff auf registrierte Maschinen (Maschinenpark). */
    public java.util.List<Roboter> gibMaschinenpark() {
        return new java.util.ArrayList<>(maschinenpark);
    }
    /** Fuegt eine neue Maschine dem Maschinenpark hinzu. */
    public void hinzufuegenRoboter(Roboter roboter) {
        if (roboter != null) {
            maschinenpark.add(roboter);
            // Neue Roboter-Threads sofort aktivieren
            try {
                roboter.setDaemon(true);
                roboter.start();
            } catch (IllegalThreadStateException ignore) {
                // falls bereits gestartet, ignorieren
            }
        }
    }

    /** Ermittelt einen verfuegbaren Holzroboter (kuerzeste Warteschlange). Fallback: Standardinstanz. */
    private Roboter waehleHolzroboter() {
        Roboter kandidat = null;
        int besteQueue = Integer.MAX_VALUE;
        for (Roboter r : maschinenpark) {
            if (r instanceof Holzverarbeitungsroboter) {
                int qs = (r.getWarteschlange() != null) ? r.getWarteschlange().size() : 0;
                if (qs < besteQueue) { besteQueue = qs; kandidat = r; }
            }
        }
        return kandidat != null ? kandidat : holzRoboter;
    }
}