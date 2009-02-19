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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * IRC Parser Ignore list.
 *
 * @author Shane Mc Cormack
 */
public class RegexStringList {

	/** Arraylist storing ignore patterns. */
	protected final List<String> ignoreInfo = new ArrayList<String>();

	/**
	 * Creates a new instance of RegexStringList.
	 */
	public RegexStringList() {
		// Do nothing
	}

	/**
	 * Creates a new instance of RegexStringList, with the specified items.
	 *
	 * @param items Items to add to this RegexStringList
	 */
	public RegexStringList(final List<String> items) {
		addAll(items);
	}

	/**
	 * Add a new ignore pattern to the ignore list.
	 *
	 * @param pattern Regex syntax for the ignore (Pattern is matched case-insensitively as ^pattern$)
	 */
	public void add(final String pattern) {
		for (String target : ignoreInfo) {
			if (pattern.equalsIgnoreCase(target)) {
				return;
			}
		}

		ignoreInfo.add(pattern);
	}

	/**
	 * Adds a set of patterns to the list.
	 *
	 * @param patterns A list of patterns to be added
	 */
	public void addAll(final List<String> patterns) {
		for (String pattern : patterns) {
			add(pattern);
		}
	}

	/**
	 * Delete an ignore from the list.
	 *
	 * @param position Position in the list to remove
	 */
	public void remove(final int position) {
		if (position < this.count()) {
			ignoreInfo.remove(position);
		}
	}

	/**
	 * Clear the ignore list.
	 */
	public void clear() {
		ignoreInfo.clear();
	}

	/**
	 * Check if a string matches any of the ignores in the list.
	 *
	 * @param check String to check (Patterns are matched case-insensitively as ^pattern$)
	 * @return integer showing the position of the first match in the ignore list (-1 if none)
	 * @throws PatternSyntaxException if one of the items in the list is an invalid regex
	 */
	public int matches(final String check) throws PatternSyntaxException {
		for (int i = 0; i < this.count(); ++i) {
			if (check.matches("(?i)"+this.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Check if a string matches a specific ignore in the list.
	 *
	 * @param position Position to check
	 * @param check String to check (Patterns are matched case-insensitively as ^pattern$)
	 * @return boolean true/false
	 * @throws PatternSyntaxException if the item is an invalid regex
	 */
	public boolean matches(final int position, final String check) throws PatternSyntaxException {
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
	public String get(final int position) {
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
	public void set(final int position, final String pattern) {
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

}
