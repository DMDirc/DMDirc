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

import uk.org.ownage.dmdirc.parser.*;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackManager;
import  uk.org.ownage.dmdirc.parser.callbacks.interfaces.ICallbackInterface;
import java.util.Hashtable;
import java.util.ArrayList;
/**
 * CallbackObjectSpecific.
 * Superclass for all callback types that have a "specific" target.
 *
 * @author            Shane Mc Cormack
 * @version           $Id$
 */
public abstract class CallbackObjectSpecific extends CallbackObject {
	/** Hashtable for storing specific information for callback. */	
	protected Hashtable<ICallbackInterface,String> specificData = new Hashtable<ICallbackInterface,String>();
	
	/**
	 * Used to check if a channel matches the specificData
	 * 
	 * @param eMethod Object that is being called back to
	 * @param cChannel ChannelInfo object for the channel to test
	 * @return true if channel given matches the specifics for the method given
	 */
	protected boolean isValidChan(ICallbackInterface eMethod, ChannelInfo cChannel) {
		if (specificData.containsKey(eMethod)) { 
			if (!cChannel.getName().equalsIgnoreCase(specificData.get(eMethod))) { return false; }
		}
		return true;
	}
	
	/**
	 * Used to check if a hostname matches the specificData
	 * 
	 * @param eMethod Object that is being called back to
	 * @param sHost Hostname of user that sent the query
	 * @return true if host given matches the specifics for the method given
	 */
	protected boolean isValidUser(ICallbackInterface eMethod, String sHost) {
		String nickname = ClientInfo.parseHost(sHost);
		if (specificData.containsKey(eMethod)) { 
			if (!nickname.equalsIgnoreCase(specificData.get(eMethod))) { return false; }
		}
		return true;
	}
	
	// We override the default add method to make sure that any add with no
	// specifics will have the specific data removed.
	/**
	 * Add a new callback.
	 *
	 * @param eMethod Object to callback to.
	 */
	public void add(ICallbackInterface eMethod) {
		addCallback(eMethod);
		if (specificData.containsKey(eMethod)) { specificData.remove(eMethod); }
	}
	
	/**
	 * Add a new callback with a specific target.
	 *
	 * @param eMethod Object to callback to.
	 * @param specificTarget Target that must match for callback to be called.
	 */
	public void add(ICallbackInterface eMethod, String specificTarget) {
		add(eMethod);
		if (!specificTarget.equals("")) {
			specificData.put(eMethod,specificTarget);
		}
	}
	
	/**
	 * Remove a callback.
	 *
	 * @param eMethod Object to remove callback to.
	 */
	public void del(ICallbackInterface eMethod) {
		delCallback(eMethod);
		if (specificData.containsKey(eMethod)) { specificData.remove(eMethod); }
	}
	
	/**
	 * Create a new instance of the Callback Object
	 *
	 * @param parser IRCParser That owns this callback
	 * @param manager CallbackManager that is in charge of this callback
	 */
	public CallbackObjectSpecific (IRCParser parser, CallbackManager manager) { super(parser, manager); }
}