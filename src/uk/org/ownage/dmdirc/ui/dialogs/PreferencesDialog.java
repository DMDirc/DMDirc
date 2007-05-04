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

import javax.swing.ImageIcon;
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
     */
    private void initGeneralTab() {
        final String tabName = "General";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addTextfieldOption(tabName, "general.closemessage",
                "Close message: ", "", Config.getOption("general", "closemessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.partmessage",
                "Part message: ", "", Config.getOption("general", "partmessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.quitmessage",
                "Quit message: ", "", Config.getOption("general", "quitmessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.cyclemessage",
                "Cycle message: ", "", Config.getOption("general", "cyclemessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.kickmessage",
                "Kick message: ", "", Config.getOption("general", "kickmessage"));
    }
    
    /**
     * Initialises the UI tab.
     */
    private void initUITab() {
        final String tabName = "GUI";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addCheckboxOption(tabName, "ui.maximisewindows",
                "Auto-Maximise windows: ","",
                Config.getOptionBool("ui", "maximisewindows"));
        preferencesPanel.addColourOption(tabName, "ui.backgroundcolour",
                "Window background colour: ", "",
                Config.getOption("ui", "backgroundcolour"), true, false);
        preferencesPanel.addColourOption(tabName, "ui.foregroundcolour",
                "Window foreground colour: ", "",
                Config.getOption("ui", "foregroundcolour"), true, false);
        preferencesPanel.addCheckboxOption(tabName, "ui.sortByMode",
                "Nicklist sort by mode: ", "",
                Config.getOptionBool("ui", "sortByMode"));
        preferencesPanel.addCheckboxOption(tabName, "ui.sortByCase",
                "Nicklist sort by case: ", "",
                Config.getOptionBool("ui", "sortByCase"));
        preferencesPanel.addCheckboxOption(tabName, "channel.splitusermodes",
                "Split user modes: ", "",
                Config.getOptionBool("channel", "splitusermodes"));
        preferencesPanel.addCheckboxOption(tabName, "ui.quickCopy",
                "Quick Copy: ", "",
                Config.getOptionBool("ui", "quickCopy"));
        preferencesPanel.addSpinnerOption(tabName, "ui.pasteProtectionLimit",
                "Paste protection trigger: ", "",
                Config.getOptionInt("ui", "pasteProtectionLimit", 1));
        preferencesPanel.addCheckboxOption(tabName, "ui.awayindicator",
                "Away indicator: ", "Shows an away indicator in the input field.",
                Config.getOptionBool("ui", "awayindicator"));
    }
    
    /**
     * Initialises the TreeView tab.
     */
    private void initTreeViewTab() {
        final String tabName = "Treeview";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addCheckboxOption(tabName, "ui.treeviewRolloverEnabled",
                "Rollover enabled: ", "",
                Config.getOptionBool("ui", "treeviewRolloverEnabled"));
        preferencesPanel.addColourOption(tabName, "ui.treeviewRolloverColour",
                "Rollover colour: ", "", Config.getOption("ui", "treeviewRolloverColour"),
                true, true);
        preferencesPanel.addCheckboxOption(tabName, "treeview.sortwindows",
                "Sort windows: ", "",
                Config.getOptionBool("treeview", "sortwindows"));
        preferencesPanel.addCheckboxOption(tabName, "treeview.sortservers",
                "Sort servers: ", "",
                Config.getOptionBool("treeview", "sortservers"));
    }
    
    /**
     * Initialises the Notifications tab.
     */
    private void initNotificationsTab() {
        final String tabName = "Notifications";
        preferencesPanel.addCategory(tabName, "");
        final String[] windowOptions
                = new String[] {"all", "active", "server", };
        
        preferencesPanel.addComboboxOption(tabName, "notifications.socketClosed",
                "Socket closed: ", "", windowOptions,
                Config.getOption("notifications", "socketClosed"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.privateNotice",
                "Private notice: ", "", windowOptions,
                Config.getOption("notifications", "privateNotice"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.privateCTCP",
                "CTCP request: ", "", windowOptions,
                Config.getOption("notifications", "privateCTCP"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.privateCTCPreply",
                "CTCP reply: ", "", windowOptions,
                Config.getOption("notifications", "privateCTCPreply"), false);
    }
    
    /**
     * Initialises the input tab.
     */
    private void initInputTab() {
        final String tabName = "Input";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addTextfieldOption(tabName, "general.commandchar",
                "Command character: ", "", Config.getOption("general", "commandchar"));
        preferencesPanel.addCheckboxOption(tabName, "tabcompletion.casesensitive",
                "Case-sensitive tab completion: ", "",
                Config.getOptionBool("tabcompletion", "casesensitive"));
    }
    
    /**
     * Initialises the logging tab.
     */
    private void initLoggingTab() {
        final String tabName = "Error Handling";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addCheckboxOption(tabName, "general.autoSubmitErrors",
                "Automatically submit errors: ", "",
                Config.getOptionBool("general", "autoSubmitErrors"));
        preferencesPanel.addComboboxOption(tabName, "logging.dateFormat",
                "Date format: ", "", new String[]
        {"EEE, d MMM yyyy HH:mm:ss Z", "d MMM yyyy HH:mm:ss", },
                Config.getOption("logging", "dateFormat"), true);
        preferencesPanel.addCheckboxOption(tabName, "logging.programLogging",
                "Program logs: ", "",
                Config.getOptionBool("logging", "programLogging"));
        preferencesPanel.addCheckboxOption(tabName, "logging.debugLogging",
                "Debug logs: ", "",
                Config.getOptionBool("logging", "debugLogging"));
        preferencesPanel.addCheckboxOption(tabName, "logging.debugLoggingSysOut",
                "Debug console output: ", "",
                Config.getOptionBool("logging", "debugLoggingSysOut"));
    }
    
    /**
     * Initialises the advanced tab.
     */
    private void initAdvancedTab() {
        final String tabName = "Advanced";
        preferencesPanel.addCategory(tabName, "");
        
        final LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();
        final String[] lafs = new String[plaf.length];
        int i = 0;
        for (LookAndFeelInfo laf : plaf) {
            lafs[i++] = laf.getName();
        }
        
        preferencesPanel.addComboboxOption(tabName, "ui.lookandfeel",
                "Look and feel: ","", lafs,
                Config.getOption("ui", "lookandfeel"), false);
        preferencesPanel.addCheckboxOption(tabName, "ui.showversion",
                "Show version: ", "",
                Config.getOptionBool("ui", "showversion"));
        preferencesPanel.addSpinnerOption(tabName, "ui.inputbuffersize",
                "Input bufer size (lines): ", "",
                Config.getOptionInt("ui", "inputbuffersize", 50));
        preferencesPanel.addSpinnerOption(tabName, "ui.frameBufferSize",
                "Frame buffer size (characters): ", "",
                Config.getOptionInt("ui", "frameBufferSize", Integer.MAX_VALUE));
        preferencesPanel.addTextfieldOption(tabName, "general.browser",
                "Browser: ", "", Config.getOption("general", "browser"));
    }
    
    /** {@inheritDoc}. */
    public void configClosed(final Properties properties) {
        for (Object configOption : properties.keySet()) {
            final String[] args = ((String) configOption).split("\\.");
            Config.setOption(args[0], args[1], (String) properties.get(configOption));
        }
    }
}
