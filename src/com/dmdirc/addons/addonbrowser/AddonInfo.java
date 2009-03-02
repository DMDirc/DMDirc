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

package com.dmdirc.addons.addonbrowser;

import com.dmdirc.Main;
import com.dmdirc.updater.UpdateChannel;
import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.util.URLBuilder;

import java.awt.Image;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * Describes an addon.
 * 
 * @author chris
 */
public class AddonInfo {
    
    /**
     * The possible addon types.
     */
    public static enum AddonType {
        /** Plugins. */
        TYPE_PLUGIN,
        /** Themes. */
        TYPE_THEME,
        /** Action packs. */
        TYPE_ACTION_PACK;

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return super.toString().charAt(5) + 
                    super.toString().substring(6).toLowerCase().replace('_', ' ');
        }
    };
    
    private final int id;
    private final String stableDownload;
    private final String unstableDownload;
    private final String title;
    private final String author;
    private final int rating;
    private final String description;
    private final AddonType type;
    private final boolean verified;
    private final int date;
    private final ImageIcon screenshot;

    public AddonInfo(final Map<String, String> entry) {
        this.id = Integer.parseInt(entry.get("id"));
        this.title = entry.get("title");
        this.author = entry.get("user");
        this.rating = Integer.parseInt(entry.get("rating"));
        this.type = entry.get("type").equals("plugin") ?
            AddonType.TYPE_PLUGIN : entry.get("type").equals("theme") ?
                AddonType.TYPE_THEME : AddonType.TYPE_ACTION_PACK;
        this.stableDownload = entry.get("stable");
        this.unstableDownload = entry.get("unstable");
        this.description = entry.get("description");
        this.verified = entry.get("verified").equals("yes");
        this.date = Integer.parseInt(entry.get("date"));
        if (entry.get("screenshot").equals("yes")) {
            this.screenshot = new ImageIcon(URLBuilder.buildURL(
                    "http://addons.dmdirc.com/addonimg/" + id));
            this.screenshot.setImage(this.screenshot.getImage().
                   getScaledInstance(150, 150,Image.SCALE_SMOOTH));
        } else {
            this.screenshot = new ImageIcon(URLBuilder.buildURL(
                    "dmdirc://com/dmdirc/res/logo.png"));
        }
    }

    public String getAuthor() {
        return author;
    }

    public int getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public int getRating() {
        return rating;
    }

    public String getStableDownload() {
        return stableDownload;
    }

    public String getTitle() {
        return title;
    }

    public AddonType getType() {
        return type;
    }

    public String getUnstableDownload() {
        return unstableDownload;
    }

    public boolean isVerified() {
        return verified;
    }

    public ImageIcon getScreenshot() {
        return screenshot;
    }
    
    public boolean isInstalled() {
        for (UpdateComponent comp : UpdateChecker.getComponents()) {
            if (comp.getName().equals("addon-" + getId())) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isDownloadable() {
        return false; /*!stableDownload.isEmpty() ||
                (Main.UPDATE_CHANNEL != UpdateChannel.STABLE
                && !unstableDownload.isEmpty());*/
    }

    public boolean matches(final String text) {
        return title.toLowerCase().indexOf(text.toLowerCase()) > -1
                || description.toLowerCase().indexOf(text.toLowerCase()) > -1;
    }

}
