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

package com.dmdirc.util.resourcemanager;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 *
 * @author chris
 */
public class DMDircResourceManager {

    /**
     * Returns the working directory for the application.
     * 
     * @return Current working directory
     */
    public static synchronized String getCurrentWorkingDirectory() {
        String path = "";        
        final URL resource = Thread.currentThread().getContextClassLoader().
                        getResource("com/dmdirc/Main.class");
        
        final String protocol = resource.getProtocol();
        
        if ("file".equals(protocol)) {
            path = Thread.currentThread().
                    getContextClassLoader().getResource("").getPath();
        } else if ("jar".equals(protocol)) {
            final String tempPath = resource.getPath();
            
            if (System.getProperty("os.name").startsWith("Windows")) {
                path = tempPath.substring(6, tempPath.length() - 23);
            } else {
                path = tempPath.substring(5, tempPath.length() - 23);
            }
            
            path = path.substring(0, path.lastIndexOf('/') + 1);
        }
        
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to decode path");
            path = "";
        }
        return path;
    }
    
    /**
     * Determines if this instance of DMDirc is running from a jar or not.
     * 
     * @return True if this instance is running from a JAR, false otherwise
     */
    public static boolean isRunningFromJar() {
        final URL resource = Thread.currentThread().getContextClassLoader().
                        getResource("com/dmdirc/Main.class");
        return "jar".equals(resource.getProtocol());
    }
    
}
