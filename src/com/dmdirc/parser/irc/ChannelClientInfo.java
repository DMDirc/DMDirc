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

import java.util.HashMap;
import java.util.Map;

/**
 * Contains information about a client on a channel.
 * 
 * @author Shane Mc Cormack
 * @author Chris Smith
 * @see IRCParser
 */
public class ChannelClientInfo {
	/** Reference to ClientInfo object this represents. */
	private final ClientInfo cClient;
	/** Integer representation of the channel modes assocated with this user. */
	private long nModes;
	/** Reference to the parser object that owns this channelclient, Used for modes. */
	private final IRCParser myParser;
	/** Reference to the channel object that owns this channelclient. */
	private final ChannelInfo myChannel;
	/** A Map to allow applications to attach misc data to this object */
	private Map myMap;
	
	/**
	 * Create a ChannelClient instance of a CLient.
	 *
	 * @param tParser Refernce to parser that owns this channelclient (used for modes)
	 * @param client Client that this channelclient represents
	 * @param channel Channel that owns this channelclient
	 */	
	public ChannelClientInfo(final IRCParser tParser, final ClientInfo client, final ChannelInfo channel) {
		myMap = new HashMap<Object, Object>();
		myParser = tParser;
		cClient = client;
		myChannel = channel;
		cClient.addChannelClientInfo(this);
	}
	
	/**
	 * Set the Map object attatched to this object.
	 *
	 * @param newMap New Map to attatch.
	 * @see #getMap
	 */
	public void setMap(final Map newMap) {
		myMap = newMap;
	}
	
	/**
	 * Get the Map object attatched to this object.
	 *
	 * @return Map to attatched to this.
	 * @see #setMap
	 */
	public Map getMap() {
		return myMap;
	}
	
	/**
	 * Get the client object represented by this channelclient.
	 *
	 * @return Client object represented by this channelclient
	 */
	public ClientInfo getClient() { return cClient; }
	/**
	 * Get the Channel object that owns this ChannelClient.
	 *
	 * @return Channel object that owns this ChannelClient
	 */
	public ChannelInfo getChannel() { return myChannel; }
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
	public void setChanMode(final long nNewMode) { nModes = nNewMode; }
	/**
	 * Get the modes this client has (Prefix modes).
	 *
	 * @return integer representing the modes this client has.
	 */
	public long getChanMode() { return nModes; }
	
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
		long nTemp = 0;
		final long nCurrentModes = this.getChanMode();

		for (long i = myParser.nNextKeyPrefix; i > 0; i = i / 2) {
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
	public long getImportantModeValue() {
		for (long i = myParser.nNextKeyPrefix; i > 0; i = i / 2) {
			if ((nModes & i) == i) { return i; }
		}
		return 0;
	}
	
	/**
	 * Get the most important mode this client has (o, v etc), or an empty
     * string if the client has no modes.
	 *
	 * @return String representing the most important mode.
	 */
	public String getImportantMode() {
		String sModes = this.getChanModeStr(false);
		if (!sModes.isEmpty()) { sModes = "" + sModes.charAt(0); }
		return sModes;
	}
	
	/**
	 * Get the most important prefix this client has (@, + etc), or an empty
     * string if the client has no modes.
	 *
	 * @return String representing the most important mode.
	 */
	public String getImportantModePrefix() {
		String sModes = this.getChanModeStr(true);
		if (!sModes.isEmpty()) { sModes = "" + sModes.charAt(0); }
		return sModes;
	}
	

	/**
	 * Get the String Value of ChannelClientInfo (ie @Nickname).
	 *
	 * @return String Value of user (inc prefix) (ie @Nickname)
	 */
	@Override
	public String toString() { 
		return this.getImportantModePrefix() + this.getNickname();
	}	
	
	/**
	 * Attempt to kick this user from the channel.
	 *
	 * @param sReason Why are they being kicked? "" for no reason
	 */
	public void kick(final String sReason) {
		myParser.sendString("KICK " + myChannel + " " + this.getNickname() + (sReason.isEmpty() ? sReason : " :" + sReason));
	}
	
	/**
	 * Get the "Complete" String Value of ChannelClientInfo (ie @+Nickname).
	 *
	 * @return String Value of user (inc prefix) (ie @+Nickname)
	 */
	public String toFullString() { return this.getChanModeStr(true) + this.getNickname(); }	

}

