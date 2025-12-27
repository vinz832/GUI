import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

/**
 * Tests zur Verknüpfung der GUI mit dem Backend (Fabrik/KundenDB).
 */
public class FabrikGUITest {

    /** Fabrik-Mock durch Subklasse: merkt letzte Aufruf-Parameter. */
    static class TestFabrik extends Fabrik {
        volatile Integer lastKundeNr;
        volatile Integer lastStd;
        volatile Integer lastPrem;
        @Override
        public void bestellungAufgeben(int kundenNr, int anzahlStandardtueren, int anzahlPremiumtueren) {
            this.lastKundeNr = kundenNr;
            this.lastStd = anzahlStandardtueren;
            this.lastPrem = anzahlPremiumtueren;
            // Ruft dennoch die echte Logik auf, um Seiteneffekte zu minimieren
            super.bestellungAufgeben(kundenNr, anzahlStandardtueren, anzahlPremiumtueren);
        }
    }

    private static Component findByName(Container root, String name) {
        if (root == null) return null;
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName())) return c;
            if (c instanceof Container) {
                Component r = findByName((Container)c, name);
                if (r != null) return r;
            }
        }
        return null;
    }

    @Test
    public void testBestellungButtonRuftBackendAuf() throws Exception {
        TestFabrik fabrik = new TestFabrik();
        Kunde k = fabrik.kundeAnlegen("Max Muster", "Muster GmbH", "Einkauf");

        FabrikGUI gui = new FabrikGUI(fabrik, false);
        JFrame frame = gui.getFrame();

        JTextField tfStd = (JTextField) findByName(frame.getContentPane(), "tfStandard");
        JTextField tfPrem = (JTextField) findByName(frame.getContentPane(), "tfPremium");
        JTextField tfKnr = (JTextField) findByName(frame.getContentPane(), "tfKundenNr");
        JButton btnOrder = (JButton) findByName(frame.getContentPane(), "btnBestellungAufgeben");

        assertNotNull(tfStd);
        assertNotNull(tfPrem);
        assertNotNull(tfKnr);
        assertNotNull(btnOrder);

        SwingUtilities.invokeAndWait(() -> {
            tfStd.setText("3");
            tfPrem.setText("2");
            tfKnr.setText(String.valueOf(k.gibKundenNr()));
            btnOrder.doClick();
        });

        // kurze Wartezeit für EDT-Verarbeitung
        Thread.sleep(50);

        assertEquals(k.gibKundenNr(), fabrik.lastKundeNr);
        assertEquals(3, fabrik.lastStd.intValue());
        assertEquals(2, fabrik.lastPrem.intValue());
    }

    @Test
    public void testKundenSpeichernAktualisiertBackend() throws Exception {
        Fabrik fabrik = new Fabrik();
        Kunde k1 = fabrik.kundeAnlegen("Anna Alt", "Alt AG", "Einkauf");
        Kunde k2 = fabrik.kundeAnlegen("Bernd Neu", "Neu GmbH", "Leitung");

        FabrikGUI gui = new FabrikGUI(fabrik, false);
        JFrame frame = gui.getFrame();

        // Menüpunkt "Kunden -> Übersicht" auslösen
        JMenuBar mb = frame.getJMenuBar();
        JMenuItem mi = null;
        for (int i=0;i<mb.getMenuCount();i++) {
            JMenu m = mb.getMenu(i);
            for (int j=0;j<m.getItemCount();j++) {
                JMenuItem it = m.getItem(j);
                if (it != null && "miKundenUebersicht".equals(it.getName())) { mi = it; break; }
            }
        }
        assertNotNull(mi);
        SwingUtilities.invokeAndWait(mi::doClick);

        JDialog dlg = gui.getLastDialog();
        assertNotNull(dlg);

        JTable tbl = (JTable) findByName(dlg.getContentPane(), "tblKunden");
        JButton btnSave = (JButton) findByName(dlg.getContentPane(), "btnKundenSpeichern");
        assertNotNull(tbl);
        assertNotNull(btnSave);

        // Werte ändern in erster Zeile
        SwingUtilities.invokeAndWait(() -> {
            int row = 0;
            tbl.setValueAt("Anna Aktualisiert", row, 1);
            tbl.setValueAt("Aktual GmbH", row, 2);
            tbl.setValueAt("CTO", row, 3);
            btnSave.doClick();
        });

        Kunde updated = fabrik.gibKundenDb().findeKunde(k1.gibKundenNr());
        assertNotNull(updated);
        assertEquals("Anna Aktualisiert", updated.gibName());
        assertEquals("Aktual GmbH", updated.gibFirma());
        assertEquals("CTO", updated.gibPosition());
    }
}
