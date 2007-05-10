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

package uk.org.ownage.dmdirc.actions;

/**
 * An enumeration of actions that are raised by the core.
 * @author chris
 */
public enum CoreActionType implements ActionType {
    
    UNKNOWN_COMMAND(CoreActionMetaType.UNKNOWN_COMMAND, "Unknown command entered"),
    
    SERVER_CONNECTED(CoreActionMetaType.SERVER_EVENT, "Server connected"),
    SERVER_BACK(CoreActionMetaType.SERVER_EVENT, "Marked as 'back'"),
    SERVER_AWAY(CoreActionMetaType.SERVER_EVENT_WITH_ARG, "Marked as 'away'"),
    
    SERVER_GOTPING(CoreActionMetaType.SERVER_PING, "Received server ping reply"),
    SERVER_NOPING(CoreActionMetaType.SERVER_PING, "Missed server ping reply"),
    
    QUERY_OPENED(CoreActionMetaType.QUERY_EVENT, "Query window opened"),
    QUERY_CLOSED(CoreActionMetaType.QUERY_EVENT, "Query window closed"),
    QUERY_MESSAGE(CoreActionMetaType.QUERY_EVENT_WITH_ARG, "Private message received"),
    QUERY_ACTION(CoreActionMetaType.QUERY_EVENT_WITH_ARG, "Private action received"),
    QUERY_SELF_MESSAGE(CoreActionMetaType.QUERY_EVENT_WITH_ARG, "Private message sent"),
    QUERY_SELF_ACTION(CoreActionMetaType.QUERY_EVENT_WITH_ARG, "Private action sent"),
    
    CHANNEL_OPENED(CoreActionMetaType.CHANNEL_EVENT, "Channel window opened"),
    CHANNEL_CLOSED(CoreActionMetaType.CHANNEL_EVENT, "Channel window closed"),
    CHANNEL_GOTNAMES(CoreActionMetaType.CHANNEL_EVENT, "Channel names reply received"),
    CHANNEL_GOTTOPIC(CoreActionMetaType.CHANNEL_EVENT, "Channel topic received"),
    
    CHANNEL_SELF_MESSAGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel message sent"),
    CHANNEL_SELF_ACTION(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel action sent"),
    
    CHANNEL_MESSAGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel message received"),
    CHANNEL_ACTION(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Channel action received"),
    
    CHANNEL_JOIN(CoreActionMetaType.CHANNEL_SOURCED_EVENT, "Someone joined a channel"),
    CHANNEL_PART(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone left a channel"),
    CHANNEL_QUIT(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone quit IRC"),
    CHANNEL_KICK(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_VICTIM, "Someone kicked someone"),
    
    CHANNEL_USERAWAY(CoreActionMetaType.CHANNEL_SOURCED_EVENT, "Someone is marked as 'away'"),
    CHANNEL_USERBACK(CoreActionMetaType.CHANNEL_SOURCED_EVENT, "Someone is marked as 'back'"),
    
    CHANNEL_MODECHANGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone changed channel modes"),
    
    CHANNEL_NICKCHANGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone changed nicknames"),
    
    CHANNEL_TOPICCHANGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG, "Someone changed channel topic");
    
    /** The type of this action. */
    private final ActionMetaType type;
    
    /** The name of this action. */
    private final String name;
    
    /**
     * Constructs a new core action.
     * @param type The type of this action
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
    
    /** {@inheritDoc} */
    public String toString() {
        return getName();
    }
    
}
