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

package com.dmdirc.logger;

import java.util.EventListener;

/**
 * Error listener, error related events.
 */
public interface ErrorListener extends EventListener {

    /**
     * Fired when the program encounters an error.
     *
     * @param error Error that occurred
     */
    void errorAdded(final ProgramError error);

    /**
     * Fired when an error is deleted.
     *
     * @param error Error that was deleted
     */
    void errorDeleted(final ProgramError error);

    /**
     * Fired when an error's status is changed.
     *
     * @param error Error that has been altered
     */
    void errorStatusChanged(final ProgramError error);

    /**
     * Returns true if the error listener is ready to receive errors
     *
     * @return true iif the error listener is ready for errors
     */
    boolean isReady();

}
