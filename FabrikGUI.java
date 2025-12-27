import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * Swing-Oberfläche für SmartDoorManufacturing: Auftragserfassung, Status-Tabelle,
 * Überwachung (Kennzahlen/Alerts) und Log-Ausgabe.
 */
public class FabrikGUI {
    private JFrame frame;
    private Fabrik fabrik;
    private boolean showFrameUi = true;
    private JDialog lastDialog;
    private JTextField tfStandard;
    private JTextField tfPremium;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblHolz, lblSchrauben, lblFarbe, lblGlas, lblKarton, lblLieferant, lblQueue, lblInProd;
    // KPI
    private JLabel kpiOpen, kpiInProd, kpiDone, kpiAvgLt, kpiMaxLt, kpiShortages, kpiAvgFlow;
    // Alerts
    private DefaultListModel<String> alertsModel;
    private JList<String> alertsList;
    // Optional mini visualization
    private JProgressBar pbQueue, pbMaterial, pbDone;
    private JTextArea logArea;
    // Restocking history model (shown in Nachbestellung-Dialog)
    private JTable histTable;
    private DefaultTableModel histModel;

    public FabrikGUI() {
        this(new Fabrik(), true);
    }

    // Alternativer Konstruktor für Tests: Backend injizieren und Sichtbarkeit steuern
    public FabrikGUI(Fabrik fabrik, boolean showFrame) {
        this.fabrik = fabrik;
        this.showFrameUi = showFrame;
        initUI();
        // Leitet Backend-Logs in die GUI um und hakt Systemausgaben ein
        GuiLogger.setLogArea(logArea);
        GuiLogger.hookSystemOutAndErr();
        startTimers();
        // System.out/err ist umgeleitet; doppelte ThreadUtil-Logs vermeiden
        // ThreadUtil.setLogger(GuiLogger::log);
    }

    private void initUI() {
        frame = new JFrame("SmartDoorManufacturing – Fabrikleiter Übersicht");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        // Menüleiste mit Nachbestellung
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Nachbestellung");
        JMenuItem miOpen = new JMenuItem("Bestand nachbestellen...");
        miOpen.setName("miNachbestellenOpen");
        miOpen.addActionListener(e -> openNachbestellungDialog(0));
        JMenuItem miAuffuellen = new JMenuItem("Auf Max auffüllen");
        miAuffuellen.setName("miAuffuellen");
        miAuffuellen.addActionListener(e -> {
            try {
                fabrik.gibLager().lagerAuffuellen();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Fehler bei Auffüllung: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        });
        JMenuItem miHistorie = new JMenuItem("Historie");
        miHistorie.setName("miHistorieOpen");
        miHistorie.addActionListener(e -> openNachbestellungDialog(1));
        menu.add(miOpen);
        menu.add(miAuffuellen);
        menu.add(miHistorie);
        menuBar.add(menu);

        // Maschinenpark top-level menu
        JMenu mpMenu = new JMenu("Maschinenpark");
        JMenuItem mpOpen = new JMenuItem("Öffnen");
        mpOpen.setName("miMaschinenparkOeffnen");
        mpOpen.addActionListener(e -> openMaschinenparkDialog());
        mpMenu.add(mpOpen);
        menuBar.add(mpMenu);
        frame.setJMenuBar(menuBar);

        // WEST: Auftragseingabe
        JPanel west = new JPanel(new GridBagLayout());
        west.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.anchor = GridBagConstraints.WEST;

        JLabel lblTitle = new JLabel("Auftrag eingeben");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD));
        c.gridx=0; c.gridy=0; c.gridwidth=2; west.add(lblTitle, c);

        c.gridwidth=1;
        c.gridx=0; c.gridy=1; west.add(new JLabel("Standardtüren:"), c);
        tfStandard = new JTextField(8);
        tfStandard.setName("tfStandard");
        c.gridx=1; c.gridy=1; west.add(tfStandard, c);

        c.gridx=0; c.gridy=2; west.add(new JLabel("Premiumtüren:"), c);
        tfPremium = new JTextField(8);
        tfPremium.setName("tfPremium");
        c.gridx=1; c.gridy=2; west.add(tfPremium, c);

        JButton btnOrder = new JButton("Bestellung aufgeben");
        btnOrder.setName("btnBestellungAufgeben");
        btnOrder.addActionListener(e -> submitOrder());
        c.gridx=0; c.gridy=3; c.gridwidth=2; c.fill = GridBagConstraints.HORIZONTAL; west.add(btnOrder, c);

        // Der Storno-Button wird künftig pro Zeile in der Tabelle angezeigt

        frame.add(west, BorderLayout.WEST);

        // CENTER: Status-Tabelle (JTable)
        String[] cols = {"Bestellnummer", "Standard", "Premium", "Beschaffungszeit", "Lieferzeit", "Status", "Aktion"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int column) { return column == 6; }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        JScrollPane centerScroll = new JScrollPane(table);
        centerScroll.setBorder(BorderFactory.createTitledBorder("Bestellungen"));
        frame.add(centerScroll, BorderLayout.CENTER);

