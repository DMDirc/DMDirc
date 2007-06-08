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

import com.dmdirc.parser.callbacks.CallbackOnChannelAction;
import com.dmdirc.parser.callbacks.CallbackOnChannelCTCP;
import com.dmdirc.parser.callbacks.CallbackOnChannelCTCPReply;
import com.dmdirc.parser.callbacks.CallbackOnChannelMessage;
import com.dmdirc.parser.callbacks.CallbackOnChannelNotice;
import com.dmdirc.parser.callbacks.CallbackOnPrivateAction;
import com.dmdirc.parser.callbacks.CallbackOnPrivateCTCP;
import com.dmdirc.parser.callbacks.CallbackOnPrivateCTCPReply;
import com.dmdirc.parser.callbacks.CallbackOnPrivateMessage;
import com.dmdirc.parser.callbacks.CallbackOnPrivateNotice;
import com.dmdirc.parser.callbacks.CallbackOnUnknownAction;
import com.dmdirc.parser.callbacks.CallbackOnUnknownCTCP;
import com.dmdirc.parser.callbacks.CallbackOnUnknownCTCPReply;
import com.dmdirc.parser.callbacks.CallbackOnUnknownMessage;
import com.dmdirc.parser.callbacks.CallbackOnUnknownNotice;


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
	public void process(final String sParam, String[] token) {
		// Ignore people!
		String sMessage = "";
		if (token[0].charAt(0) == ':') { sMessage = token[0].substring(1); } else { sMessage = token[0]; }
		// We use sMessage to be the users host (first token in the line)
		if (myParser.getIgnoreList().matches(sMessage) > -1) { return; }
		
		// Lines such as:
		// "nick!user@host PRIVMSG #channel"
		// are invalid, stop processing.
		if (token.length < 4) { return; }
		
		ChannelClientInfo iChannelClient = null;
		ChannelInfo iChannel = null;
		ClientInfo iClient = null;
		sMessage = token[token.length-1];
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
					if (!sMessage.equals("")) { sMessage = sMessage.split(Char1.toString(),2)[0]; }
					else { sCTCP = sCTCP.split(Char1.toString(),2)[0]; }
					callDebugInfo(myParser.DEBUG_INFO, "CTCP: \"%s\" \"%s\"",sCTCP,sMessage);
				}
			}
		}

		// Remove the leading : from the host.
		if (token[0].charAt(0) == ':' && token[0].length() > 1) { token[0] = token[0].substring(1); }

		iClient = getClientInfo(token[0]);
		if (myParser.ALWAYS_UPDATECLIENT && iClient != null) {
			// Facilitate DMDIRC Formatter
			if (iClient.getHost().equals("")) {iClient.setUserBits(token[0],false); }
		}
		
		// Fire the appropriate callbacks.
		// OnChannel* Callbacks are fired if the target was a channel
		// OnPrivate* Callbacks are fired if the target was us
		// OnUnknown* Callbacks are fired if the target was neither of the above
		// Actions and CTCPs are send as PRIVMSGS
		// CTCPReplies are sent as Notices
		if (isValidChannelName(token[2])) {
			iChannel = getChannelInfo(token[2]);
			if (iClient != null && iChannel != null) { iChannelClient = iChannel.getUser(iClient); }
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
		} else if (token[2].equalsIgnoreCase(myParser.cMyself.getNickname())) {
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
			callDebugInfo(myParser.DEBUG_INFO, "Message for Other ("+token[2]+")");
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
	 * @see com.dmdirc.parser.callbacks.interfaces.IChannelAction
	 * @param cChannel Channel where the action was sent to
	 * @param cChannelClient ChannelClient who sent the action (may be null if server)
	 * @param sMessage action contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callChannelAction(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
		final CallbackOnChannelAction cb = (CallbackOnChannelAction)getCallbackManager().getCallbackType("OnChannelAction");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sMessage, sHost); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the ChannelCTCP Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IChannelCTCP
	 * @param cChannel Channel where CTCP was sent
	 * @param cChannelClient ChannelClient who sent the message (may be null if server)
	 * @param sType Type of CTCP (VERSION, TIME etc)
	 * @param sMessage Additional contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callChannelCTCP(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sType, final String sMessage, final String sHost) {
		final CallbackOnChannelCTCP cb = (CallbackOnChannelCTCP)getCallbackManager().getCallbackType("OnChannelCTCP");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sType, sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelCTCPReply Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IChannelCTCPReply
	 * @param cChannel Channel where CTCPReply was sent
	 * @param cChannelClient ChannelClient who sent the message (may be null if server)
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callChannelCTCPReply(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sType, final String sMessage, final String sHost) {
		final CallbackOnChannelCTCPReply cb = (CallbackOnChannelCTCPReply)getCallbackManager().getCallbackType("OnChannelCTCPReply");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sType, sMessage, sHost); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the ChannelMessage Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IChannelMessage
	 * @param cChannel Channel where the message was sent to
	 * @param cChannelClient ChannelClient who sent the message (may be null if server)
	 * @param sMessage Message contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callChannelMessage(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
		final CallbackOnChannelMessage cb = (CallbackOnChannelMessage)getCallbackManager().getCallbackType("OnChannelMessage");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sMessage, sHost); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the ChannelNotice Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IChannelNotice
	 * @param cChannel Channel where the notice was sent to
	 * @param cChannelClient ChannelClient who sent the notice (may be null if server)
	 * @param sMessage notice contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callChannelNotice(final ChannelInfo cChannel, final ChannelClientInfo cChannelClient, final String sMessage, final String sHost) {
		final CallbackOnChannelNotice cb = (CallbackOnChannelNotice)getCallbackManager().getCallbackType("OnChannelNotice");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sMessage, sHost); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the PrivateAction Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IPrivateAction
	 * @param sMessage action contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callPrivateAction(final String sMessage, final String sHost) {
		final CallbackOnPrivateAction cb = (CallbackOnPrivateAction)getCallbackManager().getCallbackType("OnPrivateAction");
		if (cb != null) { return cb.call(sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the PrivateCTCP Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IPrivateCTCP
	 * @param sType Type of CTCP (VERSION, TIME etc)
	 * @param sMessage Additional contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callPrivateCTCP(final String sType, final String sMessage, final String sHost) {
		final CallbackOnPrivateCTCP cb = (CallbackOnPrivateCTCP)getCallbackManager().getCallbackType("OnPrivateCTCP");
		if (cb != null) { return cb.call(sType, sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the PrivateCTCPReply Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IPrivateCTCPReply
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callPrivateCTCPReply(final String sType, final String sMessage, final String sHost) {
		final CallbackOnPrivateCTCPReply cb = (CallbackOnPrivateCTCPReply)getCallbackManager().getCallbackType("OnPrivateCTCPReply");
		if (cb != null) { return cb.call(sType, sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the PrivateMessage Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IPrivateMessage
	 * @param sMessage Message contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callPrivateMessage(final String sMessage, final String sHost) {
		final CallbackOnPrivateMessage cb = (CallbackOnPrivateMessage)getCallbackManager().getCallbackType("OnPrivateMessage");
		if (cb != null) { return cb.call(sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the PrivateNotice Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IPrivateNotice
	 * @param sMessage Notice contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callPrivateNotice(final String sMessage, final String sHost) {
		final CallbackOnPrivateNotice cb = (CallbackOnPrivateNotice)getCallbackManager().getCallbackType("OnPrivateNotice");
		if (cb != null) { return cb.call(sMessage, sHost); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the UnknownAction Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IUnknownAction
	 * @param sMessage Action contents
	 * @param sTarget Actual target of action
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callUnknownAction(final String sMessage, final String sTarget, final String sHost) {
		final CallbackOnUnknownAction cb = (CallbackOnUnknownAction)getCallbackManager().getCallbackType("OnUnknownAction");
		if (cb != null) { return cb.call(sMessage, sTarget, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the UnknownCTCP Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IUnknownCTCP
	 * @param sType Type of CTCP (VERSION, TIME etc)
	 * @param sMessage Additional contents
	 * @param sTarget Actual Target of CTCP
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callUnknownCTCP(final String sType, final String sMessage, final String sTarget, final String sHost) {
		final CallbackOnUnknownCTCP cb = (CallbackOnUnknownCTCP)getCallbackManager().getCallbackType("OnUnknownCTCP");
		if (cb != null) { return cb.call(sType, sMessage, sTarget, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the UnknownCTCPReply Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IUnknownCTCPReply
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sTarget Actual Target of CTCPReply
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callUnknownCTCPReply(final String sType, final String sMessage, final String sTarget, final String sHost) {
		final CallbackOnUnknownCTCPReply cb = (CallbackOnUnknownCTCPReply)getCallbackManager().getCallbackType("OnUnknownCTCPReply");
		if (cb != null) { return cb.call(sType, sMessage, sTarget, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the UnknownMessage Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IUnknownMessage
	 * @param sMessage Message contents
	 * @param sTarget Actual target of message
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callUnknownMessage(final String sMessage, final String sTarget, final String sHost) {
		final CallbackOnUnknownMessage cb = (CallbackOnUnknownMessage)getCallbackManager().getCallbackType("OnUnknownMessage");
		if (cb != null) { return cb.call(sMessage, sTarget, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the UnknownNotice Callback.
	 *
	 * @see com.dmdirc.parser.callbacks.interfaces.IUnknownNotice
	 * @param sMessage Notice contents
	 * @param sTarget Actual target of notice
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callUnknownNotice(final String sMessage, final String sTarget, final String sHost) {
		final CallbackOnUnknownNotice cb = (CallbackOnUnknownNotice)getCallbackManager().getCallbackType("OnUnknownNotice");
		if (cb != null) { return cb.call(sMessage, sTarget, sHost); }
		return false;
	}

	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[2];
		iHandle[0] = "PRIVMSG";
		iHandle[1] = "NOTICE";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessMessage (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
