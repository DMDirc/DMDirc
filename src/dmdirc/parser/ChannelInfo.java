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
 * SVN: $Id: ServerInfo.java 51 2007-02-14 12:28:53Z chris87 $
 */

package dmdirc.parser;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Contains Channel information.
 * 
 * @author            Shane Mc Cormack
 * @author            Chris Smith
 * @version           $Id: ServerInfo.java 51 2007-02-14 12:28:53Z chris87 $
 * @see IRCParser
 */
public class ChannelInfo {
	private String sName;
	private Hashtable<String,ChannelClientInfo> hUserList = new Hashtable<String,ChannelClientInfo>();

	public ChannelInfo (String name) { sName = name; }
	
	public String GetName() { return sName; }
	public int GetUserCount() { return hUserList.length(); }
	
	public ChannelClientInfo GetUser(String sWho) {
		sWho = ClientInfo.ParseHost(sWho);
		if (hUserList.containsKey(sWho)) { return hUserList.get(sWho); } else { return null; }
	}	
	
	public ChannelClientInfo GetUser(ClientInfo cWho) {
		ChannelClientInfo cTemp = null;
		if (hUserList.containsValue(cWho)) { 
			for (Enumeration e = hUserList.keys(); e.hasMoreElements();) {
				cTemp = hUserList.get(e.nextElement());
				if (cTemp.getClient() = cWho) { return cTemp; }
			}
			cTemp = null;
		}
		return cTemp;
	}
	
	public ChannelClientInfo addClient(ClientInfo cClient) {
		ChannelClientInfo cTemp = null;
		cTemp = GetUser(cClient);
		if (cTemp == null) { 
			cTemp = new ChannelClientInfo(cClient);
			hUserList.put(cTemp.GetNickname(),cTemp);
		}
		return cTemp;
	}
}