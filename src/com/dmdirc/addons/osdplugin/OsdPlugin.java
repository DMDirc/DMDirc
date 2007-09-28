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

package com.dmdirc.addons.osdplugin;

import com.dmdirc.Main;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;
import com.dmdirc.ui.swing.components.ColourChooser;

import java.awt.BorderLayout;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

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
    
    /** Font size spinner. */
    private JSpinner spinner;
    
    /** Background colour chooser. */
    private ColourChooser bgColour;
    
    /** Background colour chooser. */
    private ColourChooser fgColour;
    
    /**
     * Creates a new instance of OsdPlugin.
     */
    public OsdPlugin() {
        super();
    }
    
    /** {@inheritDoc}. */
    public boolean onLoad() {
        command = new OsdCommand();
        
        return true;
    }
    
    /** {@inheritDoc}. */
    public void onUnload() {
        CommandManager.unregisterCommand(command);
    }
    
    /** {@inheritDoc}. */
    public String getVersion() {
        return "0.1";
    }
    
    /** {@inheritDoc}. */
    public String getAuthor() {
        return "Chris <chris@dmdirc.com>";
    }
    
    /** {@inheritDoc}. */
    public String getDescription() {
        return "Provides command to show on screen display";
    }
    
    /** {@inheritDoc}. */
    public boolean isConfigurable() {
        return true;
    }
    
    /** {@inheritDoc}. */
    public void showConfig() {
        final ConfigManager config = IdentityManager.getGlobalConfig();
        final PreferencesPanel preferencesPanel = Main.getUI().
                getPreferencesPanel(this, "OSD Plugin - Config");
        final JPanel spinnerPanel = new JPanel(new BorderLayout());
        final JPanel bgColourPanel = new JPanel(new BorderLayout());
        final JPanel fgColourPanel = new JPanel(new BorderLayout());
        spinner = new JSpinner(new SpinnerNumberModel());
        bgColour = new ColourChooser();
        fgColour = new ColourChooser();
        
        spinner.setValue(config.getOptionInt(MY_DOMAIN, "fontSize", 20));
        bgColour.setColour(config.getOption(MY_DOMAIN, "bgcolour", "2222aa"));
        fgColour.setColour(config.getOption(MY_DOMAIN, "fgcolour", "ffffff"));
        
        spinnerPanel.add(spinner);
        bgColourPanel.add(bgColour);
        fgColourPanel.add(fgColour);
        
        preferencesPanel.addCategory("General", "General configuration for OSD plugin.");
        
        preferencesPanel.addPanelOption("General", "fontsize", "Font size: ",
                "Changes the font size of the OSD", spinnerPanel);
        preferencesPanel.addPanelOption("General", "bgcolour",
                "Background Colour: ", "Background colour for the OSD",
                bgColourPanel);
        preferencesPanel.addPanelOption("General", "fgcolour",
                "Foreground Colour: ", "Foreground colour for the OSD", 
                fgColourPanel);
        preferencesPanel.addSpinnerOption("General", "timeout", "Timeout: ",
                "Length of times in seconds before the OSD window times out",
                IdentityManager.getGlobalConfig().getOptionInt(MY_DOMAIN, "timeout", 15));
        
        osdWindow = new OsdWindow("Please drag this OSD to position", true);
        
        spinner.addChangeListener(osdWindow);
        bgColour.addActionListener(osdWindow);
        bgColour.setActionCommand("backgroundColour");
        fgColour.addActionListener(osdWindow);
        fgColour.setActionCommand("foregroundColour");
        
        preferencesPanel.display();
    }
    
    /** {@inheritDoc}. */
    public void configClosed(final Properties properties) {
        final Identity config = IdentityManager.getConfigIdentity();
        
        if (spinner != null) {
            config.setOption(MY_DOMAIN, "fontSize", String.valueOf(spinner.getValue()));
        }
        if (fgColour != null) {
            config.setOption(MY_DOMAIN, "fgcolour", fgColour.getColour());
        }
        if (bgColour != null) {
            config.setOption(MY_DOMAIN, "bgcolour", bgColour.getColour());
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
    
    /** {@inheritDoc}. */
    public String toString() {
        return "OSD Plugin";
    }
}
