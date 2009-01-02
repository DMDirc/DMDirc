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

package com.dmdirc.commandparser;

/**
 * An enumeration of the types of popup menu which are supported by the
 * PopupManager.
 * 
 * @author chris
 */
public enum PopupType {
    
    /**
     * The menu that appears when right clicking in a channel window.
     * 
     * Expected arguments: none.
     */
    CHAN_NORMAL,
    
    /**
     * The menu that appears when right clicking in a nickname in a channel window.
     * 
     * Expected arguments: the nickname of the user who was clicked on.
     */
    CHAN_NICK,
    
    /**
     * The menu that appears when right clicking in a channel in a channel window.
     * 
     * Expected arguments: the nickname of the user who was clicked on.
     */
    CHAN_CHANNEL,
    
    /**
     * The menu that appears when right clicking in a hyperlink in a channel window.
     * 
     * Expected arguments: the hyperlink clicked.
     */
    CHAN_HYPERLINK,
    
    /**
     * The menu that appears when right clicking in a query window.
     * 
     * Expected arguments: the nickname of the user who the query is with.
     */
    QUERY_NORMAL,
    
    /**
     * The menu that appears when right clicking in a nickname in a query window.
     * 
     * Expected arguments: the nickname of the user who the query is with.
     */
    QUERY_NICK,
    
    /**
     * The menu that appears when right clicking in a channel in a query window.
     * 
     * Expected arguments: the nickname of the user who the query is with.
     */
    QUERY_CHANNEL,
    
    /**
     * The menu that appears when right clicking in a hyperlink in a query window.
     * 
     * Expected arguments: the hyperlink clicked.
     */
    QUERY_HYPERLINK,
    
    /**
     * The menu that appears when right clicking in a server window.
     * 
     * Expected arguments: the nickname of the user who the query is with.
     */
    SERVER_NORMAL,
    
    /**
     * The menu that appears when right clicking in a nickname in a server window.
     * 
     * Expected arguments: the nickname of the user who the query is with.
     */
    SERVER_NICK,
    
    /**
     * The menu that appears when right clicking in a channel in a server window.
     * 
     * Expected arguments: the nickname of the user who the query is with.
     */
    SERVER_CHANNEL,
    
    /**
     * The menu that appears when right clicking in a hyperlink in a server window.
     * 
     * Expected arguments: the hyperlink clicked.
     */
    SERVER_HYPERLINK;
       
}
