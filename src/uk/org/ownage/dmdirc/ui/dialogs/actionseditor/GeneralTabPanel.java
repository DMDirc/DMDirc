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

package uk.org.ownage.dmdirc.ui.dialogs.actionseditor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import uk.org.ownage.dmdirc.actions.Action;
import uk.org.ownage.dmdirc.actions.ActionManager;
import uk.org.ownage.dmdirc.actions.ActionType;

import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;
import static uk.org.ownage.dmdirc.ui.UIUtilities.layoutGrid;

/**
 * General tab panel, name and trigger editing for the actions editor dialog.
 */
public final class GeneralTabPanel extends JPanel implements ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The action. */
    private Action action;
    /** Name textfield. */
    private JTextField name;
    /** Primary trigger combobox. */
    private JComboBox trigger;
    /** Secondary trigger list. */
    private JList otherTriggers;
    
    /**
     * Creates a new instance of GeneralTabPanel.
     *
     * @param action action to be edited
     */
    public GeneralTabPanel(final Action action) {
        super();
        
        this.action = action;
        
        initComponents();
        addListeners();
        layoutComponents();
    }
    
    /** Initialises the components. */
    private void initComponents() {
        name = new JTextField();
        trigger = new JComboBox(new DefaultComboBoxModel());
        otherTriggers = new JList(new DefaultListModel());
        
        ((DefaultComboBoxModel) trigger.getModel()).addElement("");
        for (ActionType type : ActionManager.getTypes().toArray(new ActionType[0])) {
            ((DefaultComboBoxModel) trigger.getModel()).addElement(type);
        }
        
        name.setPreferredSize(new Dimension(100, name.getFont().getSize()));
        
        trigger.setPreferredSize(new Dimension(100, trigger.getFont().getSize()));
        
        otherTriggers.setVisibleRowCount(2);
        otherTriggers.setEnabled(false);
        
        if (action == null) {
            return;
        }
        name.setText(action.getName());
        
        populateOtherTriggers();
        
        trigger.setSelectedItem(action.getTriggers()[0]);
        otherTriggers.setEnabled(true);
        
        for (ActionType type : action.getTriggers()) {
            int index = ((DefaultListModel) otherTriggers.getModel()).indexOf(type);
            if (index != -1) {
                otherTriggers.getSelectionModel().addSelectionInterval(index, index);
            }
            
        }
    }
    
    private void populateOtherTriggers() {
        otherTriggers.removeAll();
        if (trigger.getSelectedIndex() == 0) {
            otherTriggers.setEnabled(false);
        } else {
            for (ActionType type : ActionManager.getCompatibleTypes((ActionType) trigger.getSelectedItem())) {
                System.out.println("adding " + type);
                ((DefaultListModel) otherTriggers.getModel()).addElement(type);
            }
        }
        otherTriggers.repaint();
    }
    
    /** Adds listeners to the components. */
    private void addListeners() {
        trigger.addActionListener(this);
    }
    
    /** Lays out components. */
    private void layoutComponents() {
        add(new JLabel("Name: "));
        add(name);
        add(new JLabel("Primary trigger: "));
        add(trigger);
        add(new JLabel("Additional triggers: "));
        add(new JScrollPane(otherTriggers));
        
        this.setLayout(new SpringLayout());
        
        layoutGrid(this, 3,
                2, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER);
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() == trigger) {
            otherTriggers.setEnabled(true);
            
            populateOtherTriggers();
        }
    }
    
}
