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

package com.dmdirc.addons.mediasource_dcop;

import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.addons.nowplaying.MediaSourceState;

import java.util.List;

/**
 * Uses DCOP to retrieve now playing info from Amarok.
 *
 * @author chris
 */
public class AmarokSource implements MediaSource {
    
    /** Instantiates the media source. */
    public AmarokSource() {
        //Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public MediaSourceState getState() {
        final List<String> res = DcopMediaSourcePlugin.getDcopResult("dcop amarok player status");
        if (res.size() > 0) {
            final String result = res.get(0).trim();
            try {
                final int status = (Integer.parseInt(result));
                switch (status) {
                    case 0:
                        return MediaSourceState.STOPPED;
                    case 1:
                        return MediaSourceState.PAUSED;
                    case 2:
                        return MediaSourceState.PLAYING;
                    default:
                        return MediaSourceState.NOTKNOWN;
                }
            } catch (NumberFormatException nfe) {
                return MediaSourceState.CLOSED;
            }
        } else {
            return MediaSourceState.CLOSED;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAppName() {
        return "Amarok";
    }
    
    /** {@inheritDoc} */
    @Override
    public String getArtist() {
        return DcopMediaSourcePlugin.getDcopResult("dcop amarok player artist").get(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return DcopMediaSourcePlugin.getDcopResult("dcop amarok player title").get(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAlbum() {
        return DcopMediaSourcePlugin.getDcopResult("dcop amarok player album").get(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getLength() {
        return DcopMediaSourcePlugin.getDcopResult("dcop amarok player totalTime").get(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getTime() {
        return DcopMediaSourcePlugin.getDcopResult(
                "dcop amarok player currentTime").get(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getFormat() {
        return DcopMediaSourcePlugin.getDcopResult("dcop amarok player type").get(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getBitrate() {
        return DcopMediaSourcePlugin.getDcopResult("dcop amarok player bitrate").get(0);
    }
    
}
