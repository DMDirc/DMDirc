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
	public boolean bAddingNames = true;
	
	private String sTopic;
	private String sTopicUser;
	private long nTopicTime;
	
	private String sName;
	private Hashtable<String,ChannelClientInfo> hChannelUserList = new Hashtable<String,ChannelClientInfo>();

	public ChannelInfo (String name) { sName = name; }
	
	public String getName() { return sName; }
	public int getUserCount() { return hChannelUserList.size(); }
	
	public void emptyChannel() { hChannelUserList.clear(); }
	
	public ChannelClientInfo getUser(String sWho) {
		sWho = ClientInfo.ParseHost(sWho);
		sWho = sWho.toLowerCase();
		if (hChannelUserList.containsKey(sWho)) { return hChannelUserList.get(sWho); } else { return null; }
	}	
	
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
	
	public ChannelClientInfo addClient(ClientInfo cClient) {
		ChannelClientInfo cTemp = null;
		cTemp = getUser(cClient);
		if (cTemp == null) { 
			cTemp = new ChannelClientInfo(cClient);
			hChannelUserList.put(cTemp.getNickname(),cTemp);
		}
		return cTemp;
	}
	
	public void delClient(ClientInfo cClient) {
		ChannelClientInfo cTemp = null;
		cTemp = getUser(cClient);
		if (cTemp != null) {
			hChannelUserList.remove(cTemp);
		}
	}	
	
	public static boolean isValidChannelName(IRCParser tParser, String sChannelName) {
		return tParser.hChanPrefix.containsKey(sChannelName.charAt(0));
	}	
	

	public void setTopicTime(long nNewTime) { nTopicTime = nNewTime; }
	public long getTopicTime() { return nTopicTime; }	
	
	public void setTopic(String sNewTopic) { sTopic = sNewTopic; }
	public String getTopic() { return sTopic; }	
	
	public void setTopicUser(String sNewUser) { sTopicUser = sNewUser; }
	public String getTopicUser() { return sTopicUser; }
}