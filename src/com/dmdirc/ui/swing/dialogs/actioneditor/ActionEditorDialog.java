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
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.swing.UIUtilities;
import com.dmdirc.ui.swing.components.StandardDialog;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import net.miginfocom.swing.MigLayout;

/**
 * Action editor dialog.
 */
public class ActionEditorDialog extends StandardDialog {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Name panel. */
    private ActionNamePanel name;
    /** Triggers panel. */
    private ActionTriggersPanel triggers;
    /** Response panel. */
    private ActionResponsePanel response;
    /** Conditions panel. */
    private ActionConditionsPanel conditions;
    /** Substitutions panel. */
    private ActionSubstitutionsPanel substitutions;
    /** Show substitutions button. */
    private JButton showSubstitutions;

    /** Instantiates the panel. */
    public ActionEditorDialog() {
        super(new JFrame(), false);

        initComponents();
        addListeners();
        layoutComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        name = new ActionNamePanel("");
        triggers = new ActionTriggersPanel();
        response = new ActionResponsePanel();
        conditions = new ActionConditionsPanel();
        substitutions = new ActionSubstitutionsPanel();
        showSubstitutions = new JButton("Show Substitutions");
    }

    /** Adds the listeners. */
    private void addListeners() {
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, hidemode 3, wrap 2"));

        add(name, "grow, wmax 225");
        add(conditions, "spany 3, grow, wmin 400, wmax 400");
        add(triggers, "grow, wmax 225");
        add(response, "grow, wmax 225");
        add(substitutions, "spanx 2, grow, wmax 625");
        add(showSubstitutions, "left, sgx button, split 3, spanx 2");
        add(getLeftButton(), "right, sgx button");
        add(getRightButton(), "right, sgx button");

        pack();
        pack();
    }

    /**
     * Test Method.
     * 
     * @param args CLI params
     */
    public static void main(final String[] args) {
        UIUtilities.initUISettings();
        IdentityManager.load();
        ActionManager.init();
        ActionEditorDialog dialog = new ActionEditorDialog();
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
        dialog.setVisible(true);
    }
}
