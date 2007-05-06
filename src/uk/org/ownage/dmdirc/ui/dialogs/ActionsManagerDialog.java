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
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import uk.org.ownage.dmdirc.actions.Action;
import uk.org.ownage.dmdirc.actions.ActionManager;

import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.components.StandardDialog;

import static uk.org.ownage.dmdirc.ui.UIUtilities.LARGE_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;

/**
 * Allows the user to manage actions.
 */
public class ActionsManagerDialog extends StandardDialog implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Height of the buttons, in pixels. */
    private static final int BUTTON_HEIGHT = 25;
    /** Width of the ubttons, in pixels. */
    private static final int BUTTON_WIDTH = 100;
    
    /** The tapped pane used for displaying groups. */
    private JTabbedPane groups;
    
    /** Creates a new instance of ActionsManagerDialog. */
    public ActionsManagerDialog() {
        super(MainFrame.getMainFrame(), false);
        
        initComponents();
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Action Manager");
        setResizable(false);
        setLocationRelativeTo(MainFrame.getMainFrame());
        setVisible(true);
    }
    
    /** Initialiases the components for this dialog. */
    private void initComponents() {
        setLayout(new GridBagLayout());
        
        final GridBagConstraints constraints = new GridBagConstraints();
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 6;
        final JLabel blurb = new JLabel("Actions allow you to make DMDirc " +
                "respond automatically to events.");
        blurb.setBorder(new EmptyBorder(LARGE_BORDER, LARGE_BORDER, SMALL_BORDER, LARGE_BORDER));
        add(blurb, constraints);
        
        constraints.gridy++;
        groups = new JTabbedPane();
        groups.setPreferredSize(new Dimension(400, 200));
        groups.setBorder(new EmptyBorder(SMALL_BORDER, LARGE_BORDER, SMALL_BORDER, LARGE_BORDER));
        add(groups, constraints);
        
        constraints.gridy++;
        constraints.gridwidth = 1;
        constraints.insets.set(SMALL_BORDER, LARGE_BORDER, SMALL_BORDER, 0);
        JButton myButton = new JButton("Add Group");
        myButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        myButton.setActionCommand("group.add");
        myButton.addActionListener(this);
        add(myButton, constraints);
        
        constraints.gridx++;
        constraints.insets.set(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, 0);
        myButton = new JButton("Delete Group");
        myButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        myButton.setActionCommand("group.delete");
        myButton.addActionListener(this);
        add(myButton, constraints);
        
        constraints.gridx++;
        constraints.insets.set(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, 0);
        myButton = new JButton("Rename Group");
        myButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        myButton.setActionCommand("group.rename");
        myButton.addActionListener(this);
        add(myButton, constraints);
        
        constraints.gridx++;
        constraints.insets.set(SMALL_BORDER, LARGE_BORDER, SMALL_BORDER, 0);
        myButton = new JButton("New Action");
        myButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        myButton.setActionCommand("action.new");
        myButton.addActionListener(this);
        add(myButton, constraints);
        
        constraints.gridx++;
        constraints.insets.set(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, 0);
        myButton = new JButton("Edit Action");
        myButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        myButton.setActionCommand("action.edit");
        myButton.addActionListener(this);
        add(myButton, constraints);
        
        constraints.gridx++;
        constraints.insets.set(SMALL_BORDER, LARGE_BORDER, SMALL_BORDER, LARGE_BORDER);
        myButton = new JButton("Close");
        myButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        myButton.setActionCommand("close");
        myButton.addActionListener(this);
        add(myButton, constraints);
        
        loadGroups();
        
        pack();
    }
    
    /**
     * Retrieves known actions from the action manager and displays the
     * appropriate groups in the dialog.
     */
    private void loadGroups() {
        
        final Map<String, List<Action>> actionGroups = ActionManager.getGroups();
                
        for (String group : actionGroups.keySet()) {
            groups.addTab(group, null);
        }
    }

    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("close")) {
            dispose();
        }
    }
    
}
