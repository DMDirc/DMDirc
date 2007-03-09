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

import java.util.Calendar;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnChannelSingleModeChanged;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnChannelNonUserModeChanged;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnChannelModeChanged;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnChannelUserModeChanged;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnUserModeChanged;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelSingleModeChanged;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelNonUserModeChanged;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelModeChanged;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelUserModeChanged;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IUserModeChanged;

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
	public void process(String sParam, String[] token) {
		String[] sModestr;
		String sChannelName;
		if (sParam.equals("324")) {
			sChannelName = token[3];
			sModestr = new String[token.length-4];
			System.arraycopy(token, 4, sModestr, 0, token.length-4);
		} else {
			sChannelName = token[2];
			sModestr = new String[token.length-3];
			System.arraycopy(token, 3, sModestr, 0, token.length-3);
		}

		if (!isValidChannelName(sChannelName)) { processUserMode(sParam, token, sModestr); }
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
	 */	
	public void processChanMode(String sParam, String token[], String sModestr[], String sChannelName) {
		String sFullModeStr;
		String sNonUserModeStr = "";
		String sNonUserModeStrParams = "";
		String sModeParam;
		String sTemp;
		int nCurrent = 0, nParam = 1, nValue = 0;
		long nTemp = 0;
		boolean bPositive = true, bBooleanMode = true;
		char cPositive = '+';
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClientInfo;
		ClientInfo iClient;
		ChannelClientInfo setterCCI;
		
		CallbackOnChannelSingleModeChanged cbSingle = null;
		CallbackOnChannelNonUserModeChanged cbNonUser = null;
		if (!sParam.equals("324")) {
			cbSingle = (CallbackOnChannelSingleModeChanged)getCallbackManager().getCallbackType("OnChannelSingleModeChanged");
			cbNonUser = (CallbackOnChannelNonUserModeChanged)getCallbackManager().getCallbackType("OnChannelNonUserModeChanged");
		}
		
		iChannel = getChannelInfo(sChannelName);
		if (iChannel == null) { 
			callErrorInfo(new ParserError(ParserError.errWarning, "Got modes for channel ("+sChannelName+") that I am not on."));
			iChannel = new ChannelInfo(myParser, sChannelName);
			myParser.hChannelList.put(iChannel.getName().toLowerCase(),iChannel);
		}
		// Get the current channel modes
		if (!sParam.equals("324")) { nCurrent = iChannel.getMode(); }
		
		setterCCI = iChannel.getUser(token[0]);
		if (myParser.alwaysUpdateClient && setterCCI != null) {
			// Facilitate dmdirc formatter
			if (setterCCI.getClient().getHost().equals("")) {setterCCI.getClient().setUserBits(token[0],false); }
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
					sModeParam = sModestr[nParam++];
					nValue = myParser.hPrefixModes.get(cMode);
					callDebugInfo(myParser.ndInfo, "User Mode: %c / %d [%s] {Positive: %b}",cMode, nValue, sModeParam, bPositive);
					iChannelClientInfo = iChannel.getUser(sModeParam);
					if (iChannelClientInfo == null) {
						// Client not known?
						callErrorInfo(new ParserError(ParserError.errWarning, "Got mode for client not known on channel - Added"));
						iClient = getClientInfo(sModeParam);
						if (iClient == null) { 
							callErrorInfo(new ParserError(ParserError.errWarning, "Got mode for client not known at all - Added"));
							iClient = new ClientInfo(myParser, sModeParam);
							myParser.hClientList.put(iClient.getNickname().toLowerCase(),iClient);
						}
						iChannelClientInfo = iChannel.addClient(iClient);
					}
					callDebugInfo(myParser.ndInfo, "\tOld Mode Value: %d",iChannelClientInfo.getChanMode());
					if (bPositive) { iChannelClientInfo.setChanMode(iChannelClientInfo.getChanMode() + nValue); sTemp = "+"; }
					else { iChannelClientInfo.setChanMode(iChannelClientInfo.getChanMode() - nValue); sTemp = "-"; }
					sTemp = sTemp+cMode;
					
					callChannelUserModeChanged(iChannel, iChannelClientInfo, setterCCI, token[0], sTemp);
					continue;
				} else {
					// unknown mode - add as boolean
					callErrorInfo(new ParserError(ParserError.errWarning, "Got unknown mode "+cMode+" - Added as boolean mode"));
					myParser.hChanModesBool.put(cMode,myParser.nNextKeyCMBool);
					nValue = myParser.nNextKeyCMBool;
					bBooleanMode = true;
					myParser.nNextKeyCMBool = myParser.nNextKeyCMBool*2;
				}
				
				if (bBooleanMode) {
					callDebugInfo(myParser.ndInfo, "Boolean Mode: %c [%d] {Positive: %b}",cMode, nValue, bPositive);
					if (bPositive) { nCurrent = nCurrent + nValue; }
					else { nCurrent = nCurrent - nValue; }
				} else {
					if (nValue == myParser.cmList) {
						// List Mode
						sModeParam = sModestr[nParam++];
						sNonUserModeStrParams = sNonUserModeStrParams+" "+sModeParam;
						nTemp = (Calendar.getInstance().getTimeInMillis() / 1000);
						iChannel.setListModeParam(cMode, new ChannelListModeItem(sModeParam, token[0], nTemp ), bPositive);
						callDebugInfo(myParser.ndInfo, "List Mode: %c [%s] {Positive: %b}",cMode, sModeParam, bPositive);
						if (cbSingle != null) { cbSingle.call(iChannel, setterCCI, token[0], cPositive+cMode+" "+sModeParam ); }
					} else {
						// Mode with a parameter
						if (bPositive) { 
							// +Mode - always needs a parameter to set
							sModeParam = sModestr[nParam++];
							sNonUserModeStrParams = sNonUserModeStrParams+" "+sModeParam;
							callDebugInfo(myParser.ndInfo, "Set Mode: %c [%s] {Positive: %b}",cMode, sModeParam, bPositive);
							iChannel.setModeParam(cMode,sModeParam);
							if (cbSingle != null) { cbSingle.call(iChannel, setterCCI, token[0], cPositive+cMode+" "+sModeParam ); }
						} else {
							// -Mode - parameter isn't always needed, we need to check
							if ((nValue & myParser.cmUnset) == myParser.cmUnset) {
								sModeParam = sModestr[nParam++];
								sNonUserModeStrParams = sNonUserModeStrParams+" "+sModeParam;
							} else {
								sModeParam = "";
							}
							callDebugInfo(myParser.ndInfo, "Unset Mode: %c [%s] {Positive: %b}",cMode, sModeParam, bPositive);
							iChannel.setModeParam(cMode,"");
							if (cbSingle != null) { cbSingle.call(iChannel, setterCCI, token[0], trim(cPositive+cMode+" "+sModeParam) ); }
						}
					}
				}
			}
		}
		
		// Call Callbacks
		sFullModeStr = "";
		for (int i = 0; i < sModestr.length; ++i) { sFullModeStr = sFullModeStr+sModestr[i]+" "; }
		
		iChannel.setMode(nCurrent);
		if (sParam.equals("324")) { callChannelModeChanged(iChannel, null, "", trim(sFullModeStr)); }
		else { callChannelModeChanged(iChannel, setterCCI, token[0], trim(sFullModeStr)); }
		if (cbNonUser != null) { cbNonUser.call(iChannel, setterCCI, token[0], trim(sNonUserModeStr+sNonUserModeStrParams)); }
	}
	
	/**
	 * Process user modes.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void processUserMode(String sParam, String token[], String sModestr[]) {
		int nCurrent = 0, nValue = 0;
		boolean bPositive = true;
		
		ClientInfo iClient;
		
		iClient = getClientInfo(token[2]);
		if (iClient == null) { return; }
		
		nCurrent = iClient.getUserMode();
		
		for (int i = 0; i < sModestr[0].length(); ++i) {
			Character cMode = sModestr[0].charAt(i);
			if (cMode.equals("+".charAt(0))) { bPositive = true; }
			else if (cMode.equals("-".charAt(0))) { bPositive = false; }
			else if (cMode.equals(":".charAt(0))) { continue; }
			else {
				if (myParser.hUserModes.containsKey(cMode)) { nValue = myParser.hUserModes.get(cMode); }
				else {
					// Unknown mode
					callErrorInfo(new ParserError(ParserError.errWarning, "Got unknown user mode "+cMode+" - Added"));
					myParser.hUserModes.put(cMode,myParser.nNextKeyUser);
					nValue = myParser.nNextKeyUser;
					myParser.nNextKeyUser = myParser.nNextKeyUser*2;
				}
				// Usermodes are always boolean
				callDebugInfo(myParser.ndInfo, "User Mode: %c [%d] {Positive: %b}",cMode, nValue, bPositive);
				if (bPositive) { nCurrent = nCurrent + nValue; }
				else { nCurrent = nCurrent - nValue; }
			}
		}
		
		iClient.setUserMode(nCurrent);
		callUserModeChanged(iClient, token[0]);
	}	
	
	/**
	 * Callback to all objects implementing the ChannelModeChanged Callback.
	 *
	 * @see IChannelModeChanged
	 * @param cChannel Channel where modes were changed
	 * @param cChannelClient Client chaning the modes (null if server)
	 * @param sHost Host doing the mode changing (User host or server name)
	 * @param sModes Exact String parsed
	 */
	protected boolean callChannelModeChanged(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sHost, String sModes) {
		CallbackOnChannelModeChanged cb = (CallbackOnChannelModeChanged)getCallbackManager().getCallbackType("OnChannelModeChanged");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sHost, sModes); }
		return false;
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
	 */
	protected boolean callChannelUserModeChanged(ChannelInfo cChannel, ChannelClientInfo cChangedClient, ChannelClientInfo cSetByClient, String sHost, String sMode) {
		CallbackOnChannelUserModeChanged cb = (CallbackOnChannelUserModeChanged)getCallbackManager().getCallbackType("OnChannelUserModeChanged");
		if (cb != null) { return cb.call(cChannel, cChangedClient, cSetByClient, sHost, sMode); }
		return false;
	}
	
	/**
	 * Callback to all objects implementing the UserModeChanged Callback.
	 *
	 * @see IUserModeChanged
	 * @param cClient Client that had the mode changed (almost always us)
	 * @param sSetby Host that set the mode (us or servername)
	 */
	protected boolean callUserModeChanged(ClientInfo cClient, String sSetby) {
		CallbackOnUserModeChanged cb = (CallbackOnUserModeChanged)getCallbackManager().getCallbackType("OnUserModeChanged");
		if (cb != null) { return cb.call(cClient, sSetby); }
		return false;
	}	
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[2];
		iHandle[0] = "MODE";
		iHandle[1] = "324";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessMode (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
