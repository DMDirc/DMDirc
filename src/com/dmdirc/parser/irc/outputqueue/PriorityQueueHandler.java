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
 * This does no rate limiting, and just sends based on priority.
 *
 * @author shane
 */
public class PriorityQueueHandler extends QueueHandler {


    public PriorityQueueHandler(final OutputQueue outputQueue, final BlockingQueue<QueueItem> queue, final PrintWriter out) {
        super(outputQueue, queue, out);
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        try {
            while (outputQueue.isQueueEnabled()) {
                final QueueItem item = queue.take();

                System.out.println("Sending: "+item);

                sendLine(item.getLine());
            }
        } catch (InterruptedException ex) {
            // Do nothing
        }
    }
}
