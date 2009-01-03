/*
 * Copyright (c) 2006-2009 Shane Mc Cormack
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

package com.dmdirc.installer.cliparser;

/**
 * Command Line argument type.
 */
public abstract class CLIParam {
	/** Single Character flag for this param. */
	private final char charFlag;
	/** String flag for this param. */
	private final String stringFlag;
	/** Description of this flag. */
	private final String description;
	/** Number of times this param has been given. */
	private int number;
	
	/**
	 * Create a new CLIParam.
	 *
	 * @param chr Single Character flag for this param.
	 * @param str String flag for this param.
	 * @param desc Description of this flag.
	 */
	protected CLIParam(final char chr, final String str, final String desc) {
		charFlag = chr;
		stringFlag = str;
		description = desc;
	}
	
	/**
	 * Get the single character flag for this param.
	 */
	public final char getChr() { return charFlag; }
	
	/**
	 * Get the string flag for this param.
	 */
	public final String getString() { return stringFlag; }
	
	/**
	 * Get the description for this param.
	 */
	public final String getDescription() { return description; }
	
	/**
	 * Get the number of times this value has been given.
	 */
	public final int getNumber() { return number; }
	
	/**
	 * Increment the number of times this value has been given.
	 */
	public final void incNumber() { ++number; }
	
	/**
	 * Set the value of this param to the value given by a string.
	 *
	 * @param value String taken from command line to use as value.
	 * @return true if value was valid, else false.
	 */
	public abstract boolean setValue(final String value);
	
	/**
	 * Return the value of this param as a string.
	 *
	 * @return The value of this param as a string.
	 */
	public abstract String getStringValue();
}
