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

package com.dmdirc.commandparser;

/**
 * An enumeration of the types of popup menu which are supported by the
 * PopupManager.
 * 
 * @author chris
 */
public enum PopupType {
    
    /**
     * The menu that appears when right clicking in a channel's nicklist.
     * 
     * Expected arguments: the nickname of the user who was clicked on.
     */
    CHAN_NICKLIST("nick", "chan-nick", "chan-nick-list"),
    
    /**
     * The menu that appears when right clicking on a nickname in normal channel text.
     * 
     * Expected arguments: the nickname of the user who was clicked on.
     */
    CHAN_NICKTEXT("nick", "chan-nick", "chan-nick-text"),
    
    /**
     * The menu that appears when right clicking in a channel window.
     * 
     * Expected arguments: none.
     */
    CHAN_CHAN("chan"),
    
    /**
     * The menu that appears when right clicking in a query window.
     * 
     * Expected arguments: the nickname of the user who the query is with.
     */
    QUERY_QUERY("nick", "query");
    
    private final String[] domains;
    
    /**
     * Instansiates one of the PopupTypes.
     * 
     * @param domains The configuration domains used for the type
     */
    PopupType(final String ... domains) {
        this.domains = domains;
    }
    
    /**
     * Retrieve a list of the domains that should be used by this type.
     * 
     * @return This type's domains
     */
    public String[] getDomains() {
        return domains.clone();
    }
    
}
