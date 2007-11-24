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

import com.dmdirc.Main;
import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;
import com.dmdirc.util.Downloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Retrieves information from VLC using its HTTP interface.
 * 
 * @author chris
 */
public class VlcMediaSourcePlugin extends Plugin implements MediaSource, PreferencesInterface {
    
    private InstructionsPanel configPanel;
    
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
        return information.get("state").equalsIgnoreCase("playing");
    }

    /** {@inheritDoc} */
    @Override    
    public String getAppName() {
        return "VLC";
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
        // This is just seconds, could do with formatting.
        return information.containsKey("length") ? information.get("length")
                : "unknown";
    }

    /** {@inheritDoc} */
    @Override    
    public String getTime() {
        // This is just seconds, could do with formatting.
        return information.containsKey("time") ? information.get("time")
                : "unknown";
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

    /** {@inheritDoc} */
    @Override
    public boolean isConfigurable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig() {
        final PreferencesPanel preferencesPanel
                = Main.getUI().getPreferencesPanel(this, "VLC Media Source - Config");
        
        configPanel = new InstructionsPanel();
        
        preferencesPanel.addCategory("Setup", "Setup instructions");
        preferencesPanel.replaceOptionPanel("Setup", configPanel);
        preferencesPanel.addCategory("Configuration", "Advanced configuration settings");
        preferencesPanel.addTextfieldOption("Configuration", "host",
                "Hostname and port", "The host and port that VLC listens on for" +
                " web connections", IdentityManager.getGlobalConfig().getOption("plugin-vlc",
                    "host", "localhost:8082"));
        preferencesPanel.display();
    }
    
    /** {@inheritDoc} */
    @Override
    public void configClosed(final Properties properties) {
        IdentityManager.getConfigIdentity().setOption("plugin-vlc", "host",
                properties.getProperty("host"));
    }

    /** {@inheritDoc} */
    @Override
    public void configCancelled() {
        // Do nothing
    }    
    
    private boolean getInformation() {
        information.clear();
        List<String> res;
        List<String> res2;
        
        try {
            res = Downloader.getPage("http://" +
                    IdentityManager.getGlobalConfig().getOption("plugin-vlc",
                    "host", "localhost:8082") + "/old/info.html");
            res2 = Downloader.getPage("http://" +
                    IdentityManager.getGlobalConfig().getOption("plugin-vlc",
                    "host", "localhost:8082") + "/old/");
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
        
        for (String line : res2) {
            final String tline = line.trim();
            
            if (tline.startsWith("State:")) {
                information.put("state", tline.substring(6, tline.indexOf('<')).trim());
            } else if (tline.startsWith("got_")) {
                final int equals = tline.indexOf('=');
                
                information.put(tline.substring(4, equals).trim(),
                        tline.substring(equals + 1, tline.length() - 1).trim());
            }
        }
        
        return true;
    }

}
