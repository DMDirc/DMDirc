/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.updater.installing;

import com.dmdirc.updater.UpdateComponent;

/**
 * Interface for objects interested in the progress of an installation attempt performed by an
 * {@link UpdateInstallationStrategy}.
 */
public interface UpdateInstallationListener {

    /**
     * Called when the progress of an install has changed.
     *
     * @param component The component being installed
     * @param progress  The percentage progress completed (0-100)
     */
    void installProgressChanged(UpdateComponent component, double progress);

    /**
     * Called when an installation has failed.
     *
     * @param component The component that failed to install
     */
    void installFailed(UpdateComponent component);

    /**
     * Called when an installation has finished.
     *
     * @param component The component that was successfully installed
     */
    void installCompleted(UpdateComponent component);

}
