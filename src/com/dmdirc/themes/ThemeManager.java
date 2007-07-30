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

package com.dmdirc.themes;

import com.dmdirc.Config;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages available themes.
 *
 * @author Chris
 */
public class ThemeManager {
    
    /** The directory to look for themes in. */
    private final String themeDir;
    
    /**
     * Creates a new instance of theme manager.
     */
    public ThemeManager() {
        themeDir = Config.getConfigDir() + "themes";
    }
    
    /**
     * Retrieves a list of available themes.
     * 
     * @return A list of available themes
     */
    public List<Theme> getAvailableThemes() {
        final List<Theme> res = new ArrayList<Theme>();
        
        final File dir = new File(themeDir);
        
        if (!dir.exists()) {
            try {
                dir.mkdirs();
                dir.createNewFile();
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.HIGH, "I/O error when creating themes directory: " + ex.getMessage());
            }
        }
        
        if (dir == null || dir.listFiles() == null) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to load themes");
        } else {
            for (File file : dir.listFiles()) {
                if (!file.isDirectory()) {
                    final Theme theme = new Theme(file);
                    
                    if (theme.isValidTheme()) {
                        res.add(theme);
                    }
                }
            }
        }
        
        return res;
    }
    
}
