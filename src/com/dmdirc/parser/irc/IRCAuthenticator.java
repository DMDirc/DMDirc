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

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import java.util.Map;
import java.util.HashMap;

/**
 * Handles proxy authentication for the parser
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

	/** Singleton instance of IRCAuthenticator. */
	private static IRCAuthenticator me = null;
	
	/** List of authentication replies. */
	private final Map<String,PasswordAuthentication> replies = new HashMap<String,PasswordAuthentication>();
	
	/**
	 * Create a new IRCAuthenticator.
	 *
	 * This creates an IRCAuthenticator and registers it as the default
	 * Authenticator.
	 */
	private IRCAuthenticator() {
/*		try {
			final Field field = Authenticator.class.getDeclaredField("theAuthenticator");
			field.setAccessible(true);
			final Object authenticator = field.get(null);
			if (authenticator instanceof Authenticator) {
				oldAuthenticator = (Authenticator)authenticator;
			}
		} catch (NoSuchFieldException nsfe) {
		} catch (IllegalAccessException iae) {
		}*/
		Authenticator.setDefault(this);
	}
	
	/**
	 * Get the instance of IRCAuthenticator
	 */
	public static synchronized IRCAuthenticator getIRCAuthenticator() {
		if (me == null) {
			me = new IRCAuthenticator();
		}
		return me;
	}

	/**
	 * Add a server to authenticate for.
	 *
	 * @param server ServerInfo object with proxy details.
	 */
	public void addAuthentication(final ServerInfo server) {
		addAuthentication(server.getProxyHost(), server.getProxyPort(), server.getProxyUser(), server.getProxyPass());
	}

	/**
	 * Add a host to authenticate for.
	 *
	 * @param host Hostname
	 * @param port Port
	 * @param username Username to return for authentication
	 * @param password Password to return for authentication
	 */
	public void addAuthentication(final String host, final int port, final String username, final String password) {
		if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
			return;
		}
		final PasswordAuthentication pass = new PasswordAuthentication(username, password.toCharArray());
		final String fullhost = host.toLowerCase()+":"+port;
		
		if (replies.containsKey(fullhost)) {
			replies.remove(fullhost);
		}
		
		replies.put(fullhost, pass);
	}
	
	/** {@inheritDoc} */
	protected PasswordAuthentication getPasswordAuthentication() {
		/*
		 * getRequestingHost: 85.234.138.2
		 * getRequestingPort: 1080
		 * getRequestingPrompt: SOCKS authentication
		 * getRequestingProtocol: SOCKS5
		 * getRequestingScheme: null
		 * getRequestingSite: /85.234.138.2
		 * getRequestingURL: null
		 * getRequestorType: SERVER
		 */

		final String fullhost = getRequestingHost().toLowerCase()+":"+getRequestingPort();
		return replies.get(fullhost);
	}
}
