import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.*;

/**
 * Tests verifizieren, dass GUI-Aktionen das Backend aufrufen.
 * Hinweis: Ausführung benötigt JUnit 5 auf dem Klassenpfad.
 */
public class FabrikGUIBackendTest {

    static class FakeLager extends Lager {
        int auffuellenCalled = 0;
        int manuellHolz, manuellSchrauben, manuellFarbe, manuellKarton, manuellGlas;
        @Override
        public void lagerAuffuellen() {
            auffuellenCalled++;
        }
        @Override
        public void manuelleNachbestellung(int holz, int schrauben, int farbe, int karton, int glas) {
            manuellHolz = holz; manuellSchrauben = schrauben; manuellFarbe = farbe; manuellKarton = karton; manuellGlas = glas;
        }
    }

    static class FakeFabrik extends Fabrik {
        final FakeLager fakeLager;
        int lastStd = -1, lastPrem = -1;
        final Produktionsmanager pm; // echter PM für Maschinenparkdialog
        public FakeFabrik() {
            fakeLager = new FakeLager();
            // Produktionsmanager mit echter Struktur, aber separatem Lager
            pm = new Produktionsmanager(this, fakeLager);
        }
        @Override
        public Lager gibLager() { return fakeLager; }
        @Override
        public void bestellungAufgeben(int std, int prem) {
            lastStd = std; lastPrem = prem;
        }
        @Override
        public Produktionsmanager gibProduktionsmanager() { return pm; }
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

    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void submitOrder_calls_backend() throws Exception {
        FakeFabrik f = new FakeFabrik();
        FabrikGUI gui = new FabrikGUI(f, false);
        JFrame frame = gui.getFrame();
        JTextField tfStd = (JTextField) findByName(frame.getContentPane(), "tfStandard");
        JTextField tfPrem = (JTextField) findByName(frame.getContentPane(), "tfPremium");
        JButton btnOrder = (JButton) findByName(frame.getContentPane(), "btnBestellungAufgeben");
        assertNotNull(tfStd); assertNotNull(tfPrem); assertNotNull(btnOrder);
        tfStd.setText("3"); tfPrem.setText("2");
        // Button-Klick simulieren
        SwingUtilities.invokeAndWait(btnOrder::doClick);
        assertEquals(3, f.lastStd);
        assertEquals(2, f.lastPrem);
    }

    @Test
    void menu_auffuellen_calls_lager() throws Exception {
        FakeFabrik f = new FakeFabrik();
        FabrikGUI gui = new FabrikGUI(f, false);
        JMenuBar bar = gui.getFrame().getJMenuBar();
        JMenuItem miAuff = null;
        // durchsuchen
        for (MenuElement me : bar.getSubElements()) {
            if (me instanceof JMenu) {
                for (MenuElement mi : ((JMenu)me).getSubElements()) {
                    Component c = mi.getComponent();
                    if (c instanceof JMenuItem && "miAuffuellen".equals(c.getName())) {
                        miAuff = (JMenuItem)c; break;
                    }
                }
            }
        }
        assertNotNull(miAuff);
        SwingUtilities.invokeAndWait(miAuff::doClick);
        assertEquals(1, f.fakeLager.auffuellenCalled);
    }

    @Test
    void nachbestellen_dialog_calls_manuelleNachbestellung() throws Exception {
        FakeFabrik f = new FakeFabrik();
        FabrikGUI gui = new FabrikGUI(f, false);
        // Menü öffnen
        JMenuBar bar = gui.getFrame().getJMenuBar();
        JMenuItem miOpen = null;
        for (MenuElement me : bar.getSubElements()) {
            if (me instanceof JMenu) {
                for (MenuElement mi : ((JMenu)me).getSubElements()) {
                    Component c = mi.getComponent();
                    if (c instanceof JMenuItem && "miNachbestellenOpen".equals(c.getName())) {
                        miOpen = (JMenuItem)c; break;
                    }
                }
            }
        }
        assertNotNull(miOpen);
        SwingUtilities.invokeAndWait(miOpen::doClick);
        JDialog dlg = gui.getLastDialog();
        assertNotNull(dlg);
        JSpinner spHolz = (JSpinner) findByName(dlg.getContentPane(), "spHolz");
        JSpinner spSchrauben = (JSpinner) findByName(dlg.getContentPane(), "spSchrauben");
        JSpinner spFarbe = (JSpinner) findByName(dlg.getContentPane(), "spFarbe");
        JSpinner spGlas = (JSpinner) findByName(dlg.getContentPane(), "spGlas");
        JSpinner spKarton = (JSpinner) findByName(dlg.getContentPane(), "spKarton");
        JButton btnAdd = (JButton) findByName(dlg.getContentPane(), "btnNachbestellenLieferant");
        assertNotNull(spHolz); assertNotNull(spSchrauben); assertNotNull(spFarbe);
        assertNotNull(spGlas); assertNotNull(spKarton); assertNotNull(btnAdd);
        spHolz.setValue(10); spSchrauben.setValue(50); spFarbe.setValue(5); spGlas.setValue(2); spKarton.setValue(20);
        SwingUtilities.invokeAndWait(btnAdd::doClick);
        assertEquals(10, f.fakeLager.manuellHolz);
        assertEquals(50, f.fakeLager.manuellSchrauben);
        assertEquals(5, f.fakeLager.manuellFarbe);
        assertEquals(20, f.fakeLager.manuellKarton);
        assertEquals(2, f.fakeLager.manuellGlas);
    }

    @Test
    void maschinenpark_service_and_pause() throws Exception {
        FakeFabrik f = new FakeFabrik();
        FabrikGUI gui = new FabrikGUI(f, false);
        // Öffnen
        JMenuBar bar = gui.getFrame().getJMenuBar();
        JMenuItem miMp = null;
        for (MenuElement me : bar.getSubElements()) {
            if (me instanceof JMenu) {
                for (MenuElement mi : ((JMenu)me).getSubElements()) {
                    Component c = mi.getComponent();
                    if (c instanceof JMenuItem && "miMaschinenparkOeffnen".equals(c.getName())) {
                        miMp = (JMenuItem)c; break;
                    }
                }
            }
        }
        assertNotNull(miMp);
        SwingUtilities.invokeAndWait(miMp::doClick);
        JDialog dlg = gui.getLastDialog();
        assertNotNull(dlg);
        JTextField tfPerson = (JTextField) findByName(dlg.getContentPane(), "tfServicePerson");
        JTextField tfNotiz = (JTextField) findByName(dlg.getContentPane(), "tfServiceNotiz");
        JButton btnSvc = (JButton) findByName(dlg.getContentPane(), "btnServiceHinzufuegen");
        JButton btnToggle = (JButton) findByName(dlg.getContentPane(), "btnPauseStart");
        assertNotNull(tfPerson); assertNotNull(tfNotiz); assertNotNull(btnSvc); assertNotNull(btnToggle);
        // Service-Eintrag hinzufügen
        tfPerson.setText("Tester"); tfNotiz.setText("Wartung durchgeführt");
        SwingUtilities.invokeAndWait(btnSvc::doClick);
        // Erster Roboter aus PM sollte einen Eintrag haben
        Roboter r = f.pm.getHolzRoboter();
        assertFalse(r.gibServiceHistorie().isEmpty());
        // Pause/Start toggeln
        boolean prev = r.istPausiert();
        SwingUtilities.invokeAndWait(btnToggle::doClick);
        assertNotEquals(prev, r.istPausiert());
    }
}
