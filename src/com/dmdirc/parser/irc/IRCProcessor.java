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

import com.dmdirc.parser.irc.callbacks.CallbackManager;

/**
 * IRCProcessor.
 * Superclass for all IRCProcessor types.
 *
 * @author Shane Mc Cormack
 */
public abstract class IRCProcessor {
	/** Reference to the IRCParser that owns this IRCProcessor. */
	protected IRCParser myParser;
	
	/** Reference to the Processing in charge of this IRCProcessor. */
	protected ProcessingManager myManager;

	// Some functions from the main parser are useful, and having to use myParser.functionName
	// is annoying, so we also implement them here (calling them again using myParser)
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected IRCProcessor(final IRCParser parser, final ProcessingManager manager) {
		this.myParser = parser;
		this.myManager = manager;
	}

	/**
	 * Callback to all objects implementing the IErrorInfo Interface.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IErrorInfo
	 * @param errorInfo ParserError object representing the error.
	 * @return true if a method was called, false otherwise
	 */
	protected final boolean callErrorInfo(final ParserError errorInfo) {
		return myParser.callErrorInfo(errorInfo);
	}
	
	/**
	 * Callback to all objects implementing the DebugInfo Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IDebugInfo
	 * @param level Debugging Level (DEBUG_INFO, ndSocket etc)
	 * @param data Debugging Information
	 * @param args Formatting String Options
	 * @return true if a method was called, false otherwise
	 */
	protected final boolean callDebugInfo(final int level, final String data, final Object... args) {
		return myParser.callDebugInfo(level, String.format(data, args));
	}
	
	/**
	 * Callback to all objects implementing the DebugInfo Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IDebugInfo
	 * @param level Debugging Level (DEBUG_INFO, ndSocket etc)
	 * @param data Debugging Information
	 * @return true if a method was called, false otherwise
	 */
	protected final boolean callDebugInfo(final int level, final String data) {
		return myParser.callDebugInfo(level, data);
	}
	
	/**
	 * Check if a channel name is valid .
	 *
	 * @param sChannelName Channel name to test
	 * @return true if name is valid on the current connection, false otherwise. (Always false before noMOTD/MOTDEnd)
	 */
	protected final boolean isValidChannelName(final String sChannelName) {
		return myParser.isValidChannelName(sChannelName);
	}
	
	/**
	 * Get the ClientInfo object for a person.
	 *
	 * @param sWho Who can be any valid identifier for a client as long as it contains a nickname (?:)nick(?!ident)(?@host)
	 * @return ClientInfo Object for the client, or null
	 */
	protected final ClientInfo getClientInfo(final String sWho) {
		return myParser.getClientInfo(sWho);
	}
	
	/**
	 * Get the ChannelInfo object for a channel.
	 *
	 * @param sWhat This is the name of the channel.
	 * @return ChannelInfo Object for the channel, or null
	 */
	protected final ChannelInfo getChannelInfo(final String sWhat) {
		return myParser.getChannelInfo(sWhat);
	}
	
	/**
	 * Get a reference to the CallbackManager.
	 *
	 * @return Reference to the CallbackManager
	 */
	protected final CallbackManager getCallbackManager() {
		return myParser.getCallbackManager();
	}
	
	/** 
	 * Send a line to the server and add proper line ending.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 */
	protected final void sendString(final String line) {
		myParser.sendString(line);
	}
	
	/**
	 * Process a Line.
	 *
	 * @param sParam Type of line to process ("005", "PRIVMSG" etc)
	 * @param token IRCTokenised line to process
	 */
	public abstract void process(final String sParam, final String[] token);
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public abstract String[] handles();
	
	/** 
	 * Get the name for this Processor.
	 * @return the name of this processor
	 */
	public final String getName() {
		final Package thisPackage = this.getClass().getPackage();
		int packageLength = 0;
		if (thisPackage != null) {
			packageLength = thisPackage.getName().length() + 1;
		}
		return this.getClass().getName().substring(packageLength);
	}
	
	/** 
	 * Get the name for this Processor in lowercase.
	 * @return lower case name of this processor
	 */
	public final String getLowerName() {
		return this.getName().toLowerCase();
	}
	
	/** 
	 * Get the name for this Processor.
	 * @return the name of this processor
	 */
	public final String toString() { return this.getName(); }
	
}
