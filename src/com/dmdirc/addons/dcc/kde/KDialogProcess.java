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

package com.dmdirc.addons.dcc.kde;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Hold a Process and stream readers for a KDialog Process
 */
public class KDialogProcess {

    /** Is kdialog in /bin? */
    private final static boolean isBin = (new File("/bin/kdialog")).exists();

    /** Does KDialog exist? */
    private final static boolean hasKDialog = (new File("/usr/bin/kdialog")).exists() || isBin;

    /** Stream for the stdout stream for this process */
    private final StreamReader stdOutputStream;

    /** Stream for the stderr stream for this process */
    private final StreamReader stdErrorStream;

    /** The actual process for this process */
    private final Process process;

    /**
     * Execute kdialog with the Parameters in params
     *
     * @param params Parameters to pass to kdialog
     */
    public KDialogProcess(final String[] params) throws IOException {
        final String[] exec = new String[params.length + 1];
        System.arraycopy(params, 0, exec, 1, params.length);
        exec[0] = (isBin) ? "/bin/kdialog" : "/usr/bin/kdialog";
        process = Runtime.getRuntime().exec(exec);
        stdOutputStream = new StreamReader(process.getInputStream(), new ArrayList<String>());
        stdErrorStream = new StreamReader(process.getErrorStream(), new ArrayList<String>());
        stdOutputStream.start();
        stdErrorStream.start();
    }

    /**
     * Does this system have kdialog?
     *
     * @return True if kdialog was found, else false
     */
    public static boolean hasKDialog() {
        return hasKDialog;
    }

    /**
     * Get the process object for this KDialogProcess
     *
     * @return The process object for this KDialogProcess
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Get the StreamReader for this KDialogProcess's stdout stream
     *
     * @return The StreamReader for this KDialogProcess's stdout stream
     */
    public StreamReader getStdOutStream() {
        return stdOutputStream;
    }

    /**
     * Get the StreamReader for this KDialogProcess's stderr stream
     *
     * @return The StreamReader for this KDialogProcess's stderr stream
     */
    public StreamReader getStdErrStream() {
        return stdErrorStream;
    }

    /**
     * Wait for the process to finish.
     */
    public void waitFor() throws InterruptedException {
        process.waitFor();
    }

}
