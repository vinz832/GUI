import java.util.ArrayList;

/**
 * Diese Klasse bildet den Lieferanten ab. Er fungiert als externer Partner, der die 
 * Materialversorgung für das Fabriklager sicherstellt. Der Zulieferer verarbeitet 
 * Bestellanforderungen, verwaltet variable Zustellfristen und stellt die fuer die 
 * Fertigung von Tueren (Standard und Premium) erforderlichen Ressourcen bereit.
 *
 * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class Lieferant extends Thread {
    private Lager lager; // Verknuepfung zum Depot, welches die Waren empfaengt
    private float neueLieferzeit = 2 * 24 * 1000f; // Voreingestellte Dauer der Zustellung in simulierten Millisekunden (entspricht 2 Tagen)
    // Nachbestellmengen (für gezielte Lieferung)
    private int mHolz = 0, mSchrauben = 0, mFarbe = 0, mKarton = 0, mGlas = 0;
    private Lager.RestockEntry entryRef = null;

    /**
     * Erzeugt eine neue Instanz der Lieferanten-Klasse.
     *
     * @param lager Die Lagerstaette der Fabrik, an die die Rohstoffe gesendet werden.
     */
    public Lieferant(Lager lager) {
        this.lager = lager; // Zuweisung der Zielinstanz fuer die Belieferung
        this.setDaemon(true);  // Markierung als Daemon-Thread, damit Testlaeufe oder die JVM nicht durch den Sleep-Modus blockiert werden
    }

    /**
     * Fragt die momentan gueltige Dauer fuer einen Liefervorgang ab.
     *
     * @return Zeitwert der Lieferung in Millisekunden.
     */
    public float gibLieferzeit() {
        return (neueLieferzeit); // Rueckgabe des aktuell hinterlegten Zeitintervalls
    }

    /**
     * Ermoeglicht die Aktualisierung der Zeitspanne, die der Lieferant fuer eine Zustellung benoetigt.
     *
     * @param lieferzeit Der neue Zeitwert in Millisekunden.
     */
    public void setzNeueLieferzeit(float lieferzeit) {
        this.neueLieferzeit = lieferzeit; // Speichert das geanderte Lieferintervall
    }

    /**
     * Initiiert den Bestellvorgang fuer die Produktionseinheiten beim Zulieferer.
     * * Prueft die Durchfuehrbarkeit der Materialanforderung.
     *
     * @param zuBestellendeHolzeinheiten   Menge an Holzkomponenten
     * @param zuBestellendeSchrauben       Anzahl der Eisenwaren/Schrauben
     * @param zuBestellendeFarbeinheiten   Volumen der Farbmittel
     * @param zuBestellendeKartoneinheiten Menge der Verpackungseinheiten
     * @param zuBestellendeGlaseinheiten   Anzahl der Glaselemente
     * * @return Gibt "true" zurueck, da die Annahme der Bestellung systemseitig garantiert ist.
     */
    public boolean wareBestellen(int zuBestellendeHolzeinheiten, int zuBestellendeSchrauben, int zuBestellendeFarbeinheiten, int zuBestellendeKartoneinheiten, int zuBestellendesGlas) {
        // Rückwärtskompatibel: ohne Historie-Eintrag, Lieferung setzt Bestand auf MAX in Lager.wareLiefern()
        this.lager.istBestellt = true;
        System.out.println("Lieferant: Auftrag eingegangen. Der Versand wird vorbereitet.");
        this.start();
        return true;
    }

    /** Überladene Variante: Lieferung mit konkreten Mengen und Historie-Verknüpfung. */
    public boolean wareBestellen(int holz, int schrauben, int farbe, int karton, int glas, Lager.RestockEntry entry) {
        this.mHolz = Math.max(0, holz);
        this.mSchrauben = Math.max(0, schrauben);
        this.mFarbe = Math.max(0, farbe);
        this.mKarton = Math.max(0, karton);
        this.mGlas = Math.max(0, glas);
        this.entryRef = entry;
        this.lager.istBestellt = true;
        System.out.println("Lieferant: Auftrag eingegangen. Der Versand wird vorbereitet.");
        this.start();
        return true;
    }

    /**
     * Steuert den eigentlichen Zustellprozess im Hintergrund. Der Ablauf wird durch das Abwarten 
     * der definierten Lieferfrist simuliert. 
     * In dieser Simulation wird die Zeit gerafft (1 Stunde entspricht 1 Sekunde), wodurch die 
     * Lieferung beschleunigt erfolgt. Nach Ablauf der Frist wird der Bestand im Lager aktualisiert.
     */
    @Override
    public void run() {
        System.out.println("Der Erhalt des Auftrags wurde quittiert.");
        ThreadUtil.sleep((int) neueLieferzeit); // Pausiert den Thread fuer die Dauer der Lieferzeit
        System.out.println("Die Rohstoffe werden nun am Lager abgeliefert.");
        if (entryRef != null) {
            // Zielgerichtete Lieferung mit Mengen und Historie-Aktualisierung
            lager.wareLiefern(mHolz, mSchrauben, mFarbe, mKarton, mGlas, entryRef);
        } else {
            // Legacy: Vollauffüllung
            lager.wareLiefern();
        }
    }
}