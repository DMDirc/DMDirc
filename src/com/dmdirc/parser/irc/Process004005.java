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

/**
 * Process ISUPPORT lines.
 */
public class Process004005 extends IRCProcessor {
	/**
	 * Process ISUPPORT lines.
	 *
	 * @param sParam Type of line to process ("005", "004")
	 * @param token IRCTokenised line to process
	 */
	@Override
	public void process(final String sParam, final String[] token) {
		if (sParam.equals("003")) {
			myParser.h005Info.put("003IRCD",token[token.length-1]);
		} else if (sParam.equals("004")) {
			// 004
			if (token.length > 4) {
				myParser.h005Info.put("004IRCD",token[4]);
				myParser.h005Info.put("USERMODES",token[5]);
				myParser.h005Info.put("USERCHANMODES",token[6]);
			} else {
				final String[] bits = token[3].split(" ");
				myParser.h005Info.put("004IRCD",bits[1]);
				myParser.h005Info.put("USERMODES",bits[2]);
				myParser.h005Info.put("USERCHANMODES",bits[3]);
			}
			myParser.parseUserModes();
		} else if (sParam.equals("005")) {
			// 005
			String[] Bits = null;
			String sKey = null, sValue = null;
			for (int i = 3; i < token.length ; i++) {
				Bits = token[i].split("=",2);
				sKey = Bits[0].toUpperCase();
				if (Bits.length == 2) { sValue = Bits[1]; } else { sValue = ""; }
				callDebugInfo(IRCParser.DEBUG_INFO, "%s => %s",sKey,sValue);
				myParser.h005Info.put(sKey,sValue);
				if (sKey.equals("NETWORK")) {
					myParser.sNetworkName = sValue;
					callGotNetwork();
				} else if (sKey.equals("CASEMAPPING")) {
					byte limit = (byte)4;
					if (sValue.equalsIgnoreCase("strict-rfc1459")) {
						limit = (byte)3;
					} else if (sValue.equalsIgnoreCase("ascii")) {
						limit = (byte)0;
					} else if (!sValue.equalsIgnoreCase("rfc1459")) {
						myParser.callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Unknown casemapping: '"+sValue+"' - assuming rfc1459", myParser.getLastLine()));
					}
					final boolean limitChanged = (myParser.getIRCStringConverter().getLimit() != limit);
					myParser.updateCharArrays(limit);
					if (limitChanged && myParser.knownClients() == 1) {
							// This means that the casemapping is not rfc1459
							// We have only added ourselves so far (from 001)
							// We can fix the hashtable easily.
						myParser.removeClient(myParser.getMyself());
						myParser.addClient(myParser.getMyself());
					}
				} else if (sKey.equals("CHANTYPES")) {
					myParser.parseChanPrefix();
				} else if (sKey.equals("PREFIX")) {
					myParser.parsePrefixModes();
				} else if (sKey.equals("CHANMODES")) {
					myParser.parseChanModes();
				} else if (sKey.equals("LISTMODE")) {
					// Support for potential future decent mode listing in the protocol
					// 
					// See my proposal: http://shane.dmdirc.com/listmodes.php
					// Add listmode handler
					String[] handles = new String[2];
					handles[0] = sValue; // List mode item
					sValue = ""+(Integer.parseInt(sValue) + 1);
					myParser.h005Info.put("LISTMODEEND", sValue);
					handles[1] = sValue; // List mode end
					// Add listmode handlers
					try {
						myParser.getProcessingManager().addProcessor(handles, myParser.getProcessingManager().getProcessor("__LISTMODE__"));
					} catch (ProcessorNotFoundException e) { }
				}
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
		return new String[]{"003", "004", "005"};
	} 
	
	/**
	 * Callback to all objects implementing the GotNetwork Callback.
	 * This takes no params of its own, but works them out itself.
	 *
	 * @see IGotNetwork
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callGotNetwork() {
		final String networkName = myParser.sNetworkName;
		final String ircdVersion = myParser.getIRCD(false);
		final String ircdType = myParser.getIRCD(true);
		
		return getCallbackManager().getCallbackType("OnGotNetwork").call(networkName, ircdVersion, ircdType);
	}
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected Process004005 (final IRCParser parser, final ProcessingManager manager) { super(parser, manager); }

}
