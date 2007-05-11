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

import java.util.Map;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.ui.MainFrame;
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
    private static final long serialVersionUID = 6;
    
    /** A previously created instance of PreferencesDialog. */
    private static PreferencesDialog me;
    
    /** preferences panel. */
    private PreferencesPanel preferencesPanel;
    
    /**
     * Creates a new instance of PreferencesDialog.
     */
    private PreferencesDialog() {
    }
    
    /** Creates the dialog if one doesn't exist, and displays it. */
    public static synchronized void showPreferencesDialog() {
        if (me == null) {
            me = new PreferencesDialog();
        }
        if (me.preferencesPanel == null) {
            me.initComponents();
        } else {
            me.preferencesPanel.requestFocus();
        }
    }
    
    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        
        preferencesPanel = new PreferencesPanel(this);
        
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
        final String general = "General";
        final String messages = "Messages";
        final String windows = "Windows";
        preferencesPanel.addCategory(general, "");
        
        preferencesPanel.addCategory(general, messages, "");
        preferencesPanel.addCategory(general, windows, "");
        
        preferencesPanel.addCheckboxOption(general, "ui.antialias",
                "System anti-alias: ", "", Config.getOptionBool("ui", "antialias"));
        
        preferencesPanel.addCheckboxOption(general, "server.friendlymodes",
                "Friendly modes: ", "", Config.getOptionBool("server", "friendlymodes"));
        preferencesPanel.addCheckboxOption(general, "general.hidequeries",
                "Hide queries : ", "", Config.getOptionBool("general", "hidequeries"));
        preferencesPanel.addSpinnerOption(general, "general.reconnectdelay",
                "Reconnect delay: ", "", Config.getOptionInt("general", "reconnectdelay", 30));
        preferencesPanel.addCheckboxOption(general, "general.reconnectonconnectfailure",
                "Reconnect on failure: ", "", Config.getOptionBool("general", "reconnectonconnectfailure"));
        preferencesPanel.addCheckboxOption(general, "general.reconnectondisconnect",
                "Reconnect on disconnect: ", "", Config.getOptionBool("general", "reconnectondisconnect"));
        preferencesPanel.addCheckboxOption(general, "general.showcolourdialog",
                "Show colour dialog: ", "", Config.getOptionBool("general", "showcolourdialog"));
        preferencesPanel.addSpinnerOption(general, "general.whotime",
                "Who request interval (ms): ", "",
                Config.getOptionInt("general", "whotime", 600000),
                10000, Integer.MAX_VALUE, 10000);
        preferencesPanel.addSpinnerOption(general, "server.pingtimeout",
                "Server timeout (ms): ", "", Config.getOptionInt("server", "pingtimeout", 60000),
                5000, Integer.MAX_VALUE, 5000);
        preferencesPanel.addCheckboxOption(general, "channel.sendwho",
                "Send channel WHOs: ", "", Config.getOptionBool("channel", "sendwho"));
        
        preferencesPanel.addTextfieldOption(messages, "general.closemessage",
                "Close message: ", "", Config.getOption("general", "closemessage"));
        preferencesPanel.addTextfieldOption(messages, "general.partmessage",
                "Part message: ", "", Config.getOption("general", "partmessage"));
        preferencesPanel.addTextfieldOption(messages, "general.quitmessage",
                "Quit message: ", "", Config.getOption("general", "quitmessage"));
        preferencesPanel.addTextfieldOption(messages, "general.cyclemessage",
                "Cycle message: ", "", Config.getOption("general", "cyclemessage"));
        preferencesPanel.addTextfieldOption(messages, "general.kickmessage",
                "Kick message: ", "", Config.getOption("general", "kickmessage"));
        preferencesPanel.addTextfieldOption(messages, "general.reconnectmessage",
                "Reconnect message: ", "", Config.getOption("general", "reconnectmessage"));
        
        preferencesPanel.addCheckboxOption(windows, "general.closechannelsonquit",
                "Close channels on quit: ", "", Config.getOptionBool("general", "closechannelsonquit"));
        preferencesPanel.addCheckboxOption(windows, "general.closechannelsondisconnect",
                "Close channels on disconnect: ", "", Config.getOptionBool("general", "closechannelsondisconnect"));
        preferencesPanel.addCheckboxOption(windows, "general.closequeriesonquit",
                "Close queries on quit: ", "", Config.getOptionBool("general", "closequeriesonquit"));
        preferencesPanel.addCheckboxOption(windows, "general.closequeriesondisconnect",
                "Close queries on disconnect: ", "", Config.getOptionBool("general", "closequeriesondisconnect"));
    }
    
    /**
     * Initialises the UI tab.
     */
    private void initUITab() {
        final String tabName = "GUI";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addCheckboxOption(tabName, "ui.maximisewindows",
                "Auto-Maximise windows: ", "",
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
        preferencesPanel.addCheckboxOption(tabName, "nicklist.altBackground",
                "Alternating nicklist", "",
                Config.getOptionBool("nicklist", "altBackground"));
        preferencesPanel.addColourOption(tabName, "nicklist.altBackgroundColour",
                "Alternate nicklist colour: ", "",
                Config.getOption("nicklist", "nicklist.altBackgroundColour", "f0f0f0"),
                false, true);
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
        
        preferencesPanel.addComboboxOption(tabName, "notifications.connectError",
                "Connect error: ", "", windowOptions,
                Config.getOption("notifications", "connectError"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.connectRetry",
                "Connect retry: ", "", windowOptions,
                Config.getOption("notifications", "connectRetry"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.stonedServer",
                "Stoned server: ", "", windowOptions,
                Config.getOption("notifications", "stonedServer"), false);
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
                "Look and feel: ", "", lafs,
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
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String[] args = ((String) entry.getKey()).split("\\.");
            Config.setOption(args[0], args[1], (String) entry.getValue());
        }
        preferencesPanel = null;
    }
}
