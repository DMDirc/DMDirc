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

import java.util.regex.PatternSyntaxException;

/**
 * Process PRIVMSGs and NOTICEs.
 * This horrible handles PRIVMSGs and NOTICES<br>
 * This inclues CTCPs and CTCPReplies<br>
 * It handles all 3 targets (Channel, Private, Unknown)<br>
 * Actions are handled here aswell separately from CTCPs.<br>
 * Each type has 5 Calls, making 15 callbacks handled here.
 */
public class ProcessMessage extends IRCProcessor {
	/**
	 * Process PRIVMSGs and NOTICEs.
	 * This horrible thing handles PRIVMSGs and NOTICES<br>
	 * This inclues CTCPs and CTCPReplies<br>
	 * It handles all 3 targets (Channel, Private, Unknown)<br>
	 * Actions are handled here aswell separately from CTCPs.<br>
	 * Each type has 5 Calls, making 15 callbacks handled here.
	 *
	 * @param sParam Type of line to process ("NOTICE", "PRIVMSG")
	 * @param token IRCTokenised line to process
	 */
	@Override
	public void process(final String sParam, String[] token) {
		// Ignore people!
		String sMessage = "";
		if (token[0].charAt(0) == ':') { sMessage = token[0].substring(1); } else { sMessage = token[0]; }
		// We use sMessage to be the users host (first token in the line)
		try {
			if (myParser.getIgnoreList().matches(sMessage) > -1) { return; }
		} catch (PatternSyntaxException pse) {
			final ParserError pe = new ParserError(ParserError.ERROR_WARNING, "Error with ignore list regex: "+pse, myParser.getLastLine());
			pe.setException(pse);
			callErrorInfo(pe);
		}
		
		// Lines such as:
		// "nick!user@host PRIVMSG"
		// are invalid, stop processing.
		if (token.length < 3) { return; }
		
		// Is this actually a notice auth?
		if (token[0].indexOf('!') == -1 && token[1].equalsIgnoreCase("NOTICE") && token[2].equalsIgnoreCase("AUTH")) {
			try {
				myParser.getProcessingManager().process("Notice Auth", token);
			} catch (ProcessorNotFoundException e) { }
			return;
		}
		
		ChannelClientInfo iChannelClient = null;
		ChannelInfo iChannel = null;
		ClientInfo iClient = null;
		// "nick!user@host PRIVMSG #Channel" should be processed as "nick!user@host PRIVMSG #Channel :"
		if (token.length < 4) {
			sMessage = "";
		} else {
			sMessage = token[token.length-1];
		}
		String bits[] = sMessage.split(" ", 2);
		final Character Char1 = Character.valueOf((char)1);
		String sCTCP = "";
		boolean isAction = false;
		boolean isCTCP = false;
		
		if (sMessage.length() > 1) {
			if (sParam.equalsIgnoreCase("PRIVMSG")) {
				// Actions are special CTCPs
				// Bits is the message been split into 2 parts, the first word and the rest
				if (bits[0].equalsIgnoreCase(Char1+"ACTION") && Character.valueOf(sMessage.charAt(sMessage.length()-1)).equals(Char1)) {
					isAction = true;
					if (bits.length > 1) {
						sMessage = bits[1];
						sMessage = sMessage.substring(0, sMessage.length()-1);
					} else { sMessage = ""; }
				}
			}
			// If the message is not an action, check if it is another type of CTCP
			if (!isAction) {
				// CTCPs have Character(1) at the start/end of the line
				if (Character.valueOf(sMessage.charAt(0)).equals(Char1) && Character.valueOf(sMessage.charAt(sMessage.length()-1)).equals(Char1)) {
					isCTCP = true;
					// Bits is the message been split into 2 parts, the first word and the rest
					// Some CTCPs have messages and some do not
					if (bits.length > 1) { sMessage = bits[1]; } else { sMessage = ""; }
					// Remove the leading char1
					bits = bits[0].split(Char1.toString(),2);
					sCTCP = bits[1];
					// remove the trailing char1
					if (!sMessage.isEmpty()) { sMessage = sMessage.split(Char1.toString(),2)[0]; }
					else { sCTCP = sCTCP.split(Char1.toString(),2)[0]; }
					callDebugInfo(IRCParser.DEBUG_INFO, "CTCP: \"%s\" \"%s\"",sCTCP,sMessage);
				}
			}
		}

		// Remove the leading : from the host.
		if (token[0].charAt(0) == ':' && token[0].length() > 1) { token[0] = token[0].substring(1); }

		iClient = getClientInfo(token[0]);
		if (IRCParser.ALWAYS_UPDATECLIENT && iClient != null) {
			// Facilitate DMDIRC Formatter
			if (iClient.getHost().isEmpty()) {iClient.setUserBits(token[0],false); }
		}
		
		// Fire the appropriate callbacks.
		// OnChannel* Callbacks are fired if the target was a channel
		// OnPrivate* Callbacks are fired if the target was us
		// OnUnknown* Callbacks are fired if the target was neither of the above
		// Actions and CTCPs are send as PRIVMSGS
		// CTCPReplies are sent as Notices
		if (isValidChannelName(token[2])) {
			iChannel = getChannelInfo(token[2]);
			if (iChannel == null) {
				// callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got message for channel ("+token[2]+") that I am not on.", myParser.getLastLine()));
				return;
			}
			if (iClient != null) { iChannelClient = iChannel.getUser(iClient); }
			if (sParam.equalsIgnoreCase("PRIVMSG")) {
				if (!isAction) {
					if (isCTCP) {
						callChannelCTCP(iChannel, iChannelClient, sCTCP, sMessage, token[0]);
					} else {
						callChannelMessage(iChannel, iChannelClient, sMessage, token[0]);
					}
				} else {
					callChannelAction(iChannel, iChannelClient, sMessage, token[0]);
				}
			} else if (sParam.equalsIgnoreCase("NOTICE")) {
				if (isCTCP) {
					callChannelCTCPReply(iChannel, iChannelClient, sCTCP, sMessage, token[0]);
				} else {
					callChannelNotice(iChannel, iChannelClient, sMessage, token[0]);
				}
			}
		} else if (myParser.getIRCStringConverter().equalsIgnoreCase(token[2], myParser.getMyNickname())) {
			if (sParam.equalsIgnoreCase("PRIVMSG")) {
				if (!isAction) {
					if (isCTCP) {
						callPrivateCTCP(sCTCP, sMessage, token[0]);
					} else {
						callPrivateMessage(sMessage, token[0]);
					}
				} else {
					callPrivateAction(sMessage, token[0]);
				}
			} else if (sParam.equalsIgnoreCase("NOTICE")) {
				if (isCTCP) {
					callPrivateCTCPReply(sCTCP, sMessage, token[0]);
				} else {
					callPrivateNotice(sMessage, token[0]);
				}
			}
		} else {
			callDebugInfo(IRCParser.DEBUG_INFO, "Message for Other ("+token[2]+")");
			if (sParam.equalsIgnoreCase("PRIVMSG")) {
				if (!isAction) {
					if (isCTCP) {
						callUnknownCTCP(sCTCP, sMessage, token[2], token[0]);
					} else {
						callUnknownMessage(sMessage, token[2], token[0]);
					}
				} else {
					callUnknownAction(sMessage, token[2], token[0]);
				}
			} else if (sParam.equalsIgnoreCase("NOTICE")) {
				if (isCTCP) {
					callUnknownCTCPReply(sCTCP, sMessage, token[2], token[0]);
				} else {
					callUnknownNotice(sMessage, token[2], token[0]);
				}
			}
		}
	}
	
