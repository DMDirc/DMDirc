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

package com.dmdirc.addons.mediasource_vlc;

import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.util.Downloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Retrieves information from VLC using its HTTP interface.
 * 
 * @author chris
 */
public class VlcMediaSourcePlugin extends Plugin implements MediaSource {
    
    private final Map<String, String> information
            = new HashMap<String, String>();

    /** {@inheritDoc} */
    @Override
    public boolean isRunning() {
        return getInformation();
    }

    /** {@inheritDoc} */
    @Override    
    public boolean isPlaying() {
        return true;
    }

    /** {@inheritDoc} */
    @Override    
    public String getAppName() {
        return "vlc";
    }

    /** {@inheritDoc} */
    @Override    
    public String getArtist() {
        return information.containsKey("artist") ? information.get("artist")
                : "unknown";
    }

    /** {@inheritDoc} */
    @Override    
    public String getTitle() {
        return information.containsKey("title") ? information.get("title")
                : "unknown";
    }

    /** {@inheritDoc} */
    @Override    
    public String getAlbum() {
        return information.containsKey("album/movie/show title")
                ? information.get("album/movie/show title") : "unknown";
    }

    /** {@inheritDoc} */
    @Override    
    public String getLength() {
        return information.containsKey("duration") ? information.get("duration")
                : "unknown";
    }

    /** {@inheritDoc} */
    @Override    
    public String getTime() {
        return "unknown";
    }

    /** {@inheritDoc} */
    @Override    
    public String getFormat() {
        return information.containsKey("codec") ? information.get("codec")
                : "unknown";
    }

    /** {@inheritDoc} */
    @Override    
    public String getBitrate() {
        return information.containsKey("bitrate") ? information.get("bitrate")
                : "unknown";
    }

    /** {@inheritDoc} */
    @Override    
    public void onLoad() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override    
    public void onUnload() {
        // Do nothing
    }
    
    private boolean getInformation() {
        information.clear();
        List<String> res;
        
        try {
            res = Downloader.getPage("http://" +
                    IdentityManager.getGlobalConfig().getOption("plugin-vlc",
                    "host", "localhost:8082") + "/old/info.html");
        } catch (MalformedURLException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
        
        for (String line : res) {
            final String tline = line.trim();
            
            if (tline.startsWith("<li>")) {
                final int colon = tline.indexOf(':');
                final String key = tline.substring(5, colon).trim().toLowerCase();
                final String value = tline.substring(colon + 1, tline.length() - 5).trim();
                
                information.put(key, value);
            }
        }
        
        return true;
    }

}
