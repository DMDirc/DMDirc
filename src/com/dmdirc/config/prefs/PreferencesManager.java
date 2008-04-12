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
package com.dmdirc.config.prefs;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.prefs.validator.NumericalValidator;
import com.dmdirc.config.prefs.validator.StringLengthValidator;
import com.dmdirc.ui.themes.Theme;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.ui.swing.components.pluginpanel.PluginPanel;
import com.dmdirc.ui.swing.components.themepanel.ThemePanel;
import com.dmdirc.ui.swing.dialogs.prefs.URLConfigPanel;
import com.dmdirc.ui.swing.dialogs.prefs.UpdateConfigPanel;
import com.dmdirc.util.ListenerList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * Manages categories that should appear in the preferences dialog.
 *
 * @author chris
 */
public class PreferencesManager {

    /** A list of categories. */
    private final List<PreferencesCategory> categories
            = new ArrayList<PreferencesCategory>();

    /** A list of listeners. */
    private final ListenerList listeners = new ListenerList();

    /**
     * Creates a new instance of PreferencesManager.
     */
    public PreferencesManager() {
        addDefaultCategories();

        ActionManager.processEvent(CoreActionType.CLIENT_PREFS_OPENED, null, this);
    }

    /**
     * Adds the specified category to the preferences manager.
     *
     * @param category The category to be added
     */
    public void addCategory(final PreferencesCategory category) {
        categories.add(category);
    }

    /**
     * Retrieves a list of categories registered with the preferences manager.
     *
     * @return An ordered list of categories
     */
    public List<PreferencesCategory> getCategories() {
        return categories;
    }

    /**
     * Finds and retrieves the category with the specified name.
     *
     * @param name The name (title) of the category to find.
     * @return The appropriate category, or null if none was found
     */
    public PreferencesCategory getCategory(final String name) {
        for (PreferencesCategory category : categories) {
            if (category.getTitle().equals(name)) {
                return category;
            }
        }

        return null;
    }

    /**
     * Adds the default categories to this preferences manager.
     */
    private void addDefaultCategories() {
        addGeneralCategory();
        addConnectionCategory();
        addMessagesCategory();
        addGuiCategory();
        addPluginsCategory();
        addUrlHandlerCategory();
        addUpdatesCategory();
        addAdvancedCategory();
    }

