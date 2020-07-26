/*
 * Copyright 2020 Frederic Rible <f1oat@f1oat.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.f1oat.launchpad.x.gateway;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.vortexel.swingswag.GBC;
import com.vortexel.swingswag.JSpacer;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author f1oat
 */
public class GUI extends javax.swing.JFrame implements iNotifier {

    private static final String APP_NAME = "Launchpad-X-gateway";
    private static final String CONFIG_FILE = "config.toml";

    MidiHandler myMidi = new MidiHandler();
    private final FileConfig cfg;
    
    public void setPadColor(int index, int r, int g, int b) {
        this.jPanelKeyboard.setPadColor(index, r, g, b);
    }
    
    /**
     * Creates new form GUI
     */
    public GUI() {

        initComponents();
        myMidi.setGUI(this);
        System.out.println("Java version " + System.getProperty("java.version"));

        ConfigSpec spec = new ConfigSpec();
        spec.define("main.fl_in", "FL_to_LPX");
        spec.define("main.fl_out", "LPX_to_FL");
        spec.define("main.fl_out_native", "LPX_to_FL (Native)");
        spec.define("main.lpx_in", "MIDIIN2 (LPX MIDI)");
        spec.define("main.lpx_out", "LPX MIDI");

        File cfgFile = new File(getConfigFile());
        try {
            cfgFile.getParentFile().mkdirs();
            cfgFile.createNewFile();
        } catch (IOException e) {
            Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
        }

        cfg = FileConfig.of(cfgFile);
        cfg.load();
        spec.correct(cfg);

        jComboBox_FL_in.getModel().setSelectedItem(cfg.get("main.fl_in"));
        jComboBox_FL_out.getModel().setSelectedItem(cfg.get("main.fl_out"));
        jComboBox_FL_out_native.getModel().setSelectedItem(cfg.get("main.fl_out_native"));
        jComboBox_LPX_in.getModel().setSelectedItem(cfg.get("main.lpx_in"));
        jComboBox_LPX_out.getModel().setSelectedItem(cfg.get("main.lpx_out"));

        cfg.save();
    }

    public String getConfigFile() {
        String configFile;
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("windows")) {
            configFile = System.getenv("APPDATA") + "/" + APP_NAME;
        } else {
            configFile = System.getProperty("user.home") + "/.config/" + APP_NAME;
        }
        configFile = configFile + "/" + CONFIG_FILE;
        return configFile;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        jComboBox_FL_in = new javax.swing.JComboBox<>();
        jComboBox_FL_out = new javax.swing.JComboBox<>();
        jComboBox_LPX_in = new javax.swing.JComboBox<>();
        jComboBox_LPX_out = new javax.swing.JComboBox<>();
        jComboBox_FL_out_native = new javax.swing.JComboBox<>();

        JLabel jLabel1 = new JLabel("IN from FL-Studio");
        JLabel jLabel2 = new JLabel("OUT to FL-Studio");
        JLabel jLabel5 = new JLabel("IN from LPX");
        JLabel jLabel6 = new JLabel("OUT to LPX");
        JLabel jLabel7 = new JLabel("OUT to FL-Studio (Native)");

        JLabel jLabel3 = new javax.swing.JLabel("Launchpad-X / FL-Studio Gateway");
        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        JLabel jLabel4 = new javax.swing.JLabel("(C) 2020 Frederic Rible");

        buttonGroupMode = new javax.swing.ButtonGroup();
        jButtonApply = new javax.swing.JButton();
        jButtonRefresh = new JButton();
        jToggleButtonRun = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        jPanelMidiIO = new JPanel();
        jPanelButtonPanel = new JPanel();
        jPanelKeyboard = new JPanelKeyboard();

