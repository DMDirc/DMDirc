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

package com.dmdirc.updater;

import com.dmdirc.Main;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.DownloadListener;
import com.dmdirc.util.Downloader;
import com.dmdirc.util.WeakList;

import java.io.IOException;
import java.util.List;

/**
 * Represents a single available update for some component.
 *
 * @author chris
 */
public final class Update implements DownloadListener {

    /** Update component. */
    private final UpdateComponent component;
    /** Remote version name. */
    private final String versionName;
    /** Update url. */
    private final String url;
    /** The progress of the current stage. */
    private float progress;

    /** A list of registered update listeners. */
    private final List<UpdateListener> listeners
            = new WeakList<UpdateListener>();

    /** Our current status. */
    private UpdateStatus status = UpdateStatus.PENDING;

    /**
     * Creates a new instance of Update, with details from the specified line.
     *
     * @param updateInfo An update information line from the update server
     */
    public Update(final String updateInfo) {
        // outofdate client STABLE 20071007 0.5.1 file
        final String[] parts = updateInfo.split(" ");

        if (parts.length == 6) {
            component = UpdateChecker.findComponent(parts[1]);
            versionName = parts[4];
            url = parts[5];
        } else {
            component = null;
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
    public UpdateComponent getComponent() {
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
    public UpdateStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of this update, and notifies all listeners of the change.
     *
     * @param newStatus This update's new status
     */
    protected void setStatus(final UpdateStatus newStatus) {
        status = newStatus;
        progress = 0;

        for (UpdateListener listener : listeners) {
            listener.updateStatusChange(this, status);
        }
    }

    /**
     * Removes the specified update listener.
     *
     * @param o The update listener to remove
     */
    public void removeUpdateListener(final Object o) {
        listeners.remove(o);
    }

    /**
     * Adds the specified update listener.
     *
     * @param e The update listener to add
     */
    public void addUpdateListener(final UpdateListener e) {
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

                setStatus(UpdateStatus.DOWNLOADING);

                try {
                    Downloader.downloadPage(getUrl(), path, Update.this);
                } catch (IOException ex) {
                    setStatus(UpdateStatus.ERROR);

                    Logger.userError(ErrorLevel.MEDIUM, "Error when updating component "
                            + component.getName(), ex);

                    return;
                }

                setStatus(UpdateStatus.INSTALLING);

                try {
                    final boolean restart = getComponent().doInstall(path);

                    if (restart) {
                        setStatus(UpdateStatus.RESTART_NEEDED);
                        UpdateChecker.removeComponent(getComponent().getName());
                    } else {
                        setStatus(UpdateStatus.INSTALLED);
                    }
                } catch (IOException ex) {
                    setStatus(UpdateStatus.ERROR);
                    Logger.userError(ErrorLevel.MEDIUM,
                            "I/O error when updating component " + component.getName(), ex);
                } catch (Exception ex) {
                    setStatus(UpdateStatus.ERROR);
                    Logger.appError(ErrorLevel.MEDIUM,
                            "Error when updating component " + component.getName(), ex);
                }
            }

        }, "Update thread").start();
    }

    /** {@inheritDoc} */
    @Override
    public void downloadProgress(final float percent) {
        progress = percent;
        
        for (UpdateListener listener : listeners) {
            listener.updateProgressChange(this, percent);
        }
    }

    /**
     * Retrieves the current progress of the current state of this update.
     * 
     * @return The percentage of the current stage that has been completed
     */
    public float getProgress() {
        return progress;
    }

    /** {@inheritDoc} */
    @Override
    public void setIndeterminate(boolean indeterminate) {
        //TODO
    }
    
}
