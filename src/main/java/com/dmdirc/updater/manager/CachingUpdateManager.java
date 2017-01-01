/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.updater.manager;

import com.dmdirc.updater.UpdateComponent;

/**
 * A specialised {@link UpdateManager} which caches the update status of each
 * {@link UpdateComponent}.
 */
public interface CachingUpdateManager extends UpdateManager {

    /**
     * Gets the current status of the given component.
     *
     * @param component The component to get a status for
     *
     * @return The current status of the component
     */
    UpdateStatus getStatus(UpdateComponent component);

    /**
     * Gets the overall status of the manager.
     *
     * @return The current status of the manager
     */
    UpdateManagerStatus getManagerStatus();

    /**
     * Adds a new {@link UpdateManagerListener} to this manager, which will be informed when the
     * manager's status changes.
     *
     * @param listener The listener to be added
     */
    void addUpdateManagerListener(UpdateManagerListener listener);

    /**
     * Removes the specified listener from this manager.
     *
     * @param listener The listener to be removed
     */
    void removeUpdateManagerListener(UpdateManagerListener listener);

}
