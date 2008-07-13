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
    public boolean isRunning() {
        final List<String> res = DcopPlugin.getDcopResult("dcop amarok player status");
        return res.size() > 0 && !res.get(0).equals("0");
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isPlaying() {
        final String result = DcopPlugin.getDcopResult(
                "dcop amarok player isPlaying").get(0);
        
        return Boolean.parseBoolean(result);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAppName() {
        return "Amarok";
    }
    
    /** {@inheritDoc} */
    @Override
    public String getArtist() {
        return DcopPlugin.getDcopResult("dcop amarok player artist").get(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return DcopPlugin.getDcopResult("dcop amarok player title").get(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAlbum() {
        return DcopPlugin.getDcopResult("dcop amarok player album").get(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getLength() {
        return DcopPlugin.getDcopResult("dcop amarok player totalTime").get(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getTime() {
        return DcopPlugin.getDcopResult(
                "dcop amarok player currentTime").get(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getFormat() {
        return DcopPlugin.getDcopResult("dcop amarok player type").get(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getBitrate() {
        return DcopPlugin.getDcopResult("dcop amarok player bitrate").get(0);
    }
    
}
