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

package uk.org.ownage.dmdirc.parser;

import java.util.Enumeration;

/**
 * Contains information about a client on a channel.
 * 
 * @author            Shane Mc Cormack
 * @author            Chris Smith
 * @version           $Id$
 * @see IRCParser
 */
public class ChannelClientInfo {
	/** Reference to ClientInfo object this represents. */
	private ClientInfo cClient = null;
	/** Integer representation of the channel modes assocated with this user. */
	private int nModes;
	/** Reference to the parser object that owns this channel, Used for modes. */
	private IRCParser myParser;
	
	/**
	 * Create a ChannelClient instance of a CLient.
	 *
	 * @param tParser Refernce to parser that owns this channelclient (used for modes)
	 * @param client Client that this channelclient represents
	 */	
	public ChannelClientInfo(IRCParser tParser, ClientInfo client) { myParser = tParser; cClient = client; }
	
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
	
	/**
	 * Get the modes this client has (Prefix modes) as a string.
	 * Returns all known modes that the client has.
	 * getChanModeStr(false).charAt(0) can be used to get the highest mode (o)
	 * getChanModeStr(true).charAt(0) can be used to get the highest prefix (@)
	 *
	 * @return String representing the modes this client has.
	 */
	public String getChanModeStr(boolean bPrefix) {
		String sModes = "", sTemp = "";
		Character cTemp;
		int nTemp = 0, nModes = this.getChanMode();

		for (int i = 1; i < myParser.nNextKeyPrefix; i = i*2) {
			if ((nModes & i) == i) {
				// Fixme - There must be a better alternative?
				for (Enumeration e = myParser.hPrefixModes.keys(); e.hasMoreElements();) {
					cTemp = (Character)e.nextElement();
					nTemp = (Integer)myParser.hPrefixModes.get(cTemp);
					if (nTemp == i) {
						if (bPrefix) { cTemp = myParser.hPrefixMap.get(cTemp); }
						sModes = sModes+cTemp;
						break;
					}
				}
			}
		}
		
		return sModes;
	}	
	
	/**
	 * Get SVN Version information
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
