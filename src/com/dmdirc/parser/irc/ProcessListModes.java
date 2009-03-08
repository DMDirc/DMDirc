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

import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Process a List Modes.
 */
public class ProcessListModes extends IRCProcessor {
	/**
	 * Process a ListModes.
	 *
	 * @param sParam Type of line to process
	 * @param token IRCTokenised line to process
	 */
	@SuppressWarnings("unchecked")
	@Override
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
		} else if (sParam.equals("940") || sParam.equals("941")) {
			// Censored words List
			mode = 'g';
			isItem = sParam.equals("941");
		} else if (sParam.equals("344") || sParam.equals("345")) {
			// Reop List, or bad words list, or quiet list. god damn.
			if (thisIRCD.equals("euircd")) {
				mode = 'w';
			} else if (thisIRCD.equals("oftc-hybrid")) {
				mode = 'q';
			} else {
				mode = 'R';
			}
			isItem = sParam.equals("344");
		} else if (thisIRCD.equals("swiftirc") && (sParam.equals("386") || sParam.equals("387"))) {
			// Channel Owner list
			mode = 'q';
			isItem = sParam.equals("387");
		} else if (thisIRCD.equals("swiftirc") && (sParam.equals("388") || sParam.equals("389"))) {
			// Protected User list
			mode = 'a';
			isItem = sParam.equals("389");
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
				myParser.callDebugInfo(IRCParser.DEBUG_LMQ, "Dropped LMQ mode "+listModeQueue.poll());
				return;
			} else {
				if (listModeQueue.peek() != null) {
					Character oldMode = mode;
					mode = listModeQueue.peek();
					myParser.callDebugInfo(IRCParser.DEBUG_LMQ, "LMQ says this is "+mode);
					
					boolean error = true;
					
					if ((thisIRCD.equals("hyperion") || thisIRCD.equals("dancer")) && (mode == 'b' || mode == 'q')) {
						LinkedList<Character> lmq = (LinkedList<Character>)listModeQueue;
						if (mode == 'b') {
							error = !(oldMode == 'q');
							lmq.remove((Character)'q');
							myParser.callDebugInfo(IRCParser.DEBUG_LMQ, "Dropping q from list");
						} else if (mode == 'q') {
							error = !(oldMode == 'b');
							lmq.remove((Character)'b');
							myParser.callDebugInfo(IRCParser.DEBUG_LMQ, "Dropping b from list");
						}
					}
					
					if (oldMode != mode && error) {
						myParser.callDebugInfo(IRCParser.DEBUG_LMQ, "LMQ disagrees with guess. LMQ: "+mode+" Guess: "+oldMode);
						myParser.callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "LMQ disagrees with guess. LMQ: "+mode+" Guess: "+oldMode, myParser.getLastLine()));
					}
					
					if (!isItem) {
						listModeQueue.poll();
					}
				}
			}
		}
		
		if (isItem) {
			if ((!isCleverMode) && listModeQueue == null && (thisIRCD.equals("hyperion") || thisIRCD.equals("dancer")) && token.length > 4 && mode == 'b') {
				// Assume mode is a 'd' mode
				mode = 'd';
				// Now work out if its not (or attempt to.)
				int identstart = token[tokenStart].indexOf('!');
				int hoststart = token[tokenStart].indexOf('@');
				// Check that ! and @ are both in the string - as required by +b and +q
				if ((identstart >= 0) && (identstart < hoststart)) {
					if (thisIRCD.equals("hyperion") && token[tokenStart].charAt(0) == '%') { mode = 'q'; }
					else { mode = 'b'; }
				}
			} // End Hyperian stupidness of using the same numeric for 3 different things..
			
			if (!channel.getAddState(mode)) {
				callDebugInfo(IRCParser.DEBUG_INFO, "New List Mode Batch ("+mode+"): Clearing!");
				final List<ChannelListModeItem> list = channel.getListModeParam(mode);
				if (list == null) {
					myParser.callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got list mode: '"+mode+"' - but channel object doesn't agree.", myParser.getLastLine()));
				} else {
					list.clear();
				}
				channel.setAddState(mode, true);
			}
			
			if (token.length > (tokenStart+2)) {
				try { time = Long.parseLong(token[tokenStart+2]); } catch (NumberFormatException e) { time = 0; }
			}
			if (token.length > (tokenStart+1)) { owner = token[tokenStart+1]; }
			if (token.length > tokenStart) { item = token[tokenStart]; }
			if (!item.isEmpty()) {
				ChannelListModeItem clmi = new ChannelListModeItem(item, owner, time);
				callDebugInfo(IRCParser.DEBUG_INFO, "List Mode: %c [%s/%s/%d]",mode, item, owner, time);
				channel.setListModeParam(mode, clmi, true);
			}
		} else {
			callDebugInfo(IRCParser.DEBUG_INFO, "List Mode Batch over");
			channel.resetAddState();
			if (isCleverMode || listModeQueue == null || ((LinkedList<Character>)listModeQueue).size() == 0) {
				callDebugInfo(IRCParser.DEBUG_INFO, "Calling GotListModes");
				channel.setHasGotListModes(true);
				callChannelGotListModes(channel);
			}
		}
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	@Override
	public String[] handles() {
		return new String[]{"367", "368",  /* Bans */
		                    "344", "345",  /* Reop list (ircnet) or bad words (euirc) */
		                    "346", "347",  /* Invite List */
		                    "348", "349",  /* Except/Exempt List */
		                    "386", "387",  /* Channel Owner List (swiftirc ) */
		                    "388", "389",  /* Protected User List (swiftirc) */
		                    "940", "941",  /* Censored words list */
		                    "482",         /* Permission Denied */
		                    "__LISTMODE__" /* Sensible List Modes */
		                   };
	}
	
	/**
	 * Callback to all objects implementing the ChannelGotListModes Callback.
	 *
	 * @see IChannelGotListModes
	 * @param cChannel Channel which the ListModes reply is for
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelGotListModes(ChannelInfo cChannel) {
		return getCallbackManager().getCallbackType("OnChannelGotListModes").call(cChannel);
	}
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessListModes (IRCParser parser, ProcessingManager manager) { super(parser, manager); }

}
