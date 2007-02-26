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

package org.ownage.dmdirc.parser;

/**
 * Contains User information.
 * 
 * @author            Shane Mc Cormack
 * @author            Chris Smith
 * @version           $Id$
 * @see IRCParser
 */
public class MyInfo {
	/** Nickname to attempt to use on IRC. */
	public String sNickname = "IRCParser";
	/**
	 * Alternative nickname to attempt to use on IRC.
	 * If the first nickname is in use, and a NickInUse message is recieved before 001, we
	 * will attempt to use this nickname instead.<br>
	 * If this also fails, we will start prepending _ to the main nickname
	 */
	public String sAltNickname = "IRC-Parser";
	/** Realname string to use. */
	public String sRealname = "Java Test IRCParser";
	/** Username to use, this doesn't matter when an ident server is running. */
	public String sUsername = "IRCParser";
	
	/**
	 * Get SVN Version information
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
