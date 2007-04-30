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

package uk.org.ownage.dmdirc.ui.dialogs;

import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.ui.components.PreferencesInterface;
import uk.org.ownage.dmdirc.ui.components.PreferencesPanel;

/**
 * Allows the user to modify global client preferences.
 */
public final class PreferencesDialog implements PreferencesInterface {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    
    /** preferences panel. */
    private PreferencesPanel preferencesPanel;
    
    /**
     * Creates a new instance of PreferencesDialog.
     */
    public PreferencesDialog() {
        preferencesPanel = new PreferencesPanel(this);
        
        initComponents();
    }
    
    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        
        initGeneralTab();
        
        initUITab();
        
        initTreeViewTab();
        
        initNotificationsTab();
        
        initInputTab();
        
        initLoggingTab();
        
        initAdvancedTab();
        
        preferencesPanel.display();
    }
    
    /**
     * Initialises the preferences tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initGeneralTab() {
        preferencesPanel.addCategory("General");
        
        preferencesPanel.addOption("General", "general.closemessage", "Close message: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("general", "closemessage"));
        preferencesPanel.addOption("General", "general.partmessage", "Part message: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("general", "partmessage"));
        preferencesPanel.addOption("General", "general.quitmessage", "Quit message: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("general", "quitmessage"));
        preferencesPanel.addOption("General", "general.cyclemessage", "Cycle message: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("general", "cyclemessage"));
        preferencesPanel.addOption("General", "general.kickmessage", "Kick message: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("general", "kickmessage"));
        preferencesPanel.addOption("General", "general.autoSubmitErrors", "Automatically submit errors: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("general", "autoSubmitErrors")));
    }
    
    /**
     * Initialises the UI tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initUITab() {
        preferencesPanel.addCategory("GUI");
        
        preferencesPanel.addOption("GUI", "ui.maximisewindows", "Auto-Maximise windows: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "maximisewindows")));
        preferencesPanel.addOption("GUI", "ui.backgroundcolour", "Window background colour: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("ui", "backgroundcolour"));
        preferencesPanel.addOption("GUI", "ui.foregroundcolour", "Window foreground colour: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("ui", "foregroundcolour"));
        preferencesPanel.addOption("GUI", "ui.sortByMode", "Nicklist sort by mode: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "sortByMode")));
        preferencesPanel.addOption("GUI", "ui.sortByCase", "Nicklist sort by case: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "sortByCase")));
        preferencesPanel.addOption("GUI", "channel.splitusermodes", "Split user modes: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("channel", "splitusermodes")));
        preferencesPanel.addOption("GUI", "ui.quickCopy", "Quick Copy: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "quickCopy")));
        preferencesPanel.addOption("GUI", "ui.pasteProtectionLimit", "Paste protection trigger: ",
                PreferencesPanel.OptionType.SPINNER, Integer.parseInt(Config.getOption("ui", "pasteProtectionLimit")));
    }
    
    /**
     * Initialises the TreeView tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initTreeViewTab() {
        preferencesPanel.addCategory("Treeview");
        
        preferencesPanel.addOption("Treeview", "ui.rolloverEnabled", "Rollover enabled: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "rolloverEnabled")));
        preferencesPanel.addOption("Treeview", "ui.rolloverColour", "Rollover colour: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("ui", "rolloverColour"));
        preferencesPanel.addOption("Treeview", "ui.sortwindows", "Sort windows: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "sortwindows")));
        preferencesPanel.addOption("Treeview", "ui.sortservers", "Sort servers: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "sortservers")));
    }
    
    /**
     * Initialises the Notifications tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initNotificationsTab() {
        preferencesPanel.addCategory("Notifications");
        final String[] windowOptions
                = new String[] {"all", "active", "server", };
        
        preferencesPanel.addOption("Notifications", "notifications.socketClosed", "Socket closed: ",
                PreferencesPanel.OptionType.COMBOBOX, windowOptions, Config.getOption("notifications", "socketClosed"), false);
        preferencesPanel.addOption("Notifications", "notifications.privateNotice", "Private notice: ",
                PreferencesPanel.OptionType.COMBOBOX, windowOptions, Config.getOption("notifications", "privateNotice"), false);
        preferencesPanel.addOption("Notifications", "notifications.privateCTCP", "CTCP request: ",
                PreferencesPanel.OptionType.COMBOBOX, windowOptions, Config.getOption("notifications", "privateCTCP"), false);
        preferencesPanel.addOption("Notifications", "notifications.privateCTCPreply", "CTCP reply: ",
                PreferencesPanel.OptionType.COMBOBOX, windowOptions, Config.getOption("notifications", "privateCTCPreply"), false);
    }
    
    /**
     * Initialises the input tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initInputTab() {
        preferencesPanel.addCategory("Input");
        
        preferencesPanel.addOption("Input", "general.commandchar", "Command character: ",
                PreferencesPanel.OptionType.TEXTFIELD, Config.getOption("general", "commandchar"));
        preferencesPanel.addOption("Input", "tabcompletion.casesensitive",
                "Case-sensitive tab completion: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("tabcompletion", "casesensitive")));
    }
    
    /**
     * Initialises the logging tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initLoggingTab() {
        preferencesPanel.addCategory("Logging");
        
        preferencesPanel.addOption("Logging", "logging.dateFormat", "Date format: ",
                PreferencesPanel.OptionType.COMBOBOX, new String[]
        {"EEE, d MMM yyyy HH:mm:ss Z", "d MMM yyyy HH:mm:ss", }, Config.getOption("logging", "dateFormat"), true);
        preferencesPanel.addOption("Logging", "logging.programLogging", "Program logs: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("logging", "programLogging")));
        preferencesPanel.addOption("Logging", "logging.debugLogging", "Debug logs: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("logging", "debugLogging")));
        preferencesPanel.addOption("Logging", "logging.debugLoggingSysOut",
                "Debug console output: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("logging", "debugLoggingSysOut")));
    }
    
    /**
     * Initialises the advanced tab.
     *
     * @param cardLayoutPanel parent pane
     */
    private void initAdvancedTab() {
        preferencesPanel.addCategory("Advanced");
        
        final LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();
        final String[] lafs = new String[plaf.length];
        int i = 0;
        for (LookAndFeelInfo laf : plaf) {
            lafs[i++] = laf.getName();
        }
        
        preferencesPanel.addOption("Advanced", "ui.lookandfeel", "Look and feel: ",
                PreferencesPanel.OptionType.COMBOBOX, lafs, Config.getOption("ui", "lookandfeel"), false);
        preferencesPanel.addOption("Advanced", "ui.showversion", "Show version: ",
                PreferencesPanel.OptionType.CHECKBOX,
                Boolean.parseBoolean(Config.getOption("ui", "showversion")));
        preferencesPanel.addOption("Advanced", "ui.inputbuffersize", "Input bufer size (lines): ",
                PreferencesPanel.OptionType.SPINNER,
                Integer.parseInt(Config.getOption("ui", "inputbuffersize")));
        preferencesPanel.addOption("Advanced", "ui.frameBufferSize", "Frame buffer size (characters): ",
                PreferencesPanel.OptionType.SPINNER,
                Integer.parseInt(Config.getOption("ui", "frameBufferSize")));
        preferencesPanel.addOption("Advanced", "general.browser", "Browser: ",
                PreferencesPanel.OptionType.TEXTFIELD,
                Config.getOption("general", "browser"));
    }
    
    /** {@inheritDoc}. */
    public void configClosed(final Properties properties) {
        for (Object configOption : properties.keySet()) {
            String[] args = ((String) configOption).split("\\.");
            Config.setOption(args[0], args[1], (String) properties.get(configOption));
        }
    }
}