        // Renderer/Editor für Aktions-Button je Zeile
        javax.swing.table.TableColumn actionCol = table.getColumnModel().getColumn(6);
        actionCol.setCellRenderer(new ActionButtonRenderer());
        actionCol.setCellEditor(new ActionButtonEditor());

        // EAST: Überwachung/Monitoring
        JPanel east = new JPanel();
        east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
        east.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        // KPI-Panel (Kennzahlen)
        JPanel kpiPanel = new JPanel(new GridLayout(0,3,8,8));
        kpiPanel.setBorder(BorderFactory.createTitledBorder("KPI"));
        kpiOpen = bigLabel();
        kpiInProd = bigLabel();
        kpiDone = bigLabel();
        kpiAvgLt = bigLabel();
        kpiMaxLt = bigLabel();
        kpiShortages = bigLabel();
        kpiAvgFlow = bigLabel();
        kpiPanel.add(compoundLabel("Offene Bestellungen", kpiOpen));
        kpiPanel.add(compoundLabel("In Produktion", kpiInProd));
        kpiPanel.add(compoundLabel("Fertig", kpiDone));
        kpiPanel.add(compoundLabel("Ø Lieferzeit (Tage)", kpiAvgLt));
        kpiPanel.add(compoundLabel("Max. Lieferzeit (Tage)", kpiMaxLt));
        kpiPanel.add(compoundLabel("Material-Engpässe", kpiShortages));
        kpiPanel.add(compoundLabel("Ø Durchlaufzeit (s)", kpiAvgFlow));
        east.add(kpiPanel);

        // Warnungen/Alerts-Panel
        alertsModel = new DefaultListModel<>();
        alertsList = new JList<>(alertsModel);
        JScrollPane alertsScroll = new JScrollPane(alertsList);
        alertsScroll.setBorder(BorderFactory.createTitledBorder("Alerts"));
        east.add(alertsScroll);

        // Historie-Modell vorbereiten (Anzeige im Nachbestellung-Dialog statt im Dashboard)
        String[] histCols = {"Zeit", "Typ", "Holz", "Schrauben", "Farbe", "Glas", "Karton", "Status/ETA"};
        histModel = new DefaultTableModel(histCols, 0) { public boolean isCellEditable(int r,int c){return false;} };

        JPanel stocks = new JPanel(new GridLayout(0,1,2,2));
        stocks.setBorder(BorderFactory.createTitledBorder("Monitoring"));
        lblHolz = new JLabel();
        lblSchrauben = new JLabel();
        lblFarbe = new JLabel();
        lblGlas = new JLabel();
        lblKarton = new JLabel();
        lblLieferant = new JLabel();
        lblQueue = new JLabel();
        lblInProd = new JLabel();
        stocks.add(lblHolz);
        stocks.add(lblSchrauben);
        stocks.add(lblFarbe);
        stocks.add(lblGlas);
        stocks.add(lblKarton);
        stocks.add(lblLieferant);
        stocks.add(lblQueue);
        stocks.add(lblInProd);
        // Optionale Mini-Visualisierung (Fortschrittsbalken)
        JPanel miniViz = new JPanel(new GridLayout(0,1,4,4));
        miniViz.setBorder(BorderFactory.createTitledBorder("Auslastung"));
        pbQueue = new JProgressBar(0, 100);
        pbMaterial = new JProgressBar(0, 100);
        pbDone = new JProgressBar(0, 100);
        pbQueue.setStringPainted(true);
        pbMaterial.setStringPainted(true);
        pbDone.setStringPainted(true);
        miniViz.add(labeledBar("Roboter Queue", pbQueue));
        miniViz.add(labeledBar("Material Health", pbMaterial));
        miniViz.add(labeledBar("Fertigstellungsquote", pbDone));
        east.add(miniViz);

        east.add(stocks);

        frame.add(east, BorderLayout.EAST);

        // SOUTH: Log-Ausgabe
        logArea = new JTextArea(8, 80);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Log"));
        frame.add(logScroll, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        if (showFrameUi) {
            frame.setVisible(true);
        }
    }

    private void openNachbestellungDialog() { openNachbestellungDialog(0); }

