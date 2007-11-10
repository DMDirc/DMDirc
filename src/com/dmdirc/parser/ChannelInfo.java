/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.parser;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Contains Channel information.
 * 
 * @author Shane Mc Cormack
 * @author Chris Smith
 * @version $Id$
 * @see IRCParser
 */
public final class ChannelInfo {
	/**
	 * Boolean repreenting the status of names requests.
	 * When this is false, any new names reply will cause current known channelclients to be removed.
	 */
	private boolean bAddingNames = true;
	
	/** Unixtimestamp representing time when the channel was created. */
	private long nCreateTime = 0;
	
	/** Current known topic in the channel. */
	private String sTopic = "";
	/** Last known user to set the topic (Full host where possible). */
	private String sTopicUser = "";
	/** Unixtimestamp representing time when the topic was set. */
	private long nTopicTime = 0;
	
	/** Known boolean-modes for channel. */
	private long nModes;
	/** Reference to the parser object that owns this channel, Used for modes. */
	private IRCParser myParser; // Reference to parser object that owns this channel. Used for Modes
	
	/** Channel Name. */
	private String sName = "";
	/** Hashtable containing references to ChannelClients. */
	private Hashtable<String, ChannelClientInfo> hChannelUserList = new Hashtable<String, ChannelClientInfo>();
	/** Hashtable storing values for modes set in the channel that use parameters. */
	private Hashtable<Character, String> hParamModes = new Hashtable<Character, String>();
	/** Hashtable storing list modes. */
	private Hashtable<Character, ArrayList<ChannelListModeItem>> hListModes = new Hashtable<Character, ArrayList<ChannelListModeItem>>();
	/**
	 * LinkedList storing status of mode adding.
	 * if an item is in this list for a mode, we are expecting new items for the list
	 */
	private LinkedList<Character> lAddingModes = new LinkedList<Character>();
	/** Modes waiting to be sent to the server. */
	private LinkedList<String> lModeQueue = new LinkedList<String>();
	/** A Map to allow applications to attach misc data to this object */
	private Map myMap;

	/**
	 * Create a new channel object.
	 *
	 * @param tParser Refernce to parser that owns this channelclient (used for modes)	 
	 * @param name Channel name.
	 */
	public ChannelInfo(final IRCParser tParser, final String name) {
		myMap = new HashMap<Object, Object>();
		myParser = tParser;
		sName = name;
	}
	
