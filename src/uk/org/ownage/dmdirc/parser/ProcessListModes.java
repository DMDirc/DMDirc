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

// import uk.org.ownage.dmdirc.parser.callbacks.;
// import uk.org.ownage.dmdirc.parser.callbacks.interfaces.;

/**
 * Process a List Modes.
 */
public class ProcessListModes extends IRCProcessor {
	/**
	 * Process a ListModes.
	 *
	 * @param sParam Type of line to process ("348", "349", "346", "347", "367", "368")
	 * @param token IRCTokenised line to process
	 */
	public void process(String sParam, String[] token) {
		//<- :uk.ircnet.org 348 DF|Laptop #dmdirc test2!*@*
		//<- :uk.ircnet.org 348 DF|Laptop #dmdirc test1!*@*
		//<- :uk.ircnet.org 349 DF|Laptop #dmdirc :End of Channel Exception List
		//<- :uk.ircnet.org 346 DF|Laptop #dmdirc test2!*@*
		//<- :uk.ircnet.org 346 DF|Laptop #dmdirc test1!*@*
		//<- :uk.ircnet.org 347 DF|Laptop #dmdirc :End of Channel Invite List
		//<- :neo.uk.CodersIRC.org 367 DF|Laptop #dmdirc test2!*@* DF|Laptop 1115086001
		//<- :neo.uk.CodersIRC.org 367 DF|Laptop #dmdirc test1!*@* DF|Laptop 1115086000
		//<- :neo.uk.CodersIRC.org 368 DF|Laptop #dmdirc :End of Channel Ban List
		ChannelInfo channel = getChannelInfo(token[3]);
		String item = "";
		String owner = "";
		long time = 0;
		char mode = 'b';
		boolean isItem = true; // true if item listing, false if "end of .." item
		if (channel == null) { return; }
		
		if (sParam.equals("367")) { mode = 'b'; isItem = true; }
		else if (sParam.equals("348")) { mode = 'e'; isItem = true; }
		else if (sParam.equals("346")) { mode = 'I'; isItem = true; }
		else if (sParam.equals("368")) { mode = 'b'; isItem = false; }
		else if (sParam.equals("349")) { mode = 'e'; isItem = false; }
		else if (sParam.equals("347")) { mode = 'I'; isItem = false; }
		
		if (isItem) {
			if (!channel.getAddState(mode)) {
				callDebugInfo(myParser.ndInfo, "New List Mode Batch: Clearing!");
				channel.getListModeParam(mode).clear();
				channel.setAddState(mode, true);
			}
			
			if (token.length > 6) { 
				try { time = Long.parseLong(token[6]); } catch (Exception e) { time = 0; }
			}
			if (token.length > 5) { owner = token[5]; }
			if (token.length > 4) { item = token[4]; }
			if (!item.equals("")) {
				ChannelListModeItem clmi = new ChannelListModeItem(item, owner, time);
				callDebugInfo(myParser.ndInfo, "List Mode: %c [%s/%s/%d]",mode, item, owner, time);
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
		String[] iHandle = new String[6];
		iHandle[0] = "348";
		iHandle[1] = "349";
		iHandle[2] = "346";
		iHandle[3] = "347";
		iHandle[4] = "367";
		iHandle[5] = "368";
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