    private void openNachbestellungDialog(int selectedTab) {
        Lager lager = fabrik.gibLager();
        JDialog dlg = new JDialog(frame, "Nachbestellung", true);
        dlg.setLayout(new BorderLayout(8,8));
        dlg.setName("dlgNachbestellung");
        // Sichtbarkeit fürs Testen steuern: in Headless/Tests nicht modal/visible
        dlg.setModal(showFrameUi);
        lastDialog = dlg;

        // Tabs: Nachbestellen + Historie
        JTabbedPane tabs = new JTabbedPane();

        // Tab 1: Nachbestellen (manuelle Mengen)
        JPanel orderPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.anchor = GridBagConstraints.WEST;

        JSpinner spHolz = new JSpinner(new SpinnerNumberModel(0, 0, Lager.MAXHOLZEINHEITEN, 10)); spHolz.setName("spHolz");
        JSpinner spSchrauben = new JSpinner(new SpinnerNumberModel(0, 0, Lager.MAXSCHRAUBEN, 50)); spSchrauben.setName("spSchrauben");
        JSpinner spFarbe = new JSpinner(new SpinnerNumberModel(0, 0, Lager.MAXFARBEINHEITEN, 10)); spFarbe.setName("spFarbe");
        JSpinner spGlas = new JSpinner(new SpinnerNumberModel(0, 0, Lager.MAXGLASEINHEITEN, 5)); spGlas.setName("spGlas");
        JSpinner spKarton = new JSpinner(new SpinnerNumberModel(0, 0, Lager.MAXKARTONEINHEITEN, 20)); spKarton.setName("spKarton");

        int y=0;
        c.gridx=0; c.gridy=y; orderPanel.add(new JLabel("Holz hinzufügen:"), c); c.gridx=1; orderPanel.add(spHolz, c); y++;
        c.gridx=0; c.gridy=y; orderPanel.add(new JLabel("Schrauben hinzufügen:"), c); c.gridx=1; orderPanel.add(spSchrauben, c); y++;
        c.gridx=0; c.gridy=y; orderPanel.add(new JLabel("Farbe hinzufügen:"), c); c.gridx=1; orderPanel.add(spFarbe, c); y++;
        c.gridx=0; c.gridy=y; orderPanel.add(new JLabel("Glas hinzufügen:"), c); c.gridx=1; orderPanel.add(spGlas, c); y++;
        c.gridx=0; c.gridy=y; orderPanel.add(new JLabel("Karton hinzufügen:"), c); c.gridx=1; orderPanel.add(spKarton, c); y++;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Nachbestellen (Lieferant)");
        btnAdd.setName("btnNachbestellenLieferant");
        JButton btnClose = new JButton("Schließen");
        btnClose.setName("btnNachbestellenSchliessen");
        buttons.add(btnAdd); buttons.add(btnClose);
        c.gridx=0; c.gridy=y; c.gridwidth=2; c.fill=GridBagConstraints.HORIZONTAL; orderPanel.add(buttons, c);

        btnAdd.addActionListener(e -> {
            try {
                int addHolz = ((Number)spHolz.getValue()).intValue();
                int addSchrauben = ((Number)spSchrauben.getValue()).intValue();
                int addFarbe = ((Number)spFarbe.getValue()).intValue();
                int addGlas = ((Number)spGlas.getValue()).intValue();
                int addKarton = ((Number)spKarton.getValue()).intValue();

                lager.manuelleNachbestellung(addHolz, addSchrauben, addFarbe, addKarton, addGlas);
                dlg.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Fehler bei Nachbestellung: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnClose.addActionListener(e -> dlg.dispose());

        tabs.addTab("Nachbestellen", orderPanel);

        // Tab 2: Historie
        if (histTable == null) {
            histTable = new JTable(histModel);
        } else {
            histTable.setModel(histModel);
        }
        JScrollPane histScroll = new JScrollPane(histTable);
        tabs.addTab("Historie", histScroll);

        dlg.add(tabs, BorderLayout.CENTER);
        tabs.setSelectedIndex(Math.max(0, Math.min(1, selectedTab)));

        dlg.setSize(new Dimension(640, 400));
        dlg.setLocationRelativeTo(frame);
        if (showFrameUi) {
            dlg.setVisible(true);
        }
    }

    private void openMaschinenparkDialog() {
        Produktionsmanager pm = fabrik.gibProduktionsmanager();
        JDialog dlg = new JDialog(frame, "Maschinenpark", true);
        dlg.setLayout(new BorderLayout(8,8));
        lastDialog = dlg;

        JTabbedPane tabs = new JTabbedPane();

        // Tab: Übersicht
        JPanel overview = new JPanel(new BorderLayout(8,8));
        JPanel emergencyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnStopAll = new JButton("Notfall-Stop");
        btnStopAll.setName("btnStopAll");
        btnStopAll.setBackground(Color.RED);
        btnStopAll.setForeground(Color.WHITE);
        JButton btnStartAll = new JButton("Produktion starten");
        btnStartAll.setName("btnStartAll");
        btnStartAll.setBackground(new Color(0,128,0));
        btnStartAll.setForeground(Color.WHITE);
        JLabel lblSystemStatus = new JLabel("Systemstatus: Normalbetrieb");
        lblSystemStatus.setName("lblSystemStatus");
        emergencyPanel.add(btnStopAll);
        emergencyPanel.add(btnStartAll);
        emergencyPanel.add(lblSystemStatus);
        overview.add(emergencyPanel, BorderLayout.NORTH);
        java.util.List<Roboter> robots = pm != null ? pm.gibMaschinenpark() : java.util.Collections.emptyList();
        DefaultListModel<Roboter> robotModel = new DefaultListModel<>();
        for (Roboter r : robots) robotModel.addElement(r);
        JList<Roboter> robotList = new JList<>(robotModel);
        robotList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        robotList.setCellRenderer(new DefaultListCellRenderer(){
            public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean isSelected,boolean cellHasFocus){
                JLabel l=(JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                if (value instanceof Roboter) {
                    Roboter rr = (Roboter)value;
                    String status = rr.istPausiert()? "Pausiert" : "Aktiv";
                    int q = rr.getWarteschlange()!=null? rr.getWarteschlange().size():0;
                    l.setText(rr.gibNamen()+"  (Q:"+q+", "+status+")");
                }
                return l;
            }
        });
        overview.add(new JScrollPane(robotList), BorderLayout.WEST);

        // Detailbereich
        JPanel detail = new JPanel(new GridBagLayout());
        GridBagConstraints dc = new GridBagConstraints();
        dc.insets = new Insets(4,4,4,4);
        dc.anchor = GridBagConstraints.WEST;
        JLabel dName = new JLabel("Name: -");
        JLabel dYear = new JLabel("Produktionsjahr: -");
        JLabel dQueue = new JLabel("Warteschlange: -");
        JLabel dState = new JLabel("Status: -");
        dc.gridx=0; dc.gridy=0; detail.add(dName, dc);
        dc.gridx=0; dc.gridy=1; detail.add(dYear, dc);
        dc.gridx=0; dc.gridy=2; detail.add(dQueue, dc);
        dc.gridx=0; dc.gridy=3; detail.add(dState, dc);
        JLabel dCurrent = new JLabel("Aktuelles Produkt: -");
        JProgressBar dProgress = new JProgressBar(0, 100);
        dProgress.setStringPainted(true);
        dc.gridx=0; dc.gridy=4; dc.fill=GridBagConstraints.NONE; dc.weightx=0; detail.add(dCurrent, dc);
        dc.gridx=1; dc.gridy=4; dc.fill=GridBagConstraints.HORIZONTAL; dc.weightx=1.0; detail.add(dProgress, dc);
        // Tabelle: Service-Historie
        String[] svcCols = {"Zeit", "Person", "Notiz"};
        DefaultTableModel svcModel = new DefaultTableModel(svcCols,0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable svcTable = new JTable(svcModel);
        JScrollPane svcScroll = new JScrollPane(svcTable);
        svcScroll.setBorder(BorderFactory.createTitledBorder("Service-Historie"));
        dc.gridx=0; dc.gridy=5; dc.gridwidth=2; dc.fill=GridBagConstraints.BOTH; dc.weightx=1.0; dc.weighty=1.0; detail.add(svcScroll, dc);

        // Eingabefelder zum Hinzufügen eines Service-Eintrags
        JTextField tfPerson = new JTextField(18); tfPerson.setName("tfServicePerson");
        JTextField tfNote = new JTextField(24); tfNote.setName("tfServiceNotiz");
        JButton btnAddSvc = new JButton("Service hinzufügen"); btnAddSvc.setName("btnServiceHinzufuegen");
        JButton btnToggle = new JButton("Pause"); btnToggle.setName("btnPauseStart");
        JPanel addSvcPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addSvcPanel.add(new JLabel("Person:")); addSvcPanel.add(tfPerson);
        addSvcPanel.add(new JLabel("Notiz:")); addSvcPanel.add(tfNote);
        addSvcPanel.add(btnAddSvc);
        addSvcPanel.add(btnToggle);
        dc.gridx=0; dc.gridy=6; dc.gridwidth=2; dc.fill=GridBagConstraints.HORIZONTAL; dc.weightx=0; dc.weighty=0; detail.add(addSvcPanel, dc);

        overview.add(detail, BorderLayout.CENTER);

        final Roboter[] current = new Roboter[1];
        robotList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                current[0] = robotList.getSelectedValue();
                refreshRobotDetail(current[0], dName, dYear, dQueue, dState, dCurrent, dProgress, svcModel);
                btnToggle.setText(current[0] != null && current[0].istPausiert()? "Start" : "Pause");
            }
        });

        // Standardmäßig den ersten Eintrag auswählen und Details anzeigen
        if (robotModel.getSize() > 0) {
            robotList.setSelectedIndex(0);
            current[0] = robotModel.getElementAt(0);
            refreshRobotDetail(current[0], dName, dYear, dQueue, dState, dCurrent, dProgress, svcModel);
            btnToggle.setText(current[0] != null && current[0].istPausiert()? "Start" : "Pause");
        }

        // ENTER soll speichern: Standard-Button setzen und Feld-Listener
        dlg.getRootPane().setDefaultButton(btnAddSvc);
        tfNote.addActionListener(e -> btnAddSvc.doClick());
        tfPerson.addActionListener(e -> btnAddSvc.doClick());

        btnAddSvc.addActionListener(e -> {
            Roboter r = current[0];
            if (r == null) return;
            String person = tfPerson.getText().trim();
            String note = tfNote.getText().trim();
            if (person.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Bitte Serviceperson angeben.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
            r.fuegeServiceEintragHinzu(person, note);
            tfPerson.setText(""); tfNote.setText("");
            refreshRobotDetail(r, dName, dYear, dQueue, dState, dCurrent, dProgress, svcModel);
        });

        btnToggle.addActionListener(e -> {
            Roboter r = current[0];
            if (r == null) return;
            r.setzePausiert(!r.istPausiert());
            btnToggle.setText(r.istPausiert()? "Start" : "Pause");
            refreshRobotDetail(r, dName, dYear, dQueue, dState, dCurrent, dProgress, svcModel);
            robotList.repaint();
        });

        Runnable refreshEmergency = () -> {
            boolean stop = pm != null && pm.istNotfallStopAktiv();
            lblSystemStatus.setText(stop ? "Systemstatus: Notfall-Stop aktiv" : "Systemstatus: Normalbetrieb");
            btnStopAll.setEnabled(!stop);
            btnStartAll.setEnabled(stop);
        };
        btnStopAll.addActionListener(e -> { if (pm != null) { pm.aktiviereNotfallStop(); } refreshEmergency.run(); robotList.repaint(); });
        btnStartAll.addActionListener(e -> { if (pm != null) { pm.hebeNotfallStopAuf(); } refreshEmergency.run(); robotList.repaint(); });

        tabs.addTab("Übersicht", overview);

        // Tab: Neuer Roboter (zukunftsfähig)
        JPanel addRobot = new JPanel(new GridBagLayout());
        GridBagConstraints rc = new GridBagConstraints();
        rc.insets = new Insets(4,4,4,4); rc.anchor = GridBagConstraints.WEST;
        JComboBox<String> type = new JComboBox<>(new String[]{
            "Holzverarbeitungsroboter",
            "Montage_Roboter",
            "Lackier_Roboter",
            "Verpackungs_Roboter"
        });
        JTextField rName = new JTextField(16);
        JSpinner rYear = new JSpinner(new SpinnerNumberModel(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), 1980, 2100, 1));
        // Jahresanzeige ohne Tausendertrennzeichen (kein Apostroph)
        JSpinner.NumberEditor yearEditor = new JSpinner.NumberEditor(rYear, "####");
        rYear.setEditor(yearEditor);
        JButton btnCreate = new JButton("Roboter hinzufügen");
        int ry=0;
        rc.gridx=0; rc.gridy=ry; addRobot.add(new JLabel("Typ:"), rc); rc.gridx=1; addRobot.add(type, rc); ry++;
        rc.gridx=0; rc.gridy=ry; addRobot.add(new JLabel("Name:"), rc); rc.gridx=1; addRobot.add(rName, rc); ry++;
        rc.gridx=0; rc.gridy=ry; addRobot.add(new JLabel("Produktionsjahr:"), rc); rc.gridx=1; addRobot.add(rYear, rc); ry++;
        rc.gridx=0; rc.gridy=ry; rc.gridwidth=2; rc.fill=GridBagConstraints.HORIZONTAL; addRobot.add(btnCreate, rc);

        btnCreate.addActionListener(e -> {
            String t = (String)type.getSelectedItem();
            String nm = rName.getText().trim();
            int yr = ((Number)rYear.getValue()).intValue();
            if (nm.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Bitte einen Namen vergeben.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Roboter newR;
            if ("Holzverarbeitungsroboter".equals(t)) {
                newR = new Holzverarbeitungsroboter(nm);
            } else if ("Montage_Roboter".equals(t)) {
                newR = new Roboter(nm); // generischer Roboter als Platzhalter
            } else if ("Lackier_Roboter".equals(t)) {
                newR = new Roboter(nm); // generischer Roboter als Platzhalter
            } else if ("Verpackungs_Roboter".equals(t)) {
                newR = new Roboter(nm); // generischer Roboter als Platzhalter
            } else {
                newR = new Roboter(nm);
            }
            newR.setzeProduktionsJahr(yr);
            if (pm != null) {
                pm.hinzufuegenRoboter(newR);
                robotModel.addElement(newR);
                JOptionPane.showMessageDialog(dlg, "Roboter hinzugefügt.");
            }
            rName.setText("");
        });

        tabs.addTab("Neuer Roboter", addRobot);
        dlg.add(tabs, BorderLayout.CENTER);
        dlg.setSize(new Dimension(740, 500));
        dlg.setLocationRelativeTo(frame);

        // Timer zur Live-Aktualisierung der Übersicht/Details
        javax.swing.Timer mpTimer = new javax.swing.Timer(1000, ev -> {
            robotList.repaint();
            if (current[0] != null) {
                refreshRobotDetail(current[0], dName, dYear, dQueue, dState, dCurrent, dProgress, svcModel);
            }
            refreshEmergency.run();
        });
        mpTimer.start();

        dlg.setVisible(true);
        mpTimer.stop();
    }

    // Debug/Tests: Zugriff auf den Frame und zuletzt geöffneten Dialog
    public JFrame getFrame() { return frame; }
    public JDialog getLastDialog() { return lastDialog; }

    private void refreshRobotDetail(Roboter r, JLabel dName, JLabel dYear, JLabel dQueue, JLabel dState, JLabel dCurrent, JProgressBar dProgress, DefaultTableModel svcModel) {
        if (r == null) {
            dName.setText("Name: -"); dYear.setText("Produktionsjahr: -"); dQueue.setText("Warteschlange: -"); dState.setText("Status: -");
            if (dCurrent != null) dCurrent.setText("Aktuelles Produkt: -"); if (dProgress != null) { dProgress.setValue(0); dProgress.setString("0%"); }
            svcModel.setRowCount(0); return;
        }
        dName.setText("Name: " + r.gibNamen());
        dYear.setText("Produktionsjahr: " + r.gibProduktionsJahr());
        int q = r.getWarteschlange()!=null? r.getWarteschlange().size():0;
        dQueue.setText("Warteschlange: " + q);
        dState.setText("Status: " + (r.istPausiert()? "Pausiert" : "Aktiv"));
        String cur = r.gibAktuellesProduktName();
        if (cur == null) { dCurrent.setText("Aktuelles Produkt: -"); dProgress.setValue(0); dProgress.setString("-"); }
        else { dCurrent.setText("Aktuelles Produkt: " + cur); int pct = r.gibAktuellenFortschrittProzent(); dProgress.setValue(pct); dProgress.setString(pct + "%"); }
        java.util.List<ServiceEntry> svcs = r.gibServiceHistorie();
        svcModel.setRowCount(0);
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (ServiceEntry se : svcs) {
            svcModel.addRow(new Object[]{ fmt.format(new java.util.Date(se.zeitpunktMs)), se.person, se.notiz });
        }
    }

    private void submitOrder() {
        try {
            String sStd = tfStandard.getText().trim();
            String sPrem = tfPremium.getText().trim();
            if (sStd.isEmpty() || sPrem.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Bitte beide Felder ausfüllen.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int std = Integer.parseInt(sStd);
            int prem = Integer.parseInt(sPrem);
            if (std < 0 || prem < 0) {
                JOptionPane.showMessageDialog(frame, "Negative Zahlen sind nicht erlaubt.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
            fabrik.bestellungAufgeben(std, prem);
            tfStandard.setText("");
            tfPremium.setText("");
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(frame, "Ungültige Zahleneingabe.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Fehler im Backend: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Zeilenaktionen werden über die Aktionsspalte bereitgestellt (Renderer/Editor unten)

    private void startTimers() {
        // Tabelle und Monitoring alle ~750ms aktualisieren
        new javax.swing.Timer(750, e -> refreshData()).start();
    }

    private void refreshData() {
        // Tabelle aktualisieren
        java.util.List<Bestellung> list = fabrik.gibBestellungen();
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (Bestellung b : list) {
                Object[] row = new Object[] {
                    b.gibBestellNr(),
                    b.gibAnzahlStandardtueren(),
                    b.gibAnzahlPremiumtueren(),
                    b.gibBeschaffungsZeit(),
                    Math.round(b.gibLieferzeit()*100)/100f,
                    b.gibStatusString(),
                    "Stornieren"
                };
                tableModel.addRow(row);
            }
        });

        // Kennzahlen, Alerts und Monitoring-Beschriftungen
        Lager lager = fabrik.gibLager();
        String lieferantStatus = (lager.istLieferungUnterwegs() ? "Lieferant: Lieferung unterwegs…" : "Lieferant: Leerlauf");
        if (lager.istLieferungUnterwegs()) {
            long now = System.currentTimeMillis();
            long start = lager.gibLetzteLieferungStartAt();
            long etaMs = (long)lager.gibLetzteLieferzeitMs() - Math.max(0, now - start);
            if (etaMs < 0) etaMs = 0;
            long mm = etaMs / 60000; long ss = (etaMs % 60000) / 1000;
            lieferantStatus = String.format("Lieferant: Lieferung unterwegs (ETA %02d:%02d)", mm, ss);
        }
        lblHolz.setText("Holz: " + lager.getVorhandeneHolzeinheiten());
        lblSchrauben.setText("Schrauben: " + lager.getVorhandeneSchrauben());
        lblFarbe.setText("Farbe: " + lager.getVorhandeneFarbeinheiten());
        lblGlas.setText("Glas: " + lager.getVorhandeneGlaseinheiten());
        lblKarton.setText("Karton: " + lager.getVorhandeneKartoneinheiten());
        lblLieferant.setText(lieferantStatus);

        Produktionsmanager pm = fabrik.gibProduktionsmanager();
        if (pm != null) {
            ArrayList<Bestellung> inProd = new ArrayList<>(pm.getBestellungenInProduktion());
            ArrayList<Bestellung> queue = new ArrayList<>(pm.getZuVerarbeitendeBestellungen());
            lblInProd.setText("In Produktion: " + inProd.size());
            lblQueue.setText("Warteschlange: " + queue.size());
            // Fortschritt der Warteschlange und mögliche Warnung
            int queueSize = 0;
            if (pm.getHolzRoboter() != null) {
                queueSize = pm.getHolzRoboter().getWarteschlange().size();
            }
            int queueWarn = Roboter.getQueueWarnThreshold();
            int queueBar = Math.min(100, (int)Math.round(100.0 * queueSize / Math.max(1, queueWarn)));
            pbQueue.setValue(queueBar);
            pbQueue.setString(queueSize + "/" + queueWarn);
            // Erzeugung von Warnungen
            buildAlerts(lager, queueSize, queueWarn);
        } else {
            lblInProd.setText("In Produktion: n/a");
            lblQueue.setText("Warteschlange: n/a");
            pbQueue.setValue(0);
            pbQueue.setString("0/" + Roboter.getQueueWarnThreshold());
            buildAlerts(lager, 0, Roboter.getQueueWarnThreshold());
        }

        // Kennzahlen (Anzahlen) und Lieferzeit-Statistiken
        int total = list.size();
        int open = 0, inProduction = 0, done = 0;
        double sumLt = 0.0; int ltCount = 0; double maxLt = 0.0;
        long sumFlowMillis = 0L; int flowCount = 0;
        for (Bestellung b : list) {
            String st = b.gibStatusString();
            if ("fertig".equalsIgnoreCase(st)) {
                done++;
            } else if ("in Produktion".equalsIgnoreCase(st)) {
                inProduction++; open++;
            } else if ("neu".equalsIgnoreCase(st)) {
                open++;
            } // "storniert" zählt nicht als offen
            float lt = b.gibLieferzeit();
            if (lt > 0) { sumLt += lt; ltCount++; if (lt > maxLt) maxLt = lt; }
            Long fin = b.gibFinishedAt();
            if (fin != null) {
                long flow = fin - b.gibCreatedAt();
                if (flow > 0) { sumFlowMillis += flow; flowCount++; }
            }
        }
        kpiOpen.setText(String.valueOf(open));
        kpiInProd.setText(String.valueOf(inProduction));
        kpiDone.setText(String.valueOf(done));
        kpiAvgLt.setText(ltCount > 0 ? String.format("%.2f", (sumLt/ltCount)) : "-");
        kpiMaxLt.setText(ltCount > 0 ? String.format("%.2f", maxLt) : "-");
        int shortages = countShortages(lager);
        kpiShortages.setText(String.valueOf(shortages));
        // Engpass-Hinweis fett darstellen, wenn > 0
        kpiShortages.setFont(kpiShortages.getFont().deriveFont(shortages > 0 ? Font.BOLD : Font.PLAIN));
        // Ø Durchlaufzeit in Sekunden (Simulation 1h≈1s; Darstellung in s)
        kpiAvgFlow.setText(flowCount > 0 ? String.valueOf(Math.round(sumFlowMillis/flowCount/1000.0)) : "-");

        // Materialzustand: minimaler Prozentwert über alle Materialien
        double minPct = minMaterialPct(lager) * 100.0;
        pbMaterial.setValue((int)Math.round(minPct));
        pbMaterial.setString(String.format("%.0f%%", minPct));
        // Fertigstellungsquote
        int donePct = total > 0 ? (int)Math.round(100.0 * done / total) : 0;
        pbDone.setValue(donePct);
        pbDone.setString(donePct + "%");

        // Nachbestell-Historie aktualisieren
        java.util.List<Lager.RestockEntry> hist = lager.gibNachbestellHistorie();
        histModel.setRowCount(0);
        for (Lager.RestockEntry re : hist) {
            String time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(re.orderedAt));
            String status;
            if (re.getDeliveredAt() == null) {
                long now2 = System.currentTimeMillis();
                long etaLeft = (long)re.lieferzeitMs - Math.max(0, now2 - re.orderedAt);
                if (etaLeft < 0) etaLeft = 0;
                long mm2 = etaLeft / 60000; long ss2 = (etaLeft % 60000) / 1000;
                status = String.format("unterwegs (ETA %02d:%02d)", mm2, ss2);
            } else {
                String delivered = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(re.getDeliveredAt()));
                status = "geliefert um " + delivered;
            }
            histModel.addRow(new Object[]{ time, re.typ, re.holz, re.schrauben, re.farbe, re.glas, re.karton, status });
        }
    }

    // Button-Renderer für die Aktionsspalte
    private class ActionButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ActionButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = String.valueOf(table.getModel().getValueAt(row, 5));
            setText("Stornieren");
            // Button aktiv für neu/in Produktion; deaktiviert für fertig/storniert
            boolean enabled = !("fertig".equalsIgnoreCase(status) || "storniert".equalsIgnoreCase(status));
            setEnabled(enabled);
            return this;
        }
    }

    // Button-Editor für die Aktionsspalte (führt Stornierung mit Bestätigung aus)
    private class ActionButtonEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor, java.awt.event.ActionListener {
        private final JButton button = new JButton();
        private int editingRow = -1;
        public ActionButtonEditor() { button.addActionListener(this); }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            String status = String.valueOf(table.getModel().getValueAt(row, 5));
            button.setText("Stornieren");
            button.setEnabled(!("fertig".equalsIgnoreCase(status) || "storniert".equalsIgnoreCase(status)));
            return button;
        }
        public Object getCellEditorValue() { return "Stornieren"; }
        public void actionPerformed(java.awt.event.ActionEvent e) {
            try {
                Object idObj = tableModel.getValueAt(editingRow, 0);
                int bestellNrSel = Integer.parseInt(String.valueOf(idObj));
                int res = JOptionPane.showConfirmDialog(frame,
                        "Bestellung " + bestellNrSel + " wirklich stornieren?",
                        "Bestätigung",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (res == JOptionPane.YES_OPTION) {
                    boolean ok = fabrik.bestellungStornieren(bestellNrSel);
                    if (ok) {
                        JOptionPane.showMessageDialog(frame, "Bestellung " + bestellNrSel + " wurde storniert.");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Stornierung nicht möglich: Bestellung ist bereits in Produktion oder nicht gefunden.", "Hinweis", JOptionPane.WARNING_MESSAGE);
                    }
                    refreshData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Fehler beim Stornieren: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
            } finally {
                fireEditingStopped();
            }
        }
    }

    private JLabel bigLabel() {
        JLabel l = new JLabel("-");
        l.setFont(l.getFont().deriveFont(Font.BOLD, 18f));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    // Hilfspanel: Titel über großem Kennzahlen-Wert
    private JPanel compoundLabel(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel t = new JLabel(title);
        t.setHorizontalAlignment(SwingConstants.CENTER);
        t.setFont(t.getFont().deriveFont(Font.PLAIN, 11f));
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    // Hilfspanel: Titel über Fortschrittsbalken
    private JPanel labeledBar(String title, JProgressBar bar) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(Font.PLAIN, 11f));
        p.add(t, BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        return p;
    }

    // Zählt Material-Engpässe unterhalb der Warnschwelle
    private int countShortages(Lager lager) {
        int shortages = 0;
        if (ratio(lager.getVorhandeneHolzeinheiten(), Lager.MAXHOLZEINHEITEN) < Lager.getWarnLevel()) shortages++;
        if (ratio(lager.getVorhandeneSchrauben(), Lager.MAXSCHRAUBEN) < Lager.getWarnLevel()) shortages++;
        if (ratio(lager.getVorhandeneFarbeinheiten(), Lager.MAXFARBEINHEITEN) < Lager.getWarnLevel()) shortages++;
        if (ratio(lager.getVorhandeneGlaseinheiten(), Lager.MAXGLASEINHEITEN) < Lager.getWarnLevel()) shortages++;
        if (ratio(lager.getVorhandeneKartoneinheiten(), Lager.MAXKARTONEINHEITEN) < Lager.getWarnLevel()) shortages++;
        return shortages;
    }

    // Minimaler Material-Prozentwert im Lager
    private double minMaterialPct(Lager lager) {
        double h = ratio(lager.getVorhandeneHolzeinheiten(), Lager.MAXHOLZEINHEITEN);
        double s = ratio(lager.getVorhandeneSchrauben(), Lager.MAXSCHRAUBEN);
        double f = ratio(lager.getVorhandeneFarbeinheiten(), Lager.MAXFARBEINHEITEN);
        double g = ratio(lager.getVorhandeneGlaseinheiten(), Lager.MAXGLASEINHEITEN);
        double k = ratio(lager.getVorhandeneKartoneinheiten(), Lager.MAXKARTONEINHEITEN);
        return Math.min(h, Math.min(s, Math.min(f, Math.min(g, k))));
    }

    // Verhältnis aktueller Bestand zu Maximalwert
    private double ratio(int current, int max) { return (max > 0 ? (current / (double)max) : 0.0); }

    // Baut die Alerts-Liste für Materialzustand und Lieferstatus
    private void buildAlerts(Lager lager, int queueSize, int queueWarn) {
        alertsModel.clear();
        // Materials
        addMaterialAlert("Holz", lager.getVorhandeneHolzeinheiten(), Lager.MAXHOLZEINHEITEN);
        addMaterialAlert("Schrauben", lager.getVorhandeneSchrauben(), Lager.MAXSCHRAUBEN);
        addMaterialAlert("Farbe", lager.getVorhandeneFarbeinheiten(), Lager.MAXFARBEINHEITEN);
        addMaterialAlert("Glas", lager.getVorhandeneGlaseinheiten(), Lager.MAXGLASEINHEITEN);
        addMaterialAlert("Karton", lager.getVorhandeneKartoneinheiten(), Lager.MAXKARTONEINHEITEN);
        // Supplier status
        if (lager.istLieferungUnterwegs()) {
            long now = System.currentTimeMillis();
            long start = lager.gibLetzteLieferungStartAt();
            long etaMs = (long)lager.gibLetzteLieferzeitMs() - Math.max(0, now - start);
            if (etaMs < 0) etaMs = 0;
            long mm = etaMs / 60000; long ss = (etaMs % 60000) / 1000;
            alertsModel.addElement(String.format("ℹ Lieferant: Bestellung unterwegs (ETA %02d:%02d)", mm, ss));
        }
        // Queue high load
        if (queueSize > queueWarn) {
            alertsModel.addElement("⚠ Hohe Last: > " + queueWarn + " Produkte in Roboter-Warteschlange");
        }
    }

    // Fügt je nach Füllstand einen Material-Warnhinweis hinzu
    private void addMaterialAlert(String name, int current, int max) {
        double pct = ratio(current, max);
        if (pct < Lager.getCritLevel()) {
            alertsModel.addElement("⚠ " + name + " kritisch – kann Lieferzeit erhöhen");
        } else if (pct < Lager.getWarnLevel()) {
            alertsModel.addElement("⚠ " + name + " unter " + (int)(Lager.getWarnLevel()*100) + "% – Nachbestellung empfohlen");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
                }
            } catch (Exception ignore) {}
            new FabrikGUI();
        });
    }
}
