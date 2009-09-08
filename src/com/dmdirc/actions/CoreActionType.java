/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.actions;

import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.actions.interfaces.ActionMetaType;
import com.dmdirc.actions.metatypes.ActionEvents;
import com.dmdirc.actions.metatypes.ChannelEvents;
import com.dmdirc.actions.metatypes.ClientEvents;
import com.dmdirc.actions.metatypes.PluginEvents;
import com.dmdirc.actions.metatypes.QueryEvents;
import com.dmdirc.actions.metatypes.ServerEvents;

/**
 * An enumeration of actions that are raised by the core.
 * @author chris
 */
public enum CoreActionType implements ActionType {
    
    /** Client opened. */
    CLIENT_OPENED(ClientEvents.CLIENT_EVENT, "Client opened"),
    /** Client closed. */
    CLIENT_CLOSED(ClientEvents.CLIENT_EVENT, "Client closed"),
    /** Client closing. */
    CLIENT_CLOSING(ClientEvents.CLIENT_EVENT, "Client closing"),
    /** Client minimised. */
    CLIENT_MINIMISED(ClientEvents.CLIENT_EVENT, "Client minimised"),
    /** Client unminimised. */
    CLIENT_UNMINIMISED(ClientEvents.CLIENT_EVENT, "Client unminimised"),
    
    /** Function key pressed. */
    CLIENT_KEY_PRESSED(ClientEvents.CLIENT_EVENT_WITH_KEY, "Function key pressed"),
    
    /** Frame changed. */
    CLIENT_FRAME_CHANGED(ClientEvents.WINDOW_EVENT, "Frame changed"),
    /** User input. */
    CLIENT_USER_INPUT(ClientEvents.CLIENT_EVENT_WITH_BUFFER, "User input"),
    /** Line added. */
    CLIENT_LINE_ADDED(ClientEvents.WINDOW_EVENT_WITH_MESSAGE, "Line added to a window"),
    /** Popup generated. */
    CLIENT_POPUP_GENERATED(ClientEvents.POPUP_EVENT, "Popup menu generated"),
    /** String styled. */
    CLIENT_STRING_STYLED(ClientEvents.CLIENT_EVENT_WITH_STYLE, "String styled"),

    /** Prefs dialog opened. */
    CLIENT_PREFS_OPENED(ClientEvents.CLIENT_EVENT_WITH_PREFS, "Preferences dialog opened"),
    /** Prefs dialog closed. */
    CLIENT_PREFS_CLOSED(ClientEvents.CLIENT_EVENT, "Preferences dialog opened"),
    
    /** Unknown command. */
    UNKNOWN_COMMAND(ClientEvents.UNKNOWN_COMMAND, "Unknown command entered"),
    
    /** Server numeric received. */
    SERVER_NUMERIC(ServerEvents.SERVER_NUMERIC, "Numeric event received"),
    /** Server connected. */
    SERVER_CONNECTED(ServerEvents.SERVER_EVENT, "Server connected"),
    /** Server connecting. */
    SERVER_CONNECTING(ServerEvents.SERVER_EVENT, "Server connecting"),
    /** Server connection error. */
    SERVER_CONNECTERROR(ServerEvents.SERVER_EVENT_WITH_ARG, "Server connection error"),    
    /** Server disconnected. */
    SERVER_DISCONNECTED(ServerEvents.SERVER_EVENT, "Server disconnected"),
    /** Marked as away. */
    SERVER_BACK(ServerEvents.SERVER_EVENT, "Marked as 'back'"),
    /** Marked as back. */
    SERVER_AWAY(ServerEvents.SERVER_EVENT_WITH_ARG, "Marked as 'away'"),
    /** Error. */
    SERVER_ERROR(ServerEvents.SERVER_EVENT_WITH_ARG, "Server error"),
    
