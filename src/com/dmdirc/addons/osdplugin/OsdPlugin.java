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

package com.dmdirc.addons.osdplugin;

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.CategoryChangeListener;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.plugins.Plugin;

/**
 * Allows the user to display on-screen-display messages.
 * @author chris
 */
public final class OsdPlugin extends Plugin implements CategoryChangeListener,
        PreferencesInterface {
    
    /** What domain do we store all settings in the global config under. */
    private static final String MY_DOMAIN = "plugin-OSD";
    
    /** Config OSD Window. */
    private OsdWindow osdWindow;
    
    /** OSD Command. */
    private OsdCommand command;
    
    /** X-axis position of OSD. */
    private int x = IdentityManager.getGlobalConfig().getOptionInt(MY_DOMAIN, "locationX", 20);
    
    /** Y-axis potion of OSD. */
    private int y = IdentityManager.getGlobalConfig().getOptionInt(MY_DOMAIN, "locationY", 20);
    
    /**
     * Creates a new instance of OsdPlugin.
     */
    public OsdPlugin() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        command = new OsdCommand();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        CommandManager.unregisterCommand(command);
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesManager manager) {
        final PreferencesCategory category = new PreferencesCategory("OSD",
                "General configuration for OSD plugin.");
        
        category.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                MY_DOMAIN, "fontSize", "20", "Font size", "Changes the font " +
                "size of the OSD"));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                MY_DOMAIN, "bgcolour", "2222aa", "Background colour", 
                "Background colour for the OSD"));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                MY_DOMAIN, "fgcolour", "ffffff", "Foreground colour", 
                "Foreground colour for the OSD"));
        category.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                MY_DOMAIN, "timeout", "15", "Timeout", "Length of time in " +
                "seconds before the OSD window closes"));

        category.addChangeListener(this);
        manager.getCategory("Plugins").addSubCategory(category);
        manager.registerSaveListener(this);

        //config.setOption(MY_DOMAIN, "locationX",
        //        String.valueOf((int) osdWindow.getLocationOnScreen().getX()));
        //config.setOption(MY_DOMAIN, "locationY",
        //        String.valueOf((int) osdWindow.getLocationOnScreen().getY()));        
    }

    /** {@inheritDoc} */
    @Override
    public void CategorySelected(final PreferencesCategory category) {
        osdWindow = new OsdWindow("Please drag this OSD to position", true, x, y);
    }

    /** {@inheritDoc} */
    @Override
    public void CategoryDeselected(final PreferencesCategory category) {
        x = osdWindow.getLocationOnScreen().x;
        y = osdWindow.getLocationOnScreen().y;
        
        osdWindow.dispose();
        osdWindow = null;
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        IdentityManager.getConfigIdentity().setOption(MY_DOMAIN, "locationX", x);
        IdentityManager.getConfigIdentity().setOption(MY_DOMAIN, "locationY", y);
    }

}
