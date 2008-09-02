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
 *
 * SVN: $Id: ServerInfo.java 4685 2008-09-02 22:12:38Z ShaneMcC $
 */

package com.dmdirc.parser;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Handles proxy authentication
 * 
 * @author Shane Mc Cormack
 * @see IRCParser
 */
public class IRCAuthenticator extends Authenticator {
	/**
	 * A version number for this class. It should be changed whenever the class
	 * structure is changed (or anything else that would prevent serialized
	 * objects being unserialized with the new class).
	 */
	private static final long serialVersionUID = 1;

	/** My Parser. */
	final IRCParser parser;
	
	/** My ServerInfo. */
	final ServerInfo serverInfo;

	/**
	 * Create a new IRCAuthenticator
	 *
	 * @param parser Parser that owns this
	 * @param serverInfo ServerInfo to use for authentication
	 */
	public IRCAuthenticator(final IRCParser parser, final ServerInfo serverInfo) {
		this.parser = parser;
		this.serverInfo = serverInfo;
	}

	/** {@inheritDoc} */
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(serverInfo.getProxyUser(), serverInfo.getProxyPass().toCharArray());
	}
}