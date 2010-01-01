/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
     * Get the state of this media source
     *
     * @return State for this media source.
     * @since 0.6.3m1
     */
    MediaSourceState getState();
    
    /**
     * Retrieves the name of the application that this source is for.
     * 
     * @return This source's application name
     */
    String getAppName();
    
    /**
     * Retrieves the artist of the curently loaded track.
     *
     * @return Current track artist
     */
    String getArtist();
    
    /**
     * Retrieves the title of the currently loaded track.
     *
     * @return Current track title
     */
    String getTitle();
    
    /**
     * Retrieves the album of the currently loaded track.
     *
     * @return Current track album
     */
    String getAlbum();
    
    /**
     * Retrieves the length of the currently loaded track ([h:]mm:ss).
     *
     * @return Current track length
     */
    String getLength();
    
    /**
     * Retrieves the time of the currently loaded track ([h:]mm:ss).
     *
     * @return Current track time
     */
    String getTime();
    
    /**
     * Retrives the format of the currently loaded track.
     *
     * @return Current track format
     */
    String getFormat();
    
    /**
     * Retrieves the bitrate of the currently loaded track.
     *
     * @return Current track bitrate
     */
    String getBitrate();

}
