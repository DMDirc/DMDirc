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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Contains Channel information.
 * 
 * @author            Shane Mc Cormack
 * @author            Chris Smith
 * @version           $Id$
 * @see IRCParser
 */
public class ChannelInfo {
	/**
	 * Boolean repreenting the status of names requests.
	 * When this is false, any new names reply will cause current known channelclients to be removed.
	 */
	protected boolean bAddingNames = true;
	
	/** Current known topic in the channel. */
	private String sTopic = "";
	/** Last known user to set the topic (Full host where possible). */
	private String sTopicUser = "";
	/** Unixtimestamp representing time when the topic was set. */
	private long nTopicTime = 0;
	
	/** Known boolean-modes for channel. */
	private int nModes = 0;
	/** Reference to the parser object that owns this channel, Used for modes. */
	private IRCParser myParser; // Reference to parser object that owns this channel. Used for Modes
	
	/** Channel Name. */
	private String sName = "";
	/** Hashtable containing references to ChannelClients. */
	private Hashtable<String,ChannelClientInfo> hChannelUserList = new Hashtable<String,ChannelClientInfo>();
	/** Hashtable storing values for modes set in the channel that use parameters. */
	private Hashtable<Character,String> hParamModes = new Hashtable<Character,String>();
	/** Hashtable storing list modes. */
	private Hashtable<Character,ArrayList<ChannelListModeItem>> hListModes = new Hashtable<Character,ArrayList<ChannelListModeItem>>();
	/**
	 * LinkedList storing status of mode adding.
	 * if an item is in this list for a mode, we are expecting new items for the list
	 */
	private LinkedList<Character> lAddingModes = new LinkedList<Character>();

