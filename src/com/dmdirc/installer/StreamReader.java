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

package com.dmdirc.installer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Simple stream reader to read a stream and add it to a text step
 */
public class StreamReader extends Thread {

    /** This is the Input Stream we are reading */
    private final InputStream stream;
    /** This is the output Prefix */
    private String prefix = null;
    /** This is the StringBuffer to store data in if wanted */
    private StringBuffer data = null;
    /** This is the Step we are outputting to, */
    private TextStep step = null;

    /**
     * Create a new Stream Reader
     *
     * @param stream The stream to read
     */
    public StreamReader(final InputStream stream) {
        this.stream = stream;
    }

    /**
     * Create a new Stream Reader that saves what it reads
     *
     * @param stream The stream to read
     * @param data The stringbuffer to store the output in
     * @since 0.6
     */
    public StreamReader(final InputStream stream, final StringBuffer data) {
        this.stream = stream;
        this.data = data;
    }

    /**
     * Create a new Stream Reader that outputs what it reads
     *
     * @param stream The stream to read
     * @param prefix Prefix of outputed messages
     * @param step Step to output to (null = console)
     */
    public StreamReader(final InputStream stream, final String prefix,
                        final TextStep step) {
        this.stream = stream;
        this.prefix = prefix;
        this.step = step;

        if (step == null) {
            System.out.printf("[%s] Started%n", prefix);
        } else {
            step.addText(String.format(" - -[%s] Started", prefix));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                stream));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (data != null) {
                    if (data.length() > 0) {
                        data.append("\n");
                    }
                    data.append(line);
                }
                if (prefix != null) {
                    if (step == null) {
                        System.out.printf("[%s] %s%n", prefix, line);
                    } else {
                        step.addText(String.format(" - -[%s] %s", prefix, line));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}