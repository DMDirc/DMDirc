/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.swing.textpane;

import java.awt.event.MouseEvent;
import java.util.EventListener;

/**
 * Interface for events on the TextPane.
 */
public interface TextPaneListener extends EventListener {
    
    /** 
     * Fired when a hyperlink is clicked. 
     *
     * @param url Hyperlink that was clicked on
     * @param event Triggering Event
     */
    void hyperlinkClicked(final String url, final MouseEvent event);
    
    /**
     * Fired when a channel is clicked.
     *
     * @param channel Name of the channel clicked on
     * @param event Triggering Event
     */
    void channelClicked(final String channel, final MouseEvent event);
    
    /**
     * Fired when a channel is clicked.
     *
     * @param nickname Name of the channel clicked on
     * @param event Triggering Event
     */
    void nickNameClicked(final String nickname, final MouseEvent event);
    
}
