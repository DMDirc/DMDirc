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

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains information about known users.
 * 
 * @author Shane Mc Cormack
 * @author Chris Smith
 * @see IRCParser
 */
public final class ClientInfo {
	/** Known nickname of client. */
	private String sNickname = "";
	/** Known ident of client. */
	private String sIdent = "";	
	/** Known host of client. */
	private String sHost = "";
	/** Known user modes of client. */
	private long nModes;
	/** Known Away Reason of client. */
	private String myAwayReason = "";
	/** Known RealName of client. */
	private String sRealName = "";
	/** Known away state for client. */
	private boolean bIsAway;
	/** Is this a fake client created just for a callback? */
	private boolean bIsFake;
	/** Reference to the parser object that owns this channel, Used for modes. */
	private final IRCParser myParser;
	/** A Map to allow applications to attach misc data to this object */
	private Map myMap;
	/** List of ChannelClientInfos that point to this */
	private final Map<String, ChannelClientInfo> myChannelClientInfos = new Hashtable<String, ChannelClientInfo>();
	/** Modes waiting to be sent to the server. */
	private final List<String> lModeQueue = new LinkedList<String>();

	/**
	 * Create a new client object from a hostmask.
	 *
 	 * @param tParser Refernce to parser that owns this channelclient (used for modes)	 
	 * @param sHostmask Hostmask parsed by parseHost to get nickname
	 * @see ClientInfo#parseHost
	 */
	public ClientInfo(final IRCParser tParser, final String sHostmask) { 
		myMap = new HashMap<Object, Object>();
		setUserBits(sHostmask, true);
		myParser = tParser;
	}

	/**
	 * Set the Map object attatched to this object.
	 *
	 * @param newMap New Map to attatch.
	 */
	public void setMap(final Map newMap) {
		myMap = newMap;
	}
	
	/**
	 * Get the Map object attatched to this object.
	 *
	 * @return Map to attatched to this.
	 */
	public Map getMap() {
		return myMap;
	}

	/**
	 * Check if this is a fake client.
	 *
	 * @return True if this is a fake client, else false
	 */
	public boolean isFake() { return bIsFake; }
	/**
	 * Check if this client is actually a server.
	 *
	 * @return True if this client is actually a server.
	 */
	public boolean isServer() { return !(sNickname.indexOf(':') == -1); }
	/**
	 * Set if this is a fake client.
	 * This returns "this" and thus can be used in the construction line.
	 *
	 * @param newValue new value for isFake - True if this is a fake client, else false
	 * @return this Object
	 */
	public ClientInfo setFake(final boolean newValue) { bIsFake = newValue; return this; }

	/**
	 * Get a nickname of a user from a hostmask.
	 * Hostmask must match (?:)nick(?!ident)(?@host)
	 *
	 * @param sWho Hostname to parse
	 * @return nickname of user
	 */
	public static String parseHost(final String sWho) {
		// Get the nickname from the string.
		return parseHostFull(sWho)[0];
	}
	
	/**
	 * Get a nick ident and host of a user from a hostmask.
	 * Hostmask must match (?:)nick(?!ident)(?@host)
	 *
	 * @param sWho Hostname to parse
	 * @return Array containing details. (result[0] -> Nick | result[1] -> Ident | result[2] -> Host)
	 */
	public static String[] parseHostFull(String sWho) {
		String[] sTemp = null;
		final String[] result = new String[3];
		if (!sWho.isEmpty() && sWho.charAt(0) == ':') { sWho = sWho.substring(1); }
		sTemp = sWho.split("@", 2);
		if (sTemp.length == 1) { result[2] = ""; } else { result[2] = sTemp[1]; }
		sTemp = sTemp[0].split("!", 2);
		if (sTemp.length == 1) { result[1] = ""; } else { result[1] = sTemp[1]; }
		result[0] = sTemp[0];
		
		return result;
	}

	/**
	 * Set the nick/ident/host of this client.
	 *
	 * @param sHostmask takes a host (?:)nick(?!ident)(?@host) and sets nick/host/ident variables
	 * @param bUpdateNick if this is false, only host/ident will be updated.
	 */	
	public void setUserBits(final String sHostmask, final boolean bUpdateNick) {
		setUserBits(sHostmask, bUpdateNick, false);
	}
	
