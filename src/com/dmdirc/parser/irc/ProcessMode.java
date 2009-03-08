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

import com.dmdirc.parser.irc.callbacks.CallbackObject;
import java.util.Calendar;

/**
 * Process a Mode line.
 */
public class ProcessMode extends IRCProcessor {
	/**
	 * Process a Mode Line.
	 *
	 * @param sParam Type of line to process ("MODE", "324")
	 * @param token IRCTokenised line to process
	 */
	@Override
	public void process(String sParam, String[] token) {
		String[] sModestr;
		String sChannelName;
		if (sParam.equals("324")) {
			sChannelName = token[3];
			sModestr = new String[token.length-4];
			System.arraycopy(token, 4, sModestr, 0, token.length-4);
		} else if (sParam.equals("221")) {
			processUserMode(sParam, token, new String[]{token[token.length-1]}, true);
			return;
		} else {
			sChannelName = token[2];
			sModestr = new String[token.length-3];
			System.arraycopy(token, 3, sModestr, 0, token.length-3);
		}

		if (!isValidChannelName(sChannelName)) { processUserMode(sParam, token, sModestr, false); }
		else { processChanMode(sParam, token, sModestr, sChannelName); }
	}
	
	/**
	 * Method to trim spaces from strings
	 *
	 * @param str String to trim
	 * @return String without spaces on the ends
	 */
	private String trim(String str) { return str.trim(); }
	
