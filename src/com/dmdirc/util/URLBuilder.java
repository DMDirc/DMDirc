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

package com.dmdirc.util;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.themes.ThemeManager;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides methods for building URLs to reference DMDirc resources.
 * 
 * @author chris
 */
public final class URLBuilder {
    
    /**
     * Creates a new instance of URLBuilder.
     */
    private URLBuilder() {
        // Shouldn't be constructed
    }

    /**
     * Constructs an URL pointing to the specified resource on the file system.
     * 
     * @param path The path that the URL is for
     * @return An URL corresponding to the specified path, or null on failure
     */
    public static URL buildFileURL(final String path) {
        final String prefix = path.startsWith("file://") ? "" : "file://";
        
        try {
            return new URL(prefix + path);
        } catch (MalformedURLException ex) {
            Logger.appError(ErrorLevel.HIGH, "Unable to build file URL", ex);
            return null;
        }
    }
    
    /**
     * Constructs an URL pointing to the specified resource within a jar file.
     * 
     * @param jarFile Path to the jar file (including scheme)
     * @param path Path to the resource within the jar file
     * @return An URL corresponding to the specified resource, or null on failure
     */
    public static URL buildJarURL(final String jarFile, final String path) {
        try {
            String url = "jar:" + buildURL(jarFile) + "!/" + path;
            if (url.startsWith("jar:file://")) {
                url = "jar:file:/" + url.substring(11);
            }
            return new URL(url);
        } catch (MalformedURLException ex) {
            Logger.appError(ErrorLevel.HIGH, "Unable to build jar URL", ex);
            return null;
        }        
    }
    
    /**
     * Constructs an URL pointing to the specified resource within the DMDirc
     * project.
     * 
     * @param resource The path to the resource
     * @return An URL corresponding to the specified resource
     */
    public static URL buildDMDircURL(final String resource) {
        return Thread.currentThread().getContextClassLoader().getResource(resource);
    }
    
    /**
     * Builds an URL pointing to a resource within a DMDirc theme.
     * 
     * @param theme The theme which the resource is located in
     * @param path The path within the theme of the resource
     * @return An URL corresponding to the specified resource, or null on failure
     */
    public static URL buildThemeURL(final String theme, final String path) {
        return buildJarURL(ThemeManager.getThemeDirectory() + theme + ".zip", path);
    }
    
    /**
     * Builds an URL pointing to a resource within a DMDirc plugin.
     * 
     * @param plugin The plugin which the resource is located in
     * @param path The path within the theme of the resource
     * @return An URL corresponding to the specified resource, or null on failure
     */
    public static URL buildPluginURL(final String plugin, final String path) {
        return buildJarURL(
                PluginManager.getPluginManager().getPluginInfoByName(plugin).getFullFilename(),
                path);
    }
    
    /**
     * Constructs an URL corresponding to the described resource.
     * 
     * @param spec The resource location. May take the form of: <ul>
     * <li>dmdirc://com/dmdirc/etc/
     * <li>jar://path/to/jarfile:path/inside/jarfile
     * <li>zip://path/to/zipfile:path/inside/zipfile
     * <li>theme://theme_name:file/inside/theme
     * <li>plugin://plugin_name:file/inside/plugin
     * <li>http://server/path
     * <li>https://server/path
     * <li>[file://]/path/on/filesystem</ul>
     * 
     * @return An URL corresponding to the specified resource, or null on failure
     */
    public static URL buildURL(final String spec) {
        if (spec.startsWith("dmdirc://")) {
            return buildDMDircURL(spec.substring(9));
        } else if (spec.startsWith("jar://") || spec.startsWith("zip://")) {
            final int offset = spec.indexOf(':', 6);
            
            if (offset < 0) {
                Logger.userError(ErrorLevel.LOW, "Invalid URL, must contain ':': " + spec);
                return null;
            } else {
                return buildJarURL(spec.substring(6, offset), spec.substring(offset + 1));
            }
        } else if (spec.startsWith("plugin://")) {
            final int offset = spec.indexOf(':', 8);
            
            if (offset < 0) {
                Logger.userError(ErrorLevel.LOW, "Invalid URL, must contain ':': " + spec);
                return null;
            } else {
                return buildPluginURL(spec.substring(9, offset), spec.substring(offset + 1));
            }            
        } else if (spec.startsWith("theme://")) {
            final int offset = spec.indexOf(':', 8);
            
            if (offset < 0) {
                Logger.userError(ErrorLevel.LOW, "Invalid URL, must contain ':': " + spec);
                return null;
            } else {
                return buildThemeURL(spec.substring(8, offset), spec.substring(offset + 1));
            }
        } else if (spec.startsWith("http://") || spec.startsWith("https://")) {
            try {
                return new URL(spec);
            } catch (MalformedURLException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to load resource", ex);
                return null;
            }
        } else {
            return buildFileURL(spec);
        }
    }
}
