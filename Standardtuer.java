/**
 * Die Klasse Standardtuer repraesentiert ein Basismodell, welches die Merkmale und
 * Funktionen der Oberklasse Produkt uebernimmt. Sie stellt eine spezifische Auspraegung 
 * dar und konkretisiert die allgemeinen Eigenschaften der Produktklasse. 
 * Hier sind die individuellen Ressourcenanforderungen sowie die kalkulierten 
 * Fertigungsintervalle fuer das Standardmodell hinterlegt.
 * * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class Standardtuer extends Produkt
{
    // Festlegung von unveraenderlichen Systemwerten (Konstanten)
    private static final int HOLZEINHEITEN = 2; // Ein Basismodell beansprucht 2 Einheiten Holz
    private static final int SCHRAUBEN = 10; // Ein Basismodell beansprucht 10 Schrauben
    private static final int FARBEINHEITEN = 2; // Ein Basismodell beansprucht 2 Farbeinheiten
    private static final int KARTONEINHEITEN = 1; // Ein Basismodell beansprucht 1 Verpackungseinheit
    private static final int PRODUKTIONSZEIT = 10; // Zeitaufwand in Minuten (entspricht 600.000 Millisekunden)
    
    /**
     * Zugriffsmethoden fuer die Materialparameter und die Herstellungsdauer.
     * Diese Schnittstellen ermoeglichen es, die spezifischen Bestandsanforderungen 
     * sowie die Produktionsintervalle fuer ein Standardmodell abzufragen.
     * * @return Die Summe der jeweiligen Ressourcen oder das Zeitmass fuer die Produktion.
     */

    /**
     * Ermittelt die Menge an Holzeinheiten, die fuer die Fertigung einer Standardtuer 
     * veranschlagt wurden.
     * * @return Die Anzahl der benoetigten Holzeinheiten.
     */
    public static int getHolzeinheiten() {
        return HOLZEINHEITEN;
    }

    /**
     * Ermittelt die Anzahl der Schrauben, die fuer den Zusammenbau einer Standardtuer 
     * erforderlich sind.
     * * @return Die Anzahl der Schrauben.
     */
    public static int getSchrauben(){
        return SCHRAUBEN;
    }

    /**
     * Gibt Auskunft ueber den Bedarf an Farbeinheiten fuer die Veredelung 
     * einer Standardtuer.
     * * @return Die Anzahl der kalkulierten Farbeinheiten.
     */
    public static int getFarbeinheiten() {
        return FARBEINHEITEN;
    }

    /**
     * Liefert den Wert fuer die Verpackungseinheiten, die fuer eine Standardtuer 
     * bereitgestellt werden muessen.
     * * @return Die Anzahl der Kartoneinheiten.
     */
    public static int getKartoneinheiten() {
        return KARTONEINHEITEN;
    }

    /**
     * Ruft die hinterlegte Produktionsdauer fuer dieses Modell ab.
     * * @return Die Herstellungszeit ausgedrueckt in Minuten.
     */
    public static int getProduktionszeit() {
        return PRODUKTIONSZEIT;
    }
}