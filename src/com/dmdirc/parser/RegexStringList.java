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

package com.dmdirc.parser;

import java.util.ArrayList;

/**
 * IRC Parser Ignore list.
 *
 * @author Shane Mc Cormack
 * @version $Id$
 */
public class RegexStringList {
	/** Arraylist storing ignore patterns */
	private ArrayList<String> ignoreInfo = new ArrayList<String>();
	
	/**
	 * Add a new ignore pattern to the ignore list.
	 *
	 * @param pattern Regex syntax for the ignore (Pattern is matched case-insensitively as ^pattern$)
	 */
	public void add(String pattern) {
		for (int i = 0; i < this.count(); ++i) {
			if (pattern.equalsIgnoreCase(this.get(i))) {
				return;
			}
		}
		ignoreInfo.add(pattern);
	}
	
	/**
	 * Delete an ignore from the list
	 *
	 * @param position Position in the list to remove
	 */
	public void remove(int position) {
		if (position < this.count()) {
			ignoreInfo.remove(position);
		}
	}
	
	/**
	 * Clear the ignore list
	 */
	public void clear() {
		ignoreInfo.clear();
	}	
	
	/**
	 * Check if a string matches any of the ignores in the list
	 *
	 * @param check String to check (Patterns are matched case-insensitively as ^pattern$)
	 * @return integer showing the position of the first match in the ignore list (-1 if none)
	 */
	public int matches(String check) {
		for (int i = 0; i < this.count(); ++i) {
			if (check.matches("(?i)"+this.get(i))) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Check if a string matches a specific ignore in the list
	 *
	 * @param position Position to check
	 * @param check String to check (Patterns are matched case-insensitively as ^pattern$)
	 * @return boolean true/false
	 */
	public boolean matches(int position, String check) {
		if (position < this.count()) {
			return check.matches("(?i)"+this.get(position));
		} else {
			return false;
		}
	}
	
	/**
	 * Get the ignore pattern in a given position in the list.
	 *
	 * @param position Position to check
	 * @return String showing the pattern. ("" if position isn't valid)
	 */
	public String get(int position) {
		if (position < this.count()) {
			return ignoreInfo.get(position);
		} else {
			return "";
		}
	}
	
	/**
	 * Change the ignore pattern in a given position in the list.
	 *
	 * @param position Position to change
	 * @param pattern New pattern
	 */
	public void set(int position, String pattern) {
		if (position < this.count()) {
			ignoreInfo.set(position, pattern);
		}
	}
	
	/**
	 * Get the amount of ignores in the list.
	 *
	 * @return int showing the number of ignores
	 */
	public int count() {
		return ignoreInfo.size();
	}
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
