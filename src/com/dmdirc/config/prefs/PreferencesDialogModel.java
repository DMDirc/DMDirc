/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.ClientPrefsClosedEvent;
import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.plugins.Service;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.validators.NumericalValidator;
import com.dmdirc.util.validators.OptionalValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Manages categories that should appear in the preferences dialog.
 */
public class PreferencesDialogModel {

    /** A list of categories. */
    private final List<PreferencesCategory> categories = new ArrayList<>();
    /** A list of listeners. */
    private final ListenerList listeners = new ListenerList();
    /** UI specific plugin panel. */
    private final PreferencesInterface pluginPanel;
    /** UI specific theme panel. */
    private final PreferencesInterface themePanel;
    /** UI specific updates panel. */
    private final PreferencesInterface updatesPanel;
    /** UI specific URL panel. */
    private final PreferencesInterface urlHandlerPanel;
    /** Config Manager to read settings from. */
    private final AggregateConfigProvider configManager;
    /** Identity to write settings to. */
    private final ConfigProvider identity;
    /** Service manager. */
    private final ServiceManager serviceManager;
    /** Event bus to post events on. */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new instance of PreferencesDialogModel.
     */
    @Inject
    public PreferencesDialogModel(final PreferencesInterface pluginPanel,
            final PreferencesInterface themePanel,
            final PreferencesInterface updatesPanel,
            final PreferencesInterface urlHandlerPanel,
            final AggregateConfigProvider configManager,
            final ConfigProvider identity,
            final ServiceManager serviceManager,
            final DMDircMBassador eventBus) {
        this.pluginPanel = pluginPanel;
        this.themePanel = themePanel;
        this.updatesPanel = updatesPanel;
        this.urlHandlerPanel = urlHandlerPanel;
        this.configManager = configManager;
        this.identity = identity;
        this.serviceManager = serviceManager;
        this.eventBus = eventBus;

        addDefaultCategories();

        eventBus.publish(new ClientPrefsOpenedEvent(this));
    }

    public AggregateConfigProvider getConfigManager() {
        return configManager;
    }

    public ConfigProvider getIdentity() {
        return identity;
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
        return Collections.unmodifiableList(categories);
    }

