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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.util.validators.NumericalValidator;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages preferences for the client.
 *
 * @since 0.6.5
 * @author chris
 */
public final class PreferencesManager {

    /** Singleton instance of the preferences manager. */
    private static final PreferencesManager ME = new PreferencesManager();

    /**
     * Constructs a new PreferencesManager.
     */
    private PreferencesManager() {
        // Do nothing
    }

    /**
     * Retrieves a category containing preferences settings which should be
     * displayed in channel-specific contexts.
     *
     * @return A preferences category populated with channel settings
     */
    public PreferencesCategory getServerSettings() {
        final PreferencesCategory category
                = new PreferencesCategory("Server settings",
                "These settings are specific to this server on this network,"
                + " any settings specified here will overwrite global settings");

        // Copy all the channel ones
        for (PreferencesSetting setting : getChannelSettings().getSettings()) {
            category.addSetting(setting);
        }

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closechannelsonquit", "Close channels on quit",
                "Close channel windows when you manually disconnect from the server"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closechannelsondisconnect",
                "Close channels on disconnect", "Close channel windows when "
                + "the server is disconnected (because of an error)"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closequeriesonquit", "Close queries on quit",
                "Close query windows when you manually disconnect from the server"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "closequeriesondisconnect",
                "Close queries on disconnect", "Close query windows when "
                + "the server is disconnected (because of an error)"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingtimer", "Ping warning time",
                "How long to wait after a ping reply is sent before showing "
                + "a warning message"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingtimeout", "Ping timeout",
                "How long to wait for a server to reply to a PING request "
                + "before assuming the server has died"));
        category.addSetting(new PreferencesSetting(PreferencesType.DURATION,
                "server", "pingfrequency", "Ping frequency",
                "How often a PING request should be sent to the server (to "
                + "check that it is still alive)"));
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
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "quitmessage",
                "Quit message", "Default quit message to use when disconnecting"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "reconnectmessage",
                "Reconnect message", "Default quit message to use when reconnecting"));

        ActionManager.processEvent(CoreActionType.CLIENT_PREFS_REQUESTED, null,
                category, Boolean.TRUE);

        return category;
    }

    /**
     * Retrieves a category containing preferences settings which should be
     * displayed in channel-specific contexts.
     *
     * @return A preferences category populated with channel settings
     */
    public PreferencesCategory getChannelSettings() {
        final PreferencesCategory category
                = new PreferencesCategory("Channel settings",
                "These settings are specific to this channel on this network,"
                + " any settings specified here will overwrite global settings");

        final Map<String, String> charsetMap = new HashMap<String, String>();
        for (Charset charset : Charset.availableCharsets().values()) {
            charsetMap.put(charset.name(), charset.displayName());
        }

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "splitusermodes", "Split user modes",
                "Show individual mode lines for each mode change that affects"
                + " a user (e.g. op, devoice)"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "sendwho", "Send channel WHOs",
                "Request information (away state, hostname, etc) on channel "
                + "users automatically"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "channel", "showmodeprefix", "Show mode prefix",
                "Prefix users' names with their mode (e.g. @) in channels"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursintext", "Show nick colours in text area",
                "Show nickname colours (if set) in text areas"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursinnicklist", "Show nick colours in nicklists",
                "Show nickname colours (if set) in channel nicklists"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "partmessage",
                "Part message", "Default part message to use when leaving channels"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "cyclemessage",
                "Cycle message", "Default part message to use when cycling channels"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                "general", "kickmessage",
                "Kick message", "Default message to use when kicking people"));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "backgroundcolour", "Background colour", "Default "
                + "background colour to use"));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "foregroundcolour", "Foreground colour", "Default "
                + "foreground colour to use"));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "inputbackgroundcolour", "Input background colour",
                "Default background colour to use for input fields"));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "inputforegroundcolour", "Input foreground colour",
                "Default foreground colour to use for input fields"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "general", "showcolourdialog", "Show colour dialog",
                "Show colour picker dialog when using colour control codes"));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "nicklistbackgroundcolour", "Nicklist background colour",
                "Background colour to use for the nicklist"));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "nicklistforegroundcolour", "Nicklist foreground colour",
                "Foreground colour to use for the nicklist"));
        category.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                "ui", "nickListAltBackgroundColour",
                "Alternate background colour",
                "Background colour to use for every other nicklist entry"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "nicklist", "sortByMode", "Sort nicklist by user mode",
                "Sort nicknames by the modes that they have?"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "nicklist", "sortByCase", "Sort nicklist by case",
                "Sort nicknames in a case-sensitive manner?"));
        category.addSetting(new PreferencesSetting(PreferencesType.OPTIONALINTEGER,
                new NumericalValidator(10, -1), "ui", "frameBufferSize",
                "Window buffer size", "The maximum number of lines in a window"
                + " buffer"));
        category.addSetting(new PreferencesSetting(PreferencesType.FONT,
                "ui", "textPaneFontName", "Textpane font",
                "Font for the textpane"));
        category.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                "ui", "textPaneFontSize", "Textpane font size",
                "Font size for the textpane"));
        category.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                "ui", "inputbuffersize", "Input buffer size",
                "Number of items of input history to keep"));
        category.addSetting(new PreferencesSetting("channel", "encoding",
                "Encoding", "Encoding to use", charsetMap));

        ActionManager.processEvent(CoreActionType.CLIENT_PREFS_REQUESTED, null,
                category, Boolean.FALSE);

        return category;
    }

    /**
     * Retrieves a singleton instance of the PreferencesManager.
     *
     * @return The global PreferencesManager instance.
     */
    public static PreferencesManager getPreferencesManager() {
        return ME;
    }

}
