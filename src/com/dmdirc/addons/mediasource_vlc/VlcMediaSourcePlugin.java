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

package com.dmdirc.addons.mediasource_vlc;

import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.util.Downloader;

import java.io.IOException;
import java.io.File;
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
        return information.containsKey("artist") ? information.get("artist") :
                getFallbackArtist();
    }
     
    private String getFallbackArtist() {
        String result = "unknown";
        
        if (information.containsKey("playlist_current")) {
            try {
                final int item = Integer.parseInt(information.get("playlist_current"));
                String[] bits = information.get("playlist_item_" + item).split((File.separatorChar=='\\' ? "\\\\" : File.separator));
                result = bits[bits.length-1];
                bits = result.split("-");
                if (bits.length > 1) {
                    result = bits[0];
                } else {
                    // Whole filename is the title, so no artist is known.
                    result = "unknown";
                }
            } catch (NumberFormatException nfe) {
                // DO nothing
            }
        }
        
        return result;
    }

    /** {@inheritDoc} */
    @Override    
    public String getTitle() {
        return information.containsKey("title") ? information.get("title")
                : getFallbackTitle();
    }
    
    private String getFallbackTitle() {
        String result = "unknown";
        
        // Title is unknown, lets guess using the filename
        if (information.containsKey("playlist_current")) {
            try {
                final int item = Integer.parseInt(information.get("playlist_current"));
                result = information.get("playlist_item_" + item);
                
                final int sepIndex = result.lastIndexOf(File.separatorChar);
                final int extIndex = result.lastIndexOf('.');
                result = result.substring(sepIndex,
                        extIndex > sepIndex ? extIndex : result.length());
                
                int offset = result.indexOf('-');
                if (offset > -1) {
                    result = result.substring(offset + 1).trim();
                }
            } catch (NumberFormatException nfe) {
                // Do nothing
            }
        }
        
        return result;
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
    public void showConfig(final PreferencesManager manager) {
        final PreferencesCategory general = new PreferencesCategory("VLC Media Source", "");
        final PreferencesCategory instr = new PreferencesCategory("Instructions",
                "", new InstructionsPanel());
        
        general.addSetting(new PreferencesSetting(PreferencesType.TEXT, 
                "plugin-vlc", "host", "localhost:8082", "Hostname and port",
                "The host and port that VLC listens on for web connections"));
        
        manager.getCategory("Plugins").addSubCategory(general);
        general.addSubCategory(instr.setInline());
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
        
        boolean isPlaylist = false;
        boolean isCurrent = false;
        boolean isItem = false;
        int playlistItem = 0;
        for (String line : res2) {
            final String tline = line.trim();
            
            if (isPlaylist) {
                if (tline.startsWith("</ul>")) {
                    isPlaylist = false;
                    information.put("playlist_items", Integer.toString(playlistItem));
                } else if (tline.equalsIgnoreCase("<strong>")) {
                    isCurrent = true;
                } else if (tline.equalsIgnoreCase("</strong>")) {
                    isCurrent = false;
                } else if (tline.startsWith("<a href=\"?control=play&amp")) {
                    isItem = true;
                } else if (isItem) {
                    String itemname = tline;
                    if (itemname.endsWith("</a>")) {
                        itemname = itemname.substring(0, itemname.length()-4);
                    }
                    if (!itemname.equals("")) {
                        if (isCurrent) {
                            information.put("playlist_current", Integer.toString(playlistItem));
                        }
                        information.put("playlist_item_"+Integer.toString(playlistItem++), itemname);
                    }
                    isItem = false;
                }
            } else if (tline.equalsIgnoreCase("<!-- Playlist -->")) {
                isPlaylist = true;
            } else if (tline.startsWith("State:")) {
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
