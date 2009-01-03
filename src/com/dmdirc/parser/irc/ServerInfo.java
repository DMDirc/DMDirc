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
 * Contains Server information.
 * 
 * @author Shane Mc Cormack
 * @author Chris Smith
 * @see IRCParser
 */
public class ServerInfo {
	/**
	 * A version number for this class. It should be changed whenever the class
	 * structure is changed (or anything else that would prevent serialized
	 * objects being unserialized with the new class).
	 */
	private static final long serialVersionUID = 1;

	/** Server to connect to (Default: "irc.quakenet.org"). */
	private String host = "irc.quakenet.org";
	/** Port server listens on for client connections (Default: 6667). */
	private int port = 6667;
	/** Optional password needed to connect to server (Default: ""). */
	private String password = "";
	/** Is this an ssl-enabled server (Default: false). */
	private boolean isSSL = false;	
	/** Are we using a socks proxy (Default: false). */
	private boolean useSocksProxy = false;
	/** Proxy server to connect to (Default: "127.0.0.1"). */
	private String proxyHost = "127.0.0.1";
	/** Port server listens on for client connections (Default: 8080). */
	private int proxyPort = 1080;
	/** Proxy username if required. */
	private String proxyUser = "";
	/** Proxy password if required. */
	private String proxyPass = "";
	
	/** Constructor using Default values. */
	public ServerInfo () { }
	
	/**
	 * Constructor using specifed host, port and password, SSL/Proxy must be specifed separately.
	 *
	 * @param serverHost Host to use
	 * @param serverPort Port to use
	 * @param serverPass Password to use
	 */
	public ServerInfo (final String serverHost, final int serverPort, final String serverPass) {
		host = serverHost;
		port = serverPort;
		password = serverPass;
	}
	
	/**
	 * Set the hostname.
	 *
	 * @param newValue Value to set to.
	 */
	public void setHost(final String newValue) { host = newValue; }
	
	/**
	 * Get the hostname.
	 *
	 * @return Current hostname
	 */
	public String getHost() { return host; }
	
	/**
	 * Set the port.
	 *
	 * @param newValue Value to set to.
	 */
	public void setPort(final int newValue) { port = newValue; }
	
	/**
	 * Get the port.
	 *
	 * @return Current port
	 */
	public int getPort() { return port; }
	
	/**
	 * Set the password.
	 *
	 * @param newValue Value to set to.
	 */
	public void setPassword(final String newValue) { password = newValue; }
	
	/**
	 * Get the password.
	 *
	 * @return Current Password
	 */
	public String getPassword() { return password; }
	
	/**
	 * Set if the server uses ssl.
	 *
	 * @param newValue true if server uses ssl, else false
	 */
	public void setSSL(final boolean newValue) { isSSL = newValue; }
	
	/**
	 * Get if the server uses ssl.
	 *
	 * @return true if server uses ssl, else false
	 */
	public boolean getSSL() { return isSSL; }
	
	/**
	 * Set if we are connecting via a socks proxy.
	 *
	 * @param newValue true if we are using socks, else false
	 */
	public void setUseSocks(final boolean newValue) { useSocksProxy = newValue; }
	
	/**
	 * Get if we are connecting via a socks proxy.
	 *
	 * @return true if we are using socks, else false
	 */
	public boolean getUseSocks() { return useSocksProxy; }
	
	/**
	 * Set the Proxy hostname.
	 *
	 * @param newValue Value to set to.
	 */
	public void setProxyHost(final String newValue) { proxyHost = newValue; }
	
	/**
	 * Get the Proxy hostname.
	 *
	 * @return Current Proxy hostname
	 */
	public String getProxyHost() { return proxyHost; }
	
	/**
	 * Set the Proxy port.
	 *
	 * @param newValue Value to set to.
	 */
	public void setProxyPort(final int newValue) { proxyPort = newValue; }
	
	/**
	 * Get the Proxy port.
	 *
	 * @return Current Proxy port
	 */
	public int getProxyPort() { return proxyPort; }

	/**
	 * Set the Proxy username.
	 *
	 * @param newValue Value to set to.
	 */
	public void setProxyUser(final String newValue) { proxyUser = newValue; }
	
	/**
	 * Get the Proxy username.
	 *
	 * @return Current Proxy username
	 */
	public String getProxyUser() { return proxyUser; }
	
	/**
	 * Set the Proxy password.
	 *
	 * @param newValue Value to set to.
	 */
	public void setProxyPass(final String newValue) { proxyPass = newValue; }
	
	/**
	 * Get the Proxy password.
	 *
	 * @return Current Proxy password
	 */
	public String getProxyPass() { return proxyPass; }
}

