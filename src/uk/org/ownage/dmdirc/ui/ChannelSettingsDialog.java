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
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import org.jdesktop.layout.GroupLayout;

/**
 * Allows the user to modify channel settings (modes, topics, etc)
 * @author chris
 */
public class ChannelSettingsDialog extends StandardDialog
        implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    private JTabbedPane tabbedPane;
    private JPanel settingsPanel;
    private JPanel identitiesPanel;
    private JButton button1;
    private JButton button2;
    
    /**
     * Creates a new instance of ChannelSettingsDialog
     * @param parent The frame that owns this dialog
     * @param modal Whether to show modally or not
     */
    public ChannelSettingsDialog(Frame parent, boolean modal) {
        super(parent, modal);
        
        initComponents();
        initListeners();
    }
    
    /** Initialises GUI components */
    private void initComponents() {
        GridBagConstraints constraints = new GridBagConstraints();
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // TODO: Change channel name
        setTitle("Channel settings for #channel");
        
        getContentPane().setLayout(new GridBagLayout());
        
        settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setPreferredSize(new Dimension(400,400));
        
        identitiesPanel = new JPanel(new GridBagLayout());
        identitiesPanel.setPreferredSize(new Dimension(400,400));
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("IRC Settings", settingsPanel);
        tabbedPane.addTab("Client Settings", identitiesPanel);
        
        button1 = new JButton();
        button1.setPreferredSize(new Dimension(100,25));
        button2 = new JButton();
        button2.setPreferredSize(new Dimension(100,25));
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(10, 10, 10, 10);
        getContentPane().add(tabbedPane, constraints);
        
        constraints.insets.set(0, 10, 10, 10);
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        getContentPane().add(button1, constraints);
        
        constraints.gridx = 2;
        getContentPane().add(button2, constraints);
        
        orderButtons(button1, button2);
        
        pack();
    }
    
    /** Initialises listeners for this dialog */
    private void initListeners() {
        button1.addActionListener(this);
        button2.addActionListener(this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            // TODO: Apply settings
            setVisible(false);
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            setVisible(false);
        }
    }
    
}
