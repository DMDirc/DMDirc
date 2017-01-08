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

import java.net.URL;

/**
 * A simple implementation of a {@link DownloadableUpdate}.
 */
public class BaseDownloadableResult extends BaseCheckResult implements DownloadableUpdate {

    /** The URL the update may be downloaded from. */
    private final URL url;

    /**
     * Creates a new instance of {@link BaseDownloadableResult}.
     *
     * @param component          The component that this result is for
     * @param url                The URL the update may be downloaded from
     * @param updatedVersionName The friendly name of the updated version
     * @param updatedVersion     The version of the file available at that URL
     */
    public BaseDownloadableResult(final UpdateComponent component, final URL url,
            final String updatedVersionName, final Version updatedVersion) {
        super(component, true, updatedVersionName, updatedVersion);

        this.url = url;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "BaseDownloadableResult{super=" + super.toString() + ", url=" + url + '}';
    }

}
