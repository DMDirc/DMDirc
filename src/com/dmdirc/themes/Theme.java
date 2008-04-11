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

package com.dmdirc.themes;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.ConfigFile;
import com.dmdirc.util.InvalidConfigFileException;
import com.dmdirc.util.resourcemanager.ZipResourceManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents one theme file.
 *
 * @author Chris
 */
public class Theme implements Comparable<Theme> {

    /** The file to load the theme from. */
    private final File file;

    /** The config file containing theme meta-data. */
    private ConfigFile metadata;

    /** The resource manager we're using for this theme. */
    private ZipResourceManager rm;

    /** Whether or not this theme is enabled. */
    private boolean enabled;
    
    /** The Identity we've registered. */
    private ThemeIdentity identity;

    /**
     * Creates a new instance of Theme.
     *
     * @param file The file to load the theme from
     */
    public Theme(final File file) {
        this.file = file;
    }

    /**
     * Determines if this theme is valid or not (i.e., it is a valid zip file,
     * and it contains one file).
     *
     * @return True if the theme is valid, false otherwise
     */
    public boolean isValidTheme() {
        if (rm == null) {
            try {
                rm = ZipResourceManager.getInstance(file.getCanonicalPath());
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "I/O error when loading theme: "
                        + file.getAbsolutePath() + ": " + ex.getMessage());

                return false;
            }

            if (rm != null && rm.getResourceInputStream("theme.config") != null) {
                metadata = new ConfigFile(rm.getResourceInputStream("theme.config"));
                
                try {
                    metadata.read();
                } catch (IOException ex) {
                    metadata = null;
                } catch (InvalidConfigFileException ex) {
                    metadata = null;
                }
            }
        }

        return rm != null && rm.getResourceInputStream("config") != null;
    }

    /**
     * Applies this theme to the client.
     */
    public void applyTheme() {
        if (!isValidTheme() || rm == null || enabled) {
            return;
        }

        enabled = true;

        final InputStream stream = rm.getResourceInputStream("config");

        if (stream != null) {
            try {
                identity = new ThemeIdentity(stream);
                IdentityManager.addIdentity(identity);
            } catch (InvalidIdentityFileException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Error loading theme identity file: "
                        + ex.getMessage());
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Error loading theme identity file: "
                        + ex.getMessage());
            }
        }
    }
    
    /**
     * Removes the effects of this theme.
     */
    public void removeTheme() {
        if (!isValidTheme() || !enabled || identity == null) {
            return;
        }
        
        IdentityManager.removeIdentity(identity);
    }

    /**
     * Determines if this theme is enabled or not.
     *
     * @return True if the theme is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Retrieves the name of this theme.
     *
     * @return This theme's name
     */
    public String getFileName() {
        return file.getName();
    }

    /**
     * Retrieves the name of this theme.
     * 
     * @return This theme's name
     */    
    public String getName() {
        return getMetaData("name", getFileName());
    }

    /**
     * Retrieves the version of this theme.
     * 
     * @return This theme's version
     */    
    public String getVersion() {
        return getMetaData("version", "?");
    }

    /**
     * Retrieves the author of this theme.
     * 
     * @return This theme's author
     */    
    public String getAuthor() {
        return getMetaData("author", "?");
    }

    /**
     * Retrieves the description of this theme.
     * 
     * @return This theme's description
     */
    public String getDescription() {
        return getMetaData("description", "?");
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(final Theme o) {
        return getName().compareTo(o.getName());
    }

    /**
     * Attempts to read the specified key from the 'data' keysection of the
     * theme's config file.
     * 
     * @param key The key to be read
     * @param fallback The value to use if the file, section or entry doesn't
     * exist
     * @return The relevant meta-data, or the fallback value
     */
    private String getMetaData(final String key, final String fallback) {
        if (metadata == null || !metadata.isKeyDomain("data") ||
                !metadata.getKeyDomain("data").containsKey(key)) {
            return fallback;
        }
        
        return metadata.getKeyDomain("data").get(key);
    }

}
