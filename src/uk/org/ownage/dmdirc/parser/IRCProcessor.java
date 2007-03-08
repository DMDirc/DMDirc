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
 * SVN: $Id: IRCProcessor.java 273 2007-03-03 02:09:17Z greboid $
 */

package uk.org.ownage.dmdirc.parser;

import uk.org.ownage.dmdirc.parser.callbacks.CallbackManager;

import java.util.Hashtable;
import java.util.ArrayList;

/**
 * IRCProcessor.
 * Superclass for all IRCProcessor types.
 *
 * @author            Shane Mc Cormack
 * @version           $Id: IRCProcessor.java 273 2007-03-03 02:09:17Z greboid $
 */
public abstract class IRCProcessor {
	/** Reference to the IRCParser that owns this IRCProcessor. */
	protected IRCParser myParser = null;
	
	/** Reference to the Processing in charge of this IRCProcessor. */
	protected ProcessingManager myManager = null;

	// Some functions from the main parser are useful, and having to use myParser.functionName
	// is annoying, so we also implement them here (calling them again using myParser)

	/**
	 * Callback to all objects implementing the IErrorInfo Interface.
	 *
	 * @see IErrorInfo
	 * @param errorInfo ParserError object representing the error.
	 */
	protected final boolean callErrorInfo(ParserError errorInfo) {
		return myParser.callErrorInfo(errorInfo);
	}
	
	/**
	 * Callback to all objects implementing the DebugInfo Callback.
	 *
	 * @see IDebugInfo
	 * @param level Debugging Level (ndInfo, ndSocket etc)
	 * @param data Debugging Information
	 * @param args Formatting String Options
	 */
	protected boolean callDebugInfo(int level, String data, Object... args) {
		return myParser.callDebugInfo(level, String.format(data, args));
	}
	
	/**
	 * Callback to all objects implementing the DebugInfo Callback.
	 *
	 * @see IDebugInfo
	 * @param level Debugging Level (ndInfo, ndSocket etc)
	 * @param data Debugging Information
	 */
	protected boolean callDebugInfo(int level, String data) {
		return myParser.callDebugInfo(level, data);
	}
	
	/**
	 * Check if a channel name is valid .
	 *
	 * @param sChannelName Channel name to test
	 */
	protected boolean isValidChannelName(String sChannelName) {
		return myParser.isValidChannelName(sChannelName);
	}
	
	/**
	 * Get the ClientInfo object for a person.
	 *
	 * @param sWho Who can be any valid identifier for a client as long as it contains a nickname (?:)nick(?!ident)(?@host)
	 * @return ClientInfo Object for the client, or null
	 */
	protected ClientInfo getClientInfo(String sWho) {
		return myParser.getClientInfo(sWho);
	}
	
	/**
	 * Get the ChannelInfo object for a channel.
	 *
	 * @param sWhat This is the name of the channel.
	 * @return ChannelInfo Object for the channel, or null
	 */
	protected ChannelInfo getChannelInfo(String sWhat) {
		return myParser.getChannelInfo(sWhat);
	}
	
	/**
	 * Get a reference to the CallbackManager
	 *
	 * @return Reference to the CallbackManager
	 */
	protected CallbackManager getCallbackManager() {
		return myParser.getCallbackManager();
	}
	
	/** 
	 * Send a line to the server and add proper line ending.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 */
	protected void sendString(String line) {
		myParser.sendString(line);
	}
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected IRCProcessor (IRCParser parser, ProcessingManager manager) {
		this.myParser = parser;
		this.myManager = manager;
	}
	
	/**
	 * Process a Line.
	 *
	 * @param sParam Type of line to process ("005", "PRIVMSG" etc)
	 * @param token IRCTokenised line to process
	 */
	public abstract void process(String sParam, String[] token);
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public abstract String[] handles();
	
	/** Get the name for this Processor */
	public String getName() {
		Package thisPackage = this.getClass().getPackage();
		int packageLength = 0;
		if (thisPackage != null) {
			packageLength = thisPackage.getName().length()+1;
		}
		return this.getClass().getName().substring(packageLength);
	}
	
	/** Get the name for this Processor in lowercase */
	public final String getLowerName() {
		return this.getName().toLowerCase();
	}
	
	/** Get the name for this Processor */
	public final String toString() { return this.getName(); }
	
	/**
	 * Get SVN Version information
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id: IRCParser.java 478 2007-03-08 05:52:14Z ShaneMcC $"; }	
}