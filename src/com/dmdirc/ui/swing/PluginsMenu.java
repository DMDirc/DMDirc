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

package com.dmdirc.ui.swing;

import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.swing.dialogs.PluginDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * Dynamic plugins menu.
 */
public class PluginsMenu extends JMenu implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Configure plugins menu. */
    private JMenu configure;
    /** Plugins list. */
    private Map<JMenuItem, String> pluginList;

    /**
     * Instantiates a new plugins menu.
     */
    public PluginsMenu() {
        setText("Plugins");
        setMnemonic('p');
        
        pluginList = new HashMap<JMenuItem, String>();

        JMenuItem manage = new JMenuItem("Manage Plugins");
        manage.setMnemonic('m');
        manage.setActionCommand("ManagePlugins");
        manage.addActionListener(this);
        add(manage);

        configure = new JMenu("Configure plugins");
        configure.setMnemonic('c');
        add(configure);
        addMenuListener(new MenuListener() {

            @Override
            public void menuSelected(final MenuEvent e) {
                populateConfigurePluginsMenu();
            }

            @Override
            public void menuDeselected(final MenuEvent e) {
            //Ignore
            }

            @Override
            public void menuCanceled(final MenuEvent e) {
            //Ignore
            }
        });
    }

    /**
     * Populated the configure plugin menu.
     */
    private void populateConfigurePluginsMenu() {
        pluginList.clear();
        configure.removeAll();

        for (PluginInfo pluginInfo : PluginManager.getPluginManager().
                getPluginInfos()) {
            if (pluginInfo.isLoaded()) {
                Plugin plugin = pluginInfo.getPlugin();
                if (plugin.isConfigurable()) {
                    final JMenuItem mi =
                            new JMenuItem(pluginInfo.getNiceName());
                    mi.setActionCommand("configurePlugin");
                    mi.addActionListener(this);
                    configure.add(mi);
                    pluginList.put(mi, pluginInfo.getFilename());
                }
            }
        }

        if (configure.getItemCount() == 0) {
            configure.setEnabled(false);
        } else {
            configure.setEnabled(true);
        }
    }

    /**
     * Adds a JMenuItem to the plugin menu.
     *
     * @param menuItem The menu item to be added.
     */
    public void addPluginMenu(final JMenuItem menuItem) {
        if (getComponents().length == 1) {
            final JSeparator seperator = new JSeparator();
            add(seperator);
        }

        add(menuItem);
    }

    /**
     * Removes a JMenuItem from the plugin menu.
     *
     * @param menuItem The menu item to be removed.
     */
    public void removePluginMenu(final JMenuItem menuItem) {
        remove(menuItem);

        if (getComponents().length == 2) {
            remove(2);
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("configurePlugin")) {
            PluginManager.getPluginManager().
                    getPluginInfo(pluginList.get(e.getSource())).getPlugin().
                    showConfig();
        } else if (e.getActionCommand().equals("ManagePlugins")) {
            PluginDialog.showPluginDialog();
        }
    }
}
