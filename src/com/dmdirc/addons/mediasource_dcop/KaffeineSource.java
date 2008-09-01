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

package com.dmdirc.addons.mediasource_dcop;

import com.dmdirc.addons.dcop.DcopPlugin;
import com.dmdirc.addons.nowplaying.MediaSource;

/**
 * Uses DCOP to retrieve now playing info from Kaffeine.
 *
 * @author chris
 */
public class KaffeineSource implements MediaSource {
    
    /** Instantiates the media source. */
    public KaffeineSource() {
        //Do nothing
    }
    
    /** {@inheritDoc} */
    public boolean isRunning() {
        return DcopPlugin.getDcopResult("dcop kaffeine KaffeineIface isPlaying").size() > 0;
    }
    
    /** {@inheritDoc} */
    public boolean isPlaying() {
        // This also returns true for paused, which makes it rather useless!
        final String result = DcopPlugin.getDcopResult("dcop kaffeine KaffeineIface isPlaying").get(0);
        
        return Boolean.parseBoolean(result);
    }
    
    /** {@inheritDoc} */
    public boolean isStopped() {
        final String result = DcopPlugin.getDcopResult("dcop kaffeine KaffeineIface isPlaying").get(0);
        
        return Boolean.parseBoolean(result);
    }
    
    /** {@inheritDoc} */
    public String getAppName() {
        return "Kaffeine";
    }
    
    /** {@inheritDoc} */
    public String getArtist() {
        return DcopPlugin.getDcopResult(
                "dcop kaffeine KaffeineIface artist").get(0);
    }

    /** {@inheritDoc} */
    public String getTitle() {
        return DcopPlugin.getDcopResult(
                "dcop kaffeine KaffeineIface title").get(0);
    }

    /** {@inheritDoc} */
    public String getAlbum() {
        return DcopPlugin.getDcopResult(
                "dcop kaffeine KaffeineIface album").get(0);
    }

    /** {@inheritDoc} */
    public String getLength() {
        return duration(Integer.parseInt(DcopPlugin.getDcopResult(
                "dcop kaffeine KaffeineIface getLength").get(0)) /1000);
    }

    /** {@inheritDoc} */
    public String getTime() {
        return duration(Integer.parseInt(DcopPlugin.getDcopResult(
                "dcop kaffeine KaffeineIface getTimePos").get(0)) /1000);
    }

    /** {@inheritDoc} */
    public String getFormat() {
        return null;
    }

    /** {@inheritDoc} */
    public String getBitrate() {
        return null;
    }
    
    /**
     * Get the duration in seconds as a string.
     *
     * @param secondsInput to get duration for
     *
     * @return Duration as a string
     */
    private String duration(final long secondsInput) {
        final StringBuilder result = new StringBuilder();
        final long hours = secondsInput / 3600;
        final long minutes = secondsInput / 60 % 60;
        final long seconds = secondsInput % 60;
        
        if (hours > 0) { 
            result.append(hours).append(":");
        }
        
        result.append(minutes).append(":");
        result.append(seconds).append(":");
        
        return result.toString();
    }
    
}
