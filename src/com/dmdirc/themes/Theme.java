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

import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.resourcemanager.ZipResourceManager;
import com.dmdirc.ui.messages.Formatter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents one theme file.
 *
 * @author Chris
 */
public class Theme {
    
    /** The file to load the theme from. */
    private final File file;
    
    private ZipResourceManager rm;
    
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
        try {
            rm = ZipResourceManager.getInstance(file.getCanonicalPath());
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "I/O error when loading theme: " + file.getAbsolutePath() + ": " + ex.getMessage());
            
            return false;
        }
        
        return true;
    }
    
    /**
     * Applies this theme to the client.
     */
    public void applyTheme() {
        if (!isValidTheme() || rm == null) {
            return;
        }
        
        final InputStream identity = rm.getResourceInputStream("config");
        final InputStream format = rm.getResourceInputStream("format");
        
        if (identity != null) {
            try {
                IdentityManager.addIdentity(new ThemeIdentity(identity));
            } catch (InvalidIdentityFileException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Error loading theme identity file: " + ex.getMessage());
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Error loading theme identity file: " + ex.getMessage());
            }
        }
        
        if (format != null) {
            Formatter.reload();
            Formatter.loadFile(format);
        }
    }
    
}
