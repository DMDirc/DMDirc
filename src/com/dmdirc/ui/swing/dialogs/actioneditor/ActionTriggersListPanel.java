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

import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.swing.components.ImageButton;
import com.dmdirc.ui.swing.components.TextLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/**
 * Action triggers list panel.
 */
public class ActionTriggersListPanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    private List<ActionType> triggers;

    /** Instantiates the panel. */
    public ActionTriggersListPanel() {
        this(new ArrayList<ActionType>());
    }

    /** Instantiates the panel. */
    public ActionTriggersListPanel(final List<ActionType> triggers) {
        super();

        this.triggers = new ArrayList<ActionType>(triggers);

        initComponents();
        addListeners();
        layoutComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        setLayout(new MigLayout("fillx, wrap 2, debug"));
    }

    /** Adds the listeners. */
    private void addListeners() {
    }

    /** Lays out the components. */
    private void layoutComponents() {
        synchronized (triggers) {
            setVisible(false);

            removeAll();

            for (final ActionType trigger : triggers) {
                final ImageButton button = new ImageButton("delete",
                        IconManager.getIconManager().getIcon("close-inactive"),
                        IconManager.getIconManager().getIcon("close-active"));
                button.addActionListener(new ActionListener() {

                    /** {@inheritDoc} */
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        delTrigger(trigger);
                    }
                });

                add(new JLabel(trigger.getName()), "growx");
                add(button, "right");
            }

            if (getComponentCount() == 0) {
                add(new TextLabel("No triggers."));
            }
            getLayout().layoutContainer(this);
            setVisible(true);
        }
    }

    public void addTrigger(final ActionType trigger) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (triggers) {
                    triggers.add(trigger);

                    layoutComponents();
                }
            }
        });
    }

    public void delTrigger(final ActionType trigger) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (triggers) {
                    triggers.remove(trigger);

                    layoutComponents();
                }
            }
        });
    }

    public List<ActionType> getTriggers() {
        synchronized (triggers) {
            return triggers;
        }
    }
}
