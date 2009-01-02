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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Command Line argument parser.
 */
public class CLIParser {
	/** Singleton instance of CLIParser. */
	private static CLIParser me;
	
	/** Singleton instance of CLIParser. */
	CLIParam helpParam = null;
	
	/**
	 * Known arguments.
	 * This hashtable stores the arguments with their flags as the key.
	 */
	private final Map<String, CLIParam> params = new Hashtable<String, CLIParam>();
	
	/**
	 * Known arguments.
	 * This ArrayList stores every param type. (used for help)
	 */
	private final List<CLIParam> paramList = new ArrayList<CLIParam>();
	
	/**
	 * Redundant Strings.
	 * This ArrayList stores redundant strings found whilst parsing the params.
	 */
	private final List<String> redundant = new ArrayList<String>();
	
	/**
	 * Get a reference to the CLIParser.
	 */
	public static synchronized CLIParser getCLIParser() {
		if (me == null) { me = new CLIParser(); }
		return me;
	}
	
	/** Private constructor for CLIParser to prevent non-singleton instance. */
	private CLIParser() { }
	
	/** Clear known params from the hashtable. */
	public void clear() {
		params.clear();
		paramList.clear();
		redundant.clear();
	}
	
	/**
	 * Add a CLIParam to the cliparser.
	 *
	 * @param param CLIParam sub-class to use as a parameter.
	 * @return true if added, false if already exists.
	 */
	public boolean add(final CLIParam param) {
		final boolean validChar = param.getChr() == 0 || !params.containsKey(param.getChr());
		final boolean validString = param.getString().isEmpty() || !params.containsKey("-"+param.getString());
		if (validChar && validString) {
			if (param.getChr() != 0) {
				params.put(String.valueOf(param.getChr()), param);
			}
			if (!param.getString().isEmpty()) {
				params.put("-"+param.getString(), param);
			}
			paramList.add(param);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Get the number of times a param was given.
	 * In the case of params with both a char and string value, this number is
	 * the total for both.
	 *
	 * @param flag Flag to get count for
	 * @return number, or -1 if the param is invalud
	 */
	public int getParamNumber(final String flag) {
		if (params.containsKey(flag)) {
			return params.get(flag).getNumber();
		} else {
			return -1;
		}
	}
	
	/**
	 * Get a CLIParam object for a given flag.
	 *
	 * @param flag Flag to get param for
	 * @return CLIParam object, or null if there is none.
	 */
	public CLIParam getParam(final String flag) {
		if (params.containsKey(flag)) {
			return params.get(flag);
		} else {
			return null;
		}
	}
	
	/**
	 * Get the list of params.
	 *
	 * @return list of params.
	 */
	public List<CLIParam> getParamList() {
		return paramList;
	}
	
	/**
	 * Get the list of redundant strings.
	 *
	 * @return list of redundant strings.
	 */
	public List<String> getRedundant() {
		final List<String> result = new ArrayList<String>();
		for (String item : redundant) {
			result.add(item);
		}
		return result;
	}
	
	/**
	 * Set the "help" command.
	 *
	 * @param param Param to look for in wantsHelp.
	 */
	public void setHelp(final CLIParam param) {
		helpParam = param;
	}
	
	/**
	 * Check if the help parameter has been passed to the CLI.
	 */
	public boolean wantsHelp(final String[] args) {
		if (helpParam == null) { return false; }
		for (String arg : args) {
			if (arg.length() > 1 && arg.charAt(0) == '-') {
				final String name = arg.substring(1);
				if (name.equals("-")) {
					return false;
				} else {
					final CLIParam param = getParam(name);
					if (param == helpParam) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Show the help
	 */
	public void showHelp(final String title, final String usage) {
		System.out.println(title);
		System.out.println("------------------");
		System.out.println(usage);
		System.out.println(" ");
		for (CLIParam param : this.getParamList()) {
			if (param.getChr() == 0) {
				System.out.print("   ");
			} else {
				System.out.print("-"+param.getChr()+" ");
			}
			if (param.getString().isEmpty()) {
				System.out.print("\t\t");
			} else {
				System.out.print("--"+param.getString()+" ");
			}
			System.out.println("\t"+param.getDescription());
		}
	}
	
	/**
	 * Given a string array of arguments, parse as CLI Params.
	 *
	 * @param args Arguments to pass
	 * @param strict if True, will terminate if a given param is invalid.
	 */
	public void parseArgs(final String[] args, final boolean strict) {
		CLIParam lastParam = null;
		boolean allRedundant = false;
		for (String arg : args) {
			if (arg.length() > 1 && arg.charAt(0) == '-' && !allRedundant) {
				if (lastParam != null) { lastParam.setValue(""); }
				final String name = arg.substring(1);
				if (name.equals("-")) {
					allRedundant = true;
				} else {
					lastParam = getParam(name);
					if (lastParam == null) {
						System.out.println("Unknown Param: -"+name);
						if (helpParam != null) {
							String command = "";
							if (helpParam.getString().isEmpty()) {
								command = String.valueOf(helpParam.getChr());
							} else if (helpParam.getChr() != 0) {
								command = helpParam.getString();
							}
							if (!command.isEmpty()) {
								System.out.println("Use "+command+" to get help.");
							}
						}
						if (strict) {
							System.exit(1);
						}
					} else {
						lastParam.incNumber();
					}
				}
			} else {
				if (arg.charAt(0) == '\\' && arg.length() > 1) { arg = arg.substring(1); }
				if (lastParam == null || allRedundant || !lastParam.setValue(arg)) {
					redundant.add(arg);
				}
			}
		}
	}
}
