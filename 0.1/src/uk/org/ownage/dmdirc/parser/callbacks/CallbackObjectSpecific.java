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
 * SVN: $Id: IRCParser.java 178 2007-02-28 20:36:16Z ShaneMcC $
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
 * @version           $Id: IRCParser.java 178 2007-02-28 20:36:16Z ShaneMcC $
 */
public abstract class CallbackObjectSpecific extends CallbackObject {
	/**
	 * Hashtable for storing specific information for callback.
	 */	
	protected Hashtable<ICallbackInterface,String> specificData = new Hashtable<ICallbackInterface,String>();
	
	/** Used to check if a channel matches the specificData */
	protected boolean isValidChan(ICallbackInterface eMethod, ChannelInfo cChannel) {
		if (specificData.containsKey(eMethod)) { 
			if (!cChannel.getName().equalsIgnoreCase(specificData.get(eMethod))) { return false; }
		}
		return true;
	}
	
	// We override the default add method to make sure that any add with no
	// specifics will have the specific data removed.
	public void add(ICallbackInterface eMethod) {
		addCallback(eMethod);
		if (specificData.containsKey(eMethod)) { specificData.remove(eMethod); }
	}
	
	public void add(ICallbackInterface eMethod, String channelName) {
		add(eMethod);
		if (!channelName.equals("")) {
			specificData.put(eMethod,channelName);
		}
	}
	
	public void del(ICallbackInterface eMethod) {
		delCallback(eMethod);
		if (specificData.containsKey(eMethod)) { specificData.remove(eMethod); }
	}
	
	// Stupid lack of Constructor inheritance...
	public CallbackObjectSpecific (IRCParser parser, CallbackManager manager) { super(parser, manager); }
}