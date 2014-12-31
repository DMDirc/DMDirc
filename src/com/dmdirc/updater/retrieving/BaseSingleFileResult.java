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

import java.nio.file.Path;

/**
 * Simple implementation of a {@link SingleFileRetrievalResult}.
 */
public class BaseSingleFileResult extends BaseRetrievalResult
        implements SingleFileRetrievalResult {

    /** The file where the update is located. */
    private final Path file;

    /**
     * Creates a new instance of {@link BaseSingleFileResult}.
     *  @param checkResult The check result that triggered this retrieval
     * @param file        The file containing the update
     */
    public BaseSingleFileResult(final UpdateCheckResult checkResult, final Path file) {
        super(checkResult, true);

        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    @Override
    public String toString() {
        return "BaseSingleFileResult{super=" + super.toString() + "file=" + file + '}';
    }

}
