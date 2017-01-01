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
 * Simple implementation of {@link UpdateCheckResult}.
 */
public class BaseCheckResult implements UpdateCheckResult {

    /** The component this result is for. */
    private final UpdateComponent component;
    /** Whether or not an update is available. */
    private final boolean updateAvailable;
    /** A user-friendly name of the updated version, if any. */
    private final String updatedVersionName;
    /** The version available to update to, if any. */
    private final Version updatedVersion;

    public BaseCheckResult(final UpdateComponent component, final boolean updateAvailable,
            final String updatedVersionName, final Version updatedVersion) {
        this.component = component;
        this.updateAvailable = updateAvailable;
        this.updatedVersionName = updatedVersionName;
        this.updatedVersion = updatedVersion;
    }

    /**
     * Creates a new {@link BaseCheckResult} which reports no update is available.
     *
     * @param component The component this result is for
     */
    public BaseCheckResult(final UpdateComponent component) {
        this.component = component;
        this.updateAvailable = false;
        this.updatedVersion = null;
        this.updatedVersionName = null;
    }

    @Override
    public UpdateComponent getComponent() {
        return component;
    }

    @Override
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    @Override
    public String getUpdatedVersionName() {
        return updatedVersionName;
    }

    @Override
    public Version getUpdatedVersion() {
        return updatedVersion;
    }

    @Override
    public String toString() {
        return "BaseCheckResult{" + "component=" + component
                + ", updateAvailable=" + updateAvailable
                + ", updatedVersionName=" + updatedVersionName
                + ", updatedVersion=" + updatedVersion + '}';
    }

}
