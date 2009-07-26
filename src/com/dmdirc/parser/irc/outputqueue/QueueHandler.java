/*
 *  Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.dmdirc.parser.irc.outputqueue;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

/**
 * Sending queue.
 *
 * @author shane
 */
public abstract class QueueHandler extends Thread {
    /** Queue we are handling. */
    protected final BlockingQueue<QueueItem> queue;

    /** Where to send the output. */
    private final PrintWriter out;

    /** The output queue that owns us. */
    protected OutputQueue outputQueue;

    /**
     * Create a new Queue Thread
     *
     * @param outputQueue the OutputQueue that owns us.
     * @param queue Queue to handle
     * @param out Writer to send to.
     */
    public QueueHandler(final OutputQueue outputQueue, final BlockingQueue<QueueItem> queue, final PrintWriter out) {
        this.queue = queue;
        this.out = out;
        this.outputQueue = outputQueue;
    }

    /**
     * Send the given item
     *
     * @param line Line to send.
     */
    public void sendLine(final String line) {
        out.printf("%s\r\n", line);
    }

    /** {@inheritDoc} */
    @Override
    public abstract void run();
}
