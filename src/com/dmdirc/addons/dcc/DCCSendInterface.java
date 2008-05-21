/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.dcc;

/**
 * This interfaces allows DCC Send Windows to receive data from a DCCSend
 *
 * @author Shane 'Dataforce' McCormack
 * @version $Id: DCCSendInterface.java 969 2007-04-30 18:38:20Z ShaneMcC $
 */
public interface DCCSendInterface {
	/**
	 * Called when the socket is closed
	 *
	 * @param dcc The DCCSend that this message is from
	 */
	void socketClosed(final DCCSend dcc);
	
	/**
	 * Called when the socket is opened
	 *
	 * @param dcc The DCCSend that this message is from
	 */
	void socketOpened(final DCCSend dcc);
	
	/**
	 * Called when data is sent/recieved
	 *
	 * @param dcc The DCCSend that this message is from
	 * @param bytes The number of new bytes that were transfered
	 */
	void dataTransfered(final DCCSend dcc, final int bytes);
}
