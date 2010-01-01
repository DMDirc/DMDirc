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

package com.dmdirc.addons.nowplaying;

/**
 * The state of a media source.
 *
 * @author Shane McCormack
 * @since 0.6.3m1
 */
public enum MediaSourceState {
	/** Media Source is closed. */
	CLOSED ("Closed"),
	/** Media Source is stopped. */
	STOPPED ("Stopped"),
	/** Media Source is paused. */
	PAUSED ("Paused"),
	/** Media Source is playing. */
	PLAYING ("Playing"),
	/** Media Source is giving an unknown state. */
	NOTKNOWN ("Unknown");
	
	/** Nice name for this state. */
	final String niceName;
	
	/**
	 * Create a new MediaSourceState
	 *
	 * @param niceName Nice name for this state.
	 */
	MediaSourceState(final String niceName) {
		this.niceName = niceName;
	}
	
	/**
	 * Get the nice name for this state.
     *
     * @return This state's nice name
	 */
	public String getNiceName() {
		return niceName;
	}
}
