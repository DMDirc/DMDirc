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
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * IRC Parser Processing Manager.
 * Manages adding/removing/calling processing stuff.
 *
 * @author            Shane Mc Cormack
 * @version           $Id$
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
		myParser.callDebugInfo(myParser.ndProcessor, line, args);
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
		// 305
		// 306
		addProcessor(new ProcessAway(myParser, this));
		// JOIN
		addProcessor(new ProcessJoin(myParser, this));
		// KICK
		addProcessor(new ProcessKick(myParser, this));
		// 346
		// 347
		// 348
		// 349
		// 367
		// 368
		addProcessor(new ProcessListModes(myParser, this));
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
		DoDebug("Adding processor: "+processor.getName());
		
		try {
			String[] handles = processor.handles();
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
	 * Process a Line.
	 *
	 * @param sParam Type of line to process ("005", "PRIVMSG" etc)
	 * @param token IRCTokenised line to process
	 * @throws ProcessorNotFound exception if no processors exists to handle the line
	 */
	public void process(String sParam, String[] token) throws ProcessorNotFound {
		IRCProcessor messageProcessor = null;
		try {
			if (processHash.containsKey(sParam.toLowerCase())) {
				messageProcessor = processHash.get(sParam.toLowerCase());
				messageProcessor.process(sParam, token);
			} else {
				throw new ProcessorNotFound("No processors will handle "+sParam);
			}
		} catch (ProcessorNotFound p) {
			throw p;
		} catch (Exception e) {
			String line = "";
			for (int i = 0; i < token.length; ++i ) { line = line+" "+token[i]; }
			line = line.trim();
			ParserError ei = new ParserError(ParserError.errWarning,"Exception in Parser. [Param: "+sParam+"] [Processor: "+messageProcessor+"] [Line: "+line+"]");
			ei.setException(e);
			myParser.callErrorInfo(ei);
		}
	}
		
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}