	/**
	 * Process Chan modes.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 * @param sModestr The modes and params
	 * @param sChannelName Channel these modes are for
	 */	
	public void processChanMode(String sParam, String token[], String sModestr[], String sChannelName) {
		StringBuilder sFullModeStr = new StringBuilder();
		String sNonUserModeStr = "";
		String sNonUserModeStrParams = "";
		String sModeParam;
		String sTemp;
		int nParam = 1;
		long nTemp = 0, nValue = 0, nCurrent = 0;
		boolean bPositive = true, bBooleanMode = true;
		char cPositive = '+';
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClientInfo;
		ClientInfo iClient;
		ChannelClientInfo setterCCI;
		
		CallbackObject cbSingle = null;
		CallbackObject cbNonUser = null;

		if (!sParam.equals("324")) {
			cbSingle = getCallbackManager().getCallbackType("OnChannelSingleModeChanged");
			cbNonUser = getCallbackManager().getCallbackType("OnChannelNonUserModeChanged");
		}
		
		iChannel = getChannelInfo(sChannelName);
		if (iChannel == null) { 
			// callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got modes for channel ("+sChannelName+") that I am not on.", myParser.getLastLine()));
			// iChannel = new ChannelInfo(myParser, sChannelName);
			// myParser.addChannel(iChannel);
			return;
		}
		// Get the current channel modes
		if (!sParam.equals("324")) { nCurrent = iChannel.getMode(); }
		
		setterCCI = iChannel.getUser(token[0]);
		if (IRCParser.ALWAYS_UPDATECLIENT && setterCCI != null) {
			// Facilitate dmdirc formatter
			if (setterCCI.getClient().getHost().isEmpty()) {setterCCI.getClient().setUserBits(token[0],false); }
		}
		
		// Loop through the mode string, and add/remove modes/params where they are needed
		for (int i = 0; i < sModestr[0].length(); ++i) {
			Character cMode = sModestr[0].charAt(i);
			if (cMode.equals(":".charAt(0))) { continue; }
			
			sNonUserModeStr = sNonUserModeStr+cMode;
			if (cMode.equals("+".charAt(0))) { cPositive = '+'; bPositive = true; }
			else if (cMode.equals("-".charAt(0))) { cPositive = '-'; bPositive = false; }
			else {
				if (myParser.hChanModesBool.containsKey(cMode)) { nValue = myParser.hChanModesBool.get(cMode); bBooleanMode = true; }
				else if (myParser.hChanModesOther.containsKey(cMode)) { nValue = myParser.hChanModesOther.get(cMode); bBooleanMode = false; }
				else if (myParser.hPrefixModes.containsKey(cMode)) { 
					// (de) OP/Voice someone
					if (sModestr.length <= nParam) {
						myParser.callErrorInfo(new ParserError(ParserError.ERROR_FATAL + ParserError.ERROR_USER, "Broken Modes. Parameter required but not given.", myParser.getLastLine()));
					}
					sModeParam = sModestr[nParam++];
					nValue = myParser.hPrefixModes.get(cMode);
					callDebugInfo(IRCParser.DEBUG_INFO, "User Mode: %c / %d [%s] {Positive: %b}",cMode, nValue, sModeParam, bPositive);
					iChannelClientInfo = iChannel.getUser(sModeParam);
					if (iChannelClientInfo == null) {
						// Client not known?
//						callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got mode for client not known on channel - Added", myParser.getLastLine()));
						iClient = getClientInfo(sModeParam);
						if (iClient == null) { 
//							callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got mode for client not known at all - Added", myParser.getLastLine()));
							iClient = new ClientInfo(myParser, sModeParam);
							myParser.addClient(iClient);
						}
						iChannelClientInfo = iChannel.addClient(iClient);
					}
					callDebugInfo(IRCParser.DEBUG_INFO, "\tOld Mode Value: %d",iChannelClientInfo.getChanMode());
					if (bPositive) { iChannelClientInfo.setChanMode(iChannelClientInfo.getChanMode() | nValue); sTemp = "+"; }
					else { iChannelClientInfo.setChanMode(iChannelClientInfo.getChanMode() ^ (iChannelClientInfo.getChanMode() & nValue)); sTemp = "-"; }
					sTemp = sTemp+cMode;
					callChannelUserModeChanged(iChannel, iChannelClientInfo, setterCCI, token[0], sTemp);
					continue;
				} else {
					// unknown mode - add as boolean
//					callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got unknown mode "+cMode+" - Added as boolean mode", myParser.getLastLine()));
					myParser.hChanModesBool.put(cMode,myParser.nNextKeyCMBool);
					nValue = myParser.nNextKeyCMBool;
					bBooleanMode = true;
					myParser.nNextKeyCMBool = myParser.nNextKeyCMBool*2;
				}
				
				if (bBooleanMode) {
					callDebugInfo(IRCParser.DEBUG_INFO, "Boolean Mode: %c [%d] {Positive: %b}",cMode, nValue, bPositive);
					
					if (bPositive) { nCurrent = nCurrent | nValue; }
					else { nCurrent = nCurrent ^ (nCurrent & nValue); }
				} else {
					
					if ((bPositive || nValue == IRCParser.MODE_LIST || ((nValue & IRCParser.MODE_UNSET) == IRCParser.MODE_UNSET)) && (sModestr.length <= nParam)) {
						myParser.callErrorInfo(new ParserError(ParserError.ERROR_FATAL + ParserError.ERROR_USER, "Broken Modes. Parameter required but not given.", myParser.getLastLine()));
					}
					
					if (nValue == IRCParser.MODE_LIST) {
						// List Mode
						sModeParam = sModestr[nParam++];
						sNonUserModeStrParams = sNonUserModeStrParams+" "+sModeParam;
						nTemp = (Calendar.getInstance().getTimeInMillis() / 1000);
						iChannel.setListModeParam(cMode, new ChannelListModeItem(sModeParam, token[0], nTemp ), bPositive);
						callDebugInfo(IRCParser.DEBUG_INFO, "List Mode: %c [%s] {Positive: %b}",cMode, sModeParam, bPositive);
						if (cbSingle != null) { cbSingle.call(iChannel, setterCCI, token[0], cPositive+cMode+" "+sModeParam ); }
					} else {
						// Mode with a parameter
						if (bPositive) { 
							// +Mode - always needs a parameter to set
							sModeParam = sModestr[nParam++];
							sNonUserModeStrParams = sNonUserModeStrParams+" "+sModeParam;
							callDebugInfo(IRCParser.DEBUG_INFO, "Set Mode: %c [%s] {Positive: %b}",cMode, sModeParam, bPositive);
							iChannel.setModeParam(cMode,sModeParam);
							if (cbSingle != null) { cbSingle.call(iChannel, setterCCI, token[0], cPositive+cMode+" "+sModeParam ); }
						} else {
							// -Mode - parameter isn't always needed, we need to check
							if ((nValue & IRCParser.MODE_UNSET) == IRCParser.MODE_UNSET) {
								sModeParam = sModestr[nParam++];
								sNonUserModeStrParams = sNonUserModeStrParams+" "+sModeParam;
							} else {
								sModeParam = "";
							}
							callDebugInfo(IRCParser.DEBUG_INFO, "Unset Mode: %c [%s] {Positive: %b}",cMode, sModeParam, bPositive);
							iChannel.setModeParam(cMode,"");
							if (cbSingle != null) { cbSingle.call(iChannel, setterCCI, token[0], trim(cPositive+cMode+" "+sModeParam) ); }
						}
					}
				}
			}
		}
		
		// Call Callbacks
		for (int i = 0; i < sModestr.length; ++i) { sFullModeStr.append(sModestr[i]).append(" "); }
		
		iChannel.setMode(nCurrent);
		if (sParam.equals("324")) { callChannelModeChanged(iChannel, null, "", sFullModeStr.toString().trim()); }
		else { callChannelModeChanged(iChannel, setterCCI, token[0], sFullModeStr.toString().trim()); }
		if (cbNonUser != null) { cbNonUser.call(iChannel, setterCCI, token[0], trim(sNonUserModeStr+sNonUserModeStrParams)); }
	}
	
