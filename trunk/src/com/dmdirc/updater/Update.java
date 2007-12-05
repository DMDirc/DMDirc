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

import com.dmdirc.Main;
import com.dmdirc.interfaces.UpdateListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.updater.Update.STATUS;
import com.dmdirc.util.Downloader;
import com.dmdirc.util.WeakList;

import java.util.List;

/**
 * Represents a single available update for some component.
 *
 * @author chris
 */
public final class Update {

    /**
     * An enumeration of possible statuses.
     */
    public static enum STATUS {
        PENDING,
        DOWNLOADING,
        INSTALLING,
        INSTALLED,
        ERROR
    }

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

    /** A list of registered update listeners. */
    private final List<UpdateListener> listeners
            = new WeakList<UpdateListener>();

    /** Our current status. */
    private STATUS status = Update.STATUS.PENDING;

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

    /**
     * Retrieves the status of this update.
     *
     * @return This update's status
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * Sets the status of this update, and notifies all listeners of the change.
     *
     * @param newStatus This update's new status
     */
    protected void setStatus(final STATUS newStatus) {
        status = newStatus;

        for (UpdateListener listener : listeners) {
            listener.updateStatusChange(this, status);
        }
    }

    /**
     * Removes the specified update listener.
     *
     * @param o The update listener to remove
     */
    public void removeUpdateListener(Object o) {
        listeners.remove(o);
    }

    /**
     * Adds the specified update listener.
     *
     * @param e The update listener to add
     */
    public void addUpdateListener(UpdateListener e) {
        listeners.add(e);
    }

    /**
     * Makes this update download and install itself.
     */
    public void doUpdate() {
        new Thread(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final String path = Main.getConfigDir() + "update.tmp."
                        + Math.round(Math.random() * 1000);

                setStatus(STATUS.DOWNLOADING);

                try {
                    Downloader.downloadPage(getUrl(), path);
                } catch (Throwable ex) {
                    setStatus(STATUS.ERROR);

                    Logger.appError(ErrorLevel.MEDIUM,
                            "Error when updating component " + component, ex);

                    return;
                }

                setStatus(STATUS.INSTALLING);

                try {
                    UpdateChecker.findComponent(getComponent()).doInstall(path);

                    setStatus(STATUS.INSTALLED);
                } catch (Throwable ex) {
                    setStatus(STATUS.ERROR);
                    Logger.appError(ErrorLevel.MEDIUM,
                            "Error when updating component " + component, ex);
                }
            }

        }, "Update thread").start();
    }

}
