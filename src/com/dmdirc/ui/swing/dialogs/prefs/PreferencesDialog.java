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
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;
import com.dmdirc.ui.swing.MainFrame;

import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

/**
 * Allows the user to modify global client preferences.
 */
public final class PreferencesDialog implements PreferencesInterface, ConfigChangeListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 9;
    
    /** The global config manager. */
    private final ConfigManager config = IdentityManager.getGlobalConfig();
    
    /** A previously created instance of PreferencesDialog. */
    private static PreferencesDialog me;
    
    /** preferences panel. */
    private PreferencesPanel preferencesPanel;
    
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
        
        preferencesPanel = Main.getUI().getPreferencesPanel(this, "Preferences");
        restartNeeded = false;
        
        manager = new PreferencesManager();
        
        for (PreferencesCategory cat : manager.getCategories()) {
            preferencesPanel.addCategory(cat);
        }
        
        preferencesPanel.display();
    }
    
    /** {@inheritDoc}. */
    @Override
    @Deprecated
    public void configClosed(final Properties properties) {
        for (PreferencesCategory category : manager.getCategories()) {
            if (category.hasObject() && category.getObject() instanceof URLConfigPanel) {
                ((URLConfigPanel) category.getObject()).save();
            } else if (category.hasObject() && category.getObject() instanceof UpdateConfigPanel) {
                ((UpdateConfigPanel) category.getObject()).save();
            }
        }
        
        final Identity identity = IdentityManager.getConfigIdentity();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String[] args = ((String) entry.getKey()).split("\\.");
            if (args.length == 2) {
                if (((String) entry.getValue()).isEmpty() || entry.getValue() == null) {
                    if (identity.hasOption(args[0], args[1])) {
                        identity.unsetOption(args[0], args[1]);
                    }
                } else {
                    final Object object;
                    if (config.hasOption(args[0], args[1])) {
                        object= config.getOption(args[0], args[1]);
                    } else {
                        object = null;
                    }

                    if (object == null || !object.equals(entry.getValue())) {
                        identity.setOption(args[0], args[1], (String) entry.getValue());
                    }
                }
            } else {
                Logger.appError(ErrorLevel.LOW, "Invalid setting value: "
                        + entry.getKey(), new IllegalArgumentException("Invalid setting: " + entry.getKey()));
            }
        }
        dispose();
    }
    
    
    /** {@inheritDoc} */
    @Override
    public void configCancelled() {
        dispose();
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
