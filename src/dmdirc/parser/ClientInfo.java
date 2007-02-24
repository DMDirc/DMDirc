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
 * Contains information about known users.
 * 
 * @author            Shane Mc Cormack
 * @author            Chris Smith
 * @version           $Id$
 * @see IRCParser
 */
public class ClientInfo {
	private String sNickname = "";
	private String sIdent = "";	
	private String sHost = "";
	
	private IRCParser myParser; // Reference to parser object that owns this channel. Used for Modes

	/**
	 * Get a nickname of a user from a hostmask.
	 * Hostmask must match (?:)nick(?!ident)(?@host)
	 *
	 * @return nickname of user
	 */
	public static String ParseHost(String sWho) {
		// Get the nickname from the string.
		String sTemp[] = null;
		sTemp = sWho.split("@",2);
		sTemp = sTemp[0].split("!",2);
		sTemp = sTemp[0].split(":",2);
		if (sTemp.length != 1) { sWho = sTemp[1]; } else { sWho = sTemp[0]; }
		return sWho;
	}

	/**
	 * Create a new client object from a hostmask.
	 *
 	 * @param tParser Refernce to parser that owns this channelclient (used for modes)	 
	 * @param sHostmask Hostmask parsed by ParseHost to get nickname
	 * @see ParseHost
	 */
	public ClientInfo (IRCParser tParser, String sHostmask) { 
		setUserBits(sHostmask,true);
		myParser = tParser;
	}
	private void setUserBits (String sHostmask, boolean bUpdateNick) {
		String sTemp[] = null;
		sTemp = sHostmask.split(":",2);
		if (sTemp.length != 1) { sHostmask = sTemp[1]; } else { sHostmask = sTemp[0]; }

		sTemp = sHostmask.split("@",2);
		if (sTemp.length != 1) { sHost = sTemp[1]; }
		sTemp = sTemp[0].split("!",2);
		if (sTemp.length != 1) { sIdent = sTemp[1]; }
		if (bUpdateNick) { sNickname = sTemp[0]; }
	}

	/**
	 * Get a string representation of the user
	 *
	 * @return String representation of the user.
	 */
	public String toString() { return "Nickname: "+sNickname+" | Ident: "+sIdent+" | Host: "+sHost; }
	
	/**
	 * Get the nickname for this user
	 *
	 * @return Known nickname for user.
	 */
	public String getNickname() { return sNickname; }
	/**
	 * Get the ident for this user
	 *
	 * @return Known ident for user. (May be "")
	 */		
	public String getIdent() { return sIdent; }
	/**
	 * Get the hostname for this user
	 *
	 * @return Known host for user. (May be "")
	 */		
	public String getHost() { return sHost; }
	
	/**
	 * Get SVN Version information
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}