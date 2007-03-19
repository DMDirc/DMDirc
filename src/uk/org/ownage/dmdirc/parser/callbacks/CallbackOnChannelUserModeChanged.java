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

package uk.org.ownage.dmdirc.parser.callbacks;

import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.parser.ParserError;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelUserModeChanged;

/**
 * Callback to all objects implementing the IChannelUserModeChanged Interface.
 */
public final class CallbackOnChannelUserModeChanged extends CallbackObjectSpecific {
    	
	/**
	 * Create a new instance of the Callback Object.
	 *
	 * @param parser IRCParser That owns this callback
	 * @param manager CallbackManager that is in charge of this callback
	 */
	public CallbackOnChannelUserModeChanged(final IRCParser parser, 
                final CallbackManager manager) { 
            super(parser, manager); 
        }
        
	/**
	 * Callback to all objects implementing the IChannelUserModeChanged Interface.
	 *
	 * @see IChannelUserModeChanged
	 * @param cChannel Channel where modes were changed
	 * @param cChangedClient Client being changed
	 * @param cSetByClient Client chaning the modes (null if server)
	 * @param sMode String representing mode change (ie +o)
	 * @param sHost Host doing the mode changing (User host or server name)
	 * @return true if a callback was called, else false
	 */
	public boolean call(final ChannelInfo cChannel, 
                final ChannelClientInfo cChangedClient, 
                final ChannelClientInfo cSetByClient, 
                final String sHost, final String sMode) {
		boolean bResult = false;
		IChannelUserModeChanged eMethod = null;
		for (int i = 0; i < callbackInfo.size(); i++) {
			eMethod = (IChannelUserModeChanged) callbackInfo.get(i);
			if (!this.isValidChan(eMethod, cChannel)) { continue; }
			try {
				eMethod.onChannelUserModeChanged(myParser, cChannel, cChangedClient, cSetByClient, sHost, sMode);
			} catch (Exception e) {
				final ParserError ei = new ParserError(ParserError.errError, "Exception in onChannelUserModeChanged");
				ei.setException(e);
				callErrorInfo(ei);
			}
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo() { 
            return "$Id$"; 
        }	
}