	/**
	 * Callback to all objects implementing the ChannelAction Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IChannelAction
	 * @param cChannel Channel where the action was sent to
	 * @param cChannelClient ChannelClient who sent the action (may be null if server)
	 * @param sMessage action contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelAction(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
		return getCallbackManager().getCallbackType("OnChannelAction").call(cChannel, cChannelClient, sMessage, sHost);
	}
	
	/**
	 * Callback to all objects implementing the ChannelCTCP Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IChannelCTCP
	 * @param cChannel Channel where CTCP was sent
	 * @param cChannelClient ChannelClient who sent the message (may be null if server)
	 * @param sType Type of CTCP (VERSION, TIME etc)
	 * @param sMessage Additional contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelCTCP(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sType, final String sMessage, final String sHost) {
		return getCallbackManager().getCallbackType("OnChannelCTCP").call(cChannel, cChannelClient, sType, sMessage, sHost);
	}

	/**
	 * Callback to all objects implementing the ChannelCTCPReply Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IChannelCTCPReply
	 * @param cChannel Channel where CTCPReply was sent
	 * @param cChannelClient ChannelClient who sent the message (may be null if server)
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelCTCPReply(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sType, final String sMessage, final String sHost) {
		return getCallbackManager().getCallbackType("OnChannelCTCPReply").call(cChannel, cChannelClient, sType, sMessage, sHost);
	}
	
	/**
	 * Callback to all objects implementing the ChannelMessage Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IChannelMessage
	 * @param cChannel Channel where the message was sent to
	 * @param cChannelClient ChannelClient who sent the message (may be null if server)
	 * @param sMessage Message contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelMessage(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
		return getCallbackManager().getCallbackType("OnChannelMessage").call(cChannel, cChannelClient, sMessage, sHost);
	}
	
	/**
	 * Callback to all objects implementing the ChannelNotice Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IChannelNotice
	 * @param cChannel Channel where the notice was sent to
	 * @param cChannelClient ChannelClient who sent the notice (may be null if server)
	 * @param sMessage notice contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelNotice(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
		return getCallbackManager().getCallbackType("OnChannelNotice").call(cChannel, cChannelClient, sMessage, sHost);
	}
	
	/**
	 * Callback to all objects implementing the PrivateAction Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IPrivateAction
	 * @param sMessage action contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callPrivateAction(final String sMessage, final String sHost) {
		return getCallbackManager().getCallbackType("OnPrivateAction").call(sMessage, sHost);
	}

	/**
	 * Callback to all objects implementing the PrivateCTCP Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IPrivateCTCP
	 * @param sType Type of CTCP (VERSION, TIME etc)
	 * @param sMessage Additional contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callPrivateCTCP(final String sType, final String sMessage, final String sHost) {
		return getCallbackManager().getCallbackType("OnPrivateCTCP").call(sType, sMessage, sHost);
	}

	/**
	 * Callback to all objects implementing the PrivateCTCPReply Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IPrivateCTCPReply
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callPrivateCTCPReply(final String sType, final String sMessage, final String sHost) {
		return getCallbackManager().getCallbackType("OnPrivateCTCPReply").call(sType, sMessage, sHost);
	}

	/**
	 * Callback to all objects implementing the PrivateMessage Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IPrivateMessage
	 * @param sMessage Message contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callPrivateMessage(final String sMessage, final String sHost) {
		return getCallbackManager().getCallbackType("OnPrivateMessage").call(sMessage, sHost);
	}

	/**
	 * Callback to all objects implementing the PrivateNotice Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IPrivateNotice
	 * @param sMessage Notice contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callPrivateNotice(final String sMessage, final String sHost) {
		return getCallbackManager().getCallbackType("OnPrivateNotice").call(sMessage, sHost);
	}
	
	/**
	 * Callback to all objects implementing the UnknownAction Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IUnknownAction
	 * @param sMessage Action contents
	 * @param sTarget Actual target of action
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callUnknownAction(final String sMessage, final String sTarget, final String sHost) {
		return getCallbackManager().getCallbackType("OnUnknownAction").call(sMessage, sTarget, sHost);
	}

	/**
	 * Callback to all objects implementing the UnknownCTCP Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IUnknownCTCP
	 * @param sType Type of CTCP (VERSION, TIME etc)
	 * @param sMessage Additional contents
	 * @param sTarget Actual Target of CTCP
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callUnknownCTCP(final String sType, final String sMessage, final String sTarget, final String sHost) {
		return getCallbackManager().getCallbackType("OnUnknownCTCP").call(sType, sMessage, sTarget, sHost);
	}

	/**
	 * Callback to all objects implementing the UnknownCTCPReply Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IUnknownCTCPReply
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sTarget Actual Target of CTCPReply
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callUnknownCTCPReply(final String sType, final String sMessage, final String sTarget, final String sHost) {
		return getCallbackManager().getCallbackType("OnUnknownCTCPReply").call(sType, sMessage, sTarget, sHost);
	}

	/**
	 * Callback to all objects implementing the UnknownMessage Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IUnknownMessage
	 * @param sMessage Message contents
	 * @param sTarget Actual target of message
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callUnknownMessage(final String sMessage, final String sTarget, final String sHost) {
		return getCallbackManager().getCallbackType("OnUnknownMessage").call(sMessage, sTarget, sHost);
	}

	/**
	 * Callback to all objects implementing the UnknownNotice Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IUnknownNotice
	 * @param sMessage Notice contents
	 * @param sTarget Actual target of notice
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callUnknownNotice(final String sMessage, final String sTarget, final String sHost) {
		return getCallbackManager().getCallbackType("OnUnknownNotice").call(sMessage, sTarget, sHost);
	}

	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	@Override
	public String[] handles() {
		return new String[]{"PRIVMSG", "NOTICE"};
	}
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessMessage (IRCParser parser, ProcessingManager manager) { super(parser, manager); }

}
