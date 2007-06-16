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

package com.dmdirc;

import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * The icon manager provides a standard way to access icons for use in DMDirc.
 * It allows the user to override the default actions using config settings
 * under the icons domain.
 *
 * @author chris
 */
public final class IconManager {
    
    /**
     * Creates a new instance of IconManager.
     */
    private IconManager() {
        // Not meant to be used
    }
    
    /**
     * Retrieves the icon with the specified type. Returns null if the icon
     * wasn't found.
     *
     * @param type The name of the icon type to retrieve
     * @return The icon that should be used for the specified type
     */
    public static Icon getIcon(final String type) {
        final ClassLoader cldr = IconManager.class.getClassLoader();
        final URL imageURL = cldr.getResource(getIconPath(type));
        return new ImageIcon(imageURL);
    }
    
    /**
     * Retrieves the path of a specified icon type.
     * 
     * @param type The name of the icon type to retrieve
     * @return The path that should be used to retrieve the specified icon
     */
    private static String getIconPath(final String type) {
        return Config.getOption("icon", type, "com/dmdirc/res/" + type + ".png");
    }
    
}