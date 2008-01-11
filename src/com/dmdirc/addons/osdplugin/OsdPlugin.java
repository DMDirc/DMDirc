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

import com.dmdirc.Main;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;

import java.util.Properties;

/**
 * Allows the user to display on-screen-display messages.
 * @author chris
 */
public final class OsdPlugin extends Plugin implements PreferencesInterface {
    
    /** What domain do we store all settings in the global config under. */
    private static final String MY_DOMAIN = "plugin-OSD";
    
    /** Config OSD Window. */
    private OsdWindow osdWindow;
    
    /** OSD Command. */
    private OsdCommand command;
    
    /**
     * Creates a new instance of OsdPlugin.
     */
    public OsdPlugin() {
        super();
    }
    
    /** {@inheritDoc}. */
    public void onLoad() {
        command = new OsdCommand();
    }
    
    /** {@inheritDoc}. */
    public void onUnload() {
        CommandManager.unregisterCommand(command);
    }

    /** {@inheritDoc}. */
    @Override
    public boolean isConfigurable() {
        return true;
    }
    
    /** {@inheritDoc}. */
    @Override
    public void showConfig() {
        final ConfigManager config = IdentityManager.getGlobalConfig();
        final PreferencesPanel preferencesPanel = Main.getUI().
                getPreferencesPanel(this, "OSD Plugin - Config");

        preferencesPanel.addCategory("General", "General configuration for OSD plugin.");
        
        preferencesPanel.addSpinnerOption("General", "fontSize", 
                "Font size", "Changes the font size of the OSD",
                config.getOptionInt(MY_DOMAIN, "fontSize", 20));
        preferencesPanel.addColourOption("General", "bgcolour", 
                "Background colour", "Background colour for the OSD", 
                config.getOption(MY_DOMAIN, "bgcolour", "2222aa"),
                true, true);
        preferencesPanel.addColourOption("General", "fgcolour", 
                "Foreground colour", "Foreground colour for the OSD", 
                config.getOption(MY_DOMAIN, "fgcolour", "ffffff"),
                true, true);
        preferencesPanel.addSpinnerOption("General", "timeout", "Timeout: ",
                "Length of times in seconds before the OSD window times out",
                IdentityManager.getGlobalConfig().getOptionInt(MY_DOMAIN, "timeout", 15));
        
        osdWindow = new OsdWindow("Please drag this OSD to position", true);
        
        preferencesPanel.display();
    }
    
    /** {@inheritDoc}. */
    public void configClosed(final Properties properties) {
        final Identity config = IdentityManager.getConfigIdentity();
        
        if (properties.containsKey("fontSize")) {
            config.setOption(MY_DOMAIN, "fontSize", properties.getProperty("fontSize"));
        }
        
        if (properties.containsKey("fgcolour")) {
            config.setOption(MY_DOMAIN, "fgcolour", properties.getProperty("fontSize"));
        }
        
        if (properties.containsKey("bgcolour")) {
            config.setOption(MY_DOMAIN, "bgcolour", properties.getProperty("fontSize"));
        }
        
        if (properties.getProperty("timeout") != null) {
            config.setOption(MY_DOMAIN, "timeout", properties.getProperty("timeout"));
        }
        
        if (osdWindow != null && osdWindow.isVisible()) {
            config.setOption(MY_DOMAIN, "locationX",
                    String.valueOf((int) osdWindow.getLocationOnScreen().getX()));
            config.setOption(MY_DOMAIN, "locationY",
                    String.valueOf((int) osdWindow.getLocationOnScreen().getY()));
            osdWindow.dispose();
        }
    }
    
    /** {@inheritDoc}. */
    public void configCancelled() {
        osdWindow.dispose();
    }
}