	/**
	 * Set the Map object attatched to this object.
	 *
	 * @param newMap New Map to attatch.
	 */
	public void setMap(Map newMap) {
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
	 * Set if we are getting a names request or not.
	 *
	 * @param newValue if false, any new names reply will cause current known channelclients to be removed.
	 */
	public void setAddingNames(final boolean newValue) { bAddingNames = newValue; }
	
	/**
	 * Get if we are getting a names request or not.
	 *
	 * @return if false, any new names reply will cause current known channelclients to be removed.
	 */
	public boolean getAddingNames() { return bAddingNames; }
	
	/**
	 * Get the name of this channel object.
	 *
	 * @return Channel name.
	 */	
	public String getName() { return sName; }
	/**
	 * Get the number of users known on this channel.
	 *
	 * @return Channel user count.
	 */
	public int getUserCount() { return hChannelUserList.size(); }
	
	/**
	 * Get the channel users.
	 *
	 * @return ArrayList of ChannelClients
	 */
	public ArrayList<ChannelClientInfo> getChannelClients() {
		final ArrayList<ChannelClientInfo> lClients = new ArrayList<ChannelClientInfo>();
		for (ChannelClientInfo client : hChannelUserList.values()) {
			lClients.add(client);
		}
		return lClients;
	}
	
	/**
	 * Empty the channel (Remove all known channelclients).
	 */
	protected void emptyChannel() {
		ClientInfo cTemp = null;
		for (ChannelClientInfo client : hChannelUserList.values()) {
			cTemp = client.getClient();
			cTemp.delChannelClientInfo(client);
			if (cTemp != myParser.getMyself() && !cTemp.checkVisibility()) {
				myParser.removeClient(cTemp);
			}
		}
		hChannelUserList.clear();
	}

	/**
	 * Get the ChannelClientInfo object associated with a nickname.
	 *
	 * @param sWho Nickname to return channelclient for
	 * @return ChannelClientInfo object requested, or null if not found
	 */
	public ChannelClientInfo getUser(String sWho) {
		sWho = ClientInfo.parseHost(sWho);
		sWho = myParser.toLowerCase(sWho);
		if (hChannelUserList.containsKey(sWho)) {
			return hChannelUserList.get(sWho);
		}
		return null;
	}
	
	/**
	 * Get the ChannelClientInfo object associated with a ClientInfo object.
	 *
	 * @param cWho ClientInfo to return ChannelClient for
	 * @return ChannelClientInfo object requested, or null if not found
	 */	
	public ChannelClientInfo getUser(final ClientInfo cWho) {
		for (ChannelClientInfo client : hChannelUserList.values()) {
			if (client.getClient() == cWho) {
				return client;
			}
		}
		return null;
	}
	
	/**
	 * Get the ChannelClientInfo object associated with a ClientInfo object.
	 *
	 * @param cClient Client object to be added to channel
	 * @return ChannelClientInfo object added, or an existing object if already known on channel
	 */
	protected ChannelClientInfo addClient(final ClientInfo cClient) {
		ChannelClientInfo cTemp = getUser(cClient);
		if (cTemp == null) { 
			cTemp = new ChannelClientInfo(myParser, cClient, this);
			hChannelUserList.put(myParser.toLowerCase(cTemp.getNickname()), cTemp);
		}
		return cTemp;
	}
	
	/**
	 * Remove ChannelClientInfo object associated with a ClientInfo object.
	 *
	 * @param cClient Client object to be removed from channel
	 */	
	protected void delClient(final ClientInfo cClient) {
		ChannelClientInfo cTemp = null;
		cTemp = getUser(cClient);
		if (cTemp != null) {
			ClientInfo clTemp = cTemp.getClient();
			clTemp.delChannelClientInfo(cTemp);
			if (clTemp != myParser.getMyself() && !clTemp.checkVisibility()) {
				myParser.removeClient(clTemp);
			}
			hChannelUserList.remove(myParser.toLowerCase(cTemp.getNickname()));
		}
	}	
	
	/**
	 * Rename a channelClient.
	 *
	 * @param oldNickname Nickname client used to be known as
	 * @param cChannelClient ChannelClient object with updated client object
	 */	
	protected void renameClient(final String oldNickname, final ChannelClientInfo cChannelClient) {
		ChannelClientInfo cTemp = null;
		if (hChannelUserList.containsKey(oldNickname)) {
			cTemp = hChannelUserList.get(oldNickname);
			if (cTemp == cChannelClient) {
				// Remove the old key
				hChannelUserList.remove(oldNickname);
				// Add with the new key. (getNickname will return the new name not the
				// old one)
				hChannelUserList.put(myParser.toLowerCase(cTemp.getNickname()), cTemp);
			}
		}
	}
	
	/**
	 * Set the create time.
	 *
	 * @param nNewTime New unixtimestamp time for the channel creation (Seconds since epoch, not milliseconds)
	 */
	protected void setCreateTime(final long nNewTime) { nCreateTime = nNewTime; }
	/**
	 * Get the Create time.
	 *
	 * @return Unixtimestamp time for the channel creation (Seconds since epoch, not milliseconds)
	 */
	public long getCreateTime() { return nCreateTime; }	
	
	/**
	 * Set the topic time.
	 *
	 * @param nNewTime New unixtimestamp time for the topic (Seconds since epoch, not milliseconds)
	 */
	protected void setTopicTime(final long nNewTime) { nTopicTime = nNewTime; }
	/**
	 * Get the topic time.
	 *
	 * @return Unixtimestamp time for the topic (Seconds since epoch, not milliseconds)
	 */
	public long getTopicTime() { return nTopicTime; }	
	
	/**
	 * Set the topic.
	 *
	 * @param sNewTopic New contents of topic
	 */	
	protected void setTopic(final String sNewTopic) { sTopic = sNewTopic; }
	/**
	 * Get the topic.
	 *
	 * @return contents of topic
	 */	
	public String getTopic() { return sTopic; }	

	/**
	 * Set the topic creator.
	 *
	 * @param sNewUser New user who set the topic (nickname if gotten on connect, full host if seen by parser)
	 */	
	protected void setTopicUser(final String sNewUser) { sTopicUser = sNewUser; }
	/**
	 * Get the topic creator.
	 *
	 * @return user who set the topic (nickname if gotten on connect, full host if seen by parser)
	 */	
	public String getTopicUser() { return sTopicUser; }
	
	/**
	 * Set the channel modes (as an integer).
	 *
	 * @param nNewMode new long representing channel modes. (Boolean only)
	 */	
	protected void setMode(final long nNewMode) { nModes = nNewMode; }
	/**
	 * Get the channel modes (as an integer).
	 *
	 * @return long representing channel modes. (Boolean only)
	 */	
	public long getMode() { return nModes; }	
	
	/**
	 * Get the channel modes (as a string representation).
	 *
	 * @return string representing modes. (boolean and non-list)
	 */	
	public String getModeStr() {
		final StringBuilder sModes = new StringBuilder("+");
		final StringBuilder sModeParams = new StringBuilder();
		String sTemp = "";
		long nTemp = 0;
		final long nChanModes = this.getMode();
		for (char cTemp : myParser.hChanModesBool.keySet()) {
			nTemp = myParser.hChanModesBool.get(cTemp);
			if ((nChanModes & nTemp) == nTemp) { sModes.append(cTemp); }
		}
		for (char cTemp : hParamModes.keySet()) {
			sTemp = hParamModes.get(cTemp);
			if (!sTemp.isEmpty()) {
				sModes.append(cTemp);
				sModeParams.append(" ").append(this.getModeParam(cTemp));
 			}
		}
		
		return sModes.append(sModeParams).toString();
	}	
	
	/**
	 * Set a channel mode that requires a parameter.
	 *
	 * @param cMode Character representing mode
	 * @param sValue String repreenting value (if "" mode is unset)
	 */	
	protected void setModeParam(final Character cMode, final String sValue) { 
		if (sValue.isEmpty()) {
			if (hParamModes.containsKey(cMode)) {
				hParamModes.remove(cMode);
			}
		} else {
			hParamModes.put(cMode, sValue);
		}
	}
	/**
	 * Get the value of a mode that requires a parameter.
	 *
	 * @param cMode Character representing mode
	 * @return string representing the value of the mode ("" if mode not set)
	 */	
	public String getModeParam(final Character cMode) { 
		if (hParamModes.containsKey(cMode)) { 
			return hParamModes.get(cMode); 
		}
		return "";
	}
	
	/**
	 * Add/Remove a value to a channel list.
	 *
	 * @param cMode Character representing mode
	 * @param newItem ChannelListModeItem representing the item
	 * @param bAdd Add or remove the value. (true for add, false for remove)
	 */
	protected void setListModeParam(final Character cMode, final ChannelListModeItem newItem, final boolean bAdd) { 
		if (!myParser.hChanModesOther.containsKey(cMode)) { return; }
		else if (myParser.hChanModesOther.get(cMode) != myParser.MODE_LIST) { return; }
		
		if (!hListModes.containsKey(cMode)) { 
			hListModes.put(cMode, new ArrayList<ChannelListModeItem>());	
		}
		final ArrayList<ChannelListModeItem> lModes = hListModes.get(cMode);
		for (int i = 0; i < lModes.size(); i++) {
			if (myParser.equalsIgnoreCase(lModes.get(i).getItem(), newItem.getItem())) { 
				if (bAdd) { return; }
				else { 
					lModes.remove(i);
					break;
				}
			}
		}
		if (bAdd) { lModes.add(newItem); }
	}
	
	/**
	 * Get the list object representing a channel mode.
	 *
	 * @param cMode Character representing mode
	 * @return ArrayList containing ChannelListModeItem in the list, or null if mode is invalid
	 */
	public ArrayList<ChannelListModeItem> getListModeParam(final Character cMode) { 
		if (!myParser.hChanModesOther.containsKey(cMode)) { return null; }
		else if (myParser.hChanModesOther.get(cMode) != myParser.MODE_LIST) { return null; }
		
		if (!hListModes.containsKey(cMode)) { 
			hListModes.put(cMode, new ArrayList<ChannelListModeItem>());
		}
		return hListModes.get(cMode);
	}
	
	/**
	 * Get the "adding state" of a list mode.
	 * 
	 * @param cMode Character representing mode 
	 * @return false if we are not expecting a 367 etc, else true.
	 */
	public boolean getAddState(final Character cMode) { 
		return lAddingModes.contains(cMode);
	}
	
	/**
	 * Get the "adding state" of a list mode.
	 * 
	 * @param cMode Character representing mode
	 * @param newState change the value returned by getAddState
	 */
	protected void setAddState(final Character cMode, final boolean newState) { 
		if (newState) {
			lAddingModes.add(cMode);
		} else {
			if (lAddingModes.contains(cMode)) { lAddingModes.remove(cMode); }
		}
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
	 * @param parameter Parameter needed to make change (not used if mode doesn't need a parameter)
	 */
	public void alterMode(final boolean positive, final Character mode, final String parameter) { 
		int modecount = 1;
		String modestr = "";
		if (myParser.h005Info.containsKey("MODES")) {
			try { 
				modecount = Integer.parseInt(myParser.h005Info.get("MODES")); 
			} catch (NumberFormatException e) { 
				modecount = 1; 
			}
		}
		if (!myParser.isUserSettable(mode)) { return; }
		if (lModeQueue.size() == modecount) { sendModes(); }
		if (positive) { modestr = "+"; } else { modestr = "-"; }
		modestr = modestr + mode;
		if (!myParser.hChanModesBool.containsKey(mode)) {
			// May need a param
			if (myParser.hPrefixModes.containsKey(mode)) {
				modestr = modestr + " " + parameter;
			} else {
				modecount = myParser.hChanModesOther.get(mode);
				if ((modecount & myParser.MODE_LIST) == myParser.MODE_LIST) {
					modestr = modestr + " " + parameter;
				} else if (!positive && ((modecount & myParser.MODE_UNSET) == myParser.MODE_UNSET)) {
					modestr = modestr + " " + parameter;
				} else if (positive && ((modecount & myParser.MODE_SET) == myParser.MODE_SET)) {
					modestr = modestr + " " + parameter;
				}
			}
		}
		myParser.callDebugInfo(myParser.DEBUG_INFO, "Queueing mode: %s", modestr);
		lModeQueue.add(modestr);
	}
	
	/**
	 * This function will send modes that are currently queued up to send.
	 * This assumes that the queue only contains the amount that are alowed to be sent
	 * and thus will try to send the entire queue in one go.<br><br>
	 * Modes are always sent negative then positive and not mixed.
	 */
	public void sendModes() { 
		if (lModeQueue.size() == 0) { return; }
		final StringBuilder positivemode = new StringBuilder();
		final StringBuilder positiveparam = new StringBuilder();
		final StringBuilder negativemode = new StringBuilder();
		final StringBuilder negativeparam = new StringBuilder();
		final StringBuilder sendModeStr = new StringBuilder();
		String modestr;
		String[] modeparam;
		boolean positive;
		for (int i = 0; i < lModeQueue.size(); ++i) {
			modeparam = lModeQueue.get(i).split(" ");
			modestr = modeparam[0];
			positive = modestr.charAt(0) == '+';
			if (positive) {
				positivemode.append(modestr.charAt(1));
				if (modeparam.length > 1) { positiveparam.append(" ").append(modeparam[1]); }
			} else {
				negativemode.append(modestr.charAt(1));
				if (modeparam.length > 1) { negativeparam.append(" ").append(modeparam[1]); }
			}
		}
		if (negativemode.length() > 0) { sendModeStr.append("-").append(negativemode); }
		if (positivemode.length() > 0) { sendModeStr.append("+").append(positivemode); }
		if (negativeparam.length() > 0) { sendModeStr.append(negativeparam); }
		if (positiveparam.length() > 0) { sendModeStr.append(positiveparam); }
		myParser.callDebugInfo(myParser.DEBUG_INFO, "Sending mode: %s", sendModeStr.toString());
		myParser.sendLine("MODE " + sName + " " + sendModeStr.toString());
		clearModeQueue();
	}
	
	/**
	 * This function will clear the mode queue (WITHOUT Sending).
	 */
	public void clearModeQueue() { 
		lModeQueue.clear();
	}
	
	/**
	 * Send a private message to the channel.
	 *
	 * @param sMessage Message to send
	 */
	public void sendMessage(final String sMessage) { 
		if (sMessage.isEmpty()) { return; }
		
		myParser.sendString("PRIVMSG " + sName + " :" + sMessage);	
	}
	
	/**
	 * Send a notice message to a target.
	 *
	 * @param sMessage Message to send
	 */
	public void sendNotice(final String sMessage) { 
		if (sMessage.isEmpty()) { return; }
		
		myParser.sendString("NOTICE " + sName + " :" + sMessage);	
	}

	/**
	 * Send a private message to a target.
	 *
	 * @param sMessage Message to send
	 */
	public void sendAction(final String sMessage) { 
		if (sMessage.isEmpty()) { return; }
		sendCTCP("ACTION", sMessage);
	}
	
	/**
	 * Send a CTCP to a target.
	 *
	 * @param sType Type of CTCP
	 * @param sMessage Optional Additional Parameters
	 */
	public void sendCTCP(final String sType, String sMessage) { 
		if (sType.isEmpty()) { return; }
		final char char1 = (char) 1;
		if (!sMessage.isEmpty()) { sMessage = " " + sMessage; }
		sendMessage(char1 + sType.toUpperCase() + sMessage + char1);
	}
	
	/**
	 * Send a CTCPReply to a target.
	 *
	 * @param sType Type of CTCP
	 * @param sMessage Optional Additional Parameters
	 */
	public void sendCTCPReply(final String sType, String sMessage) { 
		if (sType.isEmpty()) { return; }
		final char char1 = (char) 1;
		if (!sMessage.isEmpty()) { sMessage = " " + sMessage; }
		sendNotice(char1 + sType.toUpperCase() + sMessage + char1);	
	}
	
	/**
	 * Get a string representation of the Channel.
	 *
	 * @return String representation of the Channel.
	 */
	public String toString() { return sName; }
	
	/**
	 * Get the parser object that owns this channel
	 *
	 * @return The parser object that owns this channel
	 */
	public IRCParser getParser() { return myParser; }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo() { return "$Id$"; }	
}

