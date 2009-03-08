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

/**
 * Process a NoticeAuth message.
 */
public class ProcessNoticeAuth extends IRCProcessor {
	/**
	 * Process a NoticeAuth message.
	 *
	 * @param sParam Type of line to process ("Notice Auth")
	 * @param token IRCTokenised line to process
	 */
	@Override
	public void process(final String sParam, final String[] token) {
		callNoticeAuth(token[token.length-1]);
	}
	
	/**
	 * Callback to all objects implementing the NoticeAuth Callback.
	 *
	 * @see INoticeAuth
	 * @param data Incomming Line.
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callNoticeAuth(final String data) {
		return getCallbackManager().getCallbackType("OnNoticeAuth").call(data);
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	@Override
	public String[] handles() {
		return new String[]{"Notice Auth"};
	} 
	
	/**
	 * Create a new instance of the ProcessNoticeAuth Object.
	 *
	 * @param parser IRCParser That owns this object
	 * @param manager ProcessingManager that is in charge of this object
	 */
	protected ProcessNoticeAuth (final IRCParser parser, final ProcessingManager manager) { super(parser, manager); }

}
