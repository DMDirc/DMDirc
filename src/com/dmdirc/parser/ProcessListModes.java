/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import java.util.LinkedList;
import java.util.Queue;

// import com.dmdirc.parser.callbacks.;
// import com.dmdirc.parser.callbacks.interfaces.;

/**
 * Process a List Modes.
 */
public class ProcessListModes extends IRCProcessor {
	/**
	 * Process a ListModes.
	 *
	 * @param sParam Type of line to process ("348", "349", "346", "347", "367", "368", "482")
	 * @param token IRCTokenised line to process
	 */
	@SuppressWarnings("unchecked")
	public void process(String sParam, String[] token) {
		ChannelInfo channel = getChannelInfo(token[3]);
		String thisIRCD = myParser.getIRCD(true).toLowerCase();
		String item = "";
		String owner = "";
		byte tokenStart = 4; // Where do the relevent tokens start?
		boolean isCleverMode = false;
		long time = 0;
		char mode = 'b';
		boolean isItem = true; // true if item listing, false if "end of .." item
		if (channel == null) { return; }
		
		if (sParam.equals("367") || sParam.equals("368")) {
			// Ban List/Item.
			// (Also used for +d and +q on hyperion... -_-)
			mode = 'b';
			isItem = sParam.equals("367");
		} else if (sParam.equals("348") || sParam.equals("349")) {
			// Except / Exempt List etc
			mode = 'e';
			isItem = sParam.equals("348");
		} else if (sParam.equals("346") || sParam.equals("347")) {
			// Invite List
			mode = 'I';
			isItem = sParam.equals("346");
		} else if (sParam.equals("344") || sParam.equals("345")) {
			// Reop List
			mode = 'R';
			isItem = sParam.equals("344");
		} else if (sParam.equals(myParser.h005Info.get("LISTMODE")) || sParam.equals(myParser.h005Info.get("LISTMODEEND"))) {
			// Support for potential future decent mode listing in the protocol
			//
			// See my proposal: http://shane.dmdirc.com/listmodes.php
			mode = token[4].charAt(0);
			isItem = sParam.equals(myParser.h005Info.get("LISTMODE"));
			tokenStart = 5;
			isCleverMode = true;
		}
		
		final Queue<Character> listModeQueue = channel.getListModeQueue();
		if (!isCleverMode && listModeQueue != null) {
			if (sParam.equals("482")) {
				myParser.callDebugInfo(myParser.DEBUG_LMQ, "Dropped LMQ mode "+listModeQueue.poll());
			} else {
				if (listModeQueue.peek() != null) {
					Character oldMode = mode;
					mode = listModeQueue.peek();
					myParser.callDebugInfo(myParser.DEBUG_LMQ, "LMQ says this is "+mode);
					if (oldMode != mode) {
						myParser.callDebugInfo(myParser.DEBUG_LMQ, "LMQ disagrees with guess LMQ: "+mode+" Guess: "+oldMode);
					}
					if ((thisIRCD.equals("hyperion") || thisIRCD.equals("dancer")) && (mode == 'b' || mode == 'q')) {
						LinkedList<Character> lmq = (LinkedList<Character>)listModeQueue;
						if (mode == 'b') {
							lmq.remove((Character)'q');
							myParser.callDebugInfo(myParser.DEBUG_LMQ, "Dropping q from list");
						} else if (mode == 'q') {
							lmq.remove((Character)'b');
							myParser.callDebugInfo(myParser.DEBUG_LMQ, "Dropping b from list");
						}
					}
					if (!isItem) {
						listModeQueue.poll();
					}
				}
			}
		}
		
		if (sParam.equals("482")) { return; }
		
		if (isItem) {
			if ((!isCleverMode) && listModeQueue == null && (thisIRCD.equals("hyperion") || thisIRCD.equals("dancer"))) {
				if (token.length > 4) {
					if (mode == 'b') {
						// Assume mode is a 'd' mode
						mode = 'd';
						// Now work out if its not (or attempt to.)
						int identstart = token[tokenStart].indexOf('!');
						int hoststart = token[tokenStart].indexOf('@');
						// Check that ! and @ are both in the string - as required by +b and +q
						if ((identstart >= 0) && (hoststart >= 0)) {
							// Check that ! is BEFORE the @ - as required by +b and +q
							if (identstart < hoststart) {
								if (thisIRCD.equals("hyperion") && token[tokenStart].charAt(0) == '%') { mode = 'q'; }
								else { mode = 'b'; }
							}
						}
					}
				}
			} // End Hyperian stupidness of using the same numeric for 3 different things..
			
			if (!channel.getAddState(mode)) {
				callDebugInfo(myParser.DEBUG_INFO, "New List Mode Batch: Clearing!");
				channel.getListModeParam(mode).clear();
				channel.setAddState(mode, true);
			}
			
			if (token.length > (tokenStart+2)) { 
				try { time = Long.parseLong(token[tokenStart+2]); } catch (Exception e) { time = 0; }
			}
			if (token.length > (tokenStart+1)) { owner = token[tokenStart+1]; }
			if (token.length > tokenStart) { item = token[tokenStart]; }
			if (!item.isEmpty()) {
				ChannelListModeItem clmi = new ChannelListModeItem(item, owner, time);
				callDebugInfo(myParser.DEBUG_INFO, "List Mode: %c [%s/%s/%d]",mode, item, owner, time);
				channel.setListModeParam(mode, clmi, true);
			}
		} else {
			channel.setAddState(mode, false);
		}
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[10];
		int i = 0;
		// Ban List - All IRCds
		iHandle[i++] = "367"; // Item
		iHandle[i++] = "368"; // End
		
		// Reop List - ircnet 
		iHandle[i++] = "344"; // Item
		iHandle[i++] = "345"; // End
		
		// Invex List - unreal
		// Invite List - asuka austhex bahamut dancer hybrid hyperion ircnet ratbox undernet
		iHandle[i++] = "346"; // Item
		iHandle[i++] = "347"; // End
		
		// Exception list - austhex
		// Except List - dancer hybrid hyperion ircnet ratbox
		// Exempt List - bahamut
		// Ex List =- unreal
		iHandle[i++] = "348"; // Item
		iHandle[i++] = "349"; // End
		
		// "Only operator can do that"
		// This pops an item off the list mode queue
		iHandle[i++] = "482"; // End
		
		// This is here to allow finding the processor for adding LISTMODE support
		iHandle[i++] = "__LISTMODE__";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessListModes (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
