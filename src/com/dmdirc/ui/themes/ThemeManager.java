/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.ui.themes;

import com.dmdirc.interfaces.config.IdentityController;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * Manages available themes.
 */
public class ThemeManager {

    private static final Logger LOG = LoggerFactory.getLogger(ThemeManager.class);
    /** The identity controller to read settings from. */
    private final IdentityController identityController;
    /** The directory to look for themes in. */
    private final String themeDirectory;
    /** Available themes. */
    private final Map<String, Theme> themes = new HashMap<>();

    /**
     * Creates a new instance of the {@link ThemeManager}.
     *
     * @param identityController The identity controller to read settings from.
     * @param themesDirectory    The directory to load themes from.
     */
    public ThemeManager(
            final IdentityController identityController,
            final String themesDirectory) {
        this.identityController = identityController;
        this.themeDirectory = themesDirectory;
        identityController.getGlobalConfiguration().addChangeListener("themes", "enabled",
                (domain, key) -> refreshAndLoadThemes());
    }

    /**
     * Scans for available themes and loads any themes that the user has enabled.
     */
    public void refreshAndLoadThemes() {
        final File dir = new File(themeDirectory);

        if (!dir.exists() && !dir.mkdirs()) {
            LOG.error(USER_ERROR, "Could not create themes directory");
        }

        if (dir.listFiles() == null) {
            LOG.error(USER_ERROR, "Unable to load themes");
            return;
        }

        final List<String> enabled = identityController.getGlobalConfiguration()
                .getOptionList("themes", "enabled");

        synchronized (themes) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }

                loadTheme(file, enabled.contains(file.getName()));
            }
        }
    }

    /**
     * Attempts to load the theme from the specified file. If the enabled argument is true, the
     * theme will be applied automatically. If it has been previously applied and is no longer
     * enabled, it will be unapplied.
     *
     * @param file    The file pointing to the theme to be loaded
     * @param enabled Whether this theme is enabled or not
     */
    private void loadTheme(final File file, final boolean enabled) {
        final Theme theme;

        if (themes.containsKey(file.getName())) {
            theme = themes.get(file.getName());
        } else {
            theme = new Theme(identityController, file);

            if (theme.isValidTheme()) {
                themes.put(file.getName(), theme);
            } else {
                return;
            }
        }

        if (enabled && !theme.isEnabled()) {
            theme.applyTheme();
        } else if (theme.isEnabled() && !enabled) {
            theme.removeTheme();
        }
    }

    /**
     * Retrieves a list of available themes.
     *
     * @return A list of available themes
     */
    public Map<String, Theme> getAllThemes() {
        refreshAndLoadThemes();

        synchronized (themes) {
            return new HashMap<>(themes);
        }
    }

    /**
     * Retrieves the directory used for storing themes.
     *
     * @return The directory used for storing themes
     */
    public String getDirectory() {
        return themeDirectory;
    }

    /**
     * Updates the theme auto load list with the state of the specified theme.
     *
     * @param theme Theme to update auto load
     */
    public void synchroniseAutoLoad(final Theme theme) {
        final List<String> enabled = identityController.getGlobalConfiguration()
                .getOptionList("themes", "enabled", true);

        if (theme.isEnabled()) {
            enabled.add(theme.getFileName());
        } else {
            enabled.remove(theme.getFileName());
        }

        identityController.getUserSettings()
                .setOption("themes", "enabled", enabled);
    }

}
