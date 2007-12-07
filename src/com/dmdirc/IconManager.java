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

import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.config.IdentityManager;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * The icon manager provides a standard way to access icons for use in DMDirc.
 * It allows the user to override the default actions using config settings
 * under the icons domain.
 *
 * @author chris
 */
public final class IconManager implements ConfigChangeListener {
    
    /** Previously created IconManager instance. */
    private static final IconManager me = new IconManager();
        
    private final Map<String, Icon> icons;
    private final Map<String, Image> images;
    
    /** Creates a new instance of IconManager. */
    private IconManager() {        
        icons = new HashMap<String, Icon>();
        images = new HashMap<String, Image>();
        
        IdentityManager.getGlobalConfig().addChangeListener("icon", this);
    }
    
    /**
     * Returns an instance of IconManager.
     *
     * @return Instance of IconManager
     */
    public static IconManager getIconManager() {
        return me;
    }
    
    /**
     * Retrieves the icon with the specified type. Returns null if the icon
     * wasn't found.
     *
     * @param type The name of the icon type to retrieve
     *
     * @return The icon that should be used for the specified type
     */
    public Icon getIcon(final String type) {
        if (!icons.containsKey(type)) {
            icons.put(type, new ImageIcon(getScaledImage(new ImageIcon(getIconURL(type)).getImage(), 16, 16)));
        }
        return icons.get(type);
    }
    
    /**
     * Retrieves the image with the specified type. Returns null if the icon
     * wasn't found.
     *
     * @param type The name of the icon type to retrieve
     *
     * @return The image that should be used for the specified type
     */
    public Image getImage(final String type) {
        if (!images.containsKey(type)) {
            images.put(type, Toolkit.getDefaultToolkit().createImage(getIconURL(type)));
        }
        return images.get(type);
    }
    
    /**
     * Returns a scaled image.
     *
     * @param image Image to scale
     * @param width Width of resulting image
     * @param height Height of resulting image
     *
     * @return Scaled Image
     */
    private Image getScaledImage(final Image image,
            final int width, final int height) {
        return image.getScaledInstance(width , height, Image.SCALE_SMOOTH);
    }
    
    /**
     * Retrieves the URL of a specified icon type.
     *
     * @param type The name of the icon type to retrieve
     *
     * @return The URL that should be used to retrieve the specified icon
     */
    private URL getIconURL(final String type) {
        final ClassLoader cldr = Thread.currentThread().getContextClassLoader();
        final URL defaultURL = cldr.getResource("com/dmdirc/res/" + type + ".png");
        
        //Get the path for the url
        final String path = IdentityManager.getGlobalConfig().getOption("icon", type, "com/dmdirc/res/" + type + ".png");
        
        //Get the url for the speficied path
        URL imageURL = cldr.getResource(path);
        
        try {
            //if the path didnt exist see if its a file
            if (imageURL == null) {
                final File file = new File(path);
                if (file.exists()) {
                    imageURL = file.toURI().toURL();
                } else {
                    imageURL = defaultURL;
                }
            }
            
            //check if the url has content
            if (imageURL == null || imageURL.getContent() == null)  {
                imageURL = defaultURL;
            }
        } catch (IOException ex) {
            imageURL = defaultURL;
        }
        
        if (imageURL == null) {
            imageURL = cldr.getResource("com/dmdirc/res/icon.png");
        }
        
        return imageURL;
    }

    /** {@inheritDoc} */
    public void configChanged(final String domain, final String key) {
        if ("icon".equals(domain)) {
            if (images.containsKey(key)) {
                images.remove(key);
            }
            if (icons.containsKey(key)) {
                icons.remove(key);
            }
        }
    }
    
}
