import java.util.LinkedList;

/**
 * Dient als fundamentales Gerüst für alle Güter, die in der Fabrik hergestellt werden.
 * Die Klasse kontrolliert den Fertigungsfortschritt sowie die Abfolge der Bearbeitungsschritte.
 * * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class Produkt
{
    // Deklaration der Instanzvariablen
    private int zustand; // Momentaner Status: 0 = bestellt, 1 = in Arbeit, 2 = fertiggestellt
    private LinkedList <Roboter> produktionsAblauf; // Liste der Roboter-Einheiten für die Herstellung
    
    /**
     * Default-Konstruktor, um eine neue Produktinstanz zu erstellen.
     */
    public Produkt() {
        this.zustand = 0; // Neue Objekte starten automatisch im Status "Bestellt".
    }

    /**
     * Passt den aktuellen Bearbeitungsstatus des Objekts an.
     * Zulässige Kennziffern sind:
     * 0: Auftrag wurde empfangen
     * 1: Befindet sich in der Fertigung
     * 2: Herstellung ist abgeschlossen
     * * Bei Werten ausserhalb des Bereichs 0 bis 2 wird eine Fehlermeldung ausgegeben.
     * @param neuerZustand Der festzulegende Statuswert (0, 1 oder 2).
     */
    public void zustandAendern(int neuerZustand) {
        if(neuerZustand <= 2 && neuerZustand >= 0){
            this.zustand = neuerZustand; // Zuweisung des neuen Statuswerts
        } else {
            // Fehlermeldung bei unzulässiger Eingabe
            System.err.println("Fehler: Der gewählte Statuswert ist nicht definiert.");
        }
    }

    /**
     * Ruft die aktuelle Kennziffer des Fertigungsstandes ab.
     * Rückgabewerte: 0 (Bestellung), 1 (Produktion), 2 (Abgeschlossen).
     * @return Ganzzahl, die den momentanen Zustand repräsentiert.
     */
    public int aktuellerZustand() {
        return this.zustand; // Gibt den in der Variable gespeicherten Wert zurück
    }
    
    /**
     * Gibt eine Klartext-Information zum Bearbeitungsstand auf der Konsole aus.
     */
    public void Zustandausgeben() {
        // Ermittlung der passenden Textausgabe je nach Status
        if(this.zustand == 0){
            System.out.println("Produktzustand: Bestellt");  
        } else if (this.zustand == 1){
            System.out.println("Produktzustand: In Produktion");  
        } else {
            System.out.println("Produktzustand: Fertiggestellt und bereit für die Auslieferung");  
        }
    }

    /**
     * Übermittelt das Objekt an die nächste Station in der Fertigungskette.
     * Wenn keine weiteren Stationen in der Liste vorhanden sind, wird das Produkt 
     * als "Abgeschlossen" markiert.
     */   
    public void naechsteProduktionsStation(){
        if(produktionsAblauf.peek() != null){
            // Entnimmt den nächsten verfügbaren Roboter aus der Warteschlange
            Roboter roboter = produktionsAblauf.poll();
            System.out.println("Nächster Arbeitsschritt an Station: " + roboter.gibNamen());
            this.zustandAendern(1); // Setzt den Status auf laufende Bearbeitung
            roboter.fuegeProduktHinzu(this);  // Übergabe des Objekts an die Maschine
        }        
        else{
            // Fall: Die Liste der Produktionsschritte ist leer
            System.out.println("Das Produkt: " + this + " wurde erfolgreich fertiggestellt.");
            this.zustandAendern(2);
        }
    }

    /**
     * Konfiguriert die Kette der Arbeitsschritte durch eine Liste von Robotern.
     * @param produktionsAblauf Eine LinkedList mit den Robotern für den Fertigungsprozess.
     */
    public void setzeProduktionsAblauf(LinkedList <Roboter> produktionsAblauf)
    {
        this.produktionsAblauf = produktionsAblauf; // Speichert die übergebene Prozesskette
    }
    
    // Hilfsfunktion für Testzwecke
    /**
     * Ermöglicht das Auslesen der hinterlegten Liste für die Produktionsschritte.
     * @return Die verkettete Liste der zugeordneten Roboter-Stationen.
     */
    public LinkedList<Roboter> getProduktionsAblauf() {
        return this.produktionsAblauf;
    }

}