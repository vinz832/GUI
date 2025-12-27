/**
 * Diese Komponente definiert einen spezialisierten Roboter fuer die Holzverarbeitung und 
 * fungiert als Erweiterung der Basisklasse Roboter.
 * * Die Einheit uebernimmt die gezielte Zurichtung von Holzkomponenten und greift dabei auf 
 * die geerbten Eigenschaften der uebergeordneten Roboter-Klasse zu. Sie fertigt sowohl 
 * Standard- als auch Premiummodelle, wobei die Bearbeitungsdauer je nach Produkttyp 
 * (Basisvariante vs. Exklusivmodell) simuliert wird.
 * * Innerhalb der Simulation wurden die Fertigungszeiten komprimiert, um eine effiziente 
 * Durchfuehrung der Testlaeufe zu ermoeglichen. Dieses Modul dient zudem als Vorlage 
 * fuer die moegliche Implementierung weiterer Fachroboter.
 * * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class Holzverarbeitungsroboter extends Roboter
{
    /**
     * Erzeugung einer neuen Instanz des Holzbearbeitungsroboters.
     *
     * Initialisiert die Maschine mit einer Bezeichnung und ruft den Konstruktor der 
     * Basisklasse {@link Roboter} auf.
     *
     * @param name Die Identifikation des Roboters
     */
    public Holzverarbeitungsroboter(String name)
    {
        super(name); // Aufruf der Initialisierung der Elternklasse
        name = "Holzbearbeitungsroboter"; // Interne Typzuweisung fuer das Projekt
    }

    /**
     * Fragt den Namen der Maschinengattung ab.
     *
     * @return Die spezifische Bezeichnung der Roboterart
     */
    public String gibName()
    {
        String name = "Holzverarbeitungsroboter";
        return name;
    }

    /**
     * Realisierung des Herstellungsschritts fuer ein spezifisches Teil.
     *
     * Basierend auf der Art des Produkts wird ein verkürzter Zeitwert fuer die Bearbeitung 
     * festgelegt und mittels {Thread.sleep(...)} abgebildet. Dies stellt sicher, dass 
     * der Ablauf nachvollziehbar bleibt, ohne reale Zeitspannen abwarten zu muessen.
     *
     * @param produkt Das zu fertigende Teil (beispielsweise {Standardtuer} oder {Premiumtuer})
     */
    public void produziereProdukt(Produkt produkt)
    {
        int bearbeitungszeit = 0;

        // Validierung des Produkttyps zur Festlegung der simulierten Dauer (Maßstab 1:100)
        if (produkt instanceof Standardtuer) {
            bearbeitungszeit = 6000;  // Repraesentiert eine geraffte 10-Minuten-Phase
        } else if (produkt instanceof Premiumtuer) {
            bearbeitungszeit = 18000; // Repraesentiert eine geraffte 30-Minuten-Phase
        }

        System.out.println("Die Holzbearbeitung wurde gestartet für das Produkt: " + produkt.getClass().getSimpleName());

        try {
            Thread.sleep(bearbeitungszeit);
        } catch (InterruptedException ie) {
            System.out.println("Der Holzbearbeitungsroboter wurde unterbrochen. Fehler: " + ie.getMessage());
            return;
        }
    }
}