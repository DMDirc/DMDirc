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
import com.dmdirc.events.ConnectionPrefsRequestedEvent;
import com.dmdirc.events.GroupChatPrefsRequestedEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.util.validators.NumericalValidator;
import com.dmdirc.util.validators.OptionalValidator;

import javax.inject.Inject;

/**
 * Manages preferences for the client.
 *
 * @since 0.6.5
 */
public class PreferencesManager {

    /** Event bus to public events on. */
    private final DMDircMBassador eventBus;

    @Inject
    public PreferencesManager(final DMDircMBassador eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Retrieves a category containing preferences settings which should be displayed in
     * channel-specific contexts.
     *
     * @param manager  The config manager to load settings from
     * @param identity The identity to save settings to
     *
     * @return A preferences category populated with channel settings
     */
    public PreferencesCategory getServerSettings(final AggregateConfigProvider manager,
            final ConfigProvider identity) {
        final PreferencesCategory category = new PreferencesCategory("Server settings",
                "These settings are specific to this server on this network,"
                + " any settings specified here will overwrite global settings");

        // Copy all the channel ones
        getChannelSettings(manager, identity).getSettings().forEach(category::addSetting);

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

        eventBus.publish(new ConnectionPrefsRequestedEvent(category, manager, identity));

        return category;
    }

    /**
     * Retrieves a category containing preferences settings which should be displayed in
     * channel-specific contexts.
     *
     * @param manager  The config manager to load settings from
     * @param identity The identity to save settings to
     *
     * @return A preferences category populated with channel settings
     */
    public PreferencesCategory getChannelSettings(final AggregateConfigProvider manager,
            final ConfigProvider identity) {
        final PreferencesCategory category = new PreferencesCategory("Channel settings",
                "These settings are specific to this channel on this network,"
                + " any settings specified here will overwrite global settings");

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
                new OptionalValidator(new NumericalValidator(10, -1)),
                "ui", "frameBufferSize", "Window buffer size",
                "The maximum number of lines in a window buffer",
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

        eventBus.publish(new GroupChatPrefsRequestedEvent(category, manager, identity));

        return category;
    }

}