	/**
	 * Process user modes.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 * @param clearOldModes Clear old modes before applying these modes (used by 221)
	 */	
	private void processUserMode(String sParam, String token[], String sModestr[], boolean clearOldModes) {
		long nCurrent = 0, nValue = 0;
		boolean bPositive = true;
		
		ClientInfo iClient;
		
		iClient = getClientInfo(token[2]);
		if (iClient == null) { return; }
		
		if (clearOldModes) {
			nCurrent = 0;
		} else {
			nCurrent = iClient.getUserMode();
		}
		
		for (int i = 0; i < sModestr[0].length(); ++i) {
			Character cMode = sModestr[0].charAt(i);
			if (cMode.equals("+".charAt(0))) { bPositive = true; }
			else if (cMode.equals("-".charAt(0))) { bPositive = false; }
			else if (cMode.equals(":".charAt(0))) { continue; }
			else {
				if (myParser.hUserModes.containsKey(cMode)) { nValue = myParser.hUserModes.get(cMode); }
				else {
					// Unknown mode
					callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got unknown user mode "+cMode+" - Added", myParser.getLastLine()));
					myParser.hUserModes.put(cMode,myParser.nNextKeyUser);
					nValue = myParser.nNextKeyUser;
					myParser.nNextKeyUser = myParser.nNextKeyUser*2;
				}
				// Usermodes are always boolean
				callDebugInfo(IRCParser.DEBUG_INFO, "User Mode: %c [%d] {Positive: %b}",cMode, nValue, bPositive);
				if (bPositive) { nCurrent = nCurrent | nValue; }
				else { nCurrent = nCurrent ^ (nCurrent & nValue); }
			}
		}
		
		iClient.setUserMode(nCurrent);
		if (sParam.equals("221")) {
			callUserModeDiscovered(iClient, sModestr[0]);
		} else {
			callUserModeChanged(iClient, token[0], sModestr[0]);
		}
	}
	
	/**
	 * Callback to all objects implementing the ChannelModeChanged Callback.
	 *
	 * @see IChannelModeChanged
	 * @param cChannel Channel where modes were changed
	 * @param cChannelClient Client chaning the modes (null if server)
	 * @param sHost Host doing the mode changing (User host or server name)
	 * @param sModes Exact String parsed
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelModeChanged(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sHost, String sModes) {
		return getCallbackManager().getCallbackType("OnChannelModeChanged").call(cChannel, cChannelClient, sHost, sModes);
	}
	
	/**
	 * Callback to all objects implementing the ChannelUserModeChanged Callback.
	 *
	 * @see IChannelUserModeChanged
	 * @param cChannel Channel where modes were changed
	 * @param cChangedClient Client being changed
	 * @param cSetByClient Client chaning the modes (null if server)
	 * @param sMode String representing mode change (ie +o)
	 * @param sHost Host doing the mode changing (User host or server name)
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelUserModeChanged(ChannelInfo cChannel, ChannelClientInfo cChangedClient, ChannelClientInfo cSetByClient, String sHost, String sMode) {
		return getCallbackManager().getCallbackType("OnChannelUserModeChanged").call(cChannel, cChangedClient, cSetByClient, sHost, sMode);
	}
	
	/**
	 * Callback to all objects implementing the UserModeChanged Callback.
	 *
	 * @see IUserModeChanged
	 * @param cClient Client that had the mode changed (almost always us)
	 * @param sSetby Host that set the mode (us or servername)
	 * @param sModes The modes set.
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callUserModeChanged(ClientInfo cClient, String sSetby, String sModes) {
		return getCallbackManager().getCallbackType("OnUserModeChanged").call(cClient, sSetby, sModes);
	}
	
	/**
	 * Callback to all objects implementing the UserModeDiscovered Callback.
	 *
	 * @see IUserModeDiscovered
	 * @param cClient Client that had the mode changed (almost always us)
	 * @param sModes The modes set.
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callUserModeDiscovered(ClientInfo cClient, String sModes) {
		return getCallbackManager().getCallbackType("OnUserModeDiscovered").call(cClient, sModes);
	}	
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	@Override
	public String[] handles() {
		return new String[]{"MODE", "324", "221"};
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessMode (IRCParser parser, ProcessingManager manager) { super(parser, manager); }

}
