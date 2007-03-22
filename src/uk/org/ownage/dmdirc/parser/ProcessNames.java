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

import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnChannelGotNames;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelGotNames;

/**
 * Process a Names reply.
 */
public class ProcessNames extends IRCProcessor {
	/**
	 * Process a Names reply.
	 *
	 * @param sParam Type of line to process ("366", "353")
	 * @param token IRCTokenised line to process
	 */
	public void process(String sParam, String[] token) {
		ChannelInfo iChannel;
		if (sParam.equals("366")) {
			// End of names
			iChannel = getChannelInfo(token[3]);
			if (iChannel != null) {
				iChannel.setAddingNames(false);
				callChannelGotNames(iChannel);
			}
		} else {
			// Names
			
			ClientInfo iClient;
			ChannelClientInfo iChannelClient;
			
			iChannel = getChannelInfo(token[4]);
		
			if (iChannel == null) { 
				callErrorInfo(new ParserError(ParserError.ERROR_WARNING, "Got names for channel ("+token[4]+") that I am not on."));
				iChannel = new ChannelInfo(myParser, token[4]);
				myParser.hChannelList.put(iChannel.getName().toLowerCase(),iChannel);
			}
			
			// If we are not expecting names, clear the current known names - this is fresh stuff!
			if (!iChannel.getAddingNames()) { iChannel.emptyChannel(); }
			iChannel.setAddingNames(true);
			
			String[] sNames = token[token.length-1].split(" ");
			String sNameBit = "", sName = "";
			StringBuffer sModes = new StringBuffer();
			int nPrefix = 0;
			for (int j = 0; j < sNames.length; ++j) {
				sNameBit = sNames[j];
				for (int i = 0; i < sNameBit.length(); ++i) {
					Character cMode = sNameBit.charAt(i);
					if (myParser.hPrefixMap.containsKey(cMode)) {
						// hPrefixMap contains @, o, +, v this caused issue 107
						// hPrefixModes only contains o, v so if the mode is in hPrefixMap
						// and not in hPrefixModes, its ok to use.
						if (!myParser.hPrefixModes.containsKey(cMode)) {
							sModes.append(cMode);
							nPrefix = nPrefix + myParser.hPrefixModes.get(myParser.hPrefixMap.get(cMode));
						}
					} else {
						sName = sNameBit.substring(i);
						break;
					}
				}
				callDebugInfo(myParser.DEBUG_INFO, "Name: %s Modes: \"%s\" [%d]",sName,sModes.toString(),nPrefix);
				
				iClient = getClientInfo(sName);
				if (iClient == null) { iClient = new ClientInfo(myParser, sName); myParser.hClientList.put(iClient.getNickname().toLowerCase(),iClient); }
				iChannelClient = iChannel.addClient(iClient);
				iChannelClient.setChanMode(nPrefix);

				sName = "";
				sModes = "";
				nPrefix = 0;
			}
		}
	}
	
	/**
	 * Callback to all objects implementing the ChannelGotNames Callback.
	 *
	 * @see IChannelGotNames
	 * @param cChannel Channel which the names reply is for
	 */
	protected boolean callChannelGotNames(ChannelInfo cChannel) {
		CallbackOnChannelGotNames cb = (CallbackOnChannelGotNames)getCallbackManager().getCallbackType("OnChannelGotNames");
		if (cb != null) { return cb.call(cChannel); }
		return false;
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	public String[] handles() {
		String[] iHandle = new String[2];
		iHandle[0] = "353";
		iHandle[1] = "366";
		return iHandle;
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessNames (IRCParser parser, ProcessingManager manager) { super(parser, manager); }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
