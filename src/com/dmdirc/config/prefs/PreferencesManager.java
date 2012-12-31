/*
 * Copyright (c) 2006-2013 DMDirc Developers
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
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.Identity;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.util.validators.NumericalValidator;

import lombok.RequiredArgsConstructor;

/**
 * Manages preferences for the client.
 *
 * @since 0.6.5
 */
@RequiredArgsConstructor
public class PreferencesManager {

    /** Singleton instance of the preferences manager. */
    private static PreferencesManager me;

    /** The action controller to fire events on. */
    private final ActionController actionController;

    /**
     * Retrieves a category containing preferences settings which should be
     * displayed in channel-specific contexts.
     *
     * @param manager The config manager to load settings from
     * @param identity The identity to save settings to
     * @return A preferences category populated with channel settings
     */
    public PreferencesCategory getServerSettings(final ConfigManager manager,
            final Identity identity) {
        final PreferencesCategory category
                = new PreferencesCategory("Server settings",
                "These settings are specific to this server on this network,"
                + " any settings specified here will overwrite global settings");

        // Copy all the channel ones
        for (PreferencesSetting setting : getChannelSettings(manager, identity)
                .getSettings()) {
            category.addSetting(setting);
        }

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closechannelsonquit", "Close channels on quit",
                "Close channel windows when you manually disconnect from the server",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closechannelsondisconnect",
                "Close channels on disconnect", "Close channel windows when "
                + "the server is disconnected (because of an error)",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closequeriesonquit", "Close queries on quit",
                "Close query windows when you manually disconnect from the server",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closequeriesondisconnect",
                "Close queries on disconnect", "Close query windows when "
                + "the server is disconnected (because of an error)",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingtimer", "Ping warning time",
                "How long to wait after a ping reply is sent before showing "
                + "a warning message",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingtimeout", "Ping timeout",
                "How long to wait for a server to reply to a PING request "
                + "before assuming the server has died",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingfrequency", "Ping frequency",
                "How often a PING request should be sent to the server (to "
                + "check that it is still alive)",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "reconnectonconnectfailure", "Reconnect on failure",
                "Attempt to reconnect if there is an error when connecting",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "reconnectondisconnect", "Reconnect on disconnect",
                "Attempt to reconnect if the server is disconnected",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "general", "reconnectdelay", "Reconnect delay",
                "How long to wait before attempting to reconnect to a server",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "rejoinchannels", "Rejoin open channels",
                "Rejoin open channels when reconnecting to a server",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "quitmessage",
                "Quit message", "Default quit message to use when disconnecting",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "reconnectmessage",
                "Reconnect message", "Default quit message to use when reconnecting",
                manager, identity));

        actionController.triggerEvent(CoreActionType.CLIENT_PREFS_REQUESTED,
                null, category, Boolean.TRUE);

        return category;
    }

    /**
     * Retrieves a category containing preferences settings which should be
     * displayed in channel-specific contexts.
     *
     * @param manager The config manager to load settings from
     * @param identity The identity to save settings to
     * @return A preferences category populated with channel settings
     */
    public PreferencesCategory getChannelSettings(final ConfigManager manager,
            final Identity identity) {
        final PreferencesCategory category
                = new PreferencesCategory("Channel settings",
                "These settings are specific to this channel on this network,"
                + " any settings specified here will overwrite global settings");

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "splitusermodes", "Split user modes",
                "Show individual mode lines for each mode change that affects"
                + " a user (e.g. op, devoice)",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "sendwho", "Send channel WHOs",
                "Request information (away state, hostname, etc) on channel "
                + "users automatically",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "showmodeprefix", "Show mode prefix",
                "Prefix users' names with their mode (e.g. @) in channels",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursintext", "Show nick colours in text area",
                "Show nickname colours (if set) in text areas",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursinnicklist", "Show nick colours in nicklists",
                "Show nickname colours (if set) in channel nicklists",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "partmessage",
                "Part message", "Default part message to use when leaving channels",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "cyclemessage",
                "Cycle message", "Default part message to use when cycling channels",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "kickmessage",
                "Kick message", "Default message to use when kicking people",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "backgroundcolour", "Background colour", "Default "
                + "background colour to use",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "foregroundcolour", "Foreground colour", "Default "
                + "foreground colour to use",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "inputbackgroundcolour", "Input background colour",
                "Default background colour to use for input fields",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "inputforegroundcolour", "Input foreground colour",
                "Default foreground colour to use for input fields",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "showcolourdialog", "Show colour dialog",
                "Show colour picker dialog when using colour control codes",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "nicklistbackgroundcolour", "Nicklist background colour",
                "Background colour to use for the nicklist",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "nicklistforegroundcolour", "Nicklist foreground colour",
                "Foreground colour to use for the nicklist",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "nickListAltBackgroundColour",
                "Alternate background colour",
                "Background colour to use for every other nicklist entry",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "nicklist", "sortByMode", "Sort nicklist by user mode",
                "Sort nicknames by the modes that they have?",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "nicklist", "sortByCase", "Sort nicklist by case",
                "Sort nicknames in a case-sensitive manner?",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALINTEGER,
                new NumericalValidator(10, -1), "ui", "frameBufferSize",
                "Window buffer size", "The maximum number of lines in a window"
                + " buffer",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.FONT,
                "ui", "textPaneFontName", "Textpane font",
                "Font for the textpane",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                "ui", "textPaneFontSize", "Textpane font size",
                "Font size for the textpane",
                manager, identity));
        category.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                "ui", "inputbuffersize", "Input buffer size",
                "Number of items of input history to keep",
                manager, identity));

        actionController.triggerEvent(CoreActionType.CLIENT_PREFS_REQUESTED,
                null, category, Boolean.FALSE);

        return category;
    }

    /**
     * Retrieves a singleton instance of the PreferencesManager.
     *
     * @return The global PreferencesManager instance.
     */
    public static synchronized PreferencesManager getPreferencesManager() {
        if (me == null) {
            me = new PreferencesManager(ActionManager.getActionManager());
        }

        return me;
    }

}
