import java.util.LinkedList;

/**
 * Allgemeine Roboter-Basis mit Warteschlange, Service-Historie und Pause-Funktion.
 * Produktionsschritte sind in Teilintervalle aufgeteilt, um einen Notfall-Stop mit
 * exakter Wiederaufnahme an derselben Station zu ermöglichen.
 */
public class Roboter extends Thread {
    public static final int QUEUE_WARN_THRESHOLD = 10;
    private LinkedList<Produkt> warteschlange = new LinkedList<>();
    private String name;
    private int produktionsJahr = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
    private java.util.List<ServiceEntry> serviceHistorie = new java.util.ArrayList<>();

    private int produktionsZeit;
    private int produktionsZeitStandardtuer = Standardtuer.getProduktionszeit();
    private int produktionsZeitPremiumtuer = Premiumtuer.getProduktionszeit();

    private volatile boolean pausiert = false;
    private volatile String aktuellesProduktName = null;
    private volatile long aktuellerStartMs = 0L;
    private volatile int aktuellerGesamtMs = 0;

    private Produkt aktuellesProdukt = null;
    private int verbleibendeMs = 0;

    public Roboter(String name) {
        this.name = name;
        this.warteschlange = new LinkedList<>();
    }

    public LinkedList<Produkt> getWarteschlange() { return warteschlange; }

    public void run() {
        while (true) {
            if (pausiert) {
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                continue;
            }

            boolean etwasGemacht = false;
            if (aktuellesProdukt != null && verbleibendeMs > 0) {
                boolean fertig = produziereProduktIntern(aktuellesProdukt);
                etwasGemacht = true;
                if (fertig) {
                    Produkt abgeschlossen = aktuellesProdukt;
                    aktuellesProdukt = null;
                    abgeschlossen.naechsteProduktionsStation();
                }
            } else {
                Produkt p = warteschlange.peek();
                if (p != null) {
                    warteschlange.poll();
                    aktuellesProdukt = p;
                    boolean fertig = produziereProduktIntern(p);
                    etwasGemacht = true;
                    if (fertig) {
                        aktuellesProdukt = null;
                        p.naechsteProduktionsStation();
                    }
                }
            }

            if (!etwasGemacht) {
                try { Thread.sleep(1000); } catch (InterruptedException ie) { ie.printStackTrace(); }
            }
        }
    }

    private int berechneStationsDauer(Produkt produkt) {
        if (produkt instanceof Standardtuer) {
            return Math.max(1, produktionsZeitStandardtuer / 4);
        } else if (produkt instanceof Premiumtuer) {
            return Math.max(1, produktionsZeitPremiumtuer / 4);
        } else {
            int basis = (produktionsZeit > 0 ? produktionsZeit : 1500);
            return Math.max(1, basis / 4);
        }
    }

    private boolean produziereProduktIntern(Produkt produkt) {
        int dauer = berechneStationsDauer(produkt);
        if (verbleibendeMs <= 0) {
            verbleibendeMs = dauer;
            aktuellerGesamtMs = dauer;
            aktuellerStartMs = System.currentTimeMillis();
            aktuellesProduktName = produkt != null ? produkt.getClass().getSimpleName() : null;
            ThreadUtil.synchronisiertesPrintln(name + " produziert jetzt: " + (produkt != null ? produkt.getClass().getSimpleName() : "-") );
        }
        int schritt = 100;
        while (verbleibendeMs > 0) {
            if (pausiert) {
                return false;
            }
            int sleepMs = Math.min(schritt, verbleibendeMs);
            ThreadUtil.sleep(sleepMs);
            verbleibendeMs -= sleepMs;
        }
        aktuellesProduktName = null;
        aktuellerGesamtMs = 0;
        aktuellerStartMs = 0L;
        verbleibendeMs = 0;
        ThreadUtil.synchronisiertesPrintln(name + " hat fertig: " + (produkt != null ? produkt.getClass().getSimpleName() : "-") );
        return true;
    }

    // Öffentliche, blockierende Produktionsmethode (Kompatibilität für Tests)
    public void produziereProdukt(Produkt produkt) {
        aktuellesProduktName = (produkt != null ? produkt.getClass().getSimpleName() : null);
        aktuellerStartMs = System.currentTimeMillis();
        int dauer = berechneStationsDauer(produkt);
        aktuellerGesamtMs = dauer;
        ThreadUtil.sleep(dauer);
        aktuellesProduktName = null;
        aktuellerGesamtMs = 0;
        aktuellerStartMs = 0L;
    }

    public static int getQueueWarnThreshold() { return QUEUE_WARN_THRESHOLD; }

    public String gibAktuellesProduktName() { return aktuellesProduktName; }

    public int gibAktuellenFortschrittProzent() {
        if (aktuellerGesamtMs <= 0) return 0;
        int erledigt = aktuellerGesamtMs - Math.max(0, verbleibendeMs);
        int pct = (int)Math.min(100, Math.round((erledigt*100.0)/aktuellerGesamtMs));
        return Math.max(0, pct);
    }

    public void fuegeProduktHinzu(Produkt produkt) { warteschlange.add(produkt); }

    public void setzePausiert(boolean wert) { this.pausiert = wert; }
    public boolean istPausiert() { return this.pausiert; }

    public void fuegeServiceEintragHinzu(String person, String notiz) {
        serviceHistorie.add(new ServiceEntry(System.currentTimeMillis(), person, notiz));
    }
    public java.util.List<ServiceEntry> gibServiceHistorie() { return new java.util.ArrayList<>(serviceHistorie); }

    public void setzeProduktionsZeit(int zeit) { this.produktionsZeit = zeit; }
    public void setzeProduktionsZeitPremiumtuer (int produktionszeit) { this.produktionsZeitPremiumtuer = produktionszeit; }
    public void setzeProduktionsZeitStandardtuer (int produktionszeit) { this.produktionsZeitStandardtuer = produktionszeit; }

    public String gibNamen(){ return name; }
    public void setzeProduktionsJahr(int jahr){ this.produktionsJahr = jahr; }
    public int gibProduktionsJahr(){ return produktionsJahr; }
}

class ServiceEntry {
    public final long zeitpunktMs;
    public final String person;
    public final String notiz;
    public ServiceEntry(long zeitpunktMs, String person, String notiz) {
        this.zeitpunktMs = zeitpunktMs;
        this.person = person == null ? "" : person;
        this.notiz = notiz == null ? "" : notiz;
    }
}