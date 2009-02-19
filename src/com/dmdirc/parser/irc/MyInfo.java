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

/**
 * Contains User information.
 * 
 * @author Shane Mc Cormack
 * @author Chris Smith
 * @see IRCParser
 */
public final class MyInfo {
	/** Character to prepend to nickname if in use (Default "_"). */
	private char prependChar = '_';	
	/** Nickname to attempt to use on IRC. */
	private String nickname;
	/**
	 * Alternative nickname to attempt to use on IRC.
	 * If the first nickname is in use, and a NickInUse message is recieved before 001, we
	 * will attempt to use this nickname instead.<br>
	 * If this also fails, we will start prepending the prependChar character (_) to the main nickname
	 */
	private String altNickname;
	/** Realname string to use */
	private String realname;
	/** Username to use, this doesn't matter when an ident server is running*/
	private String username;
	
	/**
	 * Create a new MyInfo object.
	 */
	public MyInfo() {
		String result;
		try {
			result = System.getProperty("user.name");
		} catch (SecurityException e) {
			result = null;
		}
		if (result == null || result.isEmpty()) {
			nickname = "IRCParser";
			username = "IRCParser";
			realname = "DMDIrc IRCParser";
			altNickname = "IRC-Parser";
		} else {
			nickname = result;
			username = nickname;
			realname = nickname+" - DMDIrc";
			altNickname = nickname+"-";
		}
	}
	
	/**
	 * Set the Nickname.
	 *
	 * @param newValue Value to set to.
	 */
	public void setNickname(final String newValue) {
		if (newValue != null && !newValue.isEmpty()) {
			nickname = newValue;
		}
	}
	
	/**
	 * Get the Nickname.
	 *
	 * @return Current Nickname
	 */
	public String getNickname() { return nickname; }
	
	/**
	 * Set the Alternative Nickname.
	 *
	 * @param newValue Value to set to.
	 */
	public void setAltNickname(final String newValue) {
		if (newValue != null && !newValue.isEmpty()) {
			altNickname = newValue;
		}
	}
	
	/**
	 * Get the Alternative Nickname.
	 *
	 * @return Current Nickname
	 */
	public String getAltNickname() { return altNickname; }
	
	/**
	 * Set the Realname.
	 *
	 * @param newValue Value to set to.
	 */
	public void setRealname(final String newValue) {
		if (newValue != null && !newValue.isEmpty()) {
			realname = newValue;
		}
	}
	
	/**
	 * Get the Realname.
	 *
	 * @return Current Realname
	 */
	public String getRealname() { return realname; }
	
	/**
	 * Set the Username.
	 *
	 * @param newValue Value to set to.
	 */
	public void setUsername(final String newValue) {
		if (newValue != null && !newValue.isEmpty()) {
			username = newValue;
		}
	}
	
	/**
	 * Get the Username.
	 *
	 * @return Current Username
	 */
	public String getUsername() { return username; }
	
	/**
	 * Set the Prepend Character.
	 *
	 * @param newValue Value to set to.
	 */
	public void setPrependChar(final char newValue) { prependChar = newValue; }
	
	/**
	 * Get the Prepend Character.
	 *
	 * @return Current Prepend Character
	 */
	public char getPrependChar() { return prependChar; }	

}
