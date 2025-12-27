/**
 * Repräsentiert einen Kunden im System.
 */
public class Kunde {
    private final int kundenNr;
    private String name;
    private String firma;
    private String position;

    public Kunde(int kundenNr, String name, String firma, String position) {
        this.kundenNr = kundenNr;
        this.name = name != null ? name : "";
        this.firma = firma != null ? firma : "";
        this.position = position != null ? position : "";
    }

    public int gibKundenNr() { return kundenNr; }
    public String gibName() { return name; }
    public String gibFirma() { return firma; }
    public String gibPosition() { return position; }

    public void setzeName(String name) { this.name = name != null ? name : ""; }
    public void setzeFirma(String firma) { this.firma = firma != null ? firma : ""; }
    public void setzePosition(String position) { this.position = position != null ? position : ""; }

    @Override
    public String toString() {
        String f = (firma != null && !firma.isEmpty()) ? (" (" + firma + ")") : "";
        return "#" + kundenNr + " " + (name != null ? name : "") + f;
    }
}
