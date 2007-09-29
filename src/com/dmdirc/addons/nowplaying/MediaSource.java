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

package com.dmdirc.addons.nowplaying;

/**
 * The media source describes one source of "now playing" information
 * (i.e., one method of getting information from one media player).
 * 
 * @author chris
 */
public interface MediaSource {
    
    /**
     * Determine if the application for this source is running or not.
     * 
     * @return True if this source is running, false otherwise
     */
    boolean isRunning();
    
    /**
     * Determine if this source is currently playing or not.
     * 
     * @return True if this source is playing, false otherwise
     */
    boolean isPlaying();
    
    /**
     * Retrieves a nicely formatted string containing the relevant information
     * about the source media (such as artist and title).
     * 
     * @return The currently playing media
     */
    String getInformation();
    
    /**
     * Retrieves the name of the application that this source is for.
     * 
     * @return This source's application name
     */
    String getName();

}
