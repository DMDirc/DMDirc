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
 * @author chris
 */
public final class Update {
    
    /** Update component. */
    private String component;
    /** Local version. */
    private String localVersion;
    /** Remove version. */
    private String remoteVersion;
    /** Update url. */
    private String url;
   
    /**
     * Creates a new instance of Update, with details from the specified line.
     * @param updateInfo An update information line from the update server
     */
    public Update(final String updateInfo) {
        final String[] parts = updateInfo.split(" ");
        
        if (parts.length == 5) {
            component = parts[1];
            remoteVersion = parts[2];
            localVersion = parts[3];
            url = parts[4];
        } else {
            Logger.error(ErrorLevel.WARNING, "Invalid update line received from server: " + updateInfo);
        }
    }
    
    /**
     * Retrieves the component that this update is for.
     * @return The component of this update
     */
    public String getComponent() {
        return component;
    }
    
    /**
     * Returns the local version of the component that's updateable.
     * @return The local (outdated) version number
     */
    public String getLocalVersion() {
        return localVersion;
    }    

    /**
     * Returns the remote version of the component that's available.
     * @return The remote version number
     */
    public String getRemoteVersion() {
        return remoteVersion;
    }

    /**
     * Returns the URL where the new update may be downloaded.
     * @return The URL of the update
     */
    public String getUrl() {
        return url;
    }    
}
