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

package com.dmdirc.ui.themes;

import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages available themes.
 *
 * @author Chris
 */
public final class ThemeManager {
    
    /** The directory to look for themes in. */
    private static final String THEME_DIR = Main.getConfigDir() + "themes/";
    
    /** Available themes. */
    private static final Map<String, Theme> THEMES = new HashMap<String, Theme>();
    
    static {
        IdentityManager.getGlobalConfig().addChangeListener("themes", "enabled",
                new ConfigChangeListener() {
            /** {@inheritDoc} */
            @Override
            public void configChanged(final String domain, final String key) {
                loadThemes();
            }
        });
    }
    
    /**
     * Creates a new instance of theme manager.
     */
    private ThemeManager() {
        // Do nothing
    }
    
    /**
     * Scans for available themes and loads any themes that the user has enabled.
     */
    public static void loadThemes() {
        final File dir = new File(THEME_DIR);

        if (!dir.exists() && !dir.mkdirs()) {
            Logger.userError(ErrorLevel.HIGH, "Could not create themes directory");
        }
        
        final List<String> enabled
                = IdentityManager.getGlobalConfig().getOptionList("themes", "enabled");
            
        if (dir.listFiles() == null) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to load themes");
            return;
        }
        
        synchronized (THEMES) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }

                loadTheme(file, enabled.contains(file.getName()));
            }
        }            
    }
    
    /**
     * Attempts to load the theme from the specified file. If the enabled 
     * argument is true, the theme will be applied automatically. If it
     * has been previously applied and is no longer enabled, it will be 
     * unapplied.
     * 
     * @param file The file pointing to the theme to be loaded
     * @param enabled Whether this theme is enabled or not
     */
    private static void loadTheme(final File file, final boolean enabled) {
        Theme theme;

        if (THEMES.containsKey(file.getName())) {
            theme = THEMES.get(file.getName());
        } else {
            theme = new Theme(file);

            if (theme.isValidTheme()) {
                THEMES.put(file.getName(), theme);
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
    public static Map<String, Theme> getAvailableThemes() {
        loadThemes();
        
        synchronized (THEMES) {
            return new HashMap<String, Theme>(THEMES);
        }
    }
    
    /**
     * Retrieves the directory used for storing themes.
     * 
     * @return The directory used for storing themes
     */
    public static String getThemeDirectory() {
        return THEME_DIR;
    }

}
