/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

public class StreamReader extends Thread {
	/** This is the Input Stream we are reading */
	private InputStream stream;
	
	/** This is the output Prefix */
	private String prefix = null;
	
	/** This is the Step we are outputting to, */
	private StepInstall step = null;
	
	/**
	 * Create a new Stream Reader
	 *
	 * @param stream The stream to read
	 */
	public StreamReader(final InputStream stream) {
		this.stream = stream;
	}
	
	/**
	 * Create a new Stream Reader that outputs what it reads
	 *
	 * @param stream The stream to read
	 * @param prefix Prefix of outputed messages
	 * @param step Step to output to (null = console)
	 */
	public StreamReader(final InputStream stream, final String prefix, final StepInstall step) {
		this.stream = stream;
		this.prefix = prefix;
		this.step = step;
		
		if (step != null) {
			step.addText(String.format(" - -[%s] Started", prefix));
		} else {
			System.out.printf("[%s] Started%n", prefix);
		}
	}

	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				if (prefix != null) {
					if (step != null) {
						step.addText(String.format(" - -[%s] %s", prefix, line));
					} else {
						System.out.printf("[%s] %s%n", prefix, line);
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