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

import java.awt.Color;

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;

/**
 * An ActionComponent represents a component of some object that the user can
 * use as the subject of a condition within an action.
 * @author chris
 */
public enum ActionComponent {
    
    CHANNEL_NAME {
        Object get(final Object argument) { return ((Channel) argument).getChannelInfo().getName(); }
        Class appliesTo() { return Channel.class; }
        Class getType() { return String.class; }
    },
    
    CHANNEL_COLOUR {
        Object get(final Object argument) { return ((Channel) argument).getNotification(); }
        Class appliesTo() { return Channel.class; }
        Class getType() { return Color.class; }
    },    
    
    USER_NAME {
        Object get(final Object argument) { return ((ChannelClientInfo) argument).getNickname(); }
        Class appliesTo() { return ChannelClientInfo.class; }
        Class getType() { return String.class; }
    },
    
    USER_MODES {
        Object get(final Object argument) { return ((ChannelClientInfo) argument).getChanModeStr(false); }
        Class appliesTo() { return ChannelClientInfo.class; }
        Class getType() { return String.class; }        
    },
    
    STRING_STRING {
        Object get(final Object argument) { return argument; }
        Class appliesTo() { return String.class; }
        Class getType() { return String.class; }
    },
    
    STRINGARRAY_LENGTH {
        Object get(final Object argument) { return new Integer(((String[]) argument).length); }
        Class appliesTo() { return String[].class; }
        Class getType() { return Integer.class; }        
    };
    
    /**
     * Retrieves the component of the specified argument that this enum
     * represents.
     * @param argument The object to retrieve the component from
     * @return The relevant component of the object
     */
    abstract Object get(Object argument);
    
    /**
     * Retrieves the type of class that this component applies to.
     * @return The Class that this component can be applied to
     */
    abstract Class appliesTo();
    
    /**
     * Retrieves the type of this component.
     * @return The Class of this component.
     */
    abstract Class getType();
    
}
