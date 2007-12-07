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

package com.dmdirc.ui.swing.dialogs.actionsmanager;

import com.dmdirc.Main;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.StandardDialog;

/**
 * Allows the user to manage actions.
 */
public final class ActionsManagerDialog extends StandardDialog {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Previously created instance of ActionsManagerDialog. */
    private static ActionsManagerDialog me;

    /** Creates a new instance of ActionsManagerDialog. */
    private ActionsManagerDialog() {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        initComponents();
        addListeners();
        layoutComponents();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("DMDirc: Action Manager");
        setResizable(false);
    }

    /** Creates the dialog if one doesn't exist, and displays it. */
    public static synchronized void showActionsManagerDialog() {
        me = getActionsManagerDialog();

        me.setLocationRelativeTo((MainFrame) Main.getUI().getMainWindow());
        me.setVisible(true);
        me.requestFocus();
    }

    /**
     * Returns the current instance of the ActionsManagerDialog.
     *
     * @return The current ActionsManagerDialog instance
     */
    public static synchronized ActionsManagerDialog getActionsManagerDialog() {
        if (me == null) {
            me = new ActionsManagerDialog();
        } else {
            me.reloadGroups();
        }

        return me;
    }
    
    /**
     * Initialises the components.
     */
    private void initComponents() {
        
    }
    
    /**
     * Adds listeners.
     */
    private void addListeners() {
        
    }
    
    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        
    }
    
    /**
     * Reloads the action groups.
     */
    private void reloadGroups() {
        
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }
}
