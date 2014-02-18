/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.interfaces;

/**
 * Provides methods that control the lifecycle of the application.
 */
public interface LifecycleController {

    /**
     * Quits the client nicely, with the default closing message.
     */
    void quit();

    /**
     * Quits the client nicely, with the default closing message.
     *
     * @param exitCode This is the exit code that will be returned to the operating system when the
     *                 client exits
     */
    void quit(final int exitCode);

    /**
     * Quits the client nicely.
     *
     * @param reason The quit reason to send
     */
    void quit(final String reason);

    /**
     * Quits the client nicely.
     *
     * @param reason   The quit reason to send
     * @param exitCode This is the exit code that will be returned to the operating system when the
     *                 client exits
     */
    void quit(final String reason, final int exitCode);

}
