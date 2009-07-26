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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * This class handles the Parser output Queue.
 *
 * @author shane
 */
public class OutputQueue {
    /** PrintWriter for sending output. */
    private PrintWriter out;

    /** Is queueing enabled? */
    private boolean queueEnabled = true;

    /** The output queue! */
    private BlockingQueue<QueueItem> queue = new PriorityBlockingQueue<QueueItem>();

    /** Thread for the sending queue. */
    private Thread queueThread;

    /**
     * Create a new OutputQueue
     * 
     * @param outputStream PrintWriter to use to actually send stuff.
     */
    public OutputQueue (final OutputStream outputStream) {
        this.out = new PrintWriter(outputStream, true);
    }

    /**
     * Is output queueing enabled?
     *
     * @return true if output queueing is enabled.
     */
    public boolean isQueueEnabled() {
        return queueEnabled;
    }

    /**
     * Set if queueing is enabled.
     * if this is changed from enabled to disabled, all currently queued items
     * will be sent immediately!
     *
     * @param queueEnabled new value for queueEnabled
     */
    public void setQueueEnabled(final boolean queueEnabled) {
        final boolean old = this.queueEnabled;
        this.queueEnabled = queueEnabled;

        if (old != queueEnabled && old) {
            queueThread.interrupt();
            queueThread = null;

            while (!queue.isEmpty()) {
                try {
                    out.printf("%s\r\n", queue.take().getLine());
                } catch (InterruptedException ex) {
                    // Do nothing, we'll try again.
                }
            }
        }
    }

    /**
     * Clear the queue and stop the thread that is sending stuff.
     */
    public void clearQueue() {
        this.queueEnabled = false;
        queueThread.interrupt();
        queueThread = null;

        queue.clear();
    }

    /**
     * Send the given line.
     * If queueing is enabled, this will queue it, else it will send it
     * immediately.
     *
     * @param line Line to send
     */
    public void sendLine(final String line) {
        sendLine(line, QueuePriority.NORMAL);
    }

    /**
     * Send the given line.
     * If queueing is enabled, this will queue it, else it will send it
     * immediately.
     *
     * @param line Line to send
     * @param priority Priority of item (ignored if queue is disabled)
     */
    public void sendLine(final String line, final QueuePriority priority) {
        if (queueEnabled) {
            queue.add(new QueueItem(line, priority));

            if (queueThread == null || !queueThread.isAlive()) {
                queueThread = getQueueHandler(queue, out);
                queueThread.start();
            }
        } else {
            out.printf("%s\r\n", line);
        }
    }

    /**
     * Get a new QueueHandler instance as needed.
     * 
     * @param queue The queue to handle.
     * @param out Where to send crap.
     * @return the new queue handler object.
     */
    private QueueHandler getQueueHandler(final BlockingQueue<QueueItem> queue, final PrintWriter out) {
        return new PriorityQueueHandler(this, queue, out);
    }
}
