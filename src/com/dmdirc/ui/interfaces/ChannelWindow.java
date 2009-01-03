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

package com.dmdirc.ui.interfaces;

import com.dmdirc.Channel;
import com.dmdirc.parser.irc.ChannelClientInfo;

import java.util.List;

/**
 * Specifies the standard interface for a channel window.
 * 
 * @author Chris
 */
public interface ChannelWindow extends InputWindow {
    
    /**
     * Updates the channel's name list to the specified list of clients.
     * 
     * @param clients The new list of clients for this channel
     */
    void updateNames(List<ChannelClientInfo> clients);
    
    /**
     * Adds the specified client to this channel's name list.
     * 
     * @param client The new client to be added
     */
    void addName(ChannelClientInfo client);
    
    /**
     * Removes the specified client from this channel's name list.
     * 
     * @param client The client to be removed
     */
    void removeName(ChannelClientInfo client);
    
    /**
     * Requests that the channel window updates the displayed list of channel
     * clients, to take into account mode or nickname changes.
     */
    void updateNames();
    
    /**
     * Returns the channel associated with this window.
     * 
     * @return Associated Channel
     */
    Channel getChannel();
    
    /**
     * Redraws the nicklist belonging to this channel.
     */
    void redrawNicklist();

}
