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

import com.dmdirc.config.prefs.validator.NumericalValidator;
import com.dmdirc.config.prefs.validator.StringLengthValidator;
import com.dmdirc.ui.swing.dialogs.prefs.URLConfigPanel;

import com.dmdirc.ui.swing.dialogs.prefs.UpdateConfigPanel;
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

    /**
     * Creates a new instance of PreferencesManager.
     */
    public PreferencesManager() {
        addDefaultCategories();
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
     * Adds the default categories to this preferences manager.
     */
    private void addDefaultCategories() {
        addGeneralCategory();
        addConnectionCategory();
        addMessagesCategory();
        addGuiCategory();
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
                "ui", "confirmquit", "false", "Confirm quit",
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
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                new StringLengthValidator(1, 1), "general", "commandchar", "/",
                "Command character", "Character used to prefix a command"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                new StringLengthValidator(1, 1), "general", "silencechar", ".",
                "Silence character", "Character used to silence commands"));
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

        final Map<String, String> optionsOne = new HashMap<String, String>();
        final Map<String, String> optionsTwo = new HashMap<String, String>();

        optionsOne.put("all", "All");
        optionsOne.put("active", "Active");
        optionsOne.put("server", "Server");
        optionsOne.put("none", "Nowhere");

        optionsTwo.put("all", "All");
        optionsTwo.put("active", "Active");
        optionsTwo.put("server", "Server");
        optionsTwo.put("lastcommand:whois %4$s( %4$s)?", "Source of command");
        optionsTwo.put("none", "Nowhere");

        category.addSetting(new PreferencesSetting("notifications", "socketClosed",
                "server", "Socket closed", "Where to display socket closed notifications",
                optionsOne));
        category.addSetting(new PreferencesSetting("notifications", "privateNotice",
                "server", "Private notice", "Where to display private notices",
                optionsOne));
        category.addSetting(new PreferencesSetting("notifications", "privateCTCP",
                "server", "CTCP request", "Where to display CTCP request notifications",
                optionsOne));
        category.addSetting(new PreferencesSetting("notifications", "privateCTCPreply",
                "server", "CTCP reply", "Where to display CTCP replies",
                optionsOne));
        category.addSetting(new PreferencesSetting("notifications", "connectError",
                "server", "Connect error", "Where to display connect error notifications",
                optionsOne));
        category.addSetting(new PreferencesSetting("notifications", "connectRetry",
                "server", "Connect retry", "Where to display connect retry notifications",
                optionsOne));
        category.addSetting(new PreferencesSetting("notifications", "stonedServer",
                "server", "Stoned server", "Where to display stoned server notifications",
                optionsOne));
        category.addSetting(new PreferencesSetting("notifications", "whois",
                "lastcommand:whois %4$s( %4$s)?", "Whois output", "Where to " +
                "display /whois output",
                optionsTwo));

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
                "general", "autoSubmitErrors", "false", "Automatically submit errors",
                "Automatically submit client errors to the developers?"));
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
        // XXX: Not implemented
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

}
