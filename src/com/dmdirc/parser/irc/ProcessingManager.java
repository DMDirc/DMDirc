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

import java.util.Hashtable;

/**
 * IRC Parser Processing Manager.
 * Manages adding/removing/calling processing stuff.
 *
 * @author Shane Mc Cormack
 */
public class ProcessingManager {
	/** Reference to the parser object that owns this ProcessingManager */
	IRCParser myParser;

	/** Hashtable used to store the different types of IRCProcessor known. */
	private final Hashtable<String,IRCProcessor> processHash = new Hashtable<String,IRCProcessor>();

	/**
	 * Debugging Data to the console.
	 */
	private void doDebug(final String line, final Object... args) {
		myParser.callDebugInfo(IRCParser.DEBUG_PROCESSOR, line, args);
	}

	/**
	 * Constructor to create a ProcessingManager.
	 *
	 * @param parser IRCParser that owns this Processing Manager
	 */
	public ProcessingManager(IRCParser parser) {
		myParser = parser;
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
		// INVITE
		addProcessor(new ProcessInvite(myParser, this));
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
		// WALLOPS
		addProcessor(new ProcessWallops(myParser, this));
	}

	/**
	 * Add new Process type.
	 *
	 * @param processor IRCProcessor subclass for the processor.
	 */
	public void addProcessor(final IRCProcessor processor) {
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
	public void addProcessor(final String[] handles, final IRCProcessor processor) {
		doDebug("Adding processor: "+processor.getName());

		for (int i = 0; i < handles.length; ++i) {
			if (processHash.containsKey(handles[i].toLowerCase())) {
				// New Processors take priority over old ones
				processHash.remove(handles[i].toLowerCase());
			}
			doDebug("\t Added handler for: "+handles[i]);
			processHash.put(handles[i].toLowerCase(), processor);
		}
	}

	/**
	 * Remove a Process type.
	 *
	 * @param processor IRCProcessor subclass for the processor.
	 */
	public void delProcessor(final IRCProcessor processor) {
		IRCProcessor testProcessor;
		doDebug("Deleting processor: "+processor.getName());
		for (String elementName : processHash.keySet()) {
			doDebug("\t Checking handler for: "+elementName);
			testProcessor = processHash.get(elementName);
			if (testProcessor.getName().equalsIgnoreCase(processor.getName())) {
				doDebug("\t Removed handler for: "+elementName);
				processHash.remove(elementName);
			}
		}
	}

	/**
	 * Get the processor used for a specified token.
	 *
	 * @param sParam Type of line to process ("005", "PRIVMSG" etc)
	 * @return IRCProcessor for the given param.
	 * @throws ProcessorNotFoundException if no processer exists for the param
	 */
	public IRCProcessor getProcessor(final String sParam) throws ProcessorNotFoundException {
		if (processHash.containsKey(sParam.toLowerCase())) {
			return processHash.get(sParam.toLowerCase());
		} else {
			throw new ProcessorNotFoundException("No processors will handle "+sParam);
		}
	}

	/**
	 * Process a Line.
	 *
	 * @param sParam Type of line to process ("005", "PRIVMSG" etc)
	 * @param token IRCTokenised line to process
	 * @throws ProcessorNotFoundException exception if no processors exists to handle the line
	 */
	public void process(final String sParam, final String[] token) throws ProcessorNotFoundException {
		IRCProcessor messageProcessor = null;
		try {
			messageProcessor = getProcessor(sParam);
			messageProcessor.process(sParam, token);
		} catch (ProcessorNotFoundException p) {
			throw p;
		} catch (Exception e) {
			final ParserError ei = new ParserError(ParserError.ERROR_ERROR,"Exception in Processor. ["+messageProcessor+"]: "+e.getMessage(), myParser.getLastLine());
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
	protected boolean callNumeric(final int numeric, final String[] token) {
		return myParser.getCallbackManager().getCallbackType("OnNumeric").call(numeric, token);
	}

}