    /** Auth notice received. */
    SERVER_AUTHNOTICE(ServerEvents.SERVER_EVENT_WITH_ARG, "Received auth notice"),
    /** Unknown notice received. */
    SERVER_UNKNOWNNOTICE(ServerEvents.SERVER_UNKNOWN_EVENT, "Received unknown notice"),
    
    /** User modes changed. */
    SERVER_USERMODES(ServerEvents.SERVER_SOURCED_EVENT_WITH_ARG, "User modes changed"),
    
    /** Private CTCP received. */
    SERVER_CTCP(ServerEvents.SERVER_CTCP_EVENT, "CTCP received"),
    /** Private CTCPR received. */
    SERVER_CTCPR(ServerEvents.SERVER_CTCP_EVENT, "CTCP reply received"),
    
    /** Private notice received. */
    SERVER_NOTICE(ServerEvents.SERVER_SOURCED_EVENT_WITH_ARG, "Notice received"),
    
    /** MOTD starting. */
    SERVER_MOTDSTART(ServerEvents.SERVER_EVENT_WITH_ARG, "Start of MOTD received"),
    /** MOTD ended. */
    SERVER_MOTDEND(ServerEvents.SERVER_EVENT_WITH_ARG, "End of MOTD received"),
    /** MOTD line. */
    SERVER_MOTDLINE(ServerEvents.SERVER_EVENT_WITH_ARG, "MOTD line received"),
    
    /** Ping reply received. */
    SERVER_GOTPING(ServerEvents.SERVER_PING, "Received server ping reply"),
    /** Missed server ping reply. */
    SERVER_NOPING(ServerEvents.SERVER_PING, "Missed server ping reply"),
    /** Ping sent. */
    SERVER_PINGSENT(ServerEvents.SERVER_EVENT, "Ping request sent"),
    
    /** Invite received. */
    SERVER_INVITERECEIVED(ServerEvents.SERVER_INVITE, "Invite received"),
    
    /** Wallops. */
    SERVER_WALLOPS(ServerEvents.SERVER_SOURCED_EVENT_WITH_ARG, "Wallop received"),
    /** Wallusers. */
    SERVER_WALLUSERS(ServerEvents.SERVER_SOURCED_EVENT_WITH_ARG, "Walluser received"),    
    /** Walldesync. */
    SERVER_WALLDESYNC(ServerEvents.SERVER_SOURCED_EVENT_WITH_ARG, "Walldesync received"),        
    
    /** Nick change. */
    SERVER_NICKCHANGE(ServerEvents.SERVER_NICKCHANGE, "My nickname changed"),
    
    /** Channel window opened. */
    CHANNEL_OPENED(ChannelEvents.CHANNEL_EVENT, "Channel window opened"),
    /** Channel window closed. */
    CHANNEL_CLOSED(ChannelEvents.CHANNEL_EVENT, "Channel window closed"),
    /** Names reply received. */
    CHANNEL_GOTNAMES(ChannelEvents.CHANNEL_EVENT, "Channel names reply received"),
    /** Channel topic received. */
    CHANNEL_GOTTOPIC(ChannelEvents.CHANNEL_TOPICEVENT, "Channel topic received"),
    
