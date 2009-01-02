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
public class StringParam extends CLIParam {
	/** The value of this param. */
	private String myValue = "";
	
	/**
	 * Create a new StringParam.
	 *
	 * @param chr Single Character flag for this param.
	 * @param str String flag for this param.
	 * @param desc Description of this flag.
	 */
	public StringParam(final char chr, final String str, final String desc) {
		super(chr, str, desc);
	}
	
	/**
	 * Set the value of this param to the value given by a string.
	 *
	 * @param value String taken from command line to use as value.
	 * @return true if value was valid, else false.
	 */
	public boolean setValue(final String value) {
		myValue = value;
		return true;
	}
	
	/**
	 * Get the value of this param.
	 *
	 * @return Value of this parameter
	 */
	public String getValue() {
		return myValue;
	}
	
	/**
	 * Return the value of this param as a string.
	 *
	 * @return The value of this param as a string.
	 */
	public String getStringValue() {
		return getValue();
	}
}
