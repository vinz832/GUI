import java.util.LinkedList;

/**
 * Diese Vorlage definiert das Modell der Premiumausfuehrung (Premiumtuer), welche 
 * als spezialisierte Erweiterung der Basisklasse Produkt fungiert. Sie dient der 
 * Spezifikation saemtlicher Besonderheiten und Konditionen, die fuer die Herstellung 
 * dieser gehobenen Produktvariante massgeblich sind.
 * * Die Premiumtuer zeichnet sich durch einen spezifischen Materialverbrauch sowie 
 * eine exakt kalkulierte Dauer innerhalb der Fertigungskette aus.
 * * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class Premiumtuer extends Produkt
{
    // Definition der unveraenderlichen Ressourcenwerte fuer diesen Produkttyp
    private static final int HOLZEINHEITEN = 4;  // Pro Exemplar werden 4 Einheiten Holz veranschlagt
    private static final int SCHRAUBEN = 5; // Der Bedarf an Verbindungselementen liegt bei 5 Schrauben
    private static final int GLASEINHEITEN = 5; // Die Konstruktion erfordert 5 Glaskomponenten
    private static final int FARBEINHEITEN = 1; // Fuer das Oberflaechenfinish wird 1 Farbeinheit benoetigt
    private static final int KARTONEINHEITEN = 5; // Die Versandverpackung beansprucht 5 Einheiten Kartonage
    private static final int PRODUKTIONSZEIT = 30; // Zeitbudget fuer die Montage: 30 Minuten (entspricht 1.800.000 ms)
    
    /**
     * Methoden zum Abruf der Materialkonstanten sowie des Zeitaufwands.
     * Diese Schnittstellen ermoeglichen es, die genauen Anforderungen fuer die 
     * Ressourcenplanung der Premium-Variante auszulesen.
     * * @return Die Mengenwerte der Einzelkomponenten oder die Dauer des Herstellungsprozesses.
     */

    /**
     * Ermittelt die Menge an Holzressourcen, die fuer die Produktion einer 
     * einzelnen Premiumtuer reserviert werden muss.
     * * @return Die Summe der benoetigten Holzeinheiten.
     */
    public static int getHolzeinheiten() {
        return HOLZEINHEITEN;
    }
    
    /**
     * Bestimmt die Anzahl der Schrauben, die fuer die fachgerechte Montage 
     * einer Premiumtuer notwendig sind.
     * * @return Die Anzahl der Befestigungselemente.
     */
    public static int getSchrauben(){
        return SCHRAUBEN;
    }

    /**
     * Fragt das Volumen der Glaselemente ab, welches fuer die Komplettierung 
     * dieses spezifischen Modells geliefert werden muss.
     * * @return Die Anzahl der Glaseinheiten.
     */
    public static int getGlaseinheiten() {
        return GLASEINHEITEN;
    }

    /**
     * Gibt Auskunft ueber den Farbmittelbedarf, der fuer die optische Gestaltung 
     * einer Premiumtuer anfällt.
     * * @return Die Anzahl der Farbeinheiten.
     */
    public static int getFarbeinheiten() {
        return FARBEINHEITEN;
    }

    /**
     * Liefert den Wert fuer die Kartonmengen, die fuer den sicheren Transport 
     * einer Premiumtuer bereitstehen muessen.
     * * @return Die Anzahl der Kartoneinheiten.
     */
    public static int getKartoneinheiten() {
        return KARTONEINHEITEN;
    }

    /**
     * Ruft die Zeitspanne ab, die fuer den gesamten Fertigungsablauf einer 
     * Premiumtuer vorgesehen ist.
     * * @return Die Herstellungszeit (angegeben in Minuten bzw. Millisekunden).
     */
    public static int getProduktionszeit() {
        return PRODUKTIONSZEIT;
    }
}