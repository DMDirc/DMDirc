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

/**
 * Describes the possible statuses of an {@link com.dmdirc.updater.UpdateComponent}.
 */
public enum UpdateStatus {

    /** There is no known update available, and the manager is not active checking. */
    IDLE("Idle"),
    /** The update
     * manager is not permitted to check for updates for this component. */
    CHECKING_NOT_PERMITTED("Not enabled"),
    /** The manager is currently checking for updates for the component. */
    CHECKING("Checking..."),
    /** An update is available but it is not currently being retrieved. */
    UPDATE_PENDING("Update available"),
    /** An update is currently being retrieved. */
    RETRIEVING("Retrieving..."),
    /** An update has been retrieved but it is not currently
     * being installed. */
    INSTALL_PENDING("Retrieved"),
    /** An update is currently being installed. */
    INSTALLING("Installing..."),
    /** An update has successfully been installed. */
    UPDATED("Updated"),
    /** An update has been
     * installed but will not take effect until after a restart. */
    RESTART_PENDING("Restart required");
    /** Textual description of the status. */
    private final String description;

    UpdateStatus(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
