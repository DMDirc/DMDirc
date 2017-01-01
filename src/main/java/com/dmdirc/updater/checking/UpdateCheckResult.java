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

package com.dmdirc.updater.checking;

import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;

/**
 * Defines the result of an update check. Specifically, provides whether or not the component in
 * question is updatable, and to what version, and any additional information needed in order to
 * effect an update.
 */
public interface UpdateCheckResult {

    /**
     * Retrieves the component that this result describes.
     *
     * @return The {@link UpdateComponent} that this result is for
     */
    UpdateComponent getComponent();

    /**
     * Indicates whether an update is available.
     *
     * @return True if an update was available, false otherwise
     */
    boolean isUpdateAvailable();

    /**
     * Returns the version that the component is updatable to. The behaviour of this method is not
     * defined if {@link #isUpdateAvailable()} returns false.
     *
     * @return The version available for updating
     */
    Version getUpdatedVersion();

    /**
     * Returns a user-friendly version name of the version described by
     * {@link #getUpdatedVersion()}. The behaviour of this method is not defined if
     * {@link #isUpdateAvailable()} returns false.
     *
     * @return A user-friendly version name
     */
    String getUpdatedVersionName();

}