        jRadioButtonModeProg = new javax.swing.JRadioButton();
        jRadioButtonModeNative = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 51, 51));
        setResizable(false);
        setSize(new java.awt.Dimension(450, 450));

        jComboBox_FL_in.setModel(new MidiComboBoxModel(false));
        jComboBox_FL_out.setModel(new MidiComboBoxModel(true));
        jComboBox_LPX_in.setModel(new MidiComboBoxModel(false));
        jComboBox_LPX_out.setModel(new MidiComboBoxModel(true));
        jComboBox_FL_out_native.setModel(new MidiComboBoxModel(true));

        jButtonApply.setText("Apply");
        jButtonApply.addActionListener((evt) -> {
            myMidi.stop();
            configure();
            if (jToggleButtonRun.isSelected()) {
                myMidi.start();
            }
        });

        jButtonRefresh.setText("Refresh");
        jButtonRefresh.setToolTipText("Refresh the list of MIDI devices");
        jButtonRefresh.addActionListener((evt) -> {
            refreshComboBox(jComboBox_FL_in);
            refreshComboBox(jComboBox_FL_out);
            refreshComboBox(jComboBox_LPX_in);
            refreshComboBox(jComboBox_LPX_out);
            refreshComboBox(jComboBox_FL_out_native);
        });

        jToggleButtonRun.setText("Connect");
        jToggleButtonRun.addActionListener((evt) -> {
            if (jToggleButtonRun.isSelected()) {
                configure();
                myMidi.start();
            } else {
                myMidi.stop();
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel3))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(104, 104, 104)
                        .addComponent(jLabel4)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(12, 12, 12))
        );

        jPanelKeyboard.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                myMidi.pressPad(evt.getKeyCode(), 127);
            }
            public void keyReleased(KeyEvent evt) {
                myMidi.pressPad(evt.getKeyCode(), 0);
            }
        });
        jPanelKeyboard.setPreferredSize(new Dimension(300, 300));

        buttonGroupMode.add(jRadioButtonModeProg);
        jRadioButtonModeProg.setSelected(true);
        jRadioButtonModeProg.setText("Prog");
        jRadioButtonModeProg.addActionListener((evt) -> {
            myMidi.setMode(MidiHandler.eMode.Prog);
        });

        buttonGroupMode.add(jRadioButtonModeNative);
        jRadioButtonModeNative.setText("Native");
        jRadioButtonModeNative.addActionListener((evt) -> {
            myMidi.setMode(MidiHandler.eMode.Native);
        });


        {
            GridBagLayout layout = new GridBagLayout();
            GBC cL = new GBC(jPanelMidiIO);
            GBC cB = new GBC(jPanelMidiIO);
            jPanelMidiIO.setLayout(layout);

            cL.anchor(GBC.WEST).insets(0, 2, 4, 2).at(0, 0);
            cB.anchor(GBC.EAST).insets(4, 2, 0, 2).fill(GBC.HORIZONTAL).at(1, 0);
            cL.move(0, 0).put(jLabel1);
            cB.move(0, 0).put(jComboBox_FL_in);
            cL.move(0, 1).put(jLabel2);
            cB.move(0, 1).put(jComboBox_FL_out);
            cL.move(0, 1).put(jLabel7);
            cB.move(0, 1).put(jComboBox_FL_out_native);
            cL.move(0, 1).put(new JSpacer(1, 20));
            cB.move(0, 1);
            cL.move(0, 1).put(jLabel5);
            cB.move(0, 1).put(jComboBox_LPX_in);
            cL.move(0, 1).put(jLabel6);
            cB.move(0, 1).put(jComboBox_LPX_out);
        }

        {
            GridBagLayout layout = new GridBagLayout();
            GBC c = new GBC(jPanelButtonPanel);
            jPanelButtonPanel.setLayout(layout);

            c.at(0, 0).put(jButtonApply);
            c.at(1, 0).put(jButtonRefresh);
            c.at(2, 0).put(jToggleButtonRun);
            c.weightx = 2.f;
            c.at(3, 0).put(new JSpacer(20, 1));
            c.weightx = 1.f;
            c.at(4, 0).put(jRadioButtonModeNative);
            c.at(5, 0).put(jRadioButtonModeProg);
        }

        GridBagLayout layout = new GridBagLayout();
        GBC c = new GBC(getContentPane());
        getContentPane().setLayout(layout);

        c.fill(GBC.HORIZONTAL).insets(10, 4, 10, 4);
        c.at(0, 0).put(jPanel1);
        c.at(0, 1).put(jPanelMidiIO);
        c.at(0, 2).put(jPanelButtonPanel);
        c.fill(GBC.BOTH);
        c.at(0, 3).put(jPanelKeyboard);

        pack();
    }

    private void configure() {
        myMidi.set((MidiDevice.Info) jComboBox_FL_in.getSelectedItem(),
                   (MidiDevice.Info) jComboBox_FL_out.getSelectedItem(),
                   (MidiDevice.Info) jComboBox_FL_out_native.getSelectedItem(),
                   (MidiDevice.Info) jComboBox_LPX_in.getSelectedItem(),
                   (MidiDevice.Info) jComboBox_LPX_out.getSelectedItem()
        );
        cfg.set("main.fl_in", "" + jComboBox_FL_in.getSelectedItem());
        cfg.set("main.fl_out", "" + jComboBox_FL_out.getSelectedItem());
        cfg.set("main.fl_out_native", "" + jComboBox_FL_out_native.getSelectedItem());
        cfg.set("main.lpx_in", "" + jComboBox_LPX_in.getSelectedItem());
        cfg.set("main.lpx_out", "" + jComboBox_LPX_out.getSelectedItem());
        cfg.save();
    }

    private void refreshComboBox(JComboBox<String> cbox) {
        ((MidiComboBoxModel)cbox.getModel()).refreshMidiPorts();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws MidiUnavailableException {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException|InstantiationException|IllegalAccessException| UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new GUI().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupMode;
    private JButton jButtonRefresh;
    private javax.swing.JButton jButtonApply;
    private javax.swing.JComboBox<String> jComboBox_FL_in;
    private javax.swing.JComboBox<String> jComboBox_FL_out;
    private javax.swing.JComboBox<String> jComboBox_FL_out_native;
    private javax.swing.JComboBox<String> jComboBox_LPX_in;
    private javax.swing.JComboBox<String> jComboBox_LPX_out;
    private JPanel jPanel1;
    private JPanel jPanelMidiIO;
    private JPanel jPanelButtonPanel;
    private JPanelKeyboard jPanelKeyboard;
    private javax.swing.JRadioButton jRadioButtonModeNative;
    private javax.swing.JRadioButton jRadioButtonModeProg;
    private javax.swing.JToggleButton jToggleButtonRun;
    // End of variables declaration//GEN-END:variables

    @Override
    public void udpateStatus() {
    }

    @Override
    public void notifyError() {
        this.jToggleButtonRun.setSelected(false);
        JOptionPane.showMessageDialog(new JFrame(), "MIDI error", "Dialog", JOptionPane.ERROR_MESSAGE);
    }
}
