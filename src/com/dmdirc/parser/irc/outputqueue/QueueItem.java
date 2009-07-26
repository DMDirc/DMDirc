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

import com.dmdirc.parser.common.QueuePriority;

/**
 * Queued Item
 *
 * @author shane
 */
public class QueueItem implements Comparable<QueueItem> {
    /** Line to send */
    private final String line;

    /** Time this line was added. */
    private final long time;

    /** What is the priority of this line? */
    private final QueuePriority priority;

    /**
     * Get the value of line
     *
     * @return the value of line
     */
    public String getLine() {
        return line;
    }

    /**
     * Get the value of time
     *
     * @return the value of time
     */
    public long getTime() {
        return time;
    }

    /**
     * Get the value of priority
     *
     * @return the value of priority
     */
    public QueuePriority getPriority() {
        return priority;
    }

    /**
     * Create a new QueuePriority
     *
     * @param line Line to send
     * @param priority
     */
    public QueueItem(final String line, final QueuePriority priority) {
        this.line = line;
        this.priority = priority;

        this.time = System.currentTimeMillis();
    }

    /**
     * Compare objects.
     * Compare based on priorty firstly, if the priorities are the same,
     * compare based on time added.
     *
     * If an item has been in the queue longer than 10 seconds, it will not
     * check its priority and soley position itself based on time.
     * 
     * @param o Object to compare to
     * @return Position of this item in reference to the given item.
     */
    @Override
    public int compareTo(final QueueItem o) {
        if (this.getTime() < 10 * 1000 && this.getPriority().compareTo(o.getPriority()) > 0) {
            return 1;
        }

        if (this.getTime() < o.getTime()) {
            return 1;
        } else if (this.getTime() > o.getTime()) {
            return -1;
        } else {
            return 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format("[%s %d] %s", priority, time, line);
    }
}
