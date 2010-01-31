/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Main;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.prefs.validator.NumericalValidator;
import com.dmdirc.config.prefs.validator.OptionalValidator;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.Service;
import com.dmdirc.util.ListenerList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Saves all the settings in this manager.
     *
     * @return Is a restart needed after saving?
     */
    public boolean save() {
        fireSaveListeners();
        
        boolean restart = false;
        for (PreferencesCategory category : categories) {
            if (category.save()) {
                restart = true;
            }
        }

        return restart;
    }

    /**
     * Dismisses all the settings in this manager.
     */
    public void dismiss() {
        for (PreferencesCategory category : categories) {
            category.dismiss();
        }
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
                "ui", "confirmQuit", "Confirm quit",
                "Show a confirmation message when you try to close the client"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "splitusermodes", "Split user modes",
                "Show individual mode lines for each mode change that affects" +
                " a user (e.g. op, devoice)"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "sendwho", "Send channel WHOs",
                "Request information (away state, hostname, etc) on channel " +
                "users automatically"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "general", "whotime", "Who request interval",
                "How often to send WHO requests for a channel (if enabled)"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "showmodeprefix", "Show mode prefix",
                "Prefix users' names with their mode (e.g. @) in channels"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "hidequeries", "Hide queries",
                "Initially hide new query windows so that they don't steal focus"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "awayindicator", "Away indicator",
                "Show an indicator in windows when you are marked as away"));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALINTEGER,
                new OptionalValidator(new NumericalValidator(0, 100)), "ui", "pasteProtectionLimit",
                "Paste protection trigger", "Confirm pasting of text that " +
                "contains more than this many lines."));
        
        final Map<String, String> taboptions = new HashMap<String, String>();
        for (Service service : PluginManager.getPluginManager().getServicesByType("tabcompletion")) {
            taboptions.put(service.getName(), service.getName());
        }
        
        category.addSetting(new PreferencesSetting("tabcompletion", "style",
                "Tab completion style", "Determines the behaviour of " +
                "the tab completer when there are multiple matches", taboptions));

        addCategory(category);
    }

    /**
     * Creates and adds the "Connection" category.
     */
    private void addConnectionCategory() {
        final PreferencesCategory category = new PreferencesCategory("Connection",
                "", "category-connection");

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closechannelsonquit", "Close channels on quit",
                "Close channel windows when you manually disconnect from the server"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closechannelsondisconnect",
                "Close channels on disconnect", "Close channel windows when " +
                "the server is disconnected (because of an error)"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closequeriesonquit", "Close queries on quit",
                "Close query windows when you manually disconnect from the server"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closequeriesondisconnect",
                "Close queries on disconnect", "Close query windows when " +
                "the server is disconnected (because of an error)"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingtimer", "Ping warning time",
                "How long to wait after a ping reply is sent before showing " +
                "a warning message"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingtimeout", "Ping timeout",
                "How long to wait for a server to reply to a PING request " +
                "before assuming the server has died"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingfrequency", "Ping frequency",
                "How often a PING request should be sent to the server (to " +
                "check that it is still alive)"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "reconnectonconnectfailure", "Reconnect on failure",
                "Attempt to reconnect if there is an error when connecting"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "reconnectondisconnect", "Reconnect on disconnect",
                "Attempt to reconnect if the server is disconnected"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "general", "reconnectdelay", "Reconnect delay",
                "How long to wait before attempting to reconnect to a server"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "rejoinchannels", "Rejoin open channels",
                "Rejoin open channels when reconnecting to a server"));

        addCategory(category);
    }

    /**
     * Creates and adds the "Messages" category.
     */
    private void addMessagesCategory() {
        final PreferencesCategory category = new PreferencesCategory("Messages",
                "", "category-messages");

        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "closemessage",
                "Close message", "Default quit message to use when closing DMDirc"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "partmessage",
                "Part message", "Default part message to use when leaving channels"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "quitmessage",
                "Quit message", "Default quit message to use when disconnecting"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "cyclemessage",
                "Cycle message", "Default part message to use when cycling channels"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "kickmessage",
                "Kick message", "Default message to use when kicking people"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "reconnectmessage",
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
        final PreferencesCategory category = new PreferencesCategory("Notifications",
                "", "input-error");

        final Map<String, String> options = new HashMap<String, String>();
        final Map<String, String> commonOptions = new HashMap<String, String>();
        final Map<String, String> whoisOptions = new HashMap<String, String>();
        final Map<String, String> ctcprOptions = new HashMap<String, String>();
        final Map<String, String> mapOptions = new HashMap<String, String>();

        options.put("all", "All windows");
        options.put("active", "Active window");
        options.put("server", "Server window");
        options.put("none", "Nowhere");

        commonOptions.putAll(options);
        commonOptions.put("comchans:%1$s", "Common channels");

        whoisOptions.putAll(options);
        whoisOptions.put("lastcommand:(raw )?whois %4$s( %4$s)?", "Source of whois command");
        whoisOptions.put("comchans:%4$s", "Common channels");

        ctcprOptions.putAll(commonOptions);
        ctcprOptions.put("lastcommand:ctcp %1$s %4$S", "Source of ctcp command");

        mapOptions.putAll(options);
        mapOptions.put("window:Network Map", "Map window");

        category.addSetting(new PreferencesSetting("notifications", "socketClosed",
                "Socket closed", "Where to display socket closed notifications",
                options));
        category.addSetting(new PreferencesSetting("notifications", "privateNotice",
                "Private notice", "Where to display private notices",
                commonOptions));
        category.addSetting(new PreferencesSetting("notifications", "serverNotice",
                "Server notice", "Where to display server notices",
                options));
        category.addSetting(new PreferencesSetting("notifications", "privateCTCP",
                "CTCP request", "Where to display CTCP request notifications",
                commonOptions));
        category.addSetting(new PreferencesSetting("notifications", "privateCTCPreply",
                "CTCP reply", "Where to display CTCP replies",
                ctcprOptions));
        category.addSetting(new PreferencesSetting("notifications", "connectError",
                "Connect error", "Where to display connect error notifications",
                options));
        category.addSetting(new PreferencesSetting("notifications", "connectRetry",
                "Connect retry", "Where to display connect retry notifications",
                options));
        category.addSetting(new PreferencesSetting("notifications", "stonedServer",
                "Stoned server", "Where to display stoned server notifications",
                options));
        category.addSetting(new PreferencesSetting("notifications", "whois",
                "Whois output", "Where to display /whois output",
                whoisOptions));
        category.addSetting(new PreferencesSetting("notifications", "lusers",
                "Lusers output", "Where to display /lusers output",
                options));
        category.addSetting(new PreferencesSetting("notifications", "map",
                "Map output", "Where to display /map output",
                mapOptions));
        category.addSetting(new PreferencesSetting("notifications", "away",
                "Away notification", "Where to display /away output",
                options));
        category.addSetting(new PreferencesSetting("notifications", "back",
                "Back notification", "Where to display /away output",
                options));

        parent.addSubCategory(category);
    }

    /**
     * Creates and adds the "Advanced" category.
     */
    private void addAdvancedCategory() {
        final PreferencesCategory category = new PreferencesCategory("Advanced", 
                "", "category-advanced");

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "browser", "uselaunchdelay", "Use browser launch delay",
                "Enable delay between browser launches (to prevent mistakenly" +
                " double clicking)"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "browser", "launchdelay", "Browser launch delay",
                "Minimum time between opening of URLs if enabled"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "submitErrors", "Automatically submit errors",
                "Automatically submit client errors to the developers"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "logerrors", "Log errors to disk",
                "Save copies of all client errors to disk"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "tabcompletion", "casesensitive", "Case-sensitive tab completion",
                "Respect case when tab completing"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "quickCopy", "Quick copy", "Automatically copy" +
                " text that's selected when the mouse button is released"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "showversion", "Show version",
                "Show the current DMDirc version in the titlebar"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "showglobalwindow", "Show global window",
                "Show a global window which can be used to enter commands"));

        addCategory(category);
    }

    /**
     * Creates and adds the "GUI" category.
     */
    private void addGuiCategory() {
        final PreferencesCategory category = new PreferencesCategory("GUI", "",
                "category-gui");
        final Map<String, String> uiOptions = new HashMap<String, String>();

        for (Service uiName : PluginManager.getPluginManager().getServicesByType("ui")) {
            uiOptions.put(uiName.getName(), uiName.getName());
        }

        category.addSetting(new PreferencesSetting("general", "ui", "Active UI",
                "User interface to use", uiOptions).setRestartNeeded());
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "backgroundcolour", "Background colour", "Default " +
                "background colour to use"));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "foregroundcolour", "Foreground colour", "Default " +
                "foreground colour to use"));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "inputbackgroundcolour", "Input background colour",
                "Default background colour to use for input fields"));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "inputforegroundcolour", "Input foreground colour",
                "Default foreground colour to use for input fields"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "showcolourdialog", "Show colour dialog",
                "Show colour picker dialog when using colour control codes"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "antialias", "System anti-alias",
                "Anti-alias all fonts").setRestartNeeded());
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "maximisewindows", "Auto-maximise windows",
                "Automatically maximise newly opened windows"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursintext", "Show nick colours in text area",
                "Show nickname colours (if set) in text areas"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursinnicklist", "Show nick colours in nicklists",
                "Show nickname colours (if set) in channel nicklists"));

        addThemesCategory(category);
        addCategory(category);
    }

    /**
     * Creates and adds the "Themes" category.
     *
     * @param parent The parent category
     */
    private void addThemesCategory(final PreferencesCategory parent) {
        // TODO: Abstract the panel

        parent.addSubCategory(new PreferencesCategory("Themes", "",
                "category-addons", Main.getUI().getThemesPrefsPanel()));
    }

    /**
     * Creates and adds the "Plugins" category.
     */
    private void addPluginsCategory() {
        // TODO: Abstract the panel

        addCategory(new PreferencesCategory("Plugins", "", "category-addons",
                Main.getUI().getPluginPrefsPanel()));
    }

    /**
     * Creates and adds the "Updates" category.
     */
    private void addUpdatesCategory() {
        // TODO: Abstract the panel

        addCategory(new PreferencesCategory("Updates", "", "category-updates",
                Main.getUI().getUpdatesPrefsPanel()));
    }

    /**
     * Creates and adds the "URL Handlers" category.
     */
    private void addUrlHandlerCategory() {
        // TODO: Abstract the panel

        addCategory(new PreferencesCategory("URL Handlers",
                "Configure how DMDirc handles different types of URLs",
                "category-urlhandlers", Main.getUI().getUrlHandlersPrefsPanel()));
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

    /**
     * Fires the CLIENT_PREFS_CLOSED action
     *
     * @since 0.6
     */
    public void close() {
        ActionManager.processEvent(CoreActionType.CLIENT_PREFS_CLOSED, null);
    }

}
