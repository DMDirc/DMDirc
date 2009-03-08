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
 * Process a Names reply.
 */
public class ProcessNames extends IRCProcessor {
	/**
	 * Process a Names reply.
	 *
	 * @param sParam Type of line to process ("366", "353")
	 * @param token IRCTokenised line to process
	 */
	@Override
	public void process(String sParam, String[] token) {
		ChannelInfo iChannel;
		if (sParam.equals("366")) {
			// End of names
			iChannel = getChannelInfo(token[3]);
			if (iChannel == null) { return; }
			
			iChannel.setAddingNames(false);
			callChannelGotNames(iChannel);
			
			if (!iChannel.hasAskedForListModes()) {
				if (myParser.getAutoListMode()) {
					iChannel.requestListModes();
				}
			}
		} else {
			// Names
			
			ClientInfo iClient;
			ChannelClientInfo iChannelClient;
			
			iChannel = getChannelInfo(token[4]);
		
			if (iChannel == null) { return; }
			
			// If we are not expecting names, clear the current known names - this is fresh stuff!
			if (!iChannel.isAddingNames()) { iChannel.emptyChannel(); }
			iChannel.setAddingNames(true);
			
			String[] sNames = token[token.length-1].split(" ");
			String sNameBit = "", sName = "";
			StringBuilder sModes = new StringBuilder();
			long nPrefix = 0;
			for (int j = 0; j < sNames.length; ++j) {
				sNameBit = sNames[j];
				// If name is empty (ie there was an extra space) ignore it.
				if (sNameBit.isEmpty()) { continue; }
				// This next bit of code allows for any ircd which decides to use @+Foo in names
				for (int i = 0; i < sNameBit.length(); ++i) {
					Character cMode = sNameBit.charAt(i);
					// hPrefixMap contains @, o, +, v this caused issue 107
					// hPrefixModes only contains o, v so if the mode is in hPrefixMap
					// and not in hPrefixModes, its ok to use.
					if (myParser.hPrefixMap.containsKey(cMode) && !myParser.hPrefixModes.containsKey(cMode)) {
						sModes.append(cMode);
						nPrefix = nPrefix + myParser.hPrefixModes.get(myParser.hPrefixMap.get(cMode));
					} else {
						sName = sNameBit.substring(i);
						break;
					}
				}
				callDebugInfo(IRCParser.DEBUG_INFO, "Name: %s Modes: \"%s\" [%d]",sName,sModes.toString(),nPrefix);
				
				iClient = getClientInfo(sName);
				if (iClient == null) { iClient = new ClientInfo(myParser, sName); myParser.addClient(iClient); }
				iChannelClient = iChannel.addClient(iClient);
				iChannelClient.setChanMode(nPrefix);

				sName = "";
				sModes = new StringBuilder();
				nPrefix = 0;
			}
		}
	}
	
	/**
	 * Callback to all objects implementing the ChannelGotNames Callback.
	 *
	 * @see IChannelGotNames
	 * @param cChannel Channel which the names reply is for
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callChannelGotNames(ChannelInfo cChannel) {
		return getCallbackManager().getCallbackType("OnChannelGotNames").call(cChannel);
	}
	
	/**
	 * What does this IRCProcessor handle.
	 *
	 * @return String[] with the names of the tokens we handle.
	 */
	@Override
	public String[] handles() {
		return new String[]{"353", "366"};
	} 
	
	/**
	 * Create a new instance of the IRCProcessor Object.
	 *
	 * @param parser IRCParser That owns this IRCProcessor
	 * @param manager ProcessingManager that is in charge of this IRCProcessor
	 */
	protected ProcessNames (IRCParser parser, ProcessingManager manager) { super(parser, manager); }

}