	/**
	 * Create a new channel object.
	 *
	 * @param tParser Refernce to parser that owns this channelclient (used for modes)	 
	 * @param name Channel name.
	 */
	public ChannelInfo (final IRCParser tParser, final String name) { myParser = tParser; sName = name; }
	
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
		ArrayList<ChannelClientInfo> lClients = new ArrayList<ChannelClientInfo>();
		for (final Enumeration e = hChannelUserList.keys(); e.hasMoreElements();) {
			lClients.add(hChannelUserList.get(e.nextElement()));
		}
		return lClients;
	}
	
	/**
	 * Empty the channel (Remove all known channelclients).
	 */
	protected void emptyChannel() { hChannelUserList.clear(); }

	/**
	 * Get the ChannelClientInfo object associated with a nickname.
	 *
	 * @param sWho Nickname to return channelclient for
	 * @return ChannelClientInfo object requested, or null if not found
	 */
	public ChannelClientInfo getUser(String sWho) {
		sWho = ClientInfo.parseHost(sWho);
		sWho = sWho.toLowerCase();
		if (hChannelUserList.containsKey(sWho)) { return hChannelUserList.get(sWho); } else { return null; }
	}	
	/**
	 * Get the ChannelClientInfo object associated with a ClientInfo object.
	 *
	 * @param cWho ClientInfo to return ChannelClient for
	 * @return ChannelClientInfo object requested, or null if not found
	 */	
	public ChannelClientInfo getUser(final ClientInfo cWho) {
		ChannelClientInfo cTemp = null;
		for (Enumeration e = hChannelUserList.keys(); e.hasMoreElements();) {
			cTemp = hChannelUserList.get(e.nextElement());
			if (cTemp.getClient() == cWho) { return cTemp; }
		}
		cTemp = null;
		return cTemp;
	}
	
	/**
	 * Get the ChannelClientInfo object associated with a ClientInfo object.
	 *
	 * @param cClient Client object to be added to channel
	 * @return ChannelClientInfo object added, or an existing object if already known on channel
	 */		
	protected ChannelClientInfo addClient(final ClientInfo cClient) {
		ChannelClientInfo cTemp = null;
		cTemp = getUser(cClient);
		if (cTemp == null) { 
			cTemp = new ChannelClientInfo(myParser,cClient);
			hChannelUserList.put(cTemp.getNickname().toLowerCase(),cTemp);
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
			hChannelUserList.remove(cTemp.getNickname().toLowerCase());
		}
	}	
	
	/**
	 * Set the topic time.
	 *
	 * @param nNewTime New unixtimestamp time for the topic (Seconds sinse epoch, not milliseconds)
	 */
	public void setTopicTime(final long nNewTime) { nTopicTime = nNewTime; }
	/**
	 * Get the topic time.
	 *
	 * @return Unixtimestamp time for the topic (Seconds sinse epoch, not milliseconds)
	 */
	public long getTopicTime() { return nTopicTime; }	
	
	/**
	 * Set the topic.
	 *
	 * @param sNewTopic New contents of topic
	 */	
	public void setTopic(final String sNewTopic) { sTopic = sNewTopic; }
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
	public void setTopicUser(final String sNewUser) { sTopicUser = sNewUser; }
	/**
	 * Get the topic creator.
	 *
	 * @return user who set the topic (nickname if gotten on connect, full host if seen by parser)
	 */	
	public String getTopicUser() { return sTopicUser; }
	
	/**
	 * Set the channel modes (as an integer).
	 *
	 * @param nNewMode new integer representing channel modes. (Boolean only)
	 */	
	public void setMode(final int nNewMode) { nModes = nNewMode; }
	/**
	 * Get the channel modes (as an integer).
	 *
	 * @return integer representing channel modes. (Boolean only)
	 */	
	public int getMode() { return nModes; }	
	
	/**
	 * Get the channel modes (as a string representation).
	 *
	 * @return string representing modes. (boolean and non-list)
	 */	
	public String getModeStr() { 
		String sModes = "+", sModeParams = "", sTemp = "";
		Character cTemp;
		int nTemp = 0;
		final int nChanModes = this.getMode();
		
		for (final Enumeration e = myParser.hChanModesBool.keys(); e.hasMoreElements();) {
			cTemp = (Character)e.nextElement();
			nTemp = myParser.hChanModesBool.get(cTemp);
			if ((nChanModes & nTemp) == nTemp) { sModes = sModes+cTemp; }
		}
		for (final Enumeration e = hParamModes.keys(); e.hasMoreElements();) {
			cTemp = (Character)e.nextElement();
			sTemp = hParamModes.get(cTemp);
			if (!sTemp.equals("")) {
				sModes = sModes+cTemp;
				sModeParams = sModeParams+" "+this.getModeParam(cTemp);
 			}
		}
		
		return sModes+sModeParams;
	}	
	
	/**
	 * Set a channel mode that requires a parameter.
	 *
	 * @param cMode Character representing mode
	 * @param sValue String repreenting value (if "" mode is unset)
	 */	
	public void setModeParam(final Character cMode, final String sValue) { 
		if (sValue.equals("")) {
			if (hParamModes.containsKey(cMode)) {
				hParamModes.remove(cMode);
			}
		} else {
			hParamModes.put(cMode,sValue);
		}
	}
	/**
	 * Get the value of a mode that requires a parameter.
	 *
	 * @param cMode Character representing mode
	 * @return string representing the value of the mode ("" if mode not set)
	 */	
	public String getModeParam(final Character cMode) { 
		if (hParamModes.containsKey(cMode)) { return hParamModes.get(cMode); }
		else { return ""; }
	}
	
	/**
	 * Add/Remove a value to a channel list.
	 *
	 * @param cMode Character representing mode
	 * @param newItem ChannelListModeItem representing the item
	 * @param bAdd Add or remove the value. (true for add, false for remove)
	 */
	public void setListModeParam(final Character cMode, final ChannelListModeItem newItem, final boolean bAdd) { 
		if (!myParser.hChanModesOther.containsKey(cMode)) { return; }
		else if (myParser.hChanModesOther.get(cMode) != myParser.cmList) { return; }
		
		if (!hListModes.containsKey(cMode)) { hListModes.put(cMode, new ArrayList<ChannelListModeItem>());	}
		ArrayList<ChannelListModeItem> lModes = hListModes.get(cMode);
		for (int i = 0; i < lModes.size(); i++) {
			if (lModes.get(i).getItem().equalsIgnoreCase(newItem.getItem())) { 
				if (bAdd) { return; }
				else { 
					lModes.remove(i);
					break;
				}
			}
		}
		if (bAdd) { lModes.add(newItem); }
		return;
	}
	
	/**
	 * Get the list object representing a channel mode.
	 *
	 * @param cMode Character representing mode
	 * @return ArrayList containing ChannelListModeItem in the list, or null if mode is invalid
	 */
	public ArrayList<ChannelListModeItem> getListModeParam(final Character cMode) { 
		if (!myParser.hChanModesOther.containsKey(cMode)) { return null; }
		else if (myParser.hChanModesOther.get(cMode) != myParser.cmList) { return null; }
		
		if (!hListModes.containsKey(cMode)) { hListModes.put(cMode, new ArrayList<ChannelListModeItem>());	}
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
	public void setAddState(final Character cMode, final boolean newState) { 
		if (newState) {
			lAddingModes.add(cMode);
		} else {
			if (lAddingModes.contains(cMode)) { lAddingModes.remove(cMode); }
		}
	}
	
	/**
	 * Send a private message to the channel.
	 *
	 * @param sMessage Message to send
	 */
	public void sendMessage(final String sMessage) { 
		if (sMessage.equals("")) { return; }
		
		myParser.sendString("PRIVMSG "+sName+" :"+sMessage);	
	}
	
	/**
	 * Send a notice message to a target.
	 *
	 * @param sMessage Message to send
	 */
	public void sendNotice(final String sMessage) { 
		if (sMessage.equals("")) { return; }
		
		myParser.sendString("NOTICE "+sName+" :"+sMessage);	
	}

	/**
	 * Send a private message to a target.
	 *
	 * @param sMessage Message to send
	 */
	public void sendAction(final String sMessage) { 
		if (sMessage.equals("")) { return; }
		sendCTCP("ACTION", sMessage);
	}
	
	/**
	 * Send a CTCP to a target.
	 *
	 * @param sType Type of CTCP
	 * @param sMessage Optional Additional Parameters
	 */
	public void sendCTCP(final String sType, String sMessage) { 
		if (sType.equals("")) { return; }
		final Character char1 = Character.valueOf((char)1);
		if (!sMessage.equals("")) { sMessage = " "+sMessage; }
		sendMessage(char1+sType.toUpperCase()+sMessage+char1);
	}
	
	/**
	 * Send a CTCPReply to a target.
	 *
	 * @param sType Type of CTCP
	 * @param sMessage Optional Additional Parameters
	 */
	public void sendCTCPReply(final String sType, String sMessage) { 
		if (sType.equals("")) { return; }
		final Character char1 = Character.valueOf((char)1);
		if (!sMessage.equals("")) { sMessage = " "+sMessage; }
		sendNotice(char1+sType.toUpperCase()+sMessage+char1);	
	}
	
	/**
	 * Get a string representation of the Channel.
	 *
	 * @return String representation of the Channel.
	 */
	public String toString() { return sName; }
	
	/**
	 * Get SVN Version information
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}

