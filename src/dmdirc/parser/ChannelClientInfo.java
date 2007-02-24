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

package dmdirc.parser;

/**
 * Contains information about a client on a channel.
 * 
 * @author            Shane Mc Cormack
 * @author            Chris Smith
 * @version           $Id$
 * @see IRCParser
 */
public class ChannelClientInfo {
	private ClientInfo cClient = null;
	private int nModes;
	
	/**
	 * Create a ChannelClient instance of a CLient.
	 *
	 * @param client Client that this channelclient represents
	 */	
	public ChannelClientInfo(ClientInfo client) { cClient = client; }
	
	/**
	 * Get the client object represented by this channelclient.
	 *
	 * @return Client object represented by this channelclient
	 */
	public ClientInfo getClient() { return cClient; }
	/**
	 * Get the nickname of the client object represented by this channelclient.
	 *
	 * @return Nickname of the Client object represented by this channelclient
	 */	
	public String getNickname() { return cClient.getNickname(); }	
	
	/**
	 * Set the modes this client has (Prefix modes).
	 *
	 * @param nNewMode integer representing the modes this client has.
	 */	
	public void setChanMode(int nNewMode) { nModes = nNewMode; }
	/**
	 * Get the modes this client has (Prefix modes).
	 *
	 * @return integer representing the modes this client has.
	 */
	public int getChanMode() { return nModes; }
}