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

package com.dmdirc.updater.retrieving;

import com.dmdirc.updater.checking.UpdateCheckResult;

/**
 * Simple implementation of a {@link UpdateRetrievalResult}.
 */
public class BaseRetrievalResult implements UpdateRetrievalResult {

    /** The check result that triggered this retrieval. */
    private final UpdateCheckResult checkResult;
    /** Whether or not the retrieval was successful. */
    private final boolean successful;

    public BaseRetrievalResult(final UpdateCheckResult checkResult, final boolean successful) {
        this.checkResult = checkResult;
        this.successful = successful;
    }

    public UpdateCheckResult getCheckResult() {
        return checkResult;
    }

    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public String toString() {
        return "BaseRetrievalResult{" + "checkResult=" + checkResult
                + ", successful=" + successful + '}';
    }

}
