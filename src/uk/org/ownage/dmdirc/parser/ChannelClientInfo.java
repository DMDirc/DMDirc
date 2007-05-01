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

/**
 * Contains information about a client on a channel.
 * 
 * @author Shane Mc Cormack
 * @author Chris Smith
 * @version $Id$
 * @see IRCParser
 */
public final class ChannelClientInfo {
	/** Reference to ClientInfo object this represents. */
	private ClientInfo cClient;
	/** Integer representation of the channel modes assocated with this user. */
	private int nModes;
	/** Reference to the parser object that owns this channelclient, Used for modes. */
	private IRCParser myParser;
	/** Reference to the channel object that owns this channelclient. */
	private ChannelInfo myChannel;
	/** Misc object attached to this object. */
	private Object miscObject;
	
	/**
	 * Create a ChannelClient instance of a CLient.
	 *
	 * @param tParser Refernce to parser that owns this channelclient (used for modes)
	 * @param client Client that this channelclient represents
	 * @param channel Channel that owns this channelclient
	 */	
	public ChannelClientInfo(final IRCParser tParser, final ClientInfo client, final ChannelInfo channel) {
		myParser = tParser;
		cClient = client;
		myChannel = channel;
	}
	
	/**
	 * Set the misc object attatched to this 
	 *
	 * @param newObject New object to attatch.
	 */
	public void setMiscObject(Object newObject) {
		miscObject = newObject;
	}
	
	/**
	 * Get the misc object attatched to this 
	 *
	 * @return object to attatched to this .
	 */
	public Object getMiscObject() {
		return miscObject;
	}
	
	/**
	 * Check if this ChannelClient is linked to a fake client.
	 *
	 * @return True if this ChannelClient is linked to a fake client, else false
	 */
	public boolean isFake() { return cClient.isFake(); }
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
	public void setChanMode(final int nNewMode) { nModes = nNewMode; }
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
	 * @param bPrefix if this is true, prefixes will be returned (@+) not modes (ov)
	 * @return String representing the modes this client has.
	 */
	public String getChanModeStr(final boolean bPrefix) {
		StringBuilder sModes = new StringBuilder();
		int nTemp = 0;
		final int nCurrentModes = this.getChanMode();

		for (int i = myParser.nNextKeyPrefix; i > 0; i = i / 2) {
			if ((nCurrentModes & i) == i) {
				for (char cTemp : myParser.hPrefixModes.keySet()) {
					nTemp = myParser.hPrefixModes.get(cTemp);
					if (nTemp == i) {
						if (bPrefix) { cTemp = myParser.hPrefixMap.get(cTemp); }
						sModes = sModes.append(cTemp);
						break;
					}
				}
			}
		}
		
		return sModes.toString();
	}

	/**
	 * Get the value of the most important mode this client has (Prefix modes).
	 * A higher value, is a more important mode, 0 = no modes.
	 *
	 * @return integer representing the value of the most important mode.
	 */
	public int getImportantModeValue() {
		for (int i = myParser.nNextKeyPrefix; i > 0; i = i / 2) {
			if ((nModes & i) == i) { return i; }
		}
		return 0;
	}
	
	/**
	 * Get the most important mode this client has (o, v etc).
	 *
	 * @return String representing the most important mode.
	 */
	public String getImportantMode() {
		String sModes = this.getChanModeStr(false);
		if (!sModes.equals("")) { sModes = "" + sModes.charAt(0); }
		return sModes;
	}

	/**
	 * Get the most important prefix this client has (o, v etc).
	 *
	 * @return String representing the most important mode.
	 */
	public String getImportantModePrefix() {
		String sModes = this.getChanModeStr(true);
		if (!sModes.equals("")) { sModes = "" + sModes.charAt(0); }
		return sModes;
	}
	

	/**
	 * Get the String Value of ChannelClientInfo (ie @Nickname).
	 *
	 * @return String Value of user (inc prefix) (ie @Nickname)
	 */
	public String toString() { 
		return this.getImportantModePrefix() + this.getNickname();
	}	
	
	/**
	 * Attempt to kick this user from the channel.
	 *
	 * @param sReason Why are they being kicked? "" for no reason
	 */
	public void kick(String sReason) {
		if (!sReason.equals("")) { sReason = " :" + sReason; }
		myParser.sendString("KICK " + myChannel + " " + this.getNickname() + sReason);
	}
	
	/**
	 * Get the "Complete" String Value of ChannelClientInfo (ie @+Nickname).
	 *
	 * @return String Value of user (inc prefix) (ie @+Nickname)
	 */
	public String toFullString() { return this.getChanModeStr(true) + this.getNickname(); }	
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo() { return "$Id$"; }	
}

