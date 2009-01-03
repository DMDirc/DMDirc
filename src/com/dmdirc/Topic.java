/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc;

/**
 * Stores information about a channel topic.
 *
 * @author chris
 */
public class Topic {

    /** Topic. */
    private final String topic;
    /** Topic client. */
    private final String client;
    /** Topic time. */
    private final long time;

    /**
     * Creates a new topic.
     *
     * @param topic Topic
     * @param client Topic client
     * @param time Topic time
     */
    public Topic(final String topic, final String client, final long time) {
        this.topic = topic;
        this.client = client;
        this.time = time;
    }

    /**
     * Returns the client who set the topic.
     *
     * @return client host
     */
    public String getClient() {
        return client;
    }

    /**
     * Returns the time the topic was set.
     *
     * @return topic time
     */
    public long getTime() {
        return time;
    }

    /**
     * Returns the topic this object represents.
     *
     * @return topic
     */
    public String getTopic() {
        return topic;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return topic;
    }

}
