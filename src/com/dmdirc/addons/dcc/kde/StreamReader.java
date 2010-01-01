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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class StreamReader extends Thread {

    /** This is the Input Stream we are reading */
    private InputStream stream;

    /** This is the output Prefix */
    private String prefix = null;

    /** List to store output in */
    private List<String> list = null;

    /**
     * Create a new Stream Reader
     *
     * @param stream The stream to read
     * @param list The list to store the output from the stream in (null for no saving)
     */
    public StreamReader(final InputStream stream, final List<String> list) {
        this.stream = stream;
        this.list = list;
    }

    /**
     * Create a new Stream Reader that outputs what it reads
     *
     * @param stream The stream to read
     * @param list The list to store the output from the stream in (null for no saving)
     * @param prefix Prefix of outputed messages
     */
    public StreamReader(final InputStream stream, final List<String> list, final String prefix) {
        this.stream = stream;
        this.prefix = prefix;
        this.list = list;

        System.out.printf("[%s] Started%n", prefix);
    }

    /**
     * Get the list that the output is being stored in.
     */
    public List<String> getList() {
        return list;
    }

    /**
     * Wait for input on stream, and output/throw away/save to list
     */
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (prefix != null) {
                    System.out.printf("[%s] %s%n", prefix, line);
                }
                if (list != null) {
                    list.add(line);
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