	/**
	 * Set the nick/ident/host of this client.
	 *
	 * @param sHostmask takes a host (?:)nick(?!ident)(?@host) and sets nick/host/ident variables
	 * @param bUpdateNick if this is false, only host/ident will be updated.
	 * @param allowBlank if this is true, ident/host will be set even if
	 *                   parseHostFull returns empty values for them
	 */	
	public void setUserBits(final String sHostmask, final boolean bUpdateNick, final boolean allowBlank) {
		final String[] sTemp = parseHostFull(sHostmask);
		if (!sTemp[2].isEmpty() || allowBlank) { sHost = sTemp[2]; }
		if (!sTemp[1].isEmpty() || allowBlank) { sIdent = sTemp[1]; }
		if (bUpdateNick) { sNickname = sTemp[0]; }
	}
	
	/**
	 * Get a string representation of the user.
	 *
	 * @return String representation of the user.
	 */
	@Override
	public String toString() { return sNickname + "!" + sIdent + "@" + sHost; }
	
	/**
	 * Get the nickname for this user.
	 *
	 * @return Known nickname for user.
	 */
	public String getNickname() { return sNickname; }
	
	/**
	 * Get the ident for this user.
	 *
	 * @return Known ident for user. (May be "")
	 */		
	public String getIdent() { return sIdent; }
	
	/**
	 * Get the hostname for this user.
	 *
	 * @return Known host for user. (May be "")
	 */		
	public String getHost() { return sHost; }
	
	/**
	 * Set the away state of a user.
	 * Automatically sets away reason to "" if set to false
	 *
	 * @param bNewState Boolean representing state. true = away, false = here
	 */	
	protected void setAwayState(final boolean bNewState) {
		bIsAway = bNewState;
		if (!bIsAway) { myAwayReason = ""; }
	}
	
	/**
	 * Get the away state of a user.
	 *
	 * @return Boolean representing state. true = away, false = here
	 */	
	public boolean getAwayState() { return bIsAway; }
	
	/**
	 * Get the Away Reason for this user.
	 *
	 * @return Known away reason for user.
	 */
	public String getAwayReason() { return myAwayReason; }
	
	/**
	 * Set the Away Reason for this user.
	 * Automatically set to "" if awaystate is set to false
	 *
	 * @param newValue new away reason for user.
	 */
	protected void setAwayReason(final String newValue) { myAwayReason = newValue; }
	
	/**
	 * Get the RealName for this user.
	 *
	 * @return Known RealName for user.
	 */
	public String getRealName() { return sRealName; }
	
	/**
	 * Set the RealName for this user.
	 *
	 * @param newValue new RealName for user.
	 */
	protected void setRealName(final String newValue) { sRealName = newValue; }
	
	/**
	 * Set the user modes (as an integer).
	 *
	 * @param nNewMode new long representing channel modes. (Boolean only)
	 */	
	protected void setUserMode(final long nNewMode) { nModes = nNewMode; }
	
	/**
	 * Get the user modes (as an integer).
	 *
	 * @return long representing channel modes. (Boolean only)
	 */	
	public long getUserMode() { return nModes; }	
	
	/**
	 * Get the user modes (as a string representation).
	 *
	 * @return string representing modes. (boolean and non-list)
	 */	
	public String getUserModeStr() { 
		final StringBuilder sModes = new StringBuilder("+");
		long nTemp = 0;
		final long nChanModes = this.getUserMode();
		
		for (char cTemp : myParser.hUserModes.keySet()) {
			nTemp = myParser.hUserModes.get(cTemp);
			if ((nChanModes & nTemp) == nTemp) { sModes.append(cTemp); }
		}
		
		return sModes.toString();
	}
	
	/**
	 * Is this client an oper?
	 * This is a guess currently based on user-modes and thus only works on the
	 * parsers own client.
	 *
	 * @return True/False if this client appears to be an oper
	 */
	public boolean isOper() {
		final String modestr = getUserModeStr();
		return (modestr.indexOf('o') > -1) || (modestr.indexOf('O') > -1);
	}
	
	/**
	 * Add a ChannelClientInfo as a known reference to this client.
	 *
	 * @param cci ChannelClientInfo to add as a known reference
	 */	
	public void addChannelClientInfo(final ChannelClientInfo cci) {
		final String key = myParser.getIRCStringConverter().toLowerCase(cci.getChannel().getName());
		if (!myChannelClientInfos.containsKey(key)) {
			myChannelClientInfos.put(key, cci);
		}
	}
	