    /**
     * Creates and adds the "General" category.
     */
    private void addGeneralCategory() {
        final PreferencesCategory category = new PreferencesCategory("General", "");

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "confirmQuit", "false", "Confirm quit",
                "Do you want to confirm closing the client?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "splitusermodes", "false", "Split user modes",
                "Show individual mode lines for each mode change that affects" +
                " a user (e.g. op, devoice)"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "sendwho", "false", "Send channel WHOs",
                "Request information (away state, hostname, etc) on channel " +
                "users automatically"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "general", "whotime", "60000", "Who request interval",
                "How often to send WHO requests for a channel"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "showmodeprefix", "false", "Show mode prefix",
                "Prefix users' names with their mode in channels"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "server", "friendlymodes", "false", "Friendly modes",
                "Show friendly mode names"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "hidequeries", "false", "Hide queries",
                "Initially hide queries so that they don't steal focus"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "awayindicator", "false", "Away indicator",
                "Shows an indicator in windows when you are marked as away"));
        category.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                new NumericalValidator(0, 100), "ui", "pasteProtectionLimit", "1",
                "Paste protection trigger", "Confirm pasting of text that " +
                "contains more than this many lines."));

        addCategory(category);
    }

    /**
     * Creates and adds the "Connection" category.
     */
    private void addConnectionCategory() {
        final PreferencesCategory category = new PreferencesCategory("Connection", "");

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closechannelsonquit", "false", "Close channels on quit",
                "Close channel windows when you quit the server?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closechannelsondisconnect", "false",
                "Close channels on disconnect", "Close channel windows when " +
                "the server is disconnected?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closequeriesonquit", "false", "Close queries on quit",
                "Close query windows when you quit the server?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closequeriesondisconnect", "false",
                "Close queries on disconnect", "Close query windows when " +
                "the server is disconnected?"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingtimeout", "60000", "Server ping timeout",
                "How long to wait for a server to reply to a PING request " +
                "before disconnecting"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "reconnectonconnectfailure", "false", "Reconnect on failure",
                "Attempt to reconnect if there is an error when connecting?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "reconnectondisconnect", "false", "Reconnect on disconnect",
                "Attempt to reconnect if the server is disconnected?"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "general", "reconnectdelay", "30000", "Reconnect delay",
                "How long to wait before attempting to reconnect to a server"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "rejoinchannels", "false", "Rejoin open channels",
                "Rejoin open channels when reconnecting to a server?"));

        addCategory(category);
    }

    /**
     * Creates and adds the "Messages" category.
     */
    private void addMessagesCategory() {
        final PreferencesCategory category = new PreferencesCategory("Messages", "");

        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "closemessage", "DMDirc exiting",
                "Close message", "Default quit message to use when closing DMDirc"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "partmessage", "Using DMDirc",
                "Part message", "Default part message to use when leaving channels"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "quitmessage", "Using DMDirc",
                "Quit message", "Default quit message to use when disconnecting"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "cyclemessage", "Rejoining",
                "Cycle message", "Default part message to use when cycling channels"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "kickmessage", "Bye bye!",
                "Kick message", "Default message to use when kicking people"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "reconnectmessage", "Reconnecting",
                "Reconnect message", "Default quit message to use when reconnecting"));

        addNotificationsCategory(category);
        addCategory(category);
    }

    /**
     * Creates and adds the "Notifications" category.
     *
     * @param parent The parent category.
     */
    private void addNotificationsCategory(final PreferencesCategory parent) {
        final PreferencesCategory category = new PreferencesCategory("Notifications", "");

        final Map<String, String> options = new HashMap<String, String>();
        final Map<String, String> whoisOptions = new HashMap<String, String>();
        final Map<String, String> ctcprOptions = new HashMap<String, String>();
        final Map<String, String> mapOptions = new HashMap<String, String>();

        options.put("all", "All windows");
        options.put("active", "Active window");
        options.put("server", "Server window");
        options.put("none", "Nowhere");

        whoisOptions.putAll(options);
        whoisOptions.put("lastcommand:(raw )?whois %4$s( %4$s)?", "Source of whois command");

        ctcprOptions.putAll(options);
        ctcprOptions.put("lastcommand:ctcp %1$s %4$S", "Source of ctcp command");

        mapOptions.putAll(options);
        mapOptions.put("window:Network Map", "Map window");

        category.addSetting(new PreferencesSetting("notifications", "socketClosed",
                "server", "Socket closed", "Where to display socket closed notifications",
                options));
        category.addSetting(new PreferencesSetting("notifications", "privateNotice",
                "server", "Private notice", "Where to display private notices",
                options));
        category.addSetting(new PreferencesSetting("notifications", "privateCTCP",
                "server", "CTCP request", "Where to display CTCP request notifications",
                options));
        category.addSetting(new PreferencesSetting("notifications", "privateCTCPreply",
                "server", "CTCP reply", "Where to display CTCP replies",
                ctcprOptions));
        category.addSetting(new PreferencesSetting("notifications", "connectError",
                "server", "Connect error", "Where to display connect error notifications",
                options));
        category.addSetting(new PreferencesSetting("notifications", "connectRetry",
                "server", "Connect retry", "Where to display connect retry notifications",
                options));
        category.addSetting(new PreferencesSetting("notifications", "stonedServer",
                "server", "Stoned server", "Where to display stoned server notifications",
                options));
        category.addSetting(new PreferencesSetting("notifications", "whois",
                "lastcommand:(raw )?whois %4$s( %4$s)?", "Whois output", "Where to " +
                "display /whois output",
                whoisOptions));
        category.addSetting(new PreferencesSetting("notifications", "lusers",
                "server", "Lusers output", "Where to display /lusers output",
                options));
        category.addSetting(new PreferencesSetting("notifications", "map",
                "window:Network Map", "Map output", "Where to display /map output",
                mapOptions));
        category.addSetting(new PreferencesSetting("notifications", "away",
                "server", "Away notification", "Where to display /away output",
                options));
        category.addSetting(new PreferencesSetting("notifications", "back",
                "server", "Back notification", "Where to display /away output",
                options));

        parent.addSubCategory(category);
    }

    /**
     * Creates and adds the "Advanced" category.
     */
    private void addAdvancedCategory() {
        final PreferencesCategory category = new PreferencesCategory("Advanced", "");

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "browser", "userlaunchdelay", "false", "Use browser launch delay",
                "Enable delay between browser launches (to prevent mistakenly" +
                " double clicking)?"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "browser", "launchdelay", "500", "Browser launch delay",
                "Minimum time between opening of URLs"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "submitErrors", "false", "Automatically submit errors",
                "Automatically submit client errors to the developers?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "logerrors", "false", "Log errors to disk",
                "Save copies of all errors to disk?"));        
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "tabcompletion", "casesensitive", "false", "Case-sensitive tab completion",
                "Respect case when tab completing?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "quickCopy", "false", "Quick copy", "Automatically copy" +
                " text that's selected when the mouse button is released?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "showversion", "true", "Show version",
                "Show DMDirc version in the titlebar?"));
        category.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                new NumericalValidator(10, -1), "ui", "frameBufferSize", "100000",
                "Window buffer size", "The maximum number of lines in a window" +
                " buffer"));

        addCategory(category);
    }

    /**
     * Creates and adds the "GUI" category.
     */
    private void addGuiCategory() {
        final Map<String, String> lafs = new HashMap<String, String>();
        final Map<String, String> framemanagers = new HashMap<String, String>();
        final Map<String, String> fmpositions = new HashMap<String, String>();
        final PreferencesCategory category = new PreferencesCategory("GUI", "");

        framemanagers.put("treeview", "Treeview");
        framemanagers.put("buttonbar", "Button bar");

        fmpositions.put("top", "Top");
        fmpositions.put("bottom", "Bottom");
        fmpositions.put("left", "Left");
        fmpositions.put("right", "Right");

        final LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();
        final String sysLafClass = UIManager.getSystemLookAndFeelClassName();
        String sysLafName = "";

        lafs.put("Native", "Native");
        for (LookAndFeelInfo laf : plaf) {
            lafs.put(laf.getName(), laf.getName());

            if (laf.getClassName().equals(sysLafClass)) {
                sysLafName = laf.getName();
            }
        }

        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "backgroundcolour", "0", "Background colour", "Default " +
                "background colour to use"));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "foregroundcolour", "1", "Foreground colour", "Default " +
                "foreground colour to use"));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "inputbackgroundcolour", "false:0", "Input background colour",
                "Default background colour to use for input fields"));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "inputforegroundcolour", "false:1", "Input foreground colour",
                "Default foreground colour to use for input fields"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "showcolourdialog", "false", "Show colour dialog",
                "Show colour picker dialog when using colour control codes?"));
        category.addSetting(new PreferencesSetting("ui", "lookandfeel",
                sysLafName, "Look and feel", "The Java look and feel to use",
                lafs).setRestartNeeded());
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "antialias", "false", "System anti-alias",
                "Anti-alias all fonts?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "maximisewindows", "true", "Auto-maximise windows",
                "Automatically maximise newly opened windows?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursintext", "false", "Show nick colours in text area",
                "Show nickname colours in text areas?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursinnicklist", "false", "Show nick colours in nicklists",
                "Show nickname colours in channel nicklists?"));
        category.addSetting(new PreferencesSetting("ui", "framemanager",
                "treeview", "Window manager", "Which window manager should be used?",
                framemanagers).setRestartNeeded());
        category.addSetting(new PreferencesSetting("ui", "framemanagerPosition",
                "left", "Window manager position", "Where should the window " +
                "manager be positioned?", fmpositions).setRestartNeeded());
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "stylelinks", "true", "Style links",
                "Style links in text areas"));

        addThemesCategory(category);
        addNicklistCategory(category);
        addTreeviewCategory(category);
        addCategory(category);
    }

    /**
     * Creates and adds the "Themes" category.
     *
     * @param parent The parent category
     */
    private void addThemesCategory(final PreferencesCategory parent) {
        // TODO: Abstract the panel

        parent.addSubCategory(new PreferencesCategory("Themes", "", new ThemePanel()));        
    }

    /**
     * Creates and adds the "Nicklist" category.
     *
     * @param parent The parent category
     */
    private void addNicklistCategory(final PreferencesCategory parent) {
        final PreferencesCategory category = new PreferencesCategory("Nicklist", "");

        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "nicklistbackgroundcolour", "false:0", "Nicklist background colour",
                "Background colour to use for the nicklist"));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "nicklistforegroundcolour", "false:1", "Nicklist foreground colour",
                "Foreground colour to use for the nicklist"));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "nicklist", "altBackgroundColour", "false:f0f0f0", "Alternate background colour",
                "Background colour to use for every other nicklist entry"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "sortByMode", "true", "Sort nicklist by user mode",
                "Sort nicknames by the modes that they have?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "sortByCase", "false", "Sort nicklist by case",
                "Sort nicknames in a case-sensitive manner?"));

        parent.addSubCategory(category);
    }

    /**
     * Creates and adds the "Treeview" category.
     *
     * @param parent The parent category
     */
    private void addTreeviewCategory(final PreferencesCategory parent) {
        final PreferencesCategory category = new PreferencesCategory("Treeview", "");

        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "treeview", "backgroundcolour", "false:0", "Treeview background colour",
                "Background colour to use for the treeview"));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "treeview", "foregroundcolour", "false:1", "Treeview foreground colour",
                "Foreground colour to use for the treeview"));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "treeviewRolloverColour", "false:f0f0f0", "Treeview rollover colour",
                "Background colour to use when the mouse cursor is over a node"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "treeview", "sortwindows", "true", "Sort windows",
                "Sort windows belonging to servers in the treeview?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "treeview", "sortservers", "true", "Sort servers",
                "Sort servers in the treeview?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "treeviewActiveBold", "false", "Active node bold",
                "Make the active node bold?"));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "treeviewActiveBackground", "false:0", "Active node background",
                "Background colour to use for active treeview node"));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "treeviewActiveForeground", "false:1", "Active node foreground",
                "Foreground colour to use for active treeview node"));

        parent.addSubCategory(category);
    }

    /**
     * Creates and adds the "Plugins" category.
     */
    private void addPluginsCategory() {
        // TODO: Abstract the panel

        addCategory(new PreferencesCategory("Plugins", "", new PluginPanel()));
    }

    /**
     * Creates and adds the "Updates" category.
     */
    private void addUpdatesCategory() {
        // TODO: Abstract the panel

        addCategory(new PreferencesCategory("Updates", "", new UpdateConfigPanel()));
    }

    /**
     * Creates and adds the "URL Handlers" category.
     */
    private void addUrlHandlerCategory() {
        // TODO: Abstract the panel

        addCategory(new PreferencesCategory("URL Handlers",
                "Configure how DMDirc handles different types of URLs",
                new URLConfigPanel()));
    }

    /**
     * Registers the specified save listener with this manager.
     *
     * @param listener The listener to be registered
     */
    public void registerSaveListener(final PreferencesInterface listener) {
        listeners.add(PreferencesInterface.class, listener);
    }

    /**
     * Fires the "save" methods of all registered listeners.
     */
    public void fireSaveListeners() {
        for (PreferencesInterface iface : listeners.get(PreferencesInterface.class)) {
            iface.save();
        }

        for (PreferencesCategory category : categories) {
            fireSaveListener(category);
        }
    }

    /**
     * Fires the save listener for any objects within the specified category.
     *
     * @param category The category to check
     */
    private void fireSaveListener(final PreferencesCategory category) {
        if (category.hasObject()) {
            category.getObject().save();
        }

        for (PreferencesCategory subcategory : category.getSubcats()) {
            fireSaveListener(subcategory);
        }
    }

}
