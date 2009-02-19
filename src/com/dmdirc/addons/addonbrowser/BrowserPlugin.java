/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.addonbrowser;

import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.addons.ui_swing.components.pluginpanel.PluginPanel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * Allows the user to browse and download plugins from within the DMDirc ui.
 * 
 * @author chris
 */
public class BrowserPlugin extends Plugin implements ActionListener {
    
    /** Whether or not we're loaded. */
    private boolean loaded = false;

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        loaded = true;
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        loaded = false;
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesManager manager) {
        if (loaded) {
            final PluginPanel pp = ((PluginPanel) manager.getCategory("Plugins").getObject());
            for (Component comp : pp.getComponents()) {
                if (comp instanceof JButton && ((JButton) comp).getText().startsWith("Get more")) {
                    final JButton button = (JButton) comp;
                    
                    for (ActionListener listener : button.getActionListeners()) {
                        button.removeActionListener(listener);
                    }
                    
                    button.addActionListener(this);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e) {
        new DownloaderWindow();
    }

}
