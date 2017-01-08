/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.util;

import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.themes.ThemeManager;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * Provides methods for building URLs to reference DMDirc resources.
 */
@Singleton
public class URLBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(URLBuilder.class);
    /** Provider to retrieve a plugin manager instance when needed. */
    private final Provider<PluginManager> pluginManagerProvider;
    /** Provider to retrieve a theme manager instance when needed. */
    private final Provider<ThemeManager> themeManagerProvider;

    /**
     * Creates a new instance of URLBuilder.
     *
     * @param pluginManagerProvider Provider to retrieve a plugin manager instance when needed.
     * @param themeManagerProvider  Provider to retrieve a theme manager instance when needed.
     */
    @Inject
    public URLBuilder(
            final Provider<PluginManager> pluginManagerProvider,
            final Provider<ThemeManager> themeManagerProvider) {
        this.pluginManagerProvider = pluginManagerProvider;
        this.themeManagerProvider = themeManagerProvider;
    }

    /**
     * Constructs an URL pointing to the specified resource on the file system.
     *
     * @param path The path that the URL is for
     *
     * @return An URL corresponding to the specified path, or null on failure
     */
    public URL getUrlForFile(final String path) {
        final String prefix = path.startsWith("file:/") ? "" : "file://";

        try {
            return new URL(prefix + path);
        } catch (MalformedURLException ex) {
            LOG.error(USER_ERROR, "Unable to build file URL", ex);
            return null;
        }
    }

    /**
     * Constructs an URL pointing to the specified resource within a jar file.
     *
     * @param jarFile Path to the jar file (including scheme)
     * @param path    Path to the resource within the jar file
     *
     * @return An URL corresponding to the specified resource, or null on failure
     */
    public URL getUrlForJarFile(final String jarFile, final String path) {
        try {
            String url = "jar:" + getUrl(jarFile) + "!/" + path;
            if (url.startsWith("jar:file://")) {
                url = "jar:file:/" + url.substring(11);
            }
            return new URL(url);
        } catch (MalformedURLException ex) {
            LOG.error(USER_ERROR, "Unable to build jar URL", ex);
            return null;
        }
    }

    /**
     * Constructs an URL pointing to the specified resource within the DMDirc project.
     *
     * @param resource The path to the resource
     *
     * @return An URL corresponding to the specified resource
     */
    public URL getUrlForDMDircResource(final String resource) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        return classLoader.getResource(resource);
    }

    /**
     * Builds an URL pointing to a resource within a DMDirc theme.
     *
     * @param theme The theme which the resource is located in
     * @param path  The path within the theme of the resource
     *
     * @return An URL corresponding to the specified resource, or null on failure
     */
    public URL getUrlForThemeResource(final String theme, final String path) {
        return getUrlForJarFile(themeManagerProvider.get().getDirectory()
                + theme + ".zip", path);
    }

    /**
     * Builds an URL pointing to a resource within a DMDirc plugin.
     *
     * @param plugin The plugin which the resource is located in
     * @param path   The path within the theme of the resource
     *
     * @return An URL corresponding to the specified resource, or null on failure
     */
    public URL getUrlForPluginResource(final String plugin, final String path) {
        return getUrlForJarFile(
                pluginManagerProvider.get().getPluginInfoByName(plugin)
                .getMetaData().getPluginPath().toString(), path);
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
    public URL getUrl(final String spec) {
        if (spec.startsWith("dmdirc://")) {
            return getUrlForDMDircResource(spec.substring(9));
        } else if (spec.startsWith("jar://") || spec.startsWith("zip://")) {
            final int offset = spec.indexOf(':', 6);

            if (offset < 0) {
                LOG.info(USER_ERROR, "Invalid URL, must contain ':': {}", spec);
                return null;
            } else {
                return getUrlForJarFile(spec.substring(6, offset), spec.substring(offset + 1));
            }
        } else if (spec.startsWith("plugin://")) {
            final int offset = spec.indexOf(':', 8);

            if (offset < 0) {
                LOG.info(USER_ERROR, "Invalid URL, must contain ':': {}", spec);
                return null;
            } else {
                return getUrlForPluginResource(spec.substring(9, offset), spec.substring(offset + 1));
            }
        } else if (spec.startsWith("theme://")) {
            final int offset = spec.indexOf(':', 8);

            if (offset < 0) {
                LOG.info(USER_ERROR, "Invalid URL, must contain ':': {}", spec);
                return null;
            } else {
                return getUrlForThemeResource(spec.substring(8, offset), spec.substring(offset + 1));
            }
        } else if (spec.startsWith("http://") || spec.startsWith("https://")) {
            try {
                return new URL(spec);
            } catch (MalformedURLException ex) {
                LOG.info(USER_ERROR, "Unable to load resource", ex);
                return null;
            }
        } else {
            return getUrlForFile(spec);
        }
    }

}
