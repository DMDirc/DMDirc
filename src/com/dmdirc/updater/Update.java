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

package com.dmdirc.updater;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

/**
 * Represents a single available update for some component.
 *
 * @author chris
 */
public final class Update {
    
    /** Update component. */
    private final String component;
    /** Channel name. */
    private final String channel;
    /** Remote version number. */
    private final String versionID;
    /** Remote version name. */
    private final String versionName;
    /** Update url. */
    private final String url;
    
    /**
     * Creates a new instance of Update, with details from the specified line.
     *
     * @param updateInfo An update information line from the update server
     */
    public Update(final String updateInfo) {
        // outofdate client STABLE 20071007 0.5.1 file
        final String[] parts = updateInfo.split(" ");
        
        if (parts.length == 6) {
            component = parts[1];
            channel = parts[2];
            versionID = parts[3];
            versionName = parts[4];
            url = parts[5];            
        } else {
            component = null;
            channel = null;
            versionID = null;
            versionName = null;
            url = null;  
            
            Logger.appError(ErrorLevel.LOW,
                    "Invalid update line received from server: ",
                    new UnsupportedOperationException("line: " + updateInfo));
        }
    }
    
    /**
     * Retrieves the component that this update is for.
     *
     * @return The component of this update
     */
    public String getComponent() {
        return component;
    }
    
    /**
     * Returns the remote version of the component that's available.
     *
     * @return The remote version number
     */
    public String getRemoteVersion() {
        return versionName;
    }
    
    /**
     * Returns the URL where the new update may be downloaded.
     *
     * @return The URL of the update
     */
    public String getUrl() {
        return url;
    }
}
