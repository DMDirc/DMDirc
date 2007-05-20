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
import com.dmdirc.parser.callbacks.interfaces.INumeric;
import com.dmdirc.parser.callbacks.CallbackOnNumeric;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * IRC Parser Processing Manager.
 * Manages adding/removing/calling processing stuff.
 *
 * @author Shane Mc Cormack
 * @version $Id$
 */
public class ProcessingManager {
	/** Reference to the parser object that owns this ProcessingManager */
	IRCParser myParser = null;
	
	/** Hashtable used to store the different types of IRCProcessor known. */
	private Hashtable<String,IRCProcessor> processHash = new Hashtable<String,IRCProcessor>();

	/**
	 * Constructor to create a ProcessingManager
	 *
	 * @param parser IRCParser that owns this Processing Manager
	 */
	public ProcessingManager(IRCParser parser) {
		myParser = parser;
	}
	
	/**
	 * Debugging Data to the console.
	 */
	private void DoDebug(String line, Object... args) {
		myParser.callDebugInfo(myParser.DEBUG_PROCESSOR, line, args);
	}
	
	/**
	 * Initialise the ProcessingManager with the default processors
	 */
	public void init() {
		//------------------------------------------------
		// Add processors
		//------------------------------------------------
		// NOTICE AUTH
		addProcessor(new ProcessNoticeAuth(myParser, this));
		// 001
		addProcessor(new Process001(myParser, this));
		// 004
		// 005
		addProcessor(new Process004005(myParser, this));
		// 464
		addProcessor(new Process464(myParser, this));
		// 301
		// 305
		// 306
		addProcessor(new ProcessAway(myParser, this));
		// 352
		addProcessor(new ProcessWho(myParser, this));
		// JOIN
		addProcessor(new ProcessJoin(myParser, this));
		// KICK
		addProcessor(new ProcessKick(myParser, this));
		// PRIVMSG
		// NOTICE
		addProcessor(new ProcessMessage(myParser, this));
		// MODE
		// 324
		addProcessor(new ProcessMode(myParser, this));
		// 372
		// 375
		// 376
		// 422
		addProcessor(new ProcessMOTD(myParser, this));
		// 353
		// 366
		addProcessor(new ProcessNames(myParser, this));
		// 433
		addProcessor(new ProcessNickInUse(myParser, this));
		// NICK
		addProcessor(new ProcessNick(myParser, this));
		// PART
		addProcessor(new ProcessPart(myParser, this));
		// QUIT
		addProcessor(new ProcessQuit(myParser, this));
		// TOPIC
		// 332
		// 333
		addProcessor(new ProcessTopic(myParser, this));
		// 344
		// 345
		// 346
		// 347
		// 348
		// 349
		// 367
		// 368
		addProcessor(new ProcessListModes(myParser, this));
	}
	
	/**
	 * Remove all processors
	 */
	public void empty() {
		processHash.clear();
	}
	
	/** Empty clone method to prevent cloning to get more copies of the ProcessingManager */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * Add new Process type.
	 *
	 * @param processor IRCProcessor subclass for the processor.
	 */
	public void addProcessor(IRCProcessor processor) {	
		// handles() returns a String array of all the tokens
		// that this processor will parse.
		addProcessor(processor.handles(), processor);
	}
	
	/**
	 * Add a processor to tokens not-specified in the handles() reply.
	 *
	 * @param processor IRCProcessor subclass for the processor.
	 * @param handles String Array of tokens to add this processor as a hadler for
	 */
	public void addProcessor(String[] handles, IRCProcessor processor) {	
		DoDebug("Adding processor: "+processor.getName());
		
		try {
			for (int i = 0; i < handles.length; ++i) {
				if (processHash.containsKey(handles[i].toLowerCase())) {
					// New Processors take priority over old ones
					processHash.remove(handles[i].toLowerCase());
				}
				DoDebug("\t Added handler for: "+handles[i]);
				processHash.put(handles[i].toLowerCase(), processor);
			}
		} catch (Exception e) {
			DoDebug("\t[ERROR] "+e.getMessage()+" - Removing processor");
			delProcessor(processor);
		}
	}
		
	/**
	 * Remove a Process type.
	 *
	 * @param processor IRCProcessor subclass for the processor.
	 */
	public void delProcessor(IRCProcessor processor) {	
		IRCProcessor testProcessor;
		String elementName;
		DoDebug("Deleting processor: "+processor.getName());
		for (Enumeration e = processHash.keys(); e.hasMoreElements();) {
			elementName = (String)e.nextElement();
			DoDebug("\t Checking handler for: "+elementName);
			testProcessor = processHash.get(elementName);
			if (testProcessor.getName().equalsIgnoreCase(processor.getName())) {
				DoDebug("\t Removed handler for: "+elementName);
				processHash.remove(elementName);
			}
		}
	}
	
	/**
	 * Get the processor used for a specified token.
	 *
	 * @param sParam Type of line to process ("005", "PRIVMSG" etc)
	 * @return IRCProcessor for the given param.
	 */
	public IRCProcessor getProcessor(String sParam) throws ProcessorNotFound {
		if (processHash.containsKey(sParam.toLowerCase())) {
			return processHash.get(sParam.toLowerCase());
		} else {
			throw new ProcessorNotFound("No processors will handle "+sParam);
		}
	}
	
	/**
	 * Process a Line.
	 *
	 * @param sParam Type of line to process ("005", "PRIVMSG" etc)
	 * @param token IRCTokenised line to process
	 * @throws ProcessorNotFound exception if no processors exists to handle the line
	 */
	public void process(String sParam, String[] token) throws ProcessorNotFound {
		IRCProcessor messageProcessor = null;
		try {
			messageProcessor = getProcessor(sParam);
			messageProcessor.process(sParam, token);
		} catch (ProcessorNotFound p) {
			throw p;
		} catch (Exception e) {
			StringBuilder line = new StringBuilder();
			for (int i = 0; i < token.length; ++i ) { line.append(" ").append(token[i]); }
			ParserError ei = new ParserError(ParserError.ERROR_WARNING,"Exception in Parser. [Param: "+sParam+"] [Processor: "+messageProcessor+"] [Line: "+line.toString().trim()+"]");
			ei.setException(e);
			myParser.callErrorInfo(ei);
		} finally {
			// Try to call callNumeric. We don't want this to work if sParam is a non
			// integer param, hense the empty catch
			try {
				callNumeric(Integer.parseInt(sParam), token);
			} catch (NumberFormatException e) { }	
		}
	}

	/**
	 * Callback to all objects implementing the onNumeric Callback.
	 *
	 * @see INumeric
	 * @param numeric What numeric is this for
	 * @param token IRC Tokenised line
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callNumeric(int numeric, String[] token) {
		CallbackOnNumeric cb = (CallbackOnNumeric)myParser.getCallbackManager().getCallbackType("OnNumeric");
		if (cb != null) { return cb.call(numeric, token); }
		return false;
	}

	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}

