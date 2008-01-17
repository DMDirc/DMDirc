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

package com.dmdirc.ui.swing.dialogs.prefs;

import com.dmdirc.Main;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.ui.swing.MainFrame;

import com.dmdirc.ui.swing.components.SwingPreferencesPanel;

import javax.swing.JOptionPane;

/**
 * Allows the user to modify global client preferences.
 */
public final class PreferencesDialog implements ConfigChangeListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 9;
    
    /** A previously created instance of PreferencesDialog. */
    private static PreferencesDialog me;
    
    /** preferences panel. */
    private SwingPreferencesPanel preferencesPanel;
    
    /** restart warning issued. */
    private boolean restartNeeded;
    
    /** Our preferences manager. */
    private PreferencesManager manager;
    
    /**
     * Creates a new instance of PreferencesDialog.
     */
    private PreferencesDialog() {
        initComponents();
        
        IdentityManager.getGlobalConfig().addChangeListener("ui", this);
    }
    
    /** Creates the dialog if one doesn't exist, and displays it. */
    public static synchronized void showPreferencesDialog() {
        if (me == null) {
            me = new PreferencesDialog();
        } else {
            me.initComponents();
        }
    }
    
    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        
        manager = new PreferencesManager();
        preferencesPanel = new SwingPreferencesPanel(this, "Preferences", manager);
        restartNeeded = false;
        
        preferencesPanel.display();
    }   
    
    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if ("ui".equals(domain) && ("lookandfeel".equals(key)
        || "framemanager".equals(key) || "framemanagerPosition".equals(key))
        && !restartNeeded) {
            JOptionPane.showMessageDialog((MainFrame) Main.getUI().
                    getMainWindow(), "One or more of the changes you made "
                    + "won't take effect until you restart the client.",
                    "Restart needed", JOptionPane.INFORMATION_MESSAGE);
            restartNeeded = true;
        }
        
    }
    
    /** Disposes of this prefs dialog. */
    public void dispose() {
        synchronized (me) {
            preferencesPanel = null;
            IdentityManager.getGlobalConfig().removeListener(this);
            me = null;
        }
    }
}
