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

/**
 * Contains Channel List Mode information.
 * 
 * @author            Shane Mc Cormack
 * @author            Chris Smith
 * @version           $Id$
 * @see IRCParser
 */
public class ChannelListModeItem {
	/**
	 * The Item itself.
	 */
	protected String myItem = "";
	
	/**
	 * The Time the item was created.
	 */
	protected long myTime = 0;
	
	/**
	 * The Person who created the item
	 */
	protected String myOwner = "";
	
	/**
	 * Get The Item itself.
	 *
	 * @return The Item itself.
	 */
	public String getItem() { return myItem; }
	
	/**
	 * Get The Person who created the item.
	 *
	 * @return The Person who created the item.
	 */
	public String getOwner() { return myOwner; }
	
	/**
	 * Get The Time the item was created.
	 *
	 * @return The Time the item was created.
	 */
	public long getTime() { return myTime; }
	
	
	/**
	 * Create a new Item.
	 *
	 * @param item The item (ie: test!joe@user.com)
	 * @param owner The owner (ie: Dataforce)
	 * @param time The Time (ie: 1173389295)
	 */
	public ChannelListModeItem(final String item, final String owner, final long time) {
		myItem = item;
		myTime = time;
		myOwner = owner;
		if (!owner.equals("") && owner.charAt(0) == ':') { myOwner = owner.substring(1); }
	}
	
	/**
		* Returns a String representation of this object.
		*
		* @return String representation of this object
		*/
	public String toString() {
			return getItem();
	}
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }
}

