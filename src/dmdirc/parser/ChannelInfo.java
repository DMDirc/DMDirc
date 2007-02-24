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

import java.util.Hashtable;
import java.util.Enumeration;

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
	public boolean bAddingNames = true;
	
	private String sTopic;
	private String sTopicUser;
	private long nTopicTime;
	
	private String sName;
	private Hashtable<String,ChannelClientInfo> hChannelUserList = new Hashtable<String,ChannelClientInfo>();

	/**
	 * Create a new channel object
	 *
	 * @param name Channel name.
	 */
	public ChannelInfo (String name) { sName = name; }
	
	/**
	 * Get the name of this channel object
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
	 * Empty the channel (Remove all known channelclients)
	 */
	public void emptyChannel() { hChannelUserList.clear(); }

	/**
	 * Get the ChannelClientInfo object associated with a nickname
	 *
	 * @return ChannelClientInfo object requested, or null if not found
	 */
	public ChannelClientInfo getUser(String sWho) {
		sWho = ClientInfo.ParseHost(sWho);
		sWho = sWho.toLowerCase();
		if (hChannelUserList.containsKey(sWho)) { return hChannelUserList.get(sWho); } else { return null; }
	}	
	/**
	 * Get the ChannelClientInfo object associated with a ClientInfo object
	 *
	 * @return ChannelClientInfo object requested, or null if not found
	 */	
	public ChannelClientInfo getUser(ClientInfo cWho) {
		ChannelClientInfo cTemp = null;
		if (hChannelUserList.containsValue(cWho)) { 
			for (Enumeration e = hChannelUserList.keys(); e.hasMoreElements();) {
				cTemp = hChannelUserList.get(e.nextElement());
				if (cTemp.getClient() == cWho) { return cTemp; }
			}
			cTemp = null;
		}
		return cTemp;
	}
	
	/**
	 * Get the ChannelClientInfo object associated with a ClientInfo object
	 *
	 * @param cClient Client object to be added to channel
	 * @return ChannelClientInfo object added, or an existing object if already known on channel
	 */		
	public ChannelClientInfo addClient(ClientInfo cClient) {
		ChannelClientInfo cTemp = null;
		cTemp = getUser(cClient);
		if (cTemp == null) { 
			cTemp = new ChannelClientInfo(cClient);
			hChannelUserList.put(cTemp.getNickname(),cTemp);
		}
		return cTemp;
	}
	
	/**
	 * Remove ChannelClientInfo object associated with a ClientInfo object
	 *
	 * @param cClient Client object to be removed from channel
	 */	
	public void delClient(ClientInfo cClient) {
		ChannelClientInfo cTemp = null;
		cTemp = getUser(cClient);
		if (cTemp != null) {
			hChannelUserList.remove(cTemp);
		}
	}	
	
	/**
	 * Check if a channel name is valid in a certain parser object
	 *
	 * @param tParser Reference to parser instance that the channelname is requested for
	 * @param sChannelName Channel name to test
	 */
	public static boolean isValidChannelName(IRCParser tParser, String sChannelName) {
		return tParser.hChanPrefix.containsKey(sChannelName.charAt(0));
	}	
	
	/**
	 * Set the topic time
	 *
	 * @param nNewTime New unixtimestamp time for the topic (Seconds sinse epoch, not milliseconds)
	 */
	public void setTopicTime(long nNewTime) { nTopicTime = nNewTime; }
	/**
	 * Get the topic time
	 *
	 * @return Unixtimestamp time for the topic (Seconds sinse epoch, not milliseconds)
	 */
	public long getTopicTime() { return nTopicTime; }	
	
	/**
	 * Set the topic
	 *
	 * @param sNewTopic New contents of topic
	 */	
	public void setTopic(String sNewTopic) { sTopic = sNewTopic; }
	/**
	 * Get the topic
	 *
	 * @return contents of topic
	 */	
	public String getTopic() { return sTopic; }	

	/**
	 * Set the topic creator
	 *
	 * @param sNewUser New user who set the topic (nickname if gotten on connect, full host if seen by parser)
	 */	
	public void setTopicUser(String sNewUser) { sTopicUser = sNewUser; }
	/**
	 * Get the topic creator
	 *
	 * @return user who set the topic (nickname if gotten on connect, full host if seen by parser)
	 */	
	public String getTopicUser() { return sTopicUser; }
}