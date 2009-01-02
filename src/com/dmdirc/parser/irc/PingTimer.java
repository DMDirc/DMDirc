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

package com.dmdirc.parser.irc;

import java.util.TimerTask;
import java.util.Timer;

/**
 * Used by the parser to ping the server at a set interval to check that the
 * server is still alive.
 *
 * @author Shane Mc Cormack
 */
public class PingTimer extends TimerTask {
	/** Owning Parser. */
	final IRCParser myOwner;
	/** The Timer that owns this task */
	final Timer myTimer;
	
	/**
	 * Create the PingTimer.
	 *
	 * @param control IRCParser that owns this TimerTask.
	 * @param timer Timer that owns this TimerTask.
	 */
	public PingTimer(final IRCParser control, final Timer timer) {
		myOwner = control;
		myTimer = timer;
	}
	
	/** Timer has been executed. */
	public void run() {
		myOwner.pingTimerTask(myTimer);
	}
}
