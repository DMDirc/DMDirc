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

package com.dmdirc.ui.swing.dialogs;

import com.dmdirc.Main;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.themes.Theme;
import com.dmdirc.themes.ThemeManager;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.components.SwingPreferencesPanel;
import java.awt.Component;
import java.util.AbstractMap.SimpleImmutableEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * Allows the user to modify global client preferences.
 */
public final class PreferencesDialog implements PreferencesInterface, ConfigChangeListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 8;
    
    /** The global config manager. */
    private final ConfigManager config = IdentityManager.getGlobalConfig();
    
    /** A previously created instance of PreferencesDialog. */
    private static PreferencesDialog me;
    
    /** preferences panel. */
    private SwingPreferencesPanel preferencesPanel;
    
    /** Theme map. */
    private Map<String, String> themes;
    
    /** restart warning issued. */
    private boolean restartNeeded;
    
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
        
        preferencesPanel = new SwingPreferencesPanel(this);
        restartNeeded = false;
        
        initGeneralTab();
        initConnectionTab();
        initMessagesTab();
        initNotificationsTab();
        initGUITab();
        initThemesTab();
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
        
        preferencesPanel.addCheckboxOption(tabName, "ui.confirmQuit",
                "Confirm quit", "Do you want to confirm closing the client",
                config.getOptionBool(   "ui", "confirmQuit", false));
        preferencesPanel.addCheckboxOption(tabName, "channel.splitusermodes",
                "Split user modes: ", "Show individual mode lines for each mode change that affects a user (e.g. op, devoice)",
                config.getOptionBool("channel", "splitusermodes", false));
        preferencesPanel.addCheckboxOption(tabName, "channel.sendwho",
                "Send channel WHOs: ", "Request information (away state, hostname, etc) on channel users automatically",
                config.getOptionBool("channel", "sendwho", false));
        preferencesPanel.addSpinnerOption(tabName, "general.whotime",
                "Who request interval (ms): ", "How often to send WHO requests for a channel",
                config.getOptionInt("general", "whotime", 600000),
                10000, Integer.MAX_VALUE, 10000);
        preferencesPanel.addCheckboxOption(tabName, "channel.showmodeprefix",
                "Show mode prefix: ", "Prefix users' names with their mode in channels",
                config.getOptionBool("channel", "showmodeprefix", false));
        preferencesPanel.addCheckboxOption(tabName, "server.friendlymodes",
                "Friendly modes: ", "Show friendly mode names",
                config.getOptionBool("server", "friendlymodes", false));
        preferencesPanel.addCheckboxOption(tabName, "general.hidequeries",
                "Hide queries : ", "", config.getOptionBool("general", "hidequeries", false));
        preferencesPanel.addTextfieldOption(tabName, "general.commandchar",
                "Command character: ", "Character used to indicate a command",
                config.getOption("general", "commandchar"));
        preferencesPanel.addTextfieldOption(tabName, "general.silencechar",
                "Silence character: ", "Character used to indicate a command should be silently executed",
                config.getOption("general", "silencechar"));
        preferencesPanel.addCheckboxOption(tabName, "ui.awayindicator",
                "Away indicator: ", "Shows an away indicator in the input field.",
                config.getOptionBool("ui", "awayindicator", false));
        preferencesPanel.addSpinnerOption(tabName, "ui.pasteProtectionLimit",
                "Paste protection trigger: ", "Confirm pasting of text that contains more than this many lines",
                config.getOptionInt("ui", "pasteProtectionLimit", 1), 0, Integer.MAX_VALUE, 1);
    }
    
    /**
     * Initialises the Connection tab.
     */
    private void initConnectionTab() {
        final String tabName = "Connection";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addCheckboxOption(tabName, "general.closechannelsonquit",
                "Close channels on quit: ", "Close channel windows when you quit the server",
                config.getOptionBool("general", "closechannelsonquit", false));
        preferencesPanel.addCheckboxOption(tabName, "general.closechannelsondisconnect",
                "Close channels on disconnect: ", "Close channel windows when the server is disconnected",
                config.getOptionBool("general", "closechannelsondisconnect", false));
        preferencesPanel.addCheckboxOption(tabName, "general.closequeriesonquit",
                "Close queries on quit: ", "Close query windows when you quit the server",
                config.getOptionBool("general", "closequeriesonquit", false));
        preferencesPanel.addCheckboxOption(tabName, "general.closequeriesondisconnect",
                "Close queries on disconnect: ", "Close query windows when the server is disconnected",
                config.getOptionBool("general", "closequeriesondisconnect", false));
        preferencesPanel.addSpinnerOption(tabName, "server.pingtimeout",
                "Server timeout (ms): ", "How long to wait for a server to reply to a PING request before disconnecting",
                config.getOptionInt("server", "pingtimeout", 60000),
                5000, Integer.MAX_VALUE, 5000);
        preferencesPanel.addCheckboxOption(tabName, "general.reconnectonconnectfailure",
                "Reconnect on failure: ", "Attempt to reconnect if there's an error when connecting",
                config.getOptionBool("general", "reconnectonconnectfailure", false));
        preferencesPanel.addCheckboxOption(tabName, "general.reconnectondisconnect",
                "Reconnect on disconnect: ", "Reconnect automatically if the server is disconnected",
                config.getOptionBool("general", "reconnectondisconnect", false));
        preferencesPanel.addSpinnerOption(tabName, "general.reconnectdelay",
                "Reconnect delay: ", "How long to wait before attempting to reconnect to a server",
                config.getOptionInt("general", "reconnectdelay", 30), 0, Integer.MAX_VALUE, 1);
        preferencesPanel.addCheckboxOption(tabName, "general.rejoinchannels",
                "Rejoin open channels: ", "Rejoin open channels when reconnecting to a server",
                config.getOptionBool("general", "rejoinchannels", false));
    }
    
    /**
     * Initialises the Messages tab.
     */
    private void initMessagesTab() {
        final String tabName = "Messages";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addTextfieldOption(tabName, "general.closemessage",
                "Close message: ", "Default quit message to use when closing DMDirc",
                config.getOption("general", "closemessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.partmessage",
                "Part message: ", "Default message to use when parting a channel",
                config.getOption("general", "partmessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.quitmessage",
                "Quit message: ", "Default message to use when quitting a server",
                config.getOption("general", "quitmessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.cyclemessage",
                "Cycle message: ", "Default message to use when cycling a channel",
                config.getOption("general", "cyclemessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.kickmessage",
                "Kick message: ", "Default message to use when kicking a user from a channel",
                config.getOption("general", "kickmessage"));
        preferencesPanel.addTextfieldOption(tabName, "general.reconnectmessage",
                "Reconnect message: ", "Default message to use when quitting a server to reconnect",
                config.getOption("general", "reconnectmessage"));
    }
    
    /**
     * Initialises the Notifications tab.
     */
    private void initNotificationsTab() {
        final String tabName = "Notifications";
        preferencesPanel.addCategory("Messages", tabName, "");
        final Entry[] windowOptions = new Entry[] {
                    new SimpleImmutableEntry<String, String>("All", "all"),
                    new SimpleImmutableEntry<String, String>("Active", "active"), 
                    new SimpleImmutableEntry<String, String>("Server", "server"), 
                    new SimpleImmutableEntry<String, String>("None", "none  "), 
        };
        final Entry[] windowOptions2 = new Entry[] {
                    new SimpleImmutableEntry<String, String>("All", "all"),
                    new SimpleImmutableEntry<String, String>("Active", "active"), 
                    new SimpleImmutableEntry<String, String>("Server", "server"), 
                    new SimpleImmutableEntry<String, String>("Source of command", 
                            "lastcommand:whois %4$s( %4$s)"),
                    new SimpleImmutableEntry<String, String>("None", "none  "), 
        };
        
        preferencesPanel.addComboboxOption(tabName, "notifications.socketClosed",
                "Socket closed: ", "Where to display socket closed notifications",
                new DefaultComboBoxModel(windowOptions), new MapEntryRenderer(),
                config.getOption("notifications", "socketClosed"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.privateNotice",
                "Private notice: ", "Where to display private notice notifications",
                new DefaultComboBoxModel(windowOptions), new MapEntryRenderer(),
                config.getOption("notifications", "privateNotice"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.privateCTCP",
                "CTCP request: ", "Where to display CTCP request notifications",
                new DefaultComboBoxModel(windowOptions), new MapEntryRenderer(),
                config.getOption("notifications", "privateCTCP"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.privateCTCPreply",
                "CTCP reply: ", "Where to display CTCP reply notifications",
                new DefaultComboBoxModel(windowOptions), new MapEntryRenderer(),
                config.getOption("notifications", "privateCTCPreply"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.connectError",
                "Connect error: ", "Where to display connect error notifications",
                new DefaultComboBoxModel(windowOptions), new MapEntryRenderer(),
                config.getOption("notifications", "connectError"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.connectRetry",
                "Connect retry: ", "Where to display connect retry notifications",
                new DefaultComboBoxModel(windowOptions), new MapEntryRenderer(),
                config.getOption("notifications", "connectRetry"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.stonedServer",
                "Stoned server: ", "Where to display stoned server notifications",
                new DefaultComboBoxModel(windowOptions), new MapEntryRenderer(),
                config.getOption("notifications", "stonedServer"), false);
        preferencesPanel.addComboboxOption(tabName, "notifications.whois",
                "Whois output: ", "Where to display whois command output",
                new DefaultComboBoxModel(windowOptions2), new MapEntryRenderer(),
                config.getOption("notifications", "whois"), false);
    }
    
    /**
     * Initialises the GUI tab.
     */
    private void initGUITab() {
        final LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();
        final String sysLafClass = UIManager.getSystemLookAndFeelClassName();
        final String[] lafs = new String[plaf.length];
        final String tabName = "GUI";
        String sysLafName = "";
        
        int i = 0;
        for (LookAndFeelInfo laf : plaf) {
            lafs[i++] = laf.getName();
            if (laf.getClassName().equals(sysLafClass)) {
                sysLafName = laf.getName();
            }
        }
        
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addColourOption(tabName, "ui.backgroundcolour",
                "Window background colour: ", "Default background colour to use",
                config.getOption("ui", "backgroundcolour"), true, true);
        preferencesPanel.addColourOption(tabName, "ui.foregroundcolour",
                "Window foreground colour: ", "Default foreground colour to use",
                config.getOption("ui", "foregroundcolour"), true, true);
        preferencesPanel.addOptionalColourOption(tabName, "ui.inputbackgroundcolour",
                "Input background colour: ", "Background colour to use for input fields",
                config.getOption("ui", "inputbackgroundcolour",
                config.getOption("ui", "backgroundcolour", "")),
                config.hasOption("ui", "inputbackgroundcolour"), true, true);
        preferencesPanel.addOptionalColourOption(tabName, "ui.inputforegroundcolour",
                "Input foreground colour: ", "Foreground colour to use for input fields",
                config.getOption("ui", "inputforegroundcolour",
                config.getOption("ui", "foregroundcolour", "")),
                config.hasOption("ui", "inputforegroundcolour"), true, true);
        preferencesPanel.addCheckboxOption(tabName, "general.showcolourdialog",
                "Show colour dialog: ", "Show colour picker dialog when inserting colour control codes",
                config.getOptionBool("general", "showcolourdialog", false));
        preferencesPanel.addComboboxOption(tabName, "ui.lookandfeel",
                "Look and feel: ", "The Java Look and Feel to use", lafs,
                config.getOption("ui", "lookandfeel", sysLafName), false);
        preferencesPanel.addCheckboxOption(tabName, "ui.antialias",
                "System anti-alias: ", "Anti-alias all fonts",
                config.getOptionBool("ui", "antialias", false));
        preferencesPanel.addCheckboxOption(tabName, "ui.maximisewindows",
                "Auto-Maximise windows: ", "Automatically maximise newly opened windows",
                config.getOptionBool("ui", "maximisewindows", false));
        preferencesPanel.addCheckboxOption(tabName, "ui.showintext",
                "Show colours in text area: ", "Show nickname colours in text areas",
                config.getOptionBool("ui", "shownickcoloursintext", false));
        preferencesPanel.addCheckboxOption(tabName, "ui.showinlist",
                "Show colours in nick list: ", "Show nickname colours in the nicklist",
                config.getOptionBool("ui", "shownickcoloursinnicklist", false));
        preferencesPanel.addComboboxOption(tabName, "ui.framemanager",
                "Frame manager: ", "Which frame manager should be used",
                new String[]{"treeview", "buttonbar", },
                config.getOption("ui", "framemanager", "treeview"), false);
        preferencesPanel.addComboboxOption(tabName, "ui.framemanagerPosition",
                "Frame manager position: ", "Where should the frame manager be positioned",
                new String[]{"top", "bottom", "left", "right"},
                config.getOption("ui", "framemanagerPosition", "left"), false);
        preferencesPanel.addCheckboxOption(tabName, "ui.stylelinks",
                "Style links: ", "Style links in the textpane",
                config.getOptionBool("ui", "stylelinks", false));
    }
    
    /** Initialises the themes tab. */
    private void initThemesTab() {
        final String tabName = "Themes";
        final Map<String, Theme> availThemes = new ThemeManager().
                getAvailableThemes();
        
        themes = new HashMap<String, String>();
        
        for (Entry<String, Theme> entry : availThemes.entrySet()) {
            if (entry.getKey().indexOf('/') == -1) {
                themes.put(entry.getKey(), entry.getKey());
            } else {
                themes.put(entry.getKey().substring(entry.getKey().lastIndexOf('/'),
                        entry.getKey().length()), entry.getKey());
            }
        }
        
        themes.put("None", "");
        
        preferencesPanel.addCategory("GUI", tabName, "");
        
        preferencesPanel.addComboboxOption(tabName, "general.theme",
                "Theme: ", "DMDirc theme to user",
                themes.keySet().toArray(new String[themes.size()]),
                config.getOption("general", "theme", ""), false);
    }
    
    /**
     * Initialises the Nicklist tab.
     */
    private void initNicklistTab() {
        final String tabName = "Nicklist";
        preferencesPanel.addCategory("GUI", tabName, "");
        
        preferencesPanel.addOptionalColourOption(tabName, "ui.nicklistbackgroundcolour",
                "Nicklist background colour: ", "Background colour to use for the nicklist",
                config.getOption("ui", "nicklistbackgroundcolour",
                config.getOption("ui", "backgroundcolour", "")),
                config.hasOption("ui", "nicklistbackgroundcolour"), true, true);
        preferencesPanel.addOptionalColourOption(tabName, "ui.nicklistforegroundcolour",
                "Nicklist foreground colour: ", "Foreground colour to use for the nicklist",
                config.getOption("ui", "nicklistforegroundcolour",
                config.getOption("ui", "foregroundcolour", "")),
                config.hasOption("ui", "nicklistforegroundcolour"), true, true);
        preferencesPanel.addOptionalColourOption(tabName, "nicklist.altBackgroundColour",
                "Alternate nicklist colour: ", "Alternate background colour to use",
                config.getOption("nicklist", "altBackgroundColour", "f0f0f0"),
                config.hasOption("nicklist", "altBackgroundColour"), true, true);
        preferencesPanel.addCheckboxOption(tabName, "ui.sortByMode",
                "Nicklist sort by mode: ", "Sort nicklist by user mode",
                config.getOptionBool("ui", "sortByMode", false));
        preferencesPanel.addCheckboxOption(tabName, "ui.sortByCase",
                "Nicklist sort by case: ", "Sort nicklist by user mode",
                config.getOptionBool("ui", "sortByCase", false));
    }
    
    /**
     * Initialises the Treeview tab.
     */
    private void initTreeviewTab() {
        final String tabName = "Treeview";
        preferencesPanel.addCategory("GUI", tabName, "");
        
        preferencesPanel.addOptionalColourOption(tabName, "treeview.backgroundcolour",
                "Treeview background colour: ", "Background colour to use for the treeview",
                config.getOption("treeview", "backgroundcolour",
                config.getOption("ui", "backgroundcolour", "")),
                config.hasOption("treeview", "backgroundcolour"), true, true);
        preferencesPanel.addOptionalColourOption(tabName, "treeview.foregroundcolour",
                "Treeview foreground colour: ", "Foreground colour to use for the treeview",
                config.getOption("treeview", "foregroundcolour",
                config.getOption("ui", "foregroundcolour", "")),
                config.hasOption("treeview", "foregroundcolour"), true, true);
        preferencesPanel.addOptionalColourOption(tabName, "ui.treeviewRolloverColour",
                "Rollover colour: ", "Rollover colour to use",
                config.getOption("ui", "treeviewRolloverColour",
                config.getOption("treeview", "backgroundcolour", 
                config.getOption("ui", "backgroundcolour", "f0f0f0"))),
                config.hasOption("ui", "treeviewRolloverColour"), true, true);
        preferencesPanel.addCheckboxOption(tabName, "treeview.sortwindows",
                "Sort windows: ", "Sort windows of servers in the treeview",
                config.getOptionBool("treeview", "sortwindows"));
        preferencesPanel.addCheckboxOption(tabName, "treeview.sortservers",
                "Sort servers: ", "Sort servers in the treeview",
                config.getOptionBool("treeview", "sortservers"));
        preferencesPanel.addCheckboxOption(tabName, "ui.treeviewActiveBold",
                "Active node bold: ", "Show the active node in bold",
                config.getOptionBool("ui", "treeviewActiveBold"));
        preferencesPanel.addOptionalColourOption(tabName, "ui.treeviewActiveForeground",
                "Active node foreground: ", "Foreground colour of the active node",
                config.getOption("ui", "treeviewActiveForeground",
                config.getOption("treeview", "foregroundcolour", 
                config.getOption("ui", "foregroundcolour", ""))),
                config.hasOption("ui", "treeviewActiveForeground"), true, true);
        preferencesPanel.addOptionalColourOption(tabName, "ui.treeviewActiveBackground",
                "Active node background: ", "Background colour of the active node",
                config.getOption("ui", "treeviewActiveBackground",
                config.getOption("treeview", "backgroundcolour", 
                config.getOption("ui", "backgroundcolour", ""))),
                config.hasOption("ui", "treeviewActiveBackground"), true, true);
    }
    
    /**
     * Initialises the advanced tab.
     */
    private void initAdvancedTab() {
        final String tabName = "Advanced";
        preferencesPanel.addCategory(tabName, "");
        
        preferencesPanel.addTextfieldOption(tabName, "general.browser",
                "Browser: ", "The browser to use for opening URLs (only required when auto detection fails)",
                config.getOption("general", "browser", ""));
        preferencesPanel.addCheckboxOption(tabName, "browser.uselaunchdelay",
                "Use browser delay: ", "Enable delay between browser launches (to prevent mistakenly double clicking)",
                config.getOptionBool("browser", "uselaunchdelay", false));
        preferencesPanel.addSpinnerOption(tabName, "browser.launchdelay",
                "Browser launch delay (ms): ", "Minimum time between opening of URLs",
                config.getOptionInt("browser", "launchdelay", 500), 0, Integer.MAX_VALUE, 1);
        preferencesPanel.addCheckboxOption(tabName, "general.autoSubmitErrors",
                "Automatically submit errors: ", "Automatically submit client errors to the developers",
                config.getOptionBool("general", "autoSubmitErrors", false));
        preferencesPanel.addCheckboxOption(tabName, "tabcompletion.casesensitive",
                "Case-sensitive tab completion: ", "Respect case when tab completing",
                config.getOptionBool("tabcompletion", "casesensitive", false));
        preferencesPanel.addCheckboxOption(tabName, "ui.quickCopy",
                "Quick Copy: ", "Automatically copy text that's selected in windows when the mouse button is released",
                config.getOptionBool("ui", "quickCopy", false));
        preferencesPanel.addCheckboxOption(tabName, "ui.showversion",
                "Show version: ", "Show DMDirc version in the titlebar",
                config.getOptionBool("ui", "showversion", false));
        preferencesPanel.addSpinnerOption(tabName, "ui.frameBufferSize",
                "Frame buffer size: ", "Sets the maximum number of lines in the frame buffer.",
                config.getOptionInt("ui", "frameBufferSize", Integer.MAX_VALUE),
                1, Integer.MAX_VALUE, 1);
    }
    
    /** {@inheritDoc}. */
    @Override
    public void configClosed(final Properties properties) {
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
                    if ("general".equals(args[0]) && "theme".equals(args[1])) {
                        if (object == null || !object.equals(themes.get(entry.getValue()))) {
                            identity.setOption(args[0], args[1], themes.get(entry.getValue()));
                        }
                    } else {
                        if (object == null || !object.equals(entry.getValue())) {
                            identity.setOption(args[0], args[1], (String) entry.getValue());
                        }
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
    
    /** {@inheritDoc} */
    public void dispose() {
        synchronized (me) {
            preferencesPanel = null;
            IdentityManager.getGlobalConfig().removeListener(this);
            me = null;
        }
    }
    
    private class MapEntryRenderer extends DefaultListCellRenderer {
        
        /**
        * A version number for this class. It should be changed whenever the class
        * structure is changed (or anything else that would prevent serialized
        * objects being unserialized with the new class).
        */
        private static final long serialVersionUID = 1;
        
        /** {@inheritDoc} */
        @Override
        public Component getListCellRendererComponent(final JList list,
                final Object value, final int index, final boolean isSelected,
                final boolean hasFocus) {
        
            super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
        
            if (value == null) {
                setText("Any");
            } else if (value instanceof Entry) {
                setText((String) ((Entry) value).getKey());
            } else {
                setText(value.toString());
            }
        
            return this;
        }
        
    }
}
