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

import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.callbacks.ChannelActionListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelCtcpListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelCtcpReplyListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelMessageListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelModeMessageListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelModeNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateActionListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateCtcpListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateCtcpReplyListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateMessageListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.UnknownActionListener;
import com.dmdirc.parser.interfaces.callbacks.UnknownCtcpListener;
import com.dmdirc.parser.interfaces.callbacks.UnknownCtcpReplyListener;
import com.dmdirc.parser.interfaces.callbacks.UnknownMessageListener;
import com.dmdirc.parser.interfaces.callbacks.UnknownNoticeListener;

import java.util.regex.PatternSyntaxException;

/**
 * Process PRIVMSGs and NOTICEs.
 * This horrible handles PRIVMSGs and NOTICE<br>
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
		
		IRCChannelClientInfo iChannelClient = null;
		IRCChannelInfo iChannel = null;
		IRCClientInfo iClient = null;
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
			if (iClient.getHostname().isEmpty()) {iClient.setUserBits(token[0],false); }
		}
		
		// Fire the appropriate callbacks.
		// OnChannel* Callbacks are fired if the target was a channel
		// OnPrivate* Callbacks are fired if the target was us
		// OnUnknown* Callbacks are fired if the target was neither of the above
		// Actions and CTCPs are send as PRIVMSGS
		// CTCPReplies are sent as Notices
		
		// Check if we have a Mode Prefix for channel targets.
		// Non-Channel messages still use the whole token, even if the first char
		// is a prefix.
		// CTCP and CTCPReplies that are aimed at a channel with a prefix are
		// handled as if the prefix wasn't used. This can be changed in the future
		// if desired.
		final char modePrefix = token[2].charAt(0);
		final boolean hasModePrefix =  (myParser.prefixMap.containsKey(modePrefix) && !myParser.prefixModes.containsKey(modePrefix));
		final String targetName = (hasModePrefix) ? token[2].substring(1) : token[2];
		
		if (isValidChannelName(targetName)) {
			iChannel = getChannel(targetName);
			if (iChannel == null) {
				// callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got message for channel ("+targetName+") that I am not on.", myParser.getLastLine()));
				return;
			}
			if (iClient != null) { iChannelClient = iChannel.getChannelClient(iClient); }
			if (sParam.equalsIgnoreCase("PRIVMSG")) {
				if (!isAction) {
					if (isCTCP) {
						callChannelCTCP(iChannel, iChannelClient, sCTCP, sMessage, token[0]);
					} else if (hasModePrefix) {
						callChannelModeMessage(modePrefix, iChannel, iChannelClient, sMessage, token[0]);
					} else {
						callChannelMessage(iChannel, iChannelClient, sMessage, token[0]);
					}
				} else {
					callChannelAction(iChannel, iChannelClient, sMessage, token[0]);
				}
			} else if (sParam.equalsIgnoreCase("NOTICE")) {
				if (isCTCP) {
					callChannelCTCPReply(iChannel, iChannelClient, sCTCP, sMessage, token[0]);
				} else if (hasModePrefix) {
					callChannelModeNotice(modePrefix, iChannel, iChannelClient, sMessage, token[0]);
				} else {
					callChannelNotice(iChannel, iChannelClient, sMessage, token[0]);
				}
			}
		} else if (myParser.getStringConverter().equalsIgnoreCase(token[2], myParser.getMyNickname())) {
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
		return getCallbackManager().getCallbackType(ChannelActionListener.class).call(cChannel, cChannelClient, sMessage, sHost);
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
		return getCallbackManager().getCallbackType(ChannelCtcpListener.class).call(cChannel, cChannelClient, sType, sMessage, sHost);
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
		return getCallbackManager().getCallbackType(ChannelCtcpReplyListener.class).call(cChannel, cChannelClient, sType, sMessage, sHost);
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
		return getCallbackManager().getCallbackType(ChannelMessageListener.class).call(cChannel, cChannelClient, sMessage, sHost);
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
		return getCallbackManager().getCallbackType(ChannelNoticeListener.class).call(cChannel, cChannelClient, sMessage, sHost);
	}
	
	/**
	 * Callback to all objects implementing the ChannelModeNotice Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IChannelModeNotice
	 * @param prefix Prefix that was used to send this notice.
	 * @param cChannel Channel where the notice was sent to
	 * @param cChannelClient ChannelClient who sent the notice (may be null if server)
	 * @param sMessage notice contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelModeNotice(final char prefix, final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
		return getCallbackManager().getCallbackType(ChannelModeNoticeListener.class).call(prefix, cChannel, cChannelClient, sMessage, sHost);
	}
	
	/**
	 * Callback to all objects implementing the ChannelModeMessage Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IChannelModeMessage
	 * @param prefix Prefix that was used to send this notice.
	 * @param cChannel Channel where the notice was sent to
	 * @param cChannelClient ChannelClient who sent the notice (may be null if server)
	 * @param sMessage message contents
	 * @param sHost Hostname of sender (or servername)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelModeMessage(final char prefix, final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
		return getCallbackManager().getCallbackType(ChannelModeMessageListener.class).call(prefix, cChannel, cChannelClient, sMessage, sHost);
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
		return getCallbackManager().getCallbackType(PrivateActionListener.class).call(sMessage, sHost);
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
		return getCallbackManager().getCallbackType(PrivateCtcpListener.class).call(sType, sMessage, sHost);
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
		return getCallbackManager().getCallbackType(PrivateCtcpReplyListener.class).call(sType, sMessage, sHost);
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
		return getCallbackManager().getCallbackType(PrivateMessageListener.class).call(sMessage, sHost);
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
		return getCallbackManager().getCallbackType(PrivateNoticeListener.class).call(sMessage, sHost);
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
		return getCallbackManager().getCallbackType(UnknownActionListener.class).call(sMessage, sTarget, sHost);
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
		return getCallbackManager().getCallbackType(UnknownCtcpListener.class).call(sType, sMessage, sTarget, sHost);
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
		return getCallbackManager().getCallbackType(UnknownCtcpReplyListener.class).call(sType, sMessage, sTarget, sHost);
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
		return getCallbackManager().getCallbackType(UnknownMessageListener.class).call(sMessage, sTarget, sHost);
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
		return getCallbackManager().getCallbackType(UnknownNoticeListener.class).call(sMessage, sTarget, sHost);
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
