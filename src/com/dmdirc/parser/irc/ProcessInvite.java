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
 * Process an Invite Request.
 */
public class ProcessInvite extends IRCProcessor {
	/**
	 * Process an Invite Request.
	 *
	 * @param sParam Type of line to process ("INVITE")
	 * @param token IRCTokenised line to process
	 */
	@Override
	public void process(final String sParam, final String[] token) {
		// :Tobavaj!shane@Tobavaj.users.quakenet.org INVITE Dataforce #dataforceisgod 1188846462
		if (token.length > 2) {
			callInvite(token[0].substring(1), token[3]);
		}
	}
	
	/**
	 * Callback to all objects implementing the Invite Callback.
	 *
	 * @see IInvite
	 * @param userHost The hostname of the person who invited us
	 * @param channel The name of the channel we were invited to
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callInvite(final String userHost, final String channel) {
		return getCallbackManager().getCallbackType("OnInvite").call(userHost, channel);
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	@Override
	public String[] handles() {
		return new String[]{"INVITE"};
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessInvite (IRCParser parser, ProcessingManager manager) { super(parser, manager); }

}
