/**
 * Dieses Dienstprogramm dient als Assistenzmodul fuer die Steuerung von Threads. 
 * Es stellt praktische Funktionen bereit, welche die Koordination von parallelen 
 * Prozessen erleichtern, insbesondere durch das kontrollierte Anhalten von Threads 
 * sowie die abgesicherte Textausgabe in einem thread-sicheren Umfeld.
 *
 * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class ThreadUtil
{
    private static java.util.function.Consumer<String> extraLogger;
    public static void setLogger(java.util.function.Consumer<String> logger) {
        extraLogger = logger;
    }
 /**
  * Versetzt den aktuell ausfuehrenden Thread fuer eine definierte Dauer in den Ruhezustand.
  * Diese Funktion bewirkt eine Unterbrechung der Thread-Aktivitaet fuer die angegebene 
  * Anzahl an Millisekunden. Dies ist besonders wertvoll, um zeitliche Verzoegerungen 
  * abzubilden oder die Synchronitaet zwischen verschiedenen Ablaeufen zu regulieren.
  *
  * @param time Das Zeitintervall in Millisekunden, fuer welches der Thread pausieren soll.
  * @throws IllegalArgumentException Falls ein negativer Zeitwert uebergeben wird.
  */
    public static void sleep (int time) {
    try {
            Thread.sleep(time);
        }
        catch (InterruptedException ie) { 
            // Protokolliert den Fehlerverlauf bei einer unerwarteten Unterbrechung. 
            // Dies unterstuezt die Fehlerdiagnose, um Stoerfaktoren im Thread-Ablauf zu identifizieren.
            ie.printStackTrace();
            // Stellt den Unterbrechungs-Status wieder her, um eine saubere Weitergabe des Signals zu gewaehrleisten.
            Thread.currentThread().interrupt();
        }
    }
    
/**
 * Diese Prozedur unterbindet Konflikte beim gleichzeitigen Zugriff verschiedener Threads auf die Konsole. 
 * Dadurch wird sichergestellt, dass die Datenkanaele jeweils nur von einer Einheit exklusiv genutzt werden.
 *
 * @param message Der Textinhalt, welcher im Ausgabefenster erscheinen soll.
 * * @throws NullPointerException Falls die zu sendende Nachricht den Wert null aufweist.
 */
    public static void synchronisiertesPrintln(String message) {
        if (message == null) {
            throw new NullPointerException("Die Textnachricht darf nicht leer (null) sein.");
        }
        // Blockiert den Zugriff fuer andere Threads, waehrend die Ausgabe erfolgt.
        synchronized (System.out) {
            System.out.println(message); // Schreibt die Zeile, sobald der Thread die Zugriffsberechtigung besitzt.
            if (extraLogger != null) {
                try { extraLogger.accept(message); } catch (Exception ignore) {}
            }
        }
    }
}