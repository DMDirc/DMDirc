/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.swing.dialogs.actioneditor;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.ui.swing.components.TextLabel;
import com.dmdirc.ui.swing.components.renderers.ActionTypeRenderer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import net.miginfocom.swing.MigLayout;

/**
 * Action triggers panel.
 */
public class ActionTriggersPanel extends JPanel implements ActionListener,
        ActionTriggerRemovalListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Trigger combo box. */
    private JComboBox trigger;
    /** Add button. */
    private JButton add;
    /** Triggers list. */
    private ActionTriggersListPanel triggerList;

    /** Instantiates the panel. */
    public ActionTriggersPanel() {
        super();

        initComponents();
        addListeners();
        layoutComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        setBorder(BorderFactory.createTitledBorder(getBorder(), "Triggers"));

        trigger =
                new JComboBox(new ActionTypeModel(ActionManager.getTypeGroups()));
        //Only fire events on selection not on highlight
        trigger.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        trigger.setRenderer(new ActionTypeRenderer());
        trigger.setPrototypeDisplayValue("Testing");
        trigger.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                JComboBox box = (JComboBox) e.getSource();
                Object comp = box.getUI().getAccessibleChild(box, 0);
                if (!(comp instanceof JPopupMenu)) {
                    return;
                }
                JComponent scrollPane =
                        (JComponent) ((JPopupMenu) comp).getComponent(0);
                Dimension size = scrollPane.getPreferredSize();
                size.width = size.width * 2;
                scrollPane.setPreferredSize(size);
                scrollPane.setMaximumSize(size);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        
        add = new JButton("Add");
        add.setEnabled(trigger.getSelectedIndex() != -1);

        triggerList = new ActionTriggersListPanel();
    }

    /** Adds the listeners. */
    private void addListeners() {
        add.addActionListener(this);
        trigger.addActionListener(this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, pack"));

        add(new TextLabel("This action will be triggered when any of these events occurs: "),
                "growx, wrap, spanx");
        add(triggerList, "grow, wrap, spanx");
        add(trigger, "growx");
        add(add, "right");
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == trigger) {
            add.setEnabled(trigger.getSelectedIndex() != -1);
        } else {
            triggerList.addTrigger((ActionType) trigger.getSelectedItem());
            repopulateTriggers();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void triggerRemoved(final ActionType trigger) {
        repopulateTriggers();
    }

    /**
     * Repopulates the triggers in the panel.
     */
    private void repopulateTriggers() {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                ((ActionTypeModel) trigger.getModel()).removeAllElements();

                if (triggerList.getTriggerCount() == 0) {
                    ((ActionTypeModel) trigger.getModel()).setTypeGroup(ActionManager.getTypeGroups());
                    return;
                }

                for (ActionType thisType : ActionManager.getCompatibleTypes(triggerList.getTrigger(0))) {
                    ((ActionTypeModel) trigger.getModel()).addElement(thisType);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        triggerList.setEnabled(enabled);
        trigger.setEnabled(enabled);
        add.setEnabled(enabled);
    }
}
