/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.ui.dialogs.serversetting;

import com.dmdirc.Server;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.components.StandardDialog;
import com.dmdirc.ui.components.expandingsettings.SettingsPanel;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;
import com.dmdirc.ui.components.expandingsettings.SettingsPanel.OptionType;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Allows the user to modify server settings and the ignore list.
 */
public final class ServerSettingsDialog extends StandardDialog
        implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Parent server. */
    private final Server server;
    
    /** Buttons panel. */
    private JPanel buttonsPanel;
    
    /** Ignore list panel. */
    private IgnoreListPanel ignoreList;
    /** Settingspanel. */
    private SettingsPanel settingsPanel;
    
    /**
     * Creates a new instance of ServerSettingsDialog.
     *
     * @param server The server object that we're editing settings for
     */
    public ServerSettingsDialog(final Server server) {
        super(MainFrame.getMainFrame(), false);
        
        this.server = server;
        
        setTitle("Server settings");
        setPreferredSize(new Dimension(400, 400));
        setResizable(false);
        
        initComponents();
        initListeners();
        pack();
        setLocationRelativeTo(MainFrame.getMainFrame());
    }
    
    /** Initialises the main UI components. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        initButtonsPanel();
        
        final JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, 0, SMALL_BORDER));
        
        ignoreList = new IgnoreListPanel(server);

        settingsPanel = new SettingsPanel(IdentityManager.getNetworkConfig(
                server.getNetwork()), "These settings are specific to this " 
                + "network, any settings specified here will overwrite global " 
                + "settings");
        
        addSettings(settingsPanel);
        
        tabbedPane.add("Ignore list", ignoreList);
        tabbedPane.add("settings", settingsPanel);
        
        this.setLayout(new BorderLayout());
        
        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.PAGE_END);
    }
    
    /**
     * Adds the settings to the panel.
     *
     * @param settingsPanel Settings panel to add the options to
     */
    private void addSettings(final SettingsPanel settingsPanel) {
        settingsPanel.addOption("channel", "splitusermodes", OptionType.CHECKBOX);
        settingsPanel.addOption("channel", "sendwho", OptionType.CHECKBOX);
        settingsPanel.addOption("channel", "showmodeprefix", OptionType.CHECKBOX);
        
        settingsPanel.addOption("general", "cyclemessage", OptionType.TEXTFIELD);
        settingsPanel.addOption("general", "kickmessage", OptionType.TEXTFIELD);
        settingsPanel.addOption("general", "partmessage", OptionType.TEXTFIELD);
        
        settingsPanel.addOption("ui", "backgroundcolour", OptionType.COLOUR);
        settingsPanel.addOption("ui", "foregroundcolour", OptionType.COLOUR);
        settingsPanel.addOption("ui", "frameBufferSize", OptionType.SPINNER);
        settingsPanel.addOption("ui", "inputBufferSize", OptionType.SPINNER);
        settingsPanel.addOption("ui", "inputbackgroundcolour", OptionType.COLOUR);
        settingsPanel.addOption("ui", "inputforegroundcolour", OptionType.COLOUR);
        settingsPanel.addOption("ui", "nicklistbackgroundcolour", OptionType.COLOUR);
        settingsPanel.addOption("ui", "nicklistforegroundcolour", OptionType.COLOUR);
        settingsPanel.addOption("ui", "shownickcoloursinnicklist", OptionType.CHECKBOX);
        settingsPanel.addOption("ui", "shownickcoloursintext", OptionType.CHECKBOX);
        
        settingsPanel.addOption("general", "closechannelsonquit", OptionType.CHECKBOX);
        settingsPanel.addOption("general", "closechannelsondisconnect", OptionType.CHECKBOX);
        settingsPanel.addOption("general", "closequeriesonquit", OptionType.CHECKBOX);
        settingsPanel.addOption("general", "closequeriesondisconnect", OptionType.CHECKBOX);
        settingsPanel.addOption("general", "quitmessage", OptionType.TEXTFIELD);
        settingsPanel.addOption("general", "reconnectmessage", OptionType.TEXTFIELD);
        settingsPanel.addOption("general", "rejoinchannels", OptionType.CHECKBOX);
        
        settingsPanel.addOption("general", "friendlymodes", OptionType.CHECKBOX);
        settingsPanel.addOption("general", "pingtimeout", OptionType.SPINNER);
    }
    
    /** Initialises the button panel. */
    private void initButtonsPanel() {
        buttonsPanel = new JPanel();
        
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(getLeftButton());
        buttonsPanel.add(Box.createHorizontalStrut(SMALL_BORDER));
        buttonsPanel.add(getRightButton());
    }
    
    /** Initialises listeners for this dialog. */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }
    
    /** Saves the settings from this dialog. */
    public void saveSettings() {
        settingsPanel.save();
        ignoreList.saveList();
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            saveSettings();
            this.dispose();
        } else if (e.getSource() == getCancelButton()) {
            this.dispose();
        }
    }
    
}
