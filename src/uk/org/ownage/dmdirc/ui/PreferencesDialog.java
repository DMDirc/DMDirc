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

package uk.org.ownage.dmdirc.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * Allows the user to modify global client preferences.
 */
public class PreferencesDialog extends StandardDialog {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Size of the large borders in the dialog. */
    private static final int LARGE_BORDER = 10;
    
    /** Size of the small borders in the dialog. */
    private static final int SMALL_BORDER = 5;
    
    /**
     * Creates a new instance of PreferencesDialog.
     * @param parent The frame that owns this dialog
     * @param modal Whether to show modally or not
     */
    public PreferencesDialog(final Frame parent, final boolean modal) {
        super(parent, modal);
        
        initComponents();
    }
    
    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        final JTabbedPane tabbedPane = new JTabbedPane();
        final GridBagConstraints constraints = new GridBagConstraints();
        final JButton button1 = new JButton();
        final JButton button2 = new JButton();
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        setTitle("Preferences");
        setResizable(true);
        
        
        button1.setPreferredSize(new Dimension(100, 25));
        button2.setPreferredSize(new Dimension(100, 25));
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER);
        getContentPane().add(tabbedPane, constraints);
        
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.insets.set(0, LARGE_BORDER, LARGE_BORDER, LARGE_BORDER);
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(button1, constraints);
        
        constraints.gridx = 2;
        getContentPane().add(button2, constraints);
        
        orderButtons(button1, button2);
        
        initGeneralTab(tabbedPane);
        
        initUITab(tabbedPane);
        
        initServerTab(tabbedPane);
        
        initLoggingTab(tabbedPane);
        
        initIdentitiesTab(tabbedPane);
        
        pack();
    }
    
    /**
     * Initialises the preferences tab.
     *
     * @param tabbedPane parent pane
     */
    private void initGeneralTab(final JTabbedPane tabbedPane) {
        final JPanel generalPanel = new JPanel(new GridBagLayout());
        
        tabbedPane.addTab("General", generalPanel);
        
        generalPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
    }
    
    /**
     * Initialises the UI tab.
     *
     * @param tabbedPane parent pane
     */
    private void initUITab(final JTabbedPane tabbedPane) {
        final JTabbedPane uiTabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("GUI", uiTabbedPane);
        
        uiTabbedPane.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
        
        initNotificationsTab(uiTabbedPane);
        
        initTabCompletionTab(uiTabbedPane);
    }
    
    /**
     * Initialises the server tab.
     *
     * @param tabbedPane parent pane
     */
    private void initServerTab(final JTabbedPane tabbedPane) {
        final JPanel serverPanel = new JPanel(new GridBagLayout());
        
        tabbedPane.addTab("Server", serverPanel);
        
        serverPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
    }
    
    /**
     * Initialises the logging tab.
     *
     * @param tabbedPane parent pane
     */
    private void initLoggingTab(final JTabbedPane tabbedPane) {
        final JPanel loggingPanel = new JPanel(new GridBagLayout());
        
        tabbedPane.addTab("Logging", loggingPanel);
        
        loggingPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
    }
    
    /**
     * Initialises the identities tab.
     *
     * @param tabbedPane parent pane
     */
    private void initIdentitiesTab(final JTabbedPane tabbedPane) {
        final JPanel loggingPanel = new JPanel(new GridBagLayout());
        
        tabbedPane.addTab("Identities", loggingPanel);
        
        loggingPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
    }
    
    /**
     * Initialises the notifications tab.
     *
     * @param uiTabbedPane parent pane
     */
    private void initNotificationsTab(final JTabbedPane uiTabbedPane) {
        final JPanel notificationsPanel = new JPanel(new GridBagLayout());
        
        uiTabbedPane.addTab("Notifications", notificationsPanel);
        
        notificationsPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
    }
    
    /**
     * Initialises the tab completion tab.
     *
     * @param uiTabbedPane parent pane
     */
    private void initTabCompletionTab(final JTabbedPane uiTabbedPane) {
        final JPanel tabPanel = new JPanel(new GridBagLayout());
        
        uiTabbedPane.addTab("Tab Completion", tabPanel);
        
        tabPanel.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER, LARGE_BORDER));
    }
    
}