    /**
     * Finds and retrieves the category with the specified name.
     *
     * @param name The name (title) of the category to find.
     *
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
                restart |= true;
            }
        }

        return restart;
    }

    /**
     * Dismisses all the settings in this manager.
     */
    public void dismiss() {
        categories.forEach(PreferencesCategory::dismiss);
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
                "Show a confirmation message when you try to close the client",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "splitusermodes", "Split user modes",
                "Show individual mode lines for each mode change that affects"
                + " a user (e.g. op, devoice)", configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "showmodeprefix", "Show mode prefix",
                "Prefix users' names with their mode (e.g. @) in channels",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "awayindicator", "Away indicator",
                "Show an indicator in windows when you are marked as away",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALINTEGER,
                new OptionalValidator(new NumericalValidator(0, 100)), "ui",
                "pasteProtectionLimit", "Paste protection trigger",
                "Confirm pasting of text that contains more than this many "
                + "lines.", configManager, identity));

        addTabCompletionCategory(category);
        addCategory(category.setInlineAfter());
    }

    /**
     * Creates and adds the "Tab Completion" category.
     *
     * @param parent Parent category to add this category to
     *
     * @since 0.6.4
     */
    private void addTabCompletionCategory(final PreferencesCategory parent) {
        final PreferencesCategory category = new PreferencesCategory("Tab Completion", "");
        final Map<String, String> taboptions = new HashMap<>();

        for (Service service : serviceManager.getServicesByType("tabcompletion")) {
            taboptions.put(service.getName(), service.getName());
        }

        category.addSetting(new PreferencesSetting("tabcompletion", "style",
                "Tab completion style", "Determines the behaviour of "
                + "the tab completer when there are multiple matches",
                taboptions, configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "tabcompletion", "casesensitive", "Case-sensitive tab completion",
                "Respect case when tab completing", configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "tabcompletion", "allowempty", "Allow empty tab completion",
                "Attempt to tab complete when the Tab key is pressed even "
                + "if there is nothing to complete", configManager, identity));

        parent.addSubCategory(category.setInline());
    }

    /**
     * Creates and adds the "Connection" category.
     */
    private void addConnectionCategory() {
        final PreferencesCategory category = new PreferencesCategory("Connection",
                "", "category-connection");

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closechannelsonquit", "Close channels on quit",
                "Close channel windows when you manually disconnect from the server",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closechannelsondisconnect",
                "Close channels on disconnect", "Close channel windows when "
                + "the server is disconnected (because of an error)",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closequeriesonquit", "Close queries on quit",
                "Close query windows when you manually disconnect from the server",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closequeriesondisconnect",
                "Close queries on disconnect", "Close query windows when "
                + "the server is disconnected (because of an error)",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingtimer", "Ping warning time",
                "How long to wait after a ping reply is sent before showing "
                + "a warning message", configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingtimeout", "Ping timeout",
                "How long to wait for a server to reply to a PING request "
                + "before assuming the server has died", configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingfrequency", "Ping frequency",
                "How often a PING request should be sent to the server (to "
                + "check that it is still alive)", configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "reconnectonconnectfailure", "Reconnect on failure",
                "Attempt to reconnect if there is an error when connecting",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "reconnectondisconnect", "Reconnect on disconnect",
                "Attempt to reconnect if the server is disconnected",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "general", "reconnectdelay", "Reconnect delay",
                "How long to wait before attempting to reconnect to a server",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "rejoinchannels", "Rejoin open channels",
                "Rejoin open channels when reconnecting to a server",
                configManager, identity));

        addSSLCategory(category);
        addCategory(category);
    }

    /**
     * Creates and adds the "SSL" category.
     *
     * @param parent Parent category to add this category to
     */
    private void addSSLCategory(final PreferencesCategory parent) {
        final PreferencesCategory category = new PreferencesCategory("SSL",
                "Options relating to encrypted (SSL) connections", "secure-server");

        category.addSetting(new PreferencesSetting(PreferencesType.FILE, "ssl",
                "clientcert.file", "Client certificate", "Path to PKCS12 client "
                + "certificate to send when connecting to servers using SSL",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT, "ssl",
                "clientcert.pass", "Client password", "Password for client "
                + "certificate file", configManager, identity));

        parent.addSubCategory(category);
    }

    /**
     * Creates and adds the "Messages" category.
     */
    private void addMessagesCategory() {
        final PreferencesCategory category = new PreferencesCategory("Messages",
                "", "category-messages");

        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "closemessage",
                "Close message", "Default quit message to use when closing DMDirc",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "partmessage",
                "Part message", "Default part message to use when leaving channels",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "quitmessage",
                "Quit message", "Default quit message to use when disconnecting",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "cyclemessage",
                "Cycle message", "Default part message to use when cycling channels",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "kickmessage",
                "Kick message", "Default message to use when kicking people",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "reconnectmessage",
                "Reconnect message", "Default quit message to use when reconnecting",
                configManager, identity));

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

        final Map<String, String> options = new HashMap<>();
        final Map<String, String> commonOptions = new HashMap<>();
        final Map<String, String> whoisOptions = new HashMap<>();
        final Map<String, String> ctcprOptions = new HashMap<>();
        final Map<String, String> mapOptions = new HashMap<>();

        options.put("all", "All windows");
        options.put("active", "Active window");
        options.put("server", "Server window");
        options.put("none", "Nowhere");

        commonOptions.putAll(options);
        commonOptions.put("comchans:%1$s server", "Common channels");

        whoisOptions.putAll(options);
        whoisOptions.put("lastcommand:(raw )?whois %4$s( %4$s)?", "Source of whois command");
        whoisOptions.put("comchans:%4$s server", "Common channels");

        ctcprOptions.putAll(commonOptions);
        ctcprOptions.put("lastcommand:ctcp %1$s %4$S", "Source of ctcp command");

        mapOptions.putAll(options);
        mapOptions.put("window:Network Map", "Map window");

        category.addSetting(new PreferencesSetting("notifications", "socketClosed",
                "Socket closed", "Where to display socket closed notifications",
                options, configManager, identity));
        category.addSetting(new PreferencesSetting("notifications", "privateNotice",
                "Private notice", "Where to display private notices",
                commonOptions, configManager, identity));
        category.addSetting(new PreferencesSetting("notifications", "serverNotice",
                "Server notice", "Where to display server notices",
                options, configManager, identity));
        category.addSetting(new PreferencesSetting("notifications", "privateCTCP",
                "CTCP request", "Where to display CTCP request notifications",
                commonOptions, configManager, identity));
        category.addSetting(new PreferencesSetting("notifications", "privateCTCPreply",
                "CTCP reply", "Where to display CTCP replies",
                ctcprOptions, configManager, identity));
        category.addSetting(new PreferencesSetting("notifications", "connectError",
                "Connect error", "Where to display connect error notifications",
                options, configManager, identity));
        category.addSetting(new PreferencesSetting("notifications", "connectRetry",
                "Connect retry", "Where to display connect retry notifications",
                options, configManager, identity));
        category.addSetting(new PreferencesSetting("notifications", "stonedServer",
                "Stoned server", "Where to display stoned server notifications",
                options, configManager, identity));
        category.addSetting(new PreferencesSetting("notifications", "whois",
                "Whois output", "Where to display /whois output",
                whoisOptions, configManager, identity));
        category.addSetting(new PreferencesSetting("notifications", "lusers",
                "Lusers output", "Where to display /lusers output",
                options, configManager, identity));
        category.addSetting(new PreferencesSetting("notifications", "map",
                "Map output", "Where to display /map output",
                mapOptions, configManager, identity));
        category.addSetting(new PreferencesSetting("notifications", "away",
                "Away notification", "Where to display /away output",
                options, configManager, identity));
        category.addSetting(new PreferencesSetting("notifications", "back",
                "Back notification", "Where to display /away output",
                options, configManager, identity));

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
                "Enable delay between browser launches (to prevent mistakenly"
                + " double clicking)", configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "browser", "launchdelay", "Browser launch delay",
                "Minimum time between opening of URLs if enabled",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "submitErrors", "Automatically submit errors",
                "Automatically submit client errors to the developers",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "logerrors", "Log errors to disk",
                "Save copies of all client errors to disk",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "quickCopy", "Quick copy", "Automatically copy"
                + " text that's selected when the mouse button is released",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "showversion", "Show version",
                "Show the current DMDirc version in the titlebar",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "showglobalwindow", "Show global window",
                "Show a global window which can be used to enter commands",
                configManager, identity));

        addCategory(category);
    }

    /**
     * Creates and adds the "GUI" category.
     */
    private void addGuiCategory() {
        final PreferencesCategory category = new PreferencesCategory("GUI", "",
                "category-gui");

        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "backgroundcolour", "Background colour", "Default "
                + "background colour to use", configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "foregroundcolour", "Foreground colour", "Default "
                + "foreground colour to use", configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "inputbackgroundcolour", "Input background colour",
                "Default background colour to use for input fields",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "inputforegroundcolour", "Input foreground colour",
                "Default foreground colour to use for input fields",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "showcolourdialog", "Show colour dialog",
                "Show colour picker dialog when using colour control codes",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "antialias", "System anti-alias",
                "Anti-alias all fonts", configManager, identity).setRestartNeeded());
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursintext", "Show nick colours in text area",
                "Show nickname colours (if set) in text areas",
                configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursinnicklist", "Show nick colours in nicklists",
                "Show nickname colours (if set) in channel nicklists",
                configManager, identity));

        addThemesCategory(category);
        addStyleSubCategory(category);
        addCategory(category.setInlineAfter());
    }

    /**
     * Creates the Style subcategory in "GUI".
     *
     * @since 0.6.4
     * @param parent Parent category to add this category to
     */
    private void addStyleSubCategory(final PreferencesCategory parent) {
        final PreferencesCategory category = new PreferencesCategory("Link styles"
                + " and colours", "");
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "stylelinks", "Style hyperlinks", "Style hyperlinks in "
                + "text areas with underlines", configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "linkcolour", "Hyperlink colour", "Default colour to use "
                + "for hyperlinks in the text area", configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "stylechannels", "Style channel links", "Styles channel "
                + "links in text areas with underlines", configManager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALCOLOUR,
                "ui", "channelcolour", "Channel link colour", "Default colour to use "
                + "for channel links in the text area", configManager, identity));

        parent.addSubCategory(category.setInline());
    }

    /**
     * Creates and adds the "Themes" category.
     *
     * @param parent The parent category
     */
    private void addThemesCategory(final PreferencesCategory parent) {
        parent.addSubCategory(new PreferencesCategory("Themes", "",
                "category-addons", themePanel));
    }

    /**
     * Creates and adds the "Plugins" category.
     */
    private void addPluginsCategory() {
        addCategory(new PreferencesCategory("Plugins", "", "category-addons",
                pluginPanel));
    }

    /**
     * Creates and adds the "Updates" category.
     */
    private void addUpdatesCategory() {
        addCategory(new PreferencesCategory("Updates", "", "category-updates",
                updatesPanel));
    }

    /**
     * Creates and adds the "URL Handlers" category.
     */
    private void addUrlHandlerCategory() {
        addCategory(new PreferencesCategory("URL Handlers",
                "Configure how DMDirc handles different types of URLs",
                "category-urlhandlers", urlHandlerPanel));
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
        listeners.get(PreferencesInterface.class).forEach(PreferencesInterface::save);
        categories.forEach(this::fireSaveListener);
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

        category.getSubcats().forEach(this::fireSaveListener);
    }

    /**
     * Fires the CLIENT_PREFS_CLOSED action.
     *
     * @since 0.6
     */
    public void close() {
        eventBus.publishAsync(new ClientPrefsClosedEvent());
    }

}
