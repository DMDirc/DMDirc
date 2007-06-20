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
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.components.StandardDialog;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;
import com.dmdirc.ui.components.expandingsettings.SettingsPanel;

import java.awt.BorderLayout;
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
    private final transient Server server;
    
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
        ignoreList = new IgnoreListPanel(server);
        
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, 
                SMALL_BORDER, 0, SMALL_BORDER));
        
        tabbedPane.add("Ignore list", ignoreList);
        tabbedPane.add("settings", new JPanel());
        
        this.setLayout(new BorderLayout());
        
        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.PAGE_END);
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
