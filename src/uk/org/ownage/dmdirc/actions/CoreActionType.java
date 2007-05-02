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
    
    SERVER_CONNECTED(CoreActionMetaType.SERVER_EVENT),
    SERVER_BACK(CoreActionMetaType.SERVER_EVENT),
    SERVER_AWAY(CoreActionMetaType.SERVER_EVENT_WITH_ARG),
    
    QUERY_OPENED(CoreActionMetaType.QUERY_EVENT),
    QUERY_MESSAGE(CoreActionMetaType.QUERY_EVENT_WITH_ARG),
    QUERY_ACTION(CoreActionMetaType.QUERY_EVENT_WITH_ARG),
    QUERY_SELF_MESSAGE(CoreActionMetaType.QUERY_EVENT_WITH_ARG),
    QUERY_SELF_ACTION(CoreActionMetaType.QUERY_EVENT_WITH_ARG),    
    
    CHANNEL_OPENED(CoreActionMetaType.CHANNEL_EVENT),
    CHANNEL_GOTNAMES(CoreActionMetaType.CHANNEL_EVENT),
    CHANNEL_GOTTOPIC(CoreActionMetaType.CHANNEL_EVENT),
    
    CHANNEL_SELF_MESSAGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG),
    CHANNEL_SELF_ACTION(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG),
    
    CHANNEL_MESSAGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG),
    CHANNEL_ACTION(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG),
    
    CHANNEL_JOIN(CoreActionMetaType.CHANNEL_SOURCED_EVENT),
    CHANNEL_PART(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG),
    CHANNEL_QUIT(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG),
    CHANNEL_KICK(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_VICTIM),
    
    CHANNEL_MODECHANGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG),
    
    CHANNEL_NICKCHANGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG),
    
    CHANNEL_TOPICCHANGE(CoreActionMetaType.CHANNEL_SOURCED_EVENT_WITH_ARG);
    
    /** The type of this action. */
    private final CoreActionMetaType type;
    
    /**
     * Constructs a new core action.
     * @param type The type of this action
     */
    CoreActionType(CoreActionMetaType type) {
        this.type = type;
    }
    
    /**
     * Retrieves the type of this action.
     * @return This action's type
     */
    public CoreActionMetaType getType() {
        return type;
    }
    
}
