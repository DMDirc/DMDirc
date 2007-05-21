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

package com.dmdirc.ui.dialogs;

import java.util.Map;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.dmdirc.Config;
import com.dmdirc.ui.components.PreferencesInterface;
import com.dmdirc.ui.components.PreferencesPanel;

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
        me.initComponents();
        me.preferencesPanel.requestFocus();
    }
    
    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        
        preferencesPanel = new PreferencesPanel(this);
        
        initGeneralTab();
        initConnectionTab();
        initMessagesTab();
        initNotificationsTab();
        initGUITab();
        initNicklistTab();
        initTreeviewTab();
        initAdvancedTab();
        
        preferencesPanel.display();
    }
    
    /**
     * Initialises the preferences tab.
     */
    private void initGeneralTab() {
        final String tabName = "General";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addCheckboxOption(tabName, "channel.splitusermodes",
                "Split user modes: ", "Show individual mode lines for each mode change that affects a user (e.g. op, devoice)",
                Config.getOptionBool("channel", "splitusermodes"));
        preferencesPanel.addCheckboxOption(tabName, "channel.sendwho",
                "Send channel WHOs: ", "Request information (away state, hostname, etc) on channel users automatically",
                Config.getOptionBool("channel", "sendwho"));
        preferencesPanel.addSpinnerOption(tabName, "general.whotime",
                "Who request interval (ms): ", "How often to send WHO requests for a channel",
                Config.getOptionInt("general", "whotime", 600000),
                10000, Integer.MAX_VALUE, 10000);
        preferencesPanel.addCheckboxOption(tabName, "channel.showmodeprefix",
                "Show mode prefix: ", "Prefix users' names with their mode in channels",
                Config.getOptionBool("channel", "showmodeprefix"));
        preferencesPanel.addCheckboxOption(tabName, "server.friendlymodes",
                "Friendly modes: ", "Show friendly mode names",
                Config.getOptionBool("server", "friendlymodes"));
        preferencesPanel.addCheckboxOption(tabName, "general.hidequeries",
                "Hide queries : ", "", Config.getOptionBool("general", "hidequeries"));
        preferencesPanel.addTextfieldOption(tabName, "general.commandchar",
                "Command character: ", "Character used to indicate a command",
                Config.getCommandChar());
        preferencesPanel.addTextfieldOption(tabName, "general.silencechar",
                "Silence character: ", "Character used to indicate a command should be silently executed",
                Config.getOption("general", "silencechar"));
        preferencesPanel.addCheckboxOption(tabName, "ui.awayindicator",
                "Away indicator: ", "Shows an away indicator in the input field.",
                Config.getOptionBool("ui", "awayindicator"));
        preferencesPanel.addSpinnerOption(tabName, "ui.pasteProtectionLimit",
                "Paste protection trigger: ", "Confirm pasting of text that contains more than this many lines",
                Config.getOptionInt("ui", "pasteProtectionLimit", 1));
    }
    
    /**
     * Initialises the Connection tab.
     */
    private void initConnectionTab() {
        final String tabName = "Connection";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addCheckboxOption(tabName, "general.closechannelsonquit",
                "Close channels on quit: ", "Close channel windows when you quit the server",
                Config.getOptionBool("general", "closechannelsonquit"));
        preferencesPanel.addCheckboxOption(tabName, "general.closechannelsondisconnect",
                "Close channels on disconnect: ", "Close channel windows when the server is disconnected",
                Config.getOptionBool("general", "closechannelsondisconnect"));
        preferencesPanel.addCheckboxOption(tabName, "general.closequeriesonquit",
                "Close queries on quit: ", "Close query windows when you quit the server",
                Config.getOptionBool("general", "closequeriesonquit"));
        preferencesPanel.addCheckboxOption(tabName, "general.closequeriesondisconnect",
                "Close queries on disconnect: ", "Close query windows when the server is disconnected",
                Config.getOptionBool("general", "closequeriesondisconnect"));
        preferencesPanel.addSpinnerOption(tabName, "server.pingtimeout",
                "Server timeout (ms): ", "How long to wait for a server to reply to a PING request before disconnecting",
                Config.getOptionInt("server", "pingtimeout", 60000),
                5000, Integer.MAX_VALUE, 5000);
        preferencesPanel.addCheckboxOption(tabName, "general.reconnectonconnectfailure",
                "Reconnect on failure: ", "Attempt to reconnect if there's an error when connecting",
                Config.getOptionBool("general", "reconnectonconnectfailure"));
        preferencesPanel.addCheckboxOption(tabName, "general.reconnectondisconnect",
                "Reconnect on disconnect: ", "Reconnect automatically if the server is disconnected",
                Config.getOptionBool("general", "reconnectondisconnect"));
        preferencesPanel.addSpinnerOption(tabName, "general.reconnectdelay",
                "Reconnect delay: ", "How long to wait before attempting to reconnect to a server",
                Config.getOptionInt("general", "reconnectdelay", 30));
        preferencesPanel.addCheckboxOption(tabName, "general.rejoinchannels",
                "Rejoin open channels: ", "Rejoin open channels when reconnecting to a server",
                Config.getOptionBool("general", "rejoinchannels"));
    }
    
    /**
     * Initialises the Messages tab.
     */
    private void initMessagesTab() {
        final String tabName = "Messages";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addTextfieldOption(tabName, "general.closemessage",
                "Close message: ", "Default quit message to use when closing DMDirc",
                Config.getOption("general", "closemessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.partmessage",
                "Part message: ", "Default message to use when parting a channel",
                Config.getOption("general", "partmessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.quitmessage",
                "Quit message: ", "Default message to use when quitting a server",
                Config.getOption("general", "quitmessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.cyclemessage",
                "Cycle message: ", "Default message to use when cycling a channel",
                Config.getOption("general", "cyclemessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.kickmessage",
                "Kick message: ", "Default message to use when kicking a user from a channel",
                Config.getOption("general", "kickmessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.reconnectmessage",
                "Reconnect message: ", "Default message to use when quitting a server to reconnect",
                Config.getOption("general", "reconnectmessage"));
    }
    
    /**
     * Initialises the Notifications tab.
     */
    private void initNotificationsTab() {
        final String tabName = "Notifications";
        preferencesPanel.addCategory("Messages", tabName, "");
        final String[] windowOptions
                = new String[] {"all", "active", "server", };
        
        preferencesPanel.addComboboxOption(tabName, "notifications.socketClosed",
                "Socket closed: ", "Where to display socket closed notifications",
                windowOptions,
                Config.getOption("notifications", "socketClosed"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.privateNotice",
                "Private notice: ", "Where to display private notice notifications",
                windowOptions,
                Config.getOption("notifications", "privateNotice"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.privateCTCP",
                "CTCP request: ", "Where to display CTCP request notifications",
                windowOptions,
                Config.getOption("notifications", "privateCTCP"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.privateCTCPreply",
                "CTCP reply: ", "Where to display CTCP reply notifications",
                windowOptions,
                Config.getOption("notifications", "privateCTCPreply"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.connectError",
                "Connect error: ", "Where to display connect error notifications",
                windowOptions,
                Config.getOption("notifications", "connectError"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.connectRetry",
                "Connect retry: ", "Where to display connect retry notifications",
                windowOptions,
                Config.getOption("notifications", "connectRetry"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.stonedServer",
                "Stoned server: ", "Where to display stone server notifications",
                windowOptions,
                Config.getOption("notifications", "stonedServer"), false);
    }
    
    /**
     * Initialises the GUI tab.
     */
    private void initGUITab() {
        final LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();
        final String[] lafs = new String[plaf.length];
        final String tabName = "GUI";
        
        int i = 0;
        for (LookAndFeelInfo laf : plaf) {
            lafs[i++] = laf.getName();
        }
        
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addColourOption(tabName, "ui.backgroundcolour",
                "Window background colour: ", "Default background colour to use",
                Config.getOption("ui", "backgroundcolour"), true, true);
        preferencesPanel.addColourOption(tabName, "ui.foregroundcolour",
                "Window foreground colour: ", "Default foreground colour to use",
                Config.getOption("ui", "foregroundcolour"), true, true);
        preferencesPanel.addOptionalColourOption(tabName, "ui.inputbackgroundcolour",
                "Input background colour: ", "Background colour to use for input fields",
                Config.getOption("ui", "inputbackgroundcolour",
                Config.getOption("ui", "backgroundcolour", "")),
                false, true, true);
        preferencesPanel.addOptionalColourOption(tabName, "ui.inputforegroundcolour",
                "Input foreground colour: ", "Foreground colour to use for input fields",
                Config.getOption("ui", "inputforegroundcolour",
                Config.getOption("ui", "foregroundcolour", "")),
                false, true, true);
        preferencesPanel.addCheckboxOption(tabName, "general.showcolourdialog",
                "Show colour dialog: ", "Show colour picker dialog when inserting colour control codes",
                Config.getOptionBool("general", "showcolourdialog"));
        preferencesPanel.addComboboxOption(tabName, "ui.lookandfeel",
                "Look and feel: ", "The Java Look and Feel to use", lafs,
                Config.getOption("ui", "lookandfeel"), false);
        preferencesPanel.addCheckboxOption(tabName, "ui.antialias",
                "System anti-alias: ", "Anti-alias all fonts",
                Config.getOptionBool("ui", "antialias"));
        preferencesPanel.addCheckboxOption(tabName, "ui.maximisewindows",
                "Auto-Maximise windows: ", "Automatically maximise newly opened windows",
                Config.getOptionBool("ui", "maximisewindows"));
        preferencesPanel.addCheckboxOption(tabName, "ui.showintext",
                "Show colours in text area: ", "Show nickname colours in text areas",
                Config.getOptionBool("ui", "shownickcoloursintext"));
        preferencesPanel.addCheckboxOption(tabName, "ui.showinlist",
                "Show colours in nick list: ", "Show nickname colours in the nicklist",
                Config.getOptionBool("ui", "shownickcoloursinnicklist"));
    }
    
    /**
     * Initialises the Nicklist tab.
     */
    private void initNicklistTab() {
        final String tabName = "Nicklist";
        preferencesPanel.addCategory("GUI", tabName, "");
        
        preferencesPanel.addOptionalColourOption(tabName, "nicklist.backgroundcolour",
                "Nicklist background colour: ", "Background colour to use for the nicklist",
                Config.getOption("nicklist", "backgroundcolour",
                Config.getOption("ui", "backgroundcolour", "")),
                false, true, true);
        preferencesPanel.addOptionalColourOption(tabName, "nicklist.foregroundcolour",
                "Nicklist foreground colour: ", "Foreground colour to use for the nicklist",
                Config.getOption("nicklist", "foregroundcolour",
                Config.getOption("ui", "foregroundcolour", "")),
                false, true, true);
        preferencesPanel.addCheckboxOption(tabName, "nicklist.altBackground",
                "Alternating nicklist", "Use an alternate background colour for every other entry",
                Config.getOptionBool("nicklist", "altBackground"));
        preferencesPanel.addColourOption(tabName, "nicklist.altBackgroundColour",
                "Alternate nicklist colour: ", "Alternate background colour to use",
                Config.getOption("nicklist", "altBackgroundColour", "f0f0f0"),
                false, true);
        preferencesPanel.addCheckboxOption(tabName, "ui.sortByMode",
                "Nicklist sort by mode: ", "Sort nicklist by user mode",
                Config.getOptionBool("ui", "sortByMode"));
        preferencesPanel.addCheckboxOption(tabName, "ui.sortByCase",
                "Nicklist sort by case: ", "Sort nicklist by user mode",
                Config.getOptionBool("ui", "sortByCase"));
    }
    
    /**
     * Initialises the Treeview tab.
     */
    private void initTreeviewTab() {
        final String tabName = "Treeview";
        preferencesPanel.addCategory("GUI", tabName, "");
        
        preferencesPanel.addOptionalColourOption(tabName, "treeview.backgroundcolour",
                "Treeview background colour: ", "Background colour to use for the treeview",
                Config.getOption("treeview", "backgroundcolour",
                Config.getOption("ui", "backgroundcolour", "")),
                false, true, true);
        preferencesPanel.addOptionalColourOption(tabName, "treeview.foregroundcolour",
                "Treeview foreground colour: ", "Foreground colour to use for the treeview",
                Config.getOption("treeview", "foregroundcolour",
                Config.getOption("ui", "foregroundcolour", "")),
                false, true, true);
        preferencesPanel.addCheckboxOption(tabName, "ui.treeviewRolloverEnabled",
                "Rollover enabled: ", "Use a different background colour when cursor is over a treeview node",
                Config.getOptionBool("ui", "treeviewRolloverEnabled"));
        preferencesPanel.addColourOption(tabName, "ui.treeviewRolloverColour",
                "Rollover colour: ", "Rollover colour to use",
                Config.getOption("ui", "treeviewRolloverColour"),
                true, true);
        preferencesPanel.addCheckboxOption(tabName, "treeview.sortwindows",
                "Sort windows: ", "Sort windows of servers in the treeview",
                Config.getOptionBool("treeview", "sortwindows"));
        preferencesPanel.addCheckboxOption(tabName, "treeview.sortservers",
                "Sort servers: ", "Sort servers in the treeview",
                Config.getOptionBool("treeview", "sortservers"));
    }
    
    /**
     * Initialises the advanced tab.
     */
    private void initAdvancedTab() {
        final String tabName = "Advanced";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addTextfieldOption(tabName, "general.browser",
                "Browser: ", "The browser to use for opening URLs (only required when auto detection fails)",
                Config.getOption("general", "browser"));
        preferencesPanel.addCheckboxOption(tabName, "browser.uselaunchdelay",
                "Use browser delay: ", "Enable delay between browser launches (to prevent mistakenly double clicking)",
                Config.getOptionBool("browser", "uselaunchdelay"));
        preferencesPanel.addSpinnerOption(tabName, "browser.launchdelay",
                "Browser launch delay (ms): ", "Minimum time between opening of URLs",
                Config.getOptionInt("browser", "launchdelay", 500));
        preferencesPanel.addCheckboxOption(tabName, "general.autoSubmitErrors",
                "Automatically submit errors: ", "Automatically submit client errors to the developers",
                Config.getOptionBool("general", "autoSubmitErrors"));
        preferencesPanel.addCheckboxOption(tabName, "tabcompletion.casesensitive",
                "Case-sensitive tab completion: ", "Respect case when tab completing",
                Config.getOptionBool("tabcompletion", "casesensitive"));
        preferencesPanel.addCheckboxOption(tabName, "ui.quickCopy",
                "Quick Copy: ", "Automatically copy text that's selected in windows when the mouse button is released",
                Config.getOptionBool("ui", "quickCopy"));
        preferencesPanel.addCheckboxOption(tabName, "ui.showversion",
                "Show version: ", "Show DMDirc version in the titlebar",
                Config.getOptionBool("ui", "showversion"));
    }
    
    /** {@inheritDoc}. */
    public void configClosed(final Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String[] args = ((String) entry.getKey()).split("\\.");
            if ("".equals(entry.getValue())) {
                Config.unsetOption(args[0], args[1]);
            } else {
                Config.setOption(args[0], args[1], (String) entry.getValue());
            }
        }
        preferencesPanel = null;
    }
}