    /** Channel message sent. */
    CHANNEL_SELF_MESSAGE(ChannelEvents.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel message sent"),
    /** Channel action sent. */
    CHANNEL_SELF_ACTION(ChannelEvents.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel action sent"),
    
    /** Channel message received. */
    CHANNEL_MESSAGE(ChannelEvents.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel message received"),
    /** Channel actions received. */
    CHANNEL_ACTION(ChannelEvents.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel action received"),
    /** Channel notice received. */
    CHANNEL_NOTICE(ChannelEvents.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel notice received"),
    /** Channel mode notice received. @since 0.6.3m2 */
    CHANNEL_MODE_NOTICE(ChannelEvents.CHANNEL_SOURCED_EVENT_WITH_CHARARG, "Channel mode notice received"),
    
    /** Channel CTCP received. */
    CHANNEL_CTCP(ChannelEvents.CHANNEL_CTCP, "Channel CTCP received"),
    
    /** Someone joined a channel. */
    CHANNEL_JOIN(ChannelEvents.CHANNEL_SOURCED_EVENT, "Someone joined a channel"),
    /** Someone left a channel. */
    CHANNEL_PART(ChannelEvents.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone left a channel"),
    /** Someone quit. */
    CHANNEL_QUIT(ChannelEvents.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone quit IRC"),
    /** Someone was kicked. */
    CHANNEL_KICK(ChannelEvents.CHANNEL_SOURCED_EVENT_WITH_VICTIM, "Someone kicked someone"),
    
    /** Someone marked as away. */
    CHANNEL_USERAWAY(ChannelEvents.CHANNEL_SOURCED_EVENT, "Someone is marked as 'away'"),
    /** Someone marked as back. */
    CHANNEL_USERBACK(ChannelEvents.CHANNEL_SOURCED_EVENT, "Someone is marked as 'back'"),
    
    /** Channel mode discovered. */
    CHANNEL_MODESDISCOVERED(ChannelEvents.CHANNEL_MODEEVENT, "Channel modes discovered"),
    /** Channel mode changes. */
    CHANNEL_MODECHANGE(ChannelEvents.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone changed channel modes"),
    /** Someone changed someone else's user modes. */
    CHANNEL_USERMODECHANGE(ChannelEvents.CHANNEL_SOURCED_EVENT_WITH_VICTIM, "Someone changed someone else's modes"),
    
    /** Someone changed nickname. */
    CHANNEL_NICKCHANGE(ChannelEvents.CHANNEL_NICKEVENT, "Someone changed nicknames"),
    
    /** Someone changed a topic. */
    CHANNEL_TOPICCHANGE(ChannelEvents.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone changed channel topic"),
    
    /** Query opened. */
    QUERY_OPENED(QueryEvents.QUERY_EVENT, "Query window opened"),
    /** Query closed. */
    QUERY_CLOSED(QueryEvents.QUERY_EVENT, "Query window closed"),
    /** Query message received. */
    QUERY_MESSAGE(QueryEvents.QUERY_EVENT_WITH_ARG, "Private message received"),
    /** Query action received. */
    QUERY_ACTION(QueryEvents.QUERY_EVENT_WITH_ARG, "Private action received"),
    /** Query message sent. */
    QUERY_SELF_MESSAGE(QueryEvents.QUERY_EVENT_WITH_ARG, "Private message sent"),
    /** Query action sent. */
    QUERY_SELF_ACTION(QueryEvents.QUERY_EVENT_WITH_ARG, "Private action sent"),
    /** Query quit event. */
    QUERY_QUIT(QueryEvents.QUERY_EVENT_WITH_ARG, "Query: user quit"),
    /** Query nick change. */
    QUERY_NICKCHANGE(QueryEvents.QUERY_EVENT_WITH_ARG, "Query: user changed nicks"),
    
    /** Plugin loaded. */
    PLUGIN_LOADED(PluginEvents.PLUGIN_EVENT, "Plugin loaded"),
    /** Plugin unloaded. */
    PLUGIN_UNLOADED(PluginEvents.PLUGIN_EVENT, "Plugin unloaded"),
    
    /** Action created. */
    ACTION_CREATED(ActionEvents.ACTION_EVENT, "Action created"),
    /** Action updated. */
    ACTION_UPDATED(ActionEvents.ACTION_EVENT, "Action updated"),
    /** Action deleted. */
    ACTION_DELETED(ActionEvents.ACTION_DELETED, "Action deleted");
    
    /** The type of this action. */
    private final ActionMetaType type;
    
    /** The name of this action. */
    private final String name;
    
    /**
     * Constructs a new core action.
     * @param type The type of this action
     * @param name The name of this action
     */
    CoreActionType(final ActionMetaType type, final String name) {
        this.type = type;
        this.name = name;
    }
    
    /** {@inheritDoc} */
    @Override
    public ActionMetaType getType() {
        return type;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }
}
