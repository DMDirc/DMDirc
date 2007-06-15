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

package com.dmdirc.actions;

/**
 * An enumeration of actions that are raised by the core.
 * @author chris
 */
public enum CoreActionType implements ActionType {
    
    /** Client opened. */
    CLIENT_OPENED(CoreActionMetaType.CLIENT_EVENT, "Client opened"),
    /** Client closed. */
    CLIENT_CLOSED(CoreActionMetaType.CLIENT_EVENT, "Client closed"),
    
    /** Frame changed. */
    CLIENT_FRAME_CHANGED(CoreActionMetaType.CLIENT_EVENT_WITH_FRAME, "Frame changed"),
    
    /** Plugin loaded. */
    PLUGIN_LOADED(CoreActionMetaType.PLUGIN_EVENT, "Plugin loaded"),
    /** Plugin unloaded. */
    PLUGIN_UNLOADED(CoreActionMetaType.PLUGIN_EVENT, "Plugin unloaded"),
    /** Plugin activated. */
    PLUGIN_ACTIVATED(CoreActionMetaType.PLUGIN_EVENT, "Plugin activated"),
    /** Plugin deactivated. */
    PLUGIN_DEACTIVATED(CoreActionMetaType.PLUGIN_EVENT, "Plugin deactivated"),
    
    /** Unknown command. */
    UNKNOWN_COMMAND(CoreActionMetaType.UNKNOWN_COMMAND, "Unknown command entered"),
    
    /** Server numeric received. */
    SERVER_NUMERIC(CoreActionMetaType.SERVER_NUMERIC, "Numeric event received"),
    /** Server connected. */
    SERVER_CONNECTED(CoreActionMetaType.SERVER_EVENT, "Server connected"),
    /** Marked as away. */
    SERVER_BACK(CoreActionMetaType.SERVER_EVENT, "Marked as 'back'"),
    /** Marked as back. */
    SERVER_AWAY(CoreActionMetaType.SERVER_EVENT_WITH_ARG, "Marked as 'away'"),
    /** Auth notice received. */
    SERVER_AUTHNOTICE(CoreActionMetaType.SERVER_EVENT_WITH_ARG, "Received auth notice"),
    
    /** User modes changed. */
    SERVER_USERMODES(CoreActionMetaType.SERVER_SOURCED_EVENT_WITH_ARG, "User modes changed"),
    
    /** Ping reply received. */
    SERVER_GOTPING(CoreActionMetaType.SERVER_PING, "Received server ping reply"),
    /** Missed server ping reply. */
    SERVER_NOPING(CoreActionMetaType.SERVER_PING, "Missed server ping reply"),
    
    /** Query opened. */
    QUERY_OPENED(CoreActionMetaType.QUERY_EVENT, "Query window opened"),
    /** Query closed. */
    QUERY_CLOSED(CoreActionMetaType.QUERY_EVENT, "Query window closed"),
    /** Query message received. */
    QUERY_MESSAGE(CoreActionMetaType.QUERY_EVENT_WITH_ARG, "Private message received"),
    /** Query action received. */
    QUERY_ACTION(CoreActionMetaType.QUERY_EVENT_WITH_ARG, "Private action received"),
    /** Query message sent. */
    QUERY_SELF_MESSAGE(CoreActionMetaType.QUERY_EVENT_WITH_ARG, "Private message sent"),
    /** Query action sent. */
    QUERY_SELF_ACTION(CoreActionMetaType.QUERY_EVENT_WITH_ARG, "Private action sent"),
    
    /** Channel window opened. */
    CHANNEL_OPENED(CoreActionMetaType.CHANNEL_EVENT, "Channel window opened"),
    /** Channel window closed. */
    CHANNEL_CLOSED(CoreActionMetaType.CHANNEL_EVENT, "Channel window closed"),
    /** Names reply received. */
    CHANNEL_GOTNAMES(CoreActionMetaType.CHANNEL_EVENT, "Channel names reply received"),
    /** Channel topic received. */
    CHANNEL_GOTTOPIC(CoreActionMetaType.CHANNEL_EVENT, "Channel topic received"),
    
    /** Channel message sent. */
    CHANNEL_SELF_MESSAGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel message sent"),
    /** Channel action sent. */
    CHANNEL_SELF_ACTION(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel action sent"),

    /** Channel message received. */
    CHANNEL_MESSAGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel message received"),
    /** Channel actions received. */
    CHANNEL_ACTION(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel action received"),

    /** Someone joined a channel. */
    CHANNEL_JOIN(CoreActionMetaType.CHANNEL_SOURCED_EVENT, "Someone joined a channel"),
    /** Someone left a channel. */
    CHANNEL_PART(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone left a channel"),
    /** Someone quit. */
    CHANNEL_QUIT(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone quit IRC"),
    /** Someone was kicked. */
    CHANNEL_KICK(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_VICTIM, "Someone kicked someone"),
    
    /** Someone marked as away. */
    CHANNEL_USERAWAY(CoreActionMetaType.CHANNEL_SOURCED_EVENT, "Someone is marked as 'away'"),
    /** Someone marked as back. */
    CHANNEL_USERBACK(CoreActionMetaType.CHANNEL_SOURCED_EVENT, "Someone is marked as 'back'"),
    
    /** Channel mode changes. */
    CHANNEL_MODECHANGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone changed channel modes"),
    /** Someone changed someone else's user modes. */
    CHANNEL_USERMODECHANGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_VICTIM, "Someone changed someone else's modes"),

    /** Someone changed nickname. */
    CHANNEL_NICKCHANGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone changed nicknames"),

    /** Someone changed a topic. */
    CHANNEL_TOPICCHANGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone changed channel topic");
    
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
    public ActionMetaType getType() {
        return type;
    }
    
    /** {@inheritDoc} */
    public String getName() {
        return name;
    }
    
}
