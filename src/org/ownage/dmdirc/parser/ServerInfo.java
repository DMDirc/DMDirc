/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack
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
 * SVN: $Id$
 */

package org.ownage.dmdirc.parser;

/**
 * Contains Server information.
 * 
 * @author            Shane Mc Cormack
 * @author            Chris Smith
 * @version           $Id$
 * @see IRCParser
 */
public class ServerInfo {
	/** Server to connect to (Default: "irc.quakenet.org"). */
	public String sHost = "irc.quakenet.org";
	/** Optional password needed to connect to server (Default: ""). */
	public String sPassword = "";
	/** Port server listens on for client connections (Default: 6667). */
	public int nPort = 6667;
	/** Is this an ssl-enabled server (Default: false). */
	public boolean bSSL = false;	
	
	/** Constructor using Default values. */
	public ServerInfo () { }
	/** Constructor using specifed host, port and password, SSL must be specifed separately. */
	public ServerInfo (String host, int port, String pass) {
		sHost = host;
		nPort = port;
		sPassword = pass;
	}
	
	/**
	 * Get SVN Version information
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
