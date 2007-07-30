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

package com.dmdirc.ui.swing.dialogs;

import com.dmdirc.BrowserLauncher;
import com.dmdirc.Main;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.PluginCellRenderer;
import com.dmdirc.ui.swing.components.StandardDialog;
import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Plugin manager dialog. Allows the user to manage their plugins.
 */
public final class PluginDialog extends StandardDialog implements
        ActionListener, ListSelectionListener, HyperlinkListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
    /** Previously created instance of PluginDialog. */
    private static PluginDialog me;
    
    /** List of plugins. */
    private JList pluginList;
    
    /** plugin list scroll pane. */
    private JScrollPane scrollPane;
    
    /** Button to open plugin configuration. */
    private JButton configureButton;
    
    /** Button to enable/disable plugin. */
    private JButton toggleButton;
    
    /** The OK Button. */
    private JButton myOkButton;
    
    /** Currently selected plugin. */
    private int selectedPlugin;
    
    /** Blurb label. */
    private JTextArea blurbLabel;
    
    /** Info Label. */
    private JEditorPane infoLabel;
    
    /** Creates a new instance of PluginDialog. */
    private PluginDialog() {
        super(((MainFrame) Main.getUI().getMainWindow()), false);
        setResizable(false);
        initComponents();
        addListeners();
        layoutComponents();
        
        pluginList.setSelectedIndex(0);
        selectedPlugin = 0;
        
        setLocationRelativeTo(((MainFrame) Main.getUI().getMainWindow()));
        setVisible(true);
    }
    
    /** Creates the dialog if one doesn't exist, and displays it. */
    public static synchronized void showPluginDialog() {
        if (me == null) {
            me = new PluginDialog();
        } else {
            me.setVisible(true);
            me.requestFocus();
            me.populateList();
        }
    }
    
    /** Initialises the components. */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        myOkButton = new JButton();
        myOkButton.setText("OK");
        myOkButton.setDefaultCapable(true);
        setTitle("Manage Plugins");
        
        setPreferredSize(new Dimension(400, 400));
        
        pluginList = new JList(new DefaultListModel());
        pluginList.setCellRenderer(new PluginCellRenderer());
        
        scrollPane = new JScrollPane(pluginList);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        configureButton = new JButton("Configure");
        configureButton.setEnabled(false);
        
        toggleButton = new JButton("Enable");
        toggleButton.setEnabled(false);
        
        blurbLabel = new JTextArea("Plugins allow you to extend the functionality of DMDirc."
                + " Plugins enabled here will also be enabled next time you start the client.");
        blurbLabel.setEditable(false);
        blurbLabel.setWrapStyleWord(true);
        blurbLabel.setLineWrap(true);
        blurbLabel.setHighlighter(null);
        blurbLabel.setBackground(this.getBackground());
        
        infoLabel = new JEditorPane("text/html", "<html><center style='font-family: "
                + blurbLabel.getFont().getFamily() + "; font-size:"
                + blurbLabel.getFont().getSize() + "pt;'>You can get "
                + "more plugins from the <a href=\"http://addons.dmdirc.com/\">"
                + "Addons site</a></center></html>");
        infoLabel.setFont(blurbLabel.getFont());
        infoLabel.setEditable(false);
        infoLabel.setHighlighter(null);
        infoLabel.setBackground(this.getBackground());
        
        populateList();
    }
    
    /** Lays out the dialog. */
    private void layoutComponents() {
        final GridBagConstraints constraints = new GridBagConstraints();
        getContentPane().setLayout(new GridBagLayout());
        
        constraints.weighty = 0.0;
        constraints.weightx = 1.0;
        constraints.gridwidth = 4;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets.set(LARGE_BORDER, LARGE_BORDER, SMALL_BORDER,
                LARGE_BORDER);
        getContentPane().add(blurbLabel, constraints);
        
        constraints.weighty = 1.0;
        constraints.weightx = 1.0;
        constraints.gridy = 1;
        constraints.gridheight = 4;
        constraints.insets.set(SMALL_BORDER, LARGE_BORDER, SMALL_BORDER,
                LARGE_BORDER);
        getContentPane().add(scrollPane, constraints);
        
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridheight = 1;
        constraints.gridy = 5;
        constraints.insets.set(0, LARGE_BORDER, 0, LARGE_BORDER);
        getContentPane().add(infoLabel, constraints);
        
        constraints.insets.set(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER, 0);
        constraints.gridwidth = 1;
        constraints.gridy = 6;
        constraints.gridx = 0;
        getContentPane().add(configureButton, constraints);
        
        constraints.gridx = 1;
        getContentPane().add(toggleButton, constraints);
        
        constraints.gridx = 2;
        getContentPane().add(Box.createHorizontalBox(), constraints);
        
        constraints.insets.set(LARGE_BORDER, LARGE_BORDER, LARGE_BORDER,
                LARGE_BORDER);
        constraints.gridx = 3;
        getContentPane().add(myOkButton, constraints);
        
        pack();
    }
    
    
    /** Populates the plugins list with plugins from the plugin manager. */
    private void populateList() {
        final List<Plugin> list = PluginManager.getPluginManager().getPossiblePlugins();
        Collections.sort(list);
        
        ((DefaultListModel) pluginList.getModel()).clear();
        for (Plugin plugin : list) {
            ((DefaultListModel) pluginList.getModel()).addElement(plugin);
        }
        if (((DefaultListModel) pluginList.getModel()).size() > 0) {
            toggleButton.setEnabled(true);
        }
        pluginList.repaint();
    }
    
    /** Adds listeners to components. */
    private void addListeners() {
        myOkButton.addActionListener(this);
        configureButton.addActionListener(this);
        toggleButton.addActionListener(this);
        pluginList.addListSelectionListener(this);
        infoLabel.addHyperlinkListener(this);
    }
    
    /**
     * Invoked when an action occurs.
     * @param e The event related to this action.
     */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == myOkButton) {
            this.dispose();
        } else if (e.getSource() == configureButton && selectedPlugin >= 0) {
            final Plugin plugin = (Plugin) pluginList.getSelectedValue();
            if (plugin.isConfigurable()) {
                plugin.showConfig();
            }
        } else if (e.getSource() == toggleButton && selectedPlugin >= 0) {
            final Plugin plugin = (Plugin) pluginList.getSelectedValue();
            if (plugin.isActive()) {
                plugin.setActive(false);
                toggleButton.setText("Enable");
                configureButton.setEnabled(false);
            } else {
                plugin.setActive(true);
                toggleButton.setText("Disable");
                configureButton.setEnabled(plugin.isConfigurable());
            }
            
            PluginManager.getPluginManager().updateAutoLoad(plugin);
            
            pluginList.repaint();
        }
    }
    
    /** {@inheritDoc}. */
    public void valueChanged(final ListSelectionEvent selectionEvent) {
        if (!selectionEvent.getValueIsAdjusting()) {
            final int selected = ((JList) selectionEvent.getSource()).getSelectedIndex();
            if (selected >= 0) {
                final Plugin plugin = (Plugin) ((JList) selectionEvent.getSource()).getSelectedValue();
                if (plugin.isActive()) {
                    configureButton.setEnabled(plugin.isConfigurable());
                    toggleButton.setText("Disable");
                } else {
                    configureButton.setEnabled(false);
                    toggleButton.setText("Enable");
                }
            }
            selectedPlugin = selected;
        }
    }

    /** {@inheritDoc}. */
    public void hyperlinkUpdate(final HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            BrowserLauncher.openURL(e.getURL());
        }
    }
    
}
