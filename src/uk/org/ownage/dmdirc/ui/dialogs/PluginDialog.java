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

package uk.org.ownage.dmdirc.ui.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.components.StandardDialog;
import uk.org.ownage.dmdirc.plugins.PluginManager;
import uk.org.ownage.dmdirc.plugins.Plugin;

import static uk.org.ownage.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.layoutGrid;
/**
 * Actions editor dialog, used to edit a particular actions.
 */
public final class PluginDialog extends StandardDialog implements
        ActionListener, ListSelectionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Component panel. */
    private JPanel panel;
    
    /** List of plugins. */
    private JList pluginList;
    
    /** Button to open plugin configuration. */
    private JButton configureButton;
    
    /** The OK Button */
    private JButton myOkButton;
    
    /** The plugin Author. */
    private JTextArea pluginAuthor;
    
    /** The plugin Description. */
    private JTextArea pluginDescription;
    
    /** Currently selected plugin. */
    private int selectedPlugin = 0;
    
    /** Creates a new instance of PluginDialog. */
    public PluginDialog() {
        super(MainFrame.getMainFrame(), false);
        setResizable(false);
        initComponents();
        addListeners();
        layoutComponents();
        
        pluginList.setSelectedIndex(0);
        selectedPlugin = 0;
        
        this.setLocationRelativeTo(MainFrame.getMainFrame());
        this.setVisible(true);
    }
    
    /** Initialises the components. */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        myOkButton = new JButton();
        myOkButton.setText("OK");
        myOkButton.setDefaultCapable(true);
        setTitle("Manage Plugins");
        
        setMinimumSize(new Dimension(350, 400));
        setPreferredSize(new Dimension(350, 400));
        
        panel = new JPanel(new SpringLayout());
        panel.setVisible(true);
        
        pluginAuthor = new JTextArea("Author:\n<Nothing Selected>");
        pluginAuthor.setEditable(false);
        pluginAuthor.setWrapStyleWord(true);
        pluginAuthor.setLineWrap(true);
        pluginAuthor.setHighlighter(null);
        pluginAuthor.setBackground(panel.getBackground());
        
        pluginDescription = new JTextArea("Description:\n<Nothing Selected>");
        pluginDescription.setEditable(false);
        pluginDescription.setWrapStyleWord(true);
        pluginDescription.setLineWrap(true);
        pluginAuthor.setHighlighter(null);
        pluginDescription.setBackground(panel.getBackground());;
        
        pluginList = new JList(new DefaultListModel());
        pluginList.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER)));
        
        populateList();
        
        pluginList.setMinimumSize(new Dimension(325, Integer.MAX_VALUE));
        pluginList.setPreferredSize(new Dimension(325, Integer.MAX_VALUE));
        
        configureButton = new JButton("Configure Plugin");
    }
    
    /** Lays out the dialog. */
    private void layoutComponents() {
        int i = 0;
        final GridBagConstraints constraints = new GridBagConstraints();
        getContentPane().setLayout(new GridBagLayout());
        
        panel.add(Box.createHorizontalBox());
        
        layoutGrid(panel, 10, 0, LARGE_BORDER, LARGE_BORDER, SMALL_BORDER, SMALL_BORDER);
        
        constraints.weighty = 0.0;
        constraints.weightx = 0.0;
        constraints.gridx = 0;
        constraints.gridy = i;
        constraints.gridwidth = 1;
        constraints.gridheight = 4;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets.set(LARGE_BORDER, 0, LARGE_BORDER, 0);
        getContentPane().add(pluginList, constraints);
        i = i+4;
        
        constraints.insets.set(0, 0, LARGE_BORDER, 0);
        constraints.gridheight = 1;
        constraints.gridy = i++;
        getContentPane().add(pluginAuthor, constraints);
        
        constraints.insets.set(0, 0, LARGE_BORDER, 0);
        constraints.gridheight = 2;
        constraints.gridy = i++;
        getContentPane().add(pluginDescription, constraints);
        i = i+2;
        
        constraints.insets.set(0, 0, 0, 0);
        constraints.gridheight = 1;
        constraints.gridy = i++;
        getContentPane().add(configureButton, constraints);
        
        constraints.insets.set(LARGE_BORDER, 0, LARGE_BORDER, 0);
        constraints.gridy = i++;
        getContentPane().add(myOkButton, constraints);
        
        constraints.weighty = 1.0;
        constraints.gridy = 1;
        constraints.insets.set(0, SMALL_BORDER, 0, SMALL_BORDER);
        getContentPane().add(panel, constraints);
        
        pack();
    }

    
    /** Populates the plugins list with plugins from the plugin manager. */
    private void populateList() {
        ((DefaultListModel) pluginList.getModel()).clear();
        for (Plugin plugin : PluginManager.getPluginManager().getPossiblePlugins()) {
            ((DefaultListModel) pluginList.getModel()).addElement(plugin);
        }
    }
    
    /** Adds listeners to components. */
    private void addListeners() {
        myOkButton.addActionListener(this);
        configureButton.addActionListener(this);
        pluginList.addListSelectionListener(this);
    }

    /**
     * Invoked when an action occurs.
     * @param e The event related to this action.
     */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == myOkButton) {
            this.dispose();
        } else if (e.getSource() == configureButton) {
            if (selectedPlugin >= 0) {
                Plugin plugin = (Plugin) pluginList.getSelectedValue();
                if (plugin.isConfigurable()) {
                    plugin.showConfig();
                }
            }
        }
    }
    
    /** {@inheritDoc}. */
    public void valueChanged(final ListSelectionEvent selectionEvent) {
        if (!selectionEvent.getValueIsAdjusting()) {
            final int selected = ((JList) selectionEvent.getSource()).getSelectedIndex();
            if (selected >= 0) {
                Plugin plugin = (Plugin) ((JList) selectionEvent.getSource()).getSelectedValue();
                configureButton.setEnabled(plugin.isConfigurable());
                pluginAuthor.setText("Author:\n"+plugin.getAuthor());
                pluginDescription.setText("Description:\n"+plugin.getDescription());
            }
            selectedPlugin = selected;
        }
    }
    
}
