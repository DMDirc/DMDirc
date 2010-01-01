/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.mediasource_windows;

/**
 * Holds Output from GetMediaInfo.exe
 */
public class MediaInfoOutput {

    /** Exit Code from GetMediaInfo.exe */
    private final int exitCode;

    /** Output from GetMediaInfo.exe */
    private final String output;

    /**
     * Create a MediaInfoOutput
     *
     * @param exitCode Exit code from GetMediaInfo.exe
     * @param output Output from GetMediaInfo.exe
     */
    public MediaInfoOutput(final int exitCode, final String output) {
        this.exitCode = exitCode;
        this.output = output;
    }

    /**
     * Get the exit code
     *
     * @return exit Code
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Get the output
     *
     * @return output
     */
    public String getOutput() {
        return output;
    }

    /**
     * Get the output only if the exitCode was 0, else a blank string.
     *
     * @return The output only if the exitCode was 0, else a blank string.
     */
    public String getGoodOutput() {
        return (getExitCode() == 0) ? output : "";
    }

}
