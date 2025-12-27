import java.util.*;

/**
 * Einfache In-Memory-Datenbank für Kunden.
 */
public class KundenDatenbank {
    private final Map<Integer, Kunde> kunden = new HashMap<>();
    private int nextNr = 1000;

    /** Legt einen neuen Kunden an und gibt ihn zurück. */
    public synchronized Kunde kundeAnlegen(String name, String firma, String position) {
        int nr = nextNr++;
        Kunde k = new Kunde(nr, name, firma, position);
        kunden.put(nr, k);
        return k;
    }

    /** Liefert eine unveränderliche Kopie aller Kunden. */
    public synchronized java.util.List<Kunde> gibAlleKunden() {
        ArrayList<Kunde> list = new ArrayList<>(kunden.values());
        list.sort(Comparator.comparingInt(Kunde::gibKundenNr));
        return java.util.Collections.unmodifiableList(list);
    }

    /** Sucht einen Kunden per Nummer. */
    public synchronized Kunde findeKunde(int kundenNr) {
        return kunden.get(kundenNr);
    }

    /** Aktualisiert einen Kunden (Name/Firma/Position) anhand seiner Nummer. */
    public synchronized void kundeAktualisieren(Kunde k) {
        if (k == null) return;
        Kunde existing = kunden.get(k.gibKundenNr());
        if (existing != null) {
            existing.setzeName(k.gibName());
            existing.setzeFirma(k.gibFirma());
            existing.setzePosition(k.gibPosition());
        }
    }
}