	/**
	 * Remove a ChannelClientInfo as a known reference to this client.
	 *
	 * @param cci ChannelClientInfo to remove as a known reference
	 */	
	public void delChannelClientInfo(final ChannelClientInfo cci) {
		final String key = myParser.getIRCStringConverter().toLowerCase(cci.getChannel().getName());
		if (myChannelClientInfos.containsKey(key)) {
			myChannelClientInfos.remove(key);
		}
	}
	
	/**
	 * Check to see if a client is still known on any of the channels we are on.
	 *
	 * @return Boolean to see if client is still visable.
	 */
	public boolean checkVisibility() {
		return !myChannelClientInfos.isEmpty();
	}
	
	/**
	 * Check how many channels this client is known on.
	 *
	 * @return int with the count of known channels
	 */	
	public int channelCount() {
		return myChannelClientInfos.size();
	}
	
	/**
	 * Get a list of channelClients that point to this object.
	 *
	 * @return int with the count of known channels
	 */	
	public List<ChannelClientInfo> getChannelClients() {
		final List<ChannelClientInfo> result = new ArrayList<ChannelClientInfo>();
		for (ChannelClientInfo cci : myChannelClientInfos.values()) {
			result.add(cci);
		}
		return result;
	}
	
	/**
	 * Adjust the channel modes on a channel.
	 * This function will queue modes up to be sent in one go, according to 005 params.
	 * If less modes are altered than the queue accepts, sendModes() must be called.<br><br>
	 * sendModes is automatically called if you attempt to add more modes than is allowed
	 * to be queued
	 *
	 * @param positive Is this a positive mode change, or a negative mode change
	 * @param mode Character representing the mode to change
	 */
	public void alterMode(final boolean positive, final Character mode) {
		if (isFake()) { return; }
		int modecount = 1;
		String modestr = "";
		if (myParser.h005Info.containsKey("MODES")) {
			try { 
				modecount = Integer.parseInt(myParser.h005Info.get("MODES")); 
			} catch (NumberFormatException e) { 
				modecount = 1;
			}
		}
		modestr = ((positive) ? "+" : "-") + mode;
		if (!myParser.hUserModes.containsKey(mode)) { return; }
		final String teststr = ((positive) ? "-" : "+") + mode;
		if (lModeQueue.contains(teststr)) {
			lModeQueue.remove(teststr);
			return;
		} else if (lModeQueue.contains(modestr)) {
			return;
		}
		myParser.callDebugInfo(myParser.DEBUG_INFO, "Queueing user mode: %s", modestr);
		lModeQueue.add(modestr);
		if (lModeQueue.size() == modecount) { sendModes(); }
	}
	
	/**
	 * This function will send modes that are currently queued up to send.
	 * This assumes that the queue only contains the amount that are alowed to be sent
	 * and thus will try to send the entire queue in one go.<br><br>
	 * Modes are always sent negative then positive and not mixed.
	 */
	public void sendModes() {
		if (lModeQueue.isEmpty()) { return; }
		final StringBuilder positivemode = new StringBuilder();
		final StringBuilder negativemode = new StringBuilder();
		final StringBuilder sendModeStr = new StringBuilder();
		String modestr;
		boolean positive;
		for (int i = 0; i < lModeQueue.size(); ++i) {
			modestr = lModeQueue.get(i);
			positive = modestr.charAt(0) == '+';
			if (positive) {
				positivemode.append(modestr.charAt(1));
			} else {
				negativemode.append(modestr.charAt(1));
			}
		}
		if (negativemode.length() > 0) { sendModeStr.append("-").append(negativemode); }
		if (positivemode.length() > 0) { sendModeStr.append("+").append(positivemode); }
		myParser.callDebugInfo(IRCParser.DEBUG_INFO, "Sending mode: %s", sendModeStr.toString());
		myParser.sendLine("MODE " + sNickname + " " + sendModeStr.toString());
		clearModeQueue();
	}
	
	/**
	 * This function will clear the mode queue (WITHOUT Sending).
	 */
	public void clearModeQueue() { 
		lModeQueue.clear();
	}
	
	/**
	 * Get the parser object that owns this client.
	 *
	 * @return The parser object that owns this client
	 */
	public IRCParser getParser() { return myParser; }

}
