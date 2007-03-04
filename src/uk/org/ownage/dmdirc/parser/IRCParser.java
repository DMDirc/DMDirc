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

import uk.org.ownage.dmdirc.parser.callbacks.interfaces.*;
import uk.org.ownage.dmdirc.parser.callbacks.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import javax.net.SocketFactory;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * IRC Parser.
 *
 * @author            Shane Mc Cormack
 * @version           $Id$
 */
public class IRCParser implements Runnable {
	
	/** General Debug Information. */
	public static final int ndInfo = 1;   // Information
	/** Socket Debug Information. */	
	public static final int ndSocket = 2; // Socket Errors
//	public static final int ndSomething = 4; //Next thingy
	/** Socket Debug Information. */	
	public static final int ndGeneral = 4096; // General Debug Info. This will never be used if bDebug is false.

	/** Used in Error Reporting, Error is potentially Fatal, Desync 99% Guarenteed! */
	public static final int errFatal = 1;
	/** Used in Error Reporting, Error is not fatal, but is more severe than a warning. */
	public static final int errError = 2;
	/** Used in Error Reporting, Error was an unexpected occurance, but shouldn't be anything to worry about. */
	public static final int errWarning = 4;
	
	/** Socket is not created yet. */
	public static final byte stateNull = 0;
	/** Socket is closed. */	
	public static final byte stateClosed = 1;
	/** Socket is Open. */	
	public static final byte stateOpen = 2;

	/** Current Socket State */
	private byte nSocketState = 0;
	
	/**
	 * Get the current socket State.
	 *
	 * @return Current SocketState (stateNull, stateClosed or stateOpen)
	 */
	public byte getSocketState() { return nSocketState; }
	
	/**
	 * Enable Development Debugging info - Outputs directly to console.
	 *
	 * This is used for debugging info that is generally of no use to most people.<br>
	 * If this is set to false, self-test and any the "useless" debugging that relies on
	 * this being true are not compiled.
	 */
	public static final boolean bDebug = true;
	
	/** This is the socket used for reading from/writing to the IRC server. */
	private Socket socket = null;
	/** This is the socket used for reading from/writing to the IRC server when using SSL. */
	private SSLSocket sslSocket = null;
	/** Used for writing to the server. */
	private PrintWriter out = null;
	/** Used for reading from the server. */
	private BufferedReader in = null;
	
	/**
	 * This is what the user wants settings to be. 
	 * Nickname here is *not* always accurate.<br><br>
	 * ClientInfo variable tParser.getMyself() should be used for accurate info.
	 */
	public MyInfo me = new MyInfo();
	/**	Server Info requested by user. */
	public ServerInfo server = new ServerInfo();

	/** Name the server calls itself. */
	protected String sServerName;

	/** This is what we think the nickname should be. */
	protected String sThinkNickname;
	/** When using inbuilt pre-001 NickInUse handler, have we tried our AltNick. */
	protected boolean TriedAlt = false;
	
	/** Have we recieved the 001. */
	protected boolean Got001 = false;
	/** Has the thread started execution yet, (Prevents run() being called multiple times). */
	protected boolean HasBegan = false;
	/** Is this line the first line we have seen? */
	protected boolean IsFirst = true;
	
	/** Reference to the callback Manager */
	private CallbackManager myCallbackManager = new CallbackManager(this);
	
	/**
	 * Get a reference to the CallbackManager
	 *
	 * @return Reference to the CallbackManager
	 */
	public CallbackManager getCallbackManager() {
		return myCallbackManager;
	}
	
	/** Hashtable storing known prefix modes (ohv). */	
	protected Hashtable<Character,Integer> hPrefixModes = new Hashtable<Character,Integer>();
	/**
	 * Hashtable maping known prefix modes (ohv) to prefixes (@%+) - Both ways.
	 * Prefix map contains 2 pairs for each mode. (eg @ => o and o => @)
	 */	
	protected Hashtable<Character,Character> hPrefixMap = new Hashtable<Character,Character>();
	/** Integer representing the next avaliable integer value of a prefix mode. */
	protected int nNextKeyPrefix = 1;
	/** Hashtable storing known user modes (owxis etc). */
	protected Hashtable<Character,Integer> hUserModes = new Hashtable<Character,Integer>();
	/** Integer representing the next avaliable integer value of a User mode */	
	protected int nNextKeyUser = 1;
	/**
	 * Hashtable storing known boolean chan modes (cntmi etc).
	 * Valid Boolean Modes are stored as Hashtable.pub('m',1); where 'm' is the mode and 1 is a numeric value.<br><br>
	 * Numeric values are powers of 2. This allows up to 32 modes at present (expandable to 64)<br><br>
	 * ChannelInfo/ChannelClientInfo etc provide methods to view the modes in a human way.<br><br>
	 * <br>
	 * Channel modes discovered but not listed in 005 are stored as boolean modes automatically (and a errWarning Error is called)
	 */
	protected Hashtable<Character,Integer> hChanModesBool = new Hashtable<Character,Integer>();
	/** Integer representing the next avaliable integer value of a Boolean mode. */	
	protected int nNextKeyCMBool = 1;
	
	/**
	 * Hashtable storing known non-boolean chan modes (klbeI etc).
	 * Non Boolean Modes (for Channels) are stored together in this hashtable, the value param
	 * is used to show the type of variable. (List (1), Param just for set (2), Param for Set and Unset (2+4=6))<br><br>
	 *<br>
	 * see cmList<br>
	 * see cmSet<br>
	 * see cmUnset<br>
	 */
	protected Hashtable<Character,Byte> hChanModesOther = new Hashtable<Character,Byte>();	
	/** Byte used to show that a non-boolean mode is a list (b). */
	protected static final byte cmList = 1;
	/** Byte used to show that a non-boolean mode is not a list, and requires a parameter to set (lk). */
	protected static final byte cmSet = 2;	
	/** Byte used to show that a non-boolean mode is not a list, and requires a parameter to unset (k). */
	protected static final byte cmUnset = 4;
	
	/** 
	* Channel Prefixes (ie # + etc).
	* The "value" for these is always true.
	*/
	protected Hashtable<Character,Boolean> hChanPrefix = new Hashtable<Character,Boolean>();
	/** Hashtable storing all known clients based on nickname (in lowercase). */	
	protected Hashtable<String,ClientInfo> hClientList = new Hashtable<String,ClientInfo>();	
	/** Hashtable storing all known channels based on chanel name (inc prefix - in lowercase). */
	protected Hashtable<String,ChannelInfo> hChannelList = new Hashtable<String,ChannelInfo>();
	/** Reference to the ClientInfo object that references ourself. */	
	protected ClientInfo cMyself = null;
	/** Hashtable storing all information gathered from 005. */
	protected Hashtable<String,String> h005Info = new Hashtable<String,String>();
	
	/** This is the default TrustManager for SSL Sockets, it trusts all ssl certs. */
	private TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return null;
					}
					public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}
					public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}
			}
	};
	
	/** This is the TrustManager used for SSL Sockets. */
	private TrustManager[] myTrustManager = trustAllCerts;
	
	/**
	 * Get a reference to the default TrustManager for SSL Sockets.
	 *
	 * @return a reference to trustAllCerts
	 */
	public TrustManager[] getDefaultTrustManager() { return trustAllCerts; }
	
	/**
	 * Get a reference to the current TrustManager for SSL Sockets.
	 *
	 * @return a reference to myTrustManager;
	 */
	public TrustManager[] getTrustManager() { return myTrustManager; }
	
	/**
	 * Replace the current TrustManager for SSL Sockets with a new one.
	 *
	 * @param newTrustManager Replacement TrustManager for SSL Sockets.
	 */
	public void setTrustManager(TrustManager[] newTrustManager) { myTrustManager = newTrustManager; }


	/**
	 * Used for generalDebug stuff, when bDebug is false, this is never used.
	 *
	 * @param data Data string to log (in printf style)
	 * @param args Args to go with printf
	 */
	public boolean doDebug(String data, Object... args) {
		try {
			return callDebugInfo(ndGeneral, String.format(data, args));
		} catch (Exception e) { return false; }
	}
	
	/** Ignore List */
	protected RegexStringList myIgnoreList = new RegexStringList();
	
	/**
	 * Get a reference to the ignorelist
	 */
	public RegexStringList getIgnoreList() { return myIgnoreList; }
	
	/**
	 * Callback to all objects implementing the ChannelAction Callback.
	 *
	 * @see IChannelAction
	 * @param cChannel Channel where the action was sent to
	 * @param cChannelClient ChannelClient who sent the action (may be null if server)
	 * @param sMessage action contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callChannelAction(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost) {
		CallbackOnChannelAction cb = (CallbackOnChannelAction)myCallbackManager.getCallbackType("OnChannelAction");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelCTCP Callback.
	 *
	 * @see IChannelCTCP
	 * @param cChannel Channel where CTCP was sent
	 * @param cChannelClient ChannelClient who sent the message (may be null if server)
	 * @param sType Type of CTCP (VERSION, TIME etc)
	 * @param sMessage Additional contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callChannelCTCP(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sType, String sMessage, String sHost) {
		CallbackOnChannelCTCP cb = (CallbackOnChannelCTCP)myCallbackManager.getCallbackType("OnChannelCTCP");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sType, sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelCTCPReply Callback.
	 *
	 * @see IChannelCTCPReply
	 * @param cChannel Channel where CTCPReply was sent
	 * @param cChannelClient ChannelClient who sent the message (may be null if server)
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callChannelCTCPReply(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sType, String sMessage, String sHost) {
		CallbackOnChannelCTCPReply cb = (CallbackOnChannelCTCPReply)myCallbackManager.getCallbackType("OnChannelCTCPReply");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sType, sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelGotNames Callback.
	 *
	 * @see IChannelGotNames
	 * @param cChannel Channel which the names reply is for
	 */
	protected boolean callChannelGotNames(ChannelInfo cChannel) {
		CallbackOnChannelGotNames cb = (CallbackOnChannelGotNames)myCallbackManager.getCallbackType("OnChannelGotNames");
		if (cb != null) { return cb.call(cChannel); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelJoin Callback.
	 *
	 * @see IChannelJoin
	 * @param cChannel Channel Object
	 * @param cChannelClient ChannelClient object for new person
	 */
	protected boolean callChannelJoin(ChannelInfo cChannel, ChannelClientInfo cChannelClient) {
		CallbackOnChannelJoin cb = (CallbackOnChannelJoin)myCallbackManager.getCallbackType("OnChannelJoin");
		if (cb != null) { return cb.call(cChannel, cChannelClient); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelKick Callback.
	 *
	 * @see IChannelKick
	 * @param cChannel Channel where the kick took place
	 * @param cKickedClient ChannelClient that got kicked
	 * @param cKickedByClient ChannelClient that did the kicking (may be null if server)
	 * @param sReason Reason for kick (may be "")
	 * @param sKickedByHost Hostname of Kicker (or servername)
	 */
	protected boolean callChannelKick(ChannelInfo cChannel, ChannelClientInfo cKickedClient, ChannelClientInfo cKickedByClient, String sReason, String sKickedByHost) {
		CallbackOnChannelKick cb = (CallbackOnChannelKick)myCallbackManager.getCallbackType("OnChannelKick");
		if (cb != null) { return cb.call(cChannel, cKickedClient, cKickedByClient, sReason, sKickedByHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelMessage Callback.
	 *
	 * @see IChannelMessage
	 * @param cChannel Channel where the message was sent to
	 * @param cChannelClient ChannelClient who sent the message (may be null if server)
	 * @param sMessage Message contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callChannelMessage(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost) {
		CallbackOnChannelMessage cb = (CallbackOnChannelMessage)myCallbackManager.getCallbackType("OnChannelMessage");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sMessage, sHost); }
		return false;
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
		CallbackOnChannelModeChanged cb = (CallbackOnChannelModeChanged)myCallbackManager.getCallbackType("OnChannelModeChanged");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sHost, sModes); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelNickChanged Callback.
	 *
	 * @see IChannelNickChanged
	 * @param cChannel One of the channels that the user is on
	 * @param cChannelClient Client changing nickname
	 * @param sOldNick Nickname before change
	 */
	protected boolean callChannelNickChanged(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sOldNick) {
		CallbackOnChannelNickChanged cb = (CallbackOnChannelNickChanged)myCallbackManager.getCallbackType("OnChannelNickChanged");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sOldNick); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelNotice Callback.
	 *
	 * @see IChannelNotice
	 * @param cChannel Channel where the notice was sent to
	 * @param cChannelClient ChannelClient who sent the notice (may be null if server)
	 * @param sMessage notice contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callChannelNotice(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost) {
		CallbackOnChannelNotice cb = (CallbackOnChannelNotice)myCallbackManager.getCallbackType("OnChannelNotice");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelPart Callback.
	 *
	 * @see IChannelPart
	 * @param cChannel Channel that the user parted
	 * @param cChannelClient Client that parted
	 * @param sReason Reason given for parting (May be "")
	 */
	protected boolean callChannelPart(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason) {
		CallbackOnChannelPart cb = (CallbackOnChannelPart)myCallbackManager.getCallbackType("OnChannelPart");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sReason); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelQuit Callback.
	 *
	 * @see IChannelQuit
	 * @param cChannel Channel that user was on
	 * @param cChannelClient User thats quitting
	 * @param sReason Quit reason
	 */
	protected boolean callChannelQuit(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason) {
		CallbackOnChannelQuit cb = (CallbackOnChannelQuit)myCallbackManager.getCallbackType("OnChannelQuit");
		if (cb != null) { return cb.call(cChannel, cChannelClient, sReason); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelSelfJoin Callback.
	 *
	 * @see IChannelSelfJoin
	 * @param cChannel Channel Object
	 */
	protected boolean callChannelSelfJoin(ChannelInfo cChannel) {
		CallbackOnChannelSelfJoin cb = (CallbackOnChannelSelfJoin)myCallbackManager.getCallbackType("OnChannelSelfJoin");
		if (cb != null) { return cb.call(cChannel); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ChannelTopic Callback.
	 *
	 * @see IChannelTopic
	 * @param cChannel Channel that topic was set on
	 * @param bIsJoinTopic True when getting topic on join, false if set by user/server
	 */
	protected boolean callChannelTopic(ChannelInfo cChannel, boolean bIsJoinTopic) {
		CallbackOnChannelTopic cb = (CallbackOnChannelTopic)myCallbackManager.getCallbackType("OnChannelTopic");
		if (cb != null) { return cb.call(cChannel, bIsJoinTopic); }
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
		CallbackOnChannelUserModeChanged cb = (CallbackOnChannelUserModeChanged)myCallbackManager.getCallbackType("OnChannelUserModeChanged");
		if (cb != null) { return cb.call(cChannel, cChangedClient, cSetByClient, sHost, sMode); }
		return false;
	}

	/**
	 * Callback to all objects implementing the DataIn Callback.
	 *
	 * @see IDataIn
	 * @param data Incomming Line.
	 */
	protected boolean callDataIn(String data) {
		CallbackOnDataIn cb = (CallbackOnDataIn)myCallbackManager.getCallbackType("OnDataIn");
		if (cb != null) { return cb.call(data); }
		return false;
	}

	/**
	 * Callback to all objects implementing the DataOut Callback.
	 *
	 * @see IDataOut
	 * @param data Outgoing Data
	 * @param FromParser True if parser sent the data, false if sent using .sendLine	 
	 */
	protected boolean callDataOut(String data, boolean FromParser) {
		CallbackOnDataOut cb = (CallbackOnDataOut)myCallbackManager.getCallbackType("OnDataOut");
		if (cb != null) { return cb.call(data, FromParser); }
		return false;
	}

	/**
	 * Callback to all objects implementing the DebugInfo Callback.
	 *
	 * @see IDebugInfo
	 * @param level Debugging Level (ndInfo, ndSocket etc)
	 * @param data Debugging Information
	 */
	protected boolean callDebugInfo(int level, String data) {
//		CallbackObject cb = myCallbackManager.getCallbackType("OnDebugInfo");
		CallbackOnDebugInfo cb = (CallbackOnDebugInfo)myCallbackManager.getCallbackType("OnDebugInfo");
		if (cb != null) { return cb.call(level, data); }
//		if (cb != null) { return ((CallbackOnDebugInfo)cb).call(level, data); }
		return false;
	}

	/**
	 * Callback to all objects implementing the IErrorInfo Interface.
	 *
	 * @see IErrorInfo
	 * @param errorInfo ParserError object representing the error.
	 */
	protected boolean callErrorInfo(ParserError errorInfo) {
		CallbackOnErrorInfo cb = (CallbackOnErrorInfo)myCallbackManager.getCallbackType("OnErrorInfo");
		if (cb != null) { return cb.call(errorInfo); }
		return false;
	}

	/**
	 * Callback to all objects implementing the MOTDEnd Callback.
	 *
	 * @see IMOTDEnd
	 */
	protected boolean callMOTDEnd() {
		CallbackOnMOTDEnd cb = (CallbackOnMOTDEnd)myCallbackManager.getCallbackType("OnMOTDEnd");
		if (cb != null) { return cb.call(); }
		return false;
	}

	/**
	 * Callback to all objects implementing the NickChanged Callback.
	 *
	 * @see INickChanged
	 * @param cClient Client changing nickname
	 * @param sOldNick Nickname before change
	 */
	protected boolean callNickChanged(ClientInfo cClient, String sOldNick) {
		CallbackOnNickChanged cb = (CallbackOnNickChanged)myCallbackManager.getCallbackType("OnNickChanged");
		if (cb != null) { return cb.call(cClient, sOldNick); }
		return false;
	}

	/**
	 * Callback to all objects implementing the NickInUse Callback.
	 *
	 * @see INickInUse
	 */
	protected boolean callNickInUse() {
		CallbackOnNickInUse cb = (CallbackOnNickInUse)myCallbackManager.getCallbackType("OnNickInUse");
		if (cb != null) { return cb.call(); }
		return false;
	}

	/**
	 * Callback to all objects implementing the PrivateAction Callback.
	 *
	 * @see IPrivateAction
	 * @param sMessage action contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callPrivateAction(String sMessage, String sHost) {
		CallbackOnPrivateAction cb = (CallbackOnPrivateAction)myCallbackManager.getCallbackType("OnPrivateAction");
		if (cb != null) { return cb.call(sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the PrivateCTCP Callback.
	 *
	 * @see IPrivateCTCP
	 * @param sType Type of CTCP (VERSION, TIME etc)
	 * @param sMessage Additional contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callPrivateCTCP(String sType, String sMessage, String sHost) {
		CallbackOnPrivateCTCP cb = (CallbackOnPrivateCTCP)myCallbackManager.getCallbackType("OnPrivateCTCP");
		if (cb != null) { return cb.call(sType, sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the PrivateCTCPReply Callback.
	 *
	 * @see IPrivateCTCPReply
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callPrivateCTCPReply(String sType, String sMessage, String sHost) {
		CallbackOnPrivateCTCPReply cb = (CallbackOnPrivateCTCPReply)myCallbackManager.getCallbackType("OnPrivateCTCPReply");
		if (cb != null) { return cb.call(sType, sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the PrivateMessage Callback.
	 *
	 * @see IPrivateMessage
	 * @param sMessage Message contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callPrivateMessage(String sMessage, String sHost) {
		CallbackOnPrivateMessage cb = (CallbackOnPrivateMessage)myCallbackManager.getCallbackType("OnPrivateMessage");
		if (cb != null) { return cb.call(sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the PrivateNotice Callback.
	 *
	 * @see IPrivateNotice
	 * @param sMessage Notice contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callPrivateNotice(String sMessage, String sHost) {
		CallbackOnPrivateNotice cb = (CallbackOnPrivateNotice)myCallbackManager.getCallbackType("OnPrivateNotice");
		if (cb != null) { return cb.call(sMessage, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the Quit Callback.
	 *
	 * @see IQuit
	 * @param cClient Client Quitting
	 * @param sReason Reason for quitting (may be "")
	 */
	protected boolean callQuit(ClientInfo cClient, String sReason) {
		CallbackOnQuit cb = (CallbackOnQuit)myCallbackManager.getCallbackType("OnQuit");
		if (cb != null) { return cb.call(cClient, sReason); }
		return false;
	}

	/**
	 * Callback to all objects implementing the ServerReady Callback.
	 *
	 * @see IServerReady
	 */	
	protected boolean callServerReady() {
		CallbackOnServerReady cb = (CallbackOnServerReady)myCallbackManager.getCallbackType("OnServerReady");
		if (cb != null) { return cb.call(); }
		return false;
	}

	/**
	 * Callback to all objects implementing the UnknownAction Callback.
	 *
	 * @see IUnknownAction
	 * @param sMessage Action contents
	 * @param sTarget Actual target of action
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callUnknownAction(String sMessage, String sTarget, String sHost) {
		CallbackOnUnknownAction cb = (CallbackOnUnknownAction)myCallbackManager.getCallbackType("OnUnknownAction");
		if (cb != null) { return cb.call(sMessage, sTarget, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the UnknownCTCP Callback.
	 *
	 * @see IUnknownCTCP
	 * @param sType Type of CTCP (VERSION, TIME etc)
	 * @param sMessage Additional contents
	 * @param sTarget Actual Target of CTCP
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callUnknownCTCP(String sType, String sMessage, String sTarget, String sHost) {
		CallbackOnUnknownCTCP cb = (CallbackOnUnknownCTCP)myCallbackManager.getCallbackType("OnUnknownCTCP");
		if (cb != null) { return cb.call(sType, sMessage, sTarget, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the UnknownCTCPReply Callback.
	 *
	 * @see IUnknownCTCPReply
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sTarget Actual Target of CTCPReply
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callUnknownCTCPReply(String sType, String sMessage, String sTarget, String sHost) {
		CallbackOnUnknownCTCPReply cb = (CallbackOnUnknownCTCPReply)myCallbackManager.getCallbackType("OnUnknownCTCPReply");
		if (cb != null) { return cb.call(sType, sMessage, sTarget, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the UnknownMessage Callback.
	 *
	 * @see IUnknownMessage
	 * @param sMessage Message contents
	 * @param sTarget Actual target of message
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callUnknownMessage(String sMessage, String sTarget, String sHost) {
		CallbackOnUnknownMessage cb = (CallbackOnUnknownMessage)myCallbackManager.getCallbackType("OnUnknownMessage");
		if (cb != null) { return cb.call(sMessage, sTarget, sHost); }
		return false;
	}

	/**
	 * Callback to all objects implementing the UnknownNotice Callback.
	 *
	 * @see IUnknownNotice
	 * @param sMessage Notice contents
	 * @param sTarget Actual target of notice
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean callUnknownNotice(String sMessage, String sTarget, String sHost) {
		CallbackOnUnknownNotice cb = (CallbackOnUnknownNotice)myCallbackManager.getCallbackType("OnUnknownNotice");
		if (cb != null) { return cb.call(sMessage, sTarget, sHost); }
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
		CallbackOnUserModeChanged cb = (CallbackOnUserModeChanged)myCallbackManager.getCallbackType("OnUserModeChanged");
		if (cb != null) { return cb.call(cClient, sSetby); }
		return false;
	}	
	
	/**
	 * Perform a silent test on certain functions.
	 *
	 * @return Boolean result of test. (True only if ALL tests pass)
	 */
	public boolean doSelfTest() {
		return doSelfTest(true);
	} 
	
	/**
	 * Perform a test on certain functions.
	 *
	 * @param bSilent Should output be given? (Sent to Console)
	 * @return Boolean result of test. (True only if ALL tests pass)
	 */	
	public boolean doSelfTest(boolean bSilent) {
		if (bDebug) {
			boolean bResult = false;
			ParserTestClass ptc = new ParserTestClass();
			if (bSilent) { bResult = ptc.SelfTest(); }
			else {
				System.out.printf(" --------------------\n");
				System.out.printf("  Beginning Tests\n");
				System.out.printf(" --------------------\n");
				ptc.RunTests();
				System.out.printf(" --------------------\n");
				System.out.printf("  End\n");
				System.out.printf(" --------------------\n");		
				System.out.printf("   Total Tests: %d\n",ptc.GetTotalTests());
				System.out.printf("  Passed Tests: %d\n",ptc.GetPassedTests());
				System.out.printf("  Failed Tests: %d\n",ptc.GetFailedTests());
				System.out.printf(" --------------------\n");			
				bResult = (ptc.GetTotalTests() == ptc.GetPassedTests());
			}
			ptc = null;
			return bResult;
		} else { 
			return true;
		}
	}
	
	/**
	 * Default constructor, ServerInfo and MyInfo need to be added separately (using IRC.me and IRC.server)
	 */
	public IRCParser() { }
	/**
	 * Constructor with ServerInfo, MyInfo needs to be added separately (using IRC.me)
	 *
	 * @param serverDetails Server information.
	 */
	public IRCParser(ServerInfo serverDetails) { this(null,serverDetails); }
	/**
	 * Constructor with MyInfo, ServerInfo needs to be added separately (using IRC.server)
	 *
	 * @param myDetails Client information.
	 */
	public IRCParser(MyInfo myDetails) { this(myDetails,null); }
	/**
	 * Constructor with ServerInfo and MyInfo.
	 *
	 * @param serverDetails Server information.
	 * @param myDetails Client information.
	 */
	public IRCParser(MyInfo myDetails, ServerInfo serverDetails) {
		if (myDetails != null) { this.me = myDetails; }
		if (serverDetails != null) { this.server = serverDetails; }
	}
	
	/** Reset internal state (use before connect). */
	private void resetState() {
		// Reset General State info
		TriedAlt = false;
		Got001 = false;
		// Clear the hash tables
		hChannelList.clear();
		hClientList.clear();
		h005Info.clear();
		hPrefixModes.clear();
		hPrefixMap.clear();
		hChanModesOther.clear();
		hChanModesBool.clear();
		hUserModes.clear();
		hChanPrefix.clear();
		// Reset the mode indexes
		nNextKeyPrefix = 1;
		nNextKeyCMBool = 1;
		nNextKeyUser = 1;
	}

	
	/** Connect to IRC. */
	private void connect() throws Exception {
		try {
			resetState();
			callDebugInfo(ndSocket,"Connecting to "+server.sHost+":"+server.nPort);
			
			if (server.bSSL) {
				callDebugInfo(ndSocket,"Server is SSL.");
				
				if (myTrustManager == null) { myTrustManager = trustAllCerts; }
				
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, myTrustManager, new java.security.SecureRandom());
				
				SocketFactory socketFactory = sc.getSocketFactory();
				sslSocket = (SSLSocket)socketFactory.createSocket(server.sHost,server.nPort);
				//sslSocket.startHandshake();
				
				socket = sslSocket;
			} else {
				socket = new Socket(server.sHost,server.nPort);
			}
			
			if (bDebug) { doDebug("\t\t-> 1\n"); }
			out = new PrintWriter(socket.getOutputStream(), true);
			nSocketState = stateOpen;
			if (bDebug) { doDebug("\t\t-> 2\n"); }
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			if (bDebug) { doDebug("\t\t-> 3\n"); }
			
		} catch (Exception e) { throw e; }
	}
	
	/**
	 * Send server connection strings (NICK/USER/PASS)
	 */
	private void sendConnectionStrings() {
		if (!server.sPassword.equals("")) {
			sendString("PASS "+server.sPassword);
		}
		setNickname(me.sNickname);
		sendString("USER "+me.sUsername.toLowerCase()+" * * :"+me.sRealname);
		IsFirst = false;	
	}
	
	/**
	 * Begin execution.
	 * Connect to server, and start parsing incomming lines
	 */
	public void run() {
		callDebugInfo(ndInfo,"Begin Thread Execution");
		if (HasBegan) { return; } else { HasBegan = true; }
		try { connect(); } catch (Exception e) { callDebugInfo(ndSocket,"Error Connecting, Aborted"); return; }
		
		callDebugInfo(ndSocket,"Socket Connected");
		
		if (!server.waitForFirst) { sendConnectionStrings(); }
		
		String line = "";
		while(true) {
			try {
				line = in.readLine(); // Blocking :/
				if (line == null) {
					callDebugInfo(ndSocket,"Socket Closed");
					nSocketState = stateClosed;
					break;
				} else {
					if (IsFirst) { sendConnectionStrings(); }
					processLine(line);
				}
			} catch (IOException e) {
				callDebugInfo(ndSocket,"Socket Closed");
				nSocketState = stateClosed;
				break;
			}
		}
		callDebugInfo(ndInfo,"End Thread Execution");
	}
	
	/** Close socket on destroy. */
	protected void finalize(){
		try {
			socket.close();
		} catch (IOException e) {
			callDebugInfo(ndInfo,"Could not close socket");
		}
	}

	/**
	 * Get the trailing parameter for a line.
	 * The parameter is everything after the first occurance of " :" ot the last token in the line after a space.
	 *
	 * @param line Line to get parameter for
	 * @return Parameter of the line
	 */
	protected String getParam(String line) {
		String[] params = null;
		params = line.split(" :",2);
		return params[params.length-1];
	}
	
	/**
	 * Tokenise a line.
	 * splits by " " up to the first " :" everything after this is a single token
	 *
	 * @param line Line to tokenise
	 * @return Array of tokens
	 */
	protected String[] tokeniseLine(String line) {
		if (line == null) {
			String[] tokens = new String[1];
			tokens[0] = "";
			return tokens; // Return empty string[]
		};
		String[] params = null;
		String[] tokens = null;
		params = line.split(" :",2);
		tokens = params[0].split(" ");
		if (params.length == 2) { 
			String[] temp = new String[tokens.length+1];
			System.arraycopy(tokens, 0, temp, 0, tokens.length);
			tokens = temp;
			tokens[tokens.length-1] = params[1];
		}
		return tokens;
	}
	
	/**
	 * Get the ClientInfo object for a person.
	 *
	 * @param sWho Who can be any valid identifier for a client as long as it contains a nickname (?:)nick(?!ident)(?@host)
	 * @return ClientInfo Object for the client, or null
	 */
	public ClientInfo getClientInfo(String sWho) {
		if (bDebug) { doDebug("\t\tInput: %s | ",sWho); }
		sWho = ClientInfo.parseHost(sWho);
		if (bDebug) { doDebug("Client Name: %s\n",sWho); }
		sWho = sWho.toLowerCase();
		if (hClientList.containsKey(sWho)) { return hClientList.get(sWho); } else { return null; }
	}
	
	/**
	 * Get the ChannelInfo object for a channel.
	 *
	 * @param sWhat This is the name of the channel.
	 * @return ChannelInfo Object for the channel, or null
	 */
	public ChannelInfo getChannelInfo(String sWhat) {
		sWhat = sWhat.toLowerCase();
		if (hChannelList.containsKey(sWhat)) { return hChannelList.get(sWhat); } else { return null; }
	}	
	
	// TODO: This should do some checks on stuff?
	/**
	 * Send a line to the server.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 */
	public void sendLine(String line) {
		if (out == null) { return; }
		callDataOut(line,false);
		out.printf("%s\r\n",line);
	}
	
	/** Send a line to the server and add proper line ending. */
	protected void sendString(String line) {
		if (out == null) { return; }
		callDataOut(line,true);
		out.printf("%s\r\n",line);
	}
	
	/**
	 * Process a line and call relevent methods for handling.
	 *
	 * @param line IRC Line to process
	 */
	private void processLine(String line) {
		String[] token = tokeniseLine(line);
//		String mainParam = token[token.length-1];
		
		int nParam;
		callDataIn(line);
		String sParam = token[1];
		try {nParam = Integer.parseInt(token[1]);} catch (Exception e) { nParam = -1;}
		
		try {
			if (token[0].equals("PING") || token[1].equals("PING")) { sendString("PONG :"+sParam); }
			else {
				if (token[0].substring(0,1).equals(":")) {
					if (!Got001) {
						// Before 001 we don't care about anything. (Apart from 001, PING and NickInUse)
						switch (nParam) {
							case 1: // 001 - Welcome to IRC
								Got001 = true;
								process001(nParam,token);
								break;
							case 464: // Password Required
								ParserError ei = new ParserError(errError,"Password Required");
								callErrorInfo(ei);
								break;
							case 433: // Nick In Use
								processNickInUse(nParam,token);
								break;
							default: // Unknown
								break;
						}
					} else {
						// Post Connect
						switch (nParam) {
							case -1:
								processStringParam(sParam,token);
								break;
							case 4: // 004 - ISUPPORT
							case 5: // 005 - ISUPPORT
								process004_005(nParam,token);
								break;
							case 305: // No longer away
							case 306: // Away
								processAway(nParam,token);
								break;
							case 332: // Topic on Join
							case 333: // Topic Setter On Join
								processTopic(sParam,token);
								break;
							case 375: // MOTD Start
								break;
							case 353: // Names
							case 366: // End of Names
								processNames(nParam,token);
								break;
							case 324: // Modes
								processMode(sParam,token);
								break;
							case 329: // Channel Time
							case 368: // End of ban list
								break;
							case 376: // End of MOTD
							case 422: // No MOTD
								processEndOfMOTD(nParam,token);
								break;
							case 433: // Nick In Use
								processNickInUse(nParam,token);
								break;
							default: // Unknown
								break;
						}
					}
				} else {
					// Pre Connect
				}
			}
		} catch (Exception e) {
			ParserError ei = new ParserError(errFatal,"Exception in Parser. {"+line+"}");
			ei.setException(e);
			callErrorInfo(ei);
		}
	}
		
	/**
	 * Process an IRC Line with a string parameter rather than a Numeric.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */
	private void processStringParam(String sParam, String token[]) {
		// Process a line where the parameter is a string (IE PRIVMSG, NOTICE etc - Not including PING!)
		if (sParam.equalsIgnoreCase("PRIVMSG") || sParam.equalsIgnoreCase("NOTICE")) { processIRCMessage(sParam,token); }
		else if (sParam.equalsIgnoreCase("JOIN")) { processJoinChannel(sParam,token); }
		else if (sParam.equalsIgnoreCase("NICK")) { processNickChange(sParam,token); }
		else if (sParam.equalsIgnoreCase("KICK")) { processKickChannel(sParam,token); }
		else if (sParam.equalsIgnoreCase("PART")) { processPartChannel(sParam,token); }
		else if (sParam.equalsIgnoreCase("QUIT")) { processQuit(sParam,token); }
		else if (sParam.equalsIgnoreCase("TOPIC")) { processTopic(sParam,token); }
		else if (sParam.equalsIgnoreCase("MODE")) { processMode(sParam,token); }
	}
	
	/**
	 * Process a Nickname change.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void processNickChange(String sParam, String token[]) {
		ClientInfo iClient;
		ChannelClientInfo iChannelClient;
		ChannelInfo iChannel;
		
		iClient = getClientInfo(token[0]);
		if (iClient != null) {
			if (iClient.getHost().equals("")) { iClient.setUserBits(token[0],false); }
			hClientList.remove(iClient.getNickname().toLowerCase());
			iClient.setUserBits(token[2],true);
			hClientList.put(iClient.getNickname().toLowerCase(),iClient);
			
			for (Enumeration e = hChannelList.keys(); e.hasMoreElements();) {
				iChannel = hChannelList.get(e.nextElement());
				iChannelClient = iChannel.getUser(iClient);
				if (iChannelClient != null) {
					callChannelNickChanged(iChannel,iChannelClient,ClientInfo.parseHost(token[0]));
				}
			}
			
			callNickChanged(iClient, ClientInfo.parseHost(token[0]));
		}
		
	}
	
	/**
	 * Process a kick.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void processKickChannel(String sParam, String token[]) {
		ChannelClientInfo iChannelClient;
		ChannelClientInfo iChannelKicker;
		ChannelInfo iChannel;
		ClientInfo iClient;
		ClientInfo iKicker;
		String sReason = "";
		
		iClient = getClientInfo(token[3]);
		iKicker = getClientInfo(token[0]);
		iChannel = getChannelInfo(token[2]);
		
		if (iClient == null) { return; }
		if (iChannel == null) { 
			if (iClient != cMyself) {
				callErrorInfo(new ParserError(errWarning, "Got kick for channel ("+token[2]+") that I am not on. [User: "+token[3]+"]"));
			}
			return;
		} else {
			if (token.length > 4) { sReason = token[token.length-1]; }
			iChannelClient = iChannel.getUser(iClient);
			iChannelKicker = iChannel.getUser(token[0]);
			callChannelKick(iChannel,iChannelClient,iChannelKicker,sReason,token[0]);
			iChannel.delClient(iClient);
			if (iClient == cMyself) {
				iChannel.emptyChannel();
				hChannelList.remove(iChannel.getName().toLowerCase());
			} else { 
				if (!iClient.checkVisability()) {
					hClientList.remove(iClient.getNickname().toLowerCase());
				}
			}
		}
	}
	
	/**
	 * Process a Mode change (hands off to ProcessUserMode for usermodes).
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void processMode(String sParam, String token[]) {
		String[] sModestr;
		String sFullModeStr;
		String sChannelName;
		String sModeParam;
		String sTemp;
		int nCurrent = 0, nParam = 1, nValue = 0;
		boolean bPositive = true, bBooleanMode = true;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClientInfo;
		ClientInfo iClient;
		if (sParam.equals("324")) {
			sChannelName = token[3];
			// Java 6 Only
			// sModestr = Arrays.copyOfRange(token,4,token.length);
			sModestr = new String[token.length-4];
			System.arraycopy(token, 4, sModestr, 0, token.length-4);
		} else {
			sChannelName = token[2];
			// Java 6 Only
			// sModestr = Arrays.copyOfRange(token,3,token.length);
			sModestr = new String[token.length-3];
			System.arraycopy(token, 3, sModestr, 0, token.length-3);
		}

		if (!isValidChannelName(sChannelName)) { processUserMode(sParam, token, sModestr); return; }
		
		iChannel = getChannelInfo(sChannelName);
		if (iChannel == null) { 
			callErrorInfo(new ParserError(errWarning, "Got modes for channel ("+sChannelName+") that I am not on."));
			iChannel = new ChannelInfo(this, sChannelName);
			hChannelList.put(iChannel.getName().toLowerCase(),iChannel);
		}
		if (!sParam.equals("324")) { nCurrent = iChannel.getMode(); }
		
		for (int i = 0; i < sModestr[0].length(); ++i) {
			Character cMode = sModestr[0].charAt(i);
			if (cMode.equals("+".charAt(0))) { bPositive = true; }
			else if (cMode.equals("-".charAt(0))) { bPositive = false; }
			else if (cMode.equals(":".charAt(0))) { continue; }
			else {
				if (hChanModesBool.containsKey(cMode)) { nValue = hChanModesBool.get(cMode); bBooleanMode = true; }
				else if (hChanModesOther.containsKey(cMode)) { nValue = hChanModesOther.get(cMode); bBooleanMode = false; }
				else if (hPrefixModes.containsKey(cMode)) { 
					// (de) OP/Voice someone
					sModeParam = sModestr[nParam++];
					nValue = hPrefixModes.get(cMode);
					if (bDebug) { doDebug("User Mode: %c [%s] {Positive: %b}\n",cMode, sModeParam, bPositive); }
					iChannelClientInfo = iChannel.getUser(sModeParam);
					if (iChannelClientInfo == null) {
						// Client not known?
						callErrorInfo(new ParserError(errWarning, "Got mode for client not known on channel - Added"));
						iClient = getClientInfo(sModeParam);
						if (iClient == null) { 
							callErrorInfo(new ParserError(errWarning, "Got mode for client not known at all - Added"));
							iClient = new ClientInfo(this, sModeParam);
							hClientList.put(iClient.getNickname().toLowerCase(),iClient);
						}
						iChannelClientInfo = iChannel.addClient(iClient);
					}
					if (bPositive) { iChannelClientInfo.setChanMode(iChannelClientInfo.getChanMode() + nValue); sTemp = "+"; }
					else { iChannelClientInfo.setChanMode(iChannelClientInfo.getChanMode() - nValue); sTemp = "-"; }
					sTemp = sTemp+cMode+" "+iChannelClientInfo.getNickname();
					
					callChannelUserModeChanged(iChannel, iChannelClientInfo, iChannel.getUser(token[0]), token[0], sTemp);
					continue;
				} else {
					callErrorInfo(new ParserError(errWarning, "Got unknown mode "+cMode+" - Added as boolean mode"));
					hChanModesBool.put(cMode,nNextKeyCMBool);
					nValue = nNextKeyCMBool;
					bBooleanMode = true;
					nNextKeyCMBool = nNextKeyCMBool*2;
				}
				
				if (bBooleanMode) {
					if (bDebug) { doDebug("Boolean Mode: %c [%d] {Positive: %b}\n",cMode, nValue, bPositive); }
					if (bPositive) { nCurrent = nCurrent + nValue; }
					else { nCurrent = nCurrent - nValue; }
				} else {
					if (nValue == cmList) {
						sModeParam = sModestr[nParam++];
						iChannel.setListModeParam(cMode, sModeParam, bPositive);
						if (bDebug) { doDebug("List Mode: %c [%s] {Positive: %b}\n",cMode, sModeParam, bPositive); }
					} else {
						if (bPositive) { 
							sModeParam = sModestr[nParam++];
							if (bDebug) { doDebug("Set Mode: %c [%s] {Positive: %b}\n",cMode, sModeParam, bPositive); }
							iChannel.setModeParam(cMode,sModeParam);
						} else {
							if ((nValue & cmUnset) == cmUnset) { sModeParam = sModestr[nParam++]; } else { sModeParam = ""; }
							if (bDebug) { doDebug("Unset Mode: %c [%s] {Positive: %b}\n",cMode, sModeParam, bPositive); }
							iChannel.setModeParam(cMode,"");
						}
					}
				}
			}
		}
		
		sFullModeStr = "";
		for (int i = 0; i < sModestr.length; ++i) { sFullModeStr = sFullModeStr+sModestr[i]+" "; }
		
		iChannel.setMode(nCurrent);
		if (sParam.equals("324")) { callChannelModeChanged(iChannel, null, "", sFullModeStr); }
		else { callChannelModeChanged(iChannel, iChannel.getUser(token[0]), token[0], sFullModeStr); }
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
				if (hUserModes.containsKey(cMode)) { nValue = hUserModes.get(cMode); }
				else {
					callErrorInfo(new ParserError(errWarning, "Got unknown user mode "+cMode+" - Added"));
					hUserModes.put(cMode,nNextKeyUser);
					nValue = nNextKeyUser;
					nNextKeyUser = nNextKeyUser*2;
				}
				
				if (bDebug) { doDebug("User Mode: %c [%d] {Positive: %b}\n",cMode, nValue, bPositive); }
				if (bPositive) { nCurrent = nCurrent + nValue; }
				else { nCurrent = nCurrent - nValue; }
			}
		}
		
		iClient.setUserMode(nCurrent);
		callUserModeChanged(iClient, token[0]);
	}	
	
	/**
	 * Process an Away/Back message.
	 *
	 * @param nParam Integer representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */
	private void processAway(int nParam, String token[]) {
		cMyself.setAwayState(nParam == 306);
	}
	
	/**
	 * Process a Names reply.
	 *
	 * @param nParam Integer representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void processNames(int nParam, String token[]) {
		ChannelInfo iChannel;
		if (nParam == 366) {
			// End of names
			iChannel = getChannelInfo(token[3]);
			if (iChannel != null) {
				iChannel.bAddingNames = false;
				callChannelGotNames(iChannel);
			}
		} else {
			// Names
			
			ClientInfo iClient;
			ChannelClientInfo iChannelClient;
			
			iChannel = getChannelInfo(token[4]);
		
			if (iChannel == null) { 
				callErrorInfo(new ParserError(errWarning, "Got names for channel ("+token[4]+") that I am not on."));
				iChannel = new ChannelInfo(this, token[4]);
				hChannelList.put(iChannel.getName().toLowerCase(),iChannel);
			}
			
			// If we are not expecting names, clear the current known names - this is fresh stuff!
			if (!iChannel.bAddingNames) { iChannel.emptyChannel(); }
			iChannel.bAddingNames = true;
			
			String[] sNames = token[token.length-1].split(" ");
			String sNameBit = "", sModes = "", sName = "";
			int nPrefix = 0;
			for (int j = 0; j < sNames.length; ++j) {
				sNameBit = sNames[j];
				for (int i = 0; i < sNameBit.length(); ++i) {
					Character cMode = sNameBit.charAt(i);
					if (hPrefixMap.containsKey(cMode)) {
						sModes = sModes+cMode;
						nPrefix = nPrefix + hPrefixModes.get(hPrefixMap.get(cMode));
					} else {
						sName = sNameBit.substring(i);
						break;
					}
				}
				if (bDebug) { doDebug("Name: %s Modes: \"%s\" [%d]\n",sName,sModes,nPrefix); }
				
				iClient = getClientInfo(sName);
				if (iClient == null) { iClient = new ClientInfo(this, sName); hClientList.put(iClient.getNickname().toLowerCase(),iClient); }
				iChannelClient = iChannel.addClient(iClient);
				iChannelClient.setChanMode(nPrefix);

				sName = "";
				sModes = "";
				nPrefix = 0;
			}
		}
	}
	
	/**
	 * Process PRIVMSGs and NOTICEs.
	 * This horrible thing handles PRIVMSGs and NOTICES<br>
	 * This inclues CTCPs and CTCPReplies<br>
	 * It handles all 3 targets (Channel, Private, Unknown)<br>
	 * Actions are handled here aswell separately from CTCPs.<br>
	 * Each type has 5 Calls, making 15 callbacks handled here.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void processIRCMessage(String sParam, String token[]) {
		// Ignore people!
		String bits[] = token[0].split(":",2);
		String sMessage = "";
		if (bits.length > 1) { sMessage = bits[1]; } else { sMessage = bits[0]; }
		
		if (myIgnoreList.matches(sMessage) > -1) { return; }
		
		ChannelClientInfo iChannelClient = null;
		ChannelInfo iChannel = null;
		ClientInfo iClient = null;
		sMessage = token[token.length-1];
		bits = sMessage.split(" ", 2);
		Character Char1 = Character.valueOf((char)1);
		String sCTCP = "";
		boolean isAction = false;
		boolean isCTCP = false;
		
		if (sParam.equalsIgnoreCase("PRIVMSG")) {
			if (bits[0].equalsIgnoreCase(Char1+"ACTION") && Character.valueOf(sMessage.charAt(sMessage.length()-1)).equals(Char1)) {
				isAction = true;
				if (bits.length > 1) {
					sMessage = bits[1];
					sMessage = sMessage.substring(0, sMessage.length()-1);
				} else { sMessage = ""; }
			}
		}
		if (!isAction) {
			if (Character.valueOf(sMessage.charAt(0)).equals(Char1) && Character.valueOf(sMessage.charAt(sMessage.length()-1)).equals(Char1)) {
				isCTCP = true;
				if (bits.length > 1) { sMessage = bits[1]; } else { sMessage = ""; }
				bits = bits[0].split(Char1.toString());
				sCTCP = bits[1];
				if (bDebug) { doDebug("CTCP: \"%s\" \"%s\"\n",sCTCP,sMessage); }
			}
		}

		if (isValidChannelName(token[2])) {
			iClient = getClientInfo(token[0]);
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
		} else if (token[2].equalsIgnoreCase(cMyself.getNickname())) {
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
			if (bDebug) { doDebug("Message for Other ("+token[2]+")\n"); }
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
	 * Process a topic change.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void processTopic(String sParam, String token[]) {
		ChannelInfo iChannel;
		if (sParam.equals("332")) {
			iChannel = getChannelInfo(token[3]);
			if (iChannel == null) { return; };
			iChannel.setTopic(token[token.length-1]);
		} else if (sParam.equals("333")) {
			iChannel = getChannelInfo(token[3]);
			if (iChannel == null) { return; };
			iChannel.setTopicTime(Long.parseLong(token[5]));
			iChannel.setTopicUser(token[4]);
			callChannelTopic(iChannel,true);
		} else {
			iChannel = getChannelInfo(token[2]);
			if (iChannel == null) { return; };
			iChannel.setTopicTime(java.util.Calendar.getInstance().getTimeInMillis() / 1000);
			String sTemp[] = token[0].split(":",2);
			if (sTemp.length > 1) { token[0] = sTemp[1]; }
			iChannel.setTopicUser(token[0]);
			iChannel.setTopic(token[token.length-1]);
			callChannelTopic(iChannel,false);
		}
	}
	
	/**
	 * Process a channel join.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void processJoinChannel(String sParam, String token[]) {
		// :nick!ident@host JOIN (:)#Channel
		Character cTemp;
		Byte nTemp;
		if (token.length < 3) { return; }
		ClientInfo iClient;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		iClient = getClientInfo(token[0]);
		iChannel = getChannelInfo(token[token.length-1]);
		
		if (iClient == null) { iClient = new ClientInfo(this, token[0]); hClientList.put(iClient.getNickname().toLowerCase(),iClient); }
		if (iChannel == null) { 
			if (iClient != cMyself) {
				callErrorInfo(new ParserError(errWarning, "Got join for channel ("+token[token.length-1]+") that I am not on. [User: "+token[0]+"]"));
			}
			iChannel = new ChannelInfo(this, token[token.length-1]);
			hChannelList.put(iChannel.getName().toLowerCase(),iChannel);
			sendString("MODE "+iChannel.getName());
			
			for (Enumeration e = hChanModesOther.keys(); e.hasMoreElements();) {
				cTemp = (Character)e.nextElement();
				nTemp = hChanModesOther.get(cTemp);
				if (nTemp == cmList) { sendString("MODE "+iChannel.getName()+" "+cTemp); }
			}
			callChannelSelfJoin(iChannel);
		} else {
			// This is only done if we are on the channel. Else we wait for names.
			iChannelClient = iChannel.addClient(iClient);
			callChannelJoin(iChannel, iChannelClient);
		}
	}	
	
	/**
	 * Process a channel part.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void processPartChannel(String sParam, String token[]) {
		// :nick!ident@host PART #Channel
		// :nick!ident@host PART #Channel :reason
		if (token.length < 3) { return; }
		ClientInfo iClient;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		iClient = getClientInfo(token[0]);
		iChannel = getChannelInfo(token[2]);
		
		if (iClient == null) { return; }
		if (iChannel == null) { 
			if (iClient != cMyself) {
				callErrorInfo(new ParserError(errWarning, "Got part for channel ("+token[2]+") that I am not on. [User: "+token[0]+"]"));
			}
			return;
		} else {
			String sReason = "";
			if (token.length > 3) { sReason = token[token.length-1]; }
			iChannelClient = iChannel.getUser(iClient);
			if (iChannelClient == null) {
				callErrorInfo(new ParserError(errWarning, "Got part for channel ("+token[2]+") for a non-existant user. [User: "+token[0]+"]"));
				return;
			}
			callChannelPart(iChannel,iChannelClient,sReason);
			iChannel.delClient(iClient);
			if (iClient == cMyself) {
				iChannel.emptyChannel();
				hChannelList.remove(iChannel.getName().toLowerCase());
			} else { iClient.checkVisability(); }
		}
	}
	
	/**
	 * Process a Quit message.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void processQuit(String sParam, String token[]) {
		// :nick!ident@host QUIT
		// :nick!ident@host QUIT :reason
		if (token.length < 2) { return; }
		ClientInfo iClient;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		iClient = getClientInfo(token[0]);
		
		if (iClient == null) { return; }

		String sReason = "";
		if (token.length > 2) { sReason = token[token.length-1]; }
		
		for (Enumeration e = hChannelList.keys(); e.hasMoreElements();) {
			iChannel = hChannelList.get(e.nextElement());
			iChannelClient = iChannel.getUser(iClient);
			if (iChannelClient != null) {
				callChannelQuit(iChannel,iChannelClient,sReason);
				if (iClient == cMyself) {
					iChannel.emptyChannel();
					hChannelList.remove(iChannel.getName().toLowerCase());
				} else {
					iChannel.delClient(iClient);
				}
			}
		}

		callQuit(iClient,sReason);
		if (iClient == cMyself) {
			hClientList.clear();
		} else {
			hClientList.remove(iClient.getNickname().toLowerCase());
		}
	}	

	/**
	 * Process a 004 or 005 message.
	 *
	 * @param nParam Integer representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void process004_005(int nParam, String token[]) {
		if (nParam == 4) {
			// 004
			h005Info.put("USERMODES",token[5]);
		} else {
			// 005
			String[] Bits = null;
			String sKey = null, sValue = null;
			for (int i = 3; i < token.length ; i++) {
				Bits = token[i].split("=",2);
				sKey = Bits[0].toUpperCase();
				if (Bits.length == 2) { sValue = Bits[1]; } else { sValue = ""; }
				if (bDebug) { doDebug("%s => %s \r\n",sKey,sValue); }
				h005Info.put(sKey,sValue);
			}
		}
	}
	
	/**
	 * Process CHANMODES from 005.
	 */	
	protected void parseChanModes() {
		final String sDefaultModes = "b,k,l,imnpstrc";
		String[] Bits = null;
		String ModeStr;
		if (h005Info.containsKey("CHANMODES")) { ModeStr = h005Info.get("CHANMODES");	}
		else { ModeStr = sDefaultModes; h005Info.put("CHANMODES",ModeStr); }
		Bits = ModeStr.split(",",4);
		if (Bits.length != 4) {
			ModeStr = sDefaultModes;
			callErrorInfo(new ParserError(errError, "CHANMODES String not valid. Using default string of \""+ModeStr+"\""));
			h005Info.put("CHANMODES",ModeStr);
			Bits = ModeStr.split(",",4);
		}
		
		// resetState
		hChanModesOther.clear();
		hChanModesBool.clear();
		nNextKeyCMBool = 1;
		
		// List modes.
		for (int i = 0; i < Bits[0].length(); ++i) {
			Character cMode = Bits[0].charAt(i);
			if (bDebug) { doDebug("List Mode: %c\n",cMode); }
			if (!hChanModesOther.containsKey(cMode)) { hChanModesOther.put(cMode,cmList); }
		}
		
		// Param for Set and Unset.
		Byte nBoth = (cmSet+cmUnset);
		for (int i = 0; i < Bits[1].length(); ++i) {
			Character cMode = Bits[1].charAt(i);
			if (bDebug) { doDebug("Set/Unset Mode: %c\n",cMode); }
			if (!hChanModesOther.containsKey(cMode)) { hChanModesOther.put(cMode,nBoth); }
		}
		
		// Param just for Set
		for (int i = 0; i < Bits[2].length(); ++i) {
			Character cMode = Bits[2].charAt(i);
			if (bDebug) { doDebug("Set Only Mode: %c\n",cMode); }
			if (!hChanModesOther.containsKey(cMode)) { hChanModesOther.put(cMode,cmSet); }
		}
		
		// Boolean Mode
		for (int i = 0; i < Bits[3].length(); ++i) {
			Character cMode = Bits[3].charAt(i);
			if (bDebug) { doDebug("Boolean Mode: %c [%d]\n",cMode,nNextKeyCMBool); }
			if (!hChanModesBool.containsKey(cMode)) {
				hChanModesBool.put(cMode,nNextKeyCMBool);
				nNextKeyCMBool = nNextKeyCMBool*2;
			}
		}
	}
	
	/**
	 * Process USERMODES from 004.
	 */	
	protected void parseUserModes() {
		final String sDefaultModes = "nwdoi";
		String[] Bits = null;
		String ModeStr;
		if (h005Info.containsKey("USERMODES")) { ModeStr = h005Info.get("USERMODES");	}
		else { ModeStr = sDefaultModes; h005Info.put("USERMODES", sDefaultModes); }
		
		// resetState
		hUserModes.clear();
		nNextKeyUser = 1;
		
		// Boolean Mode
		for (int i = 0; i < ModeStr.length(); ++i) {
			Character cMode = ModeStr.charAt(i);
			if (bDebug) { doDebug("User Mode: %c [%d]\n",cMode,nNextKeyUser); }
			if (!hUserModes.containsKey(cMode)) {
				hUserModes.put(cMode,nNextKeyUser);
				nNextKeyUser = nNextKeyUser*2;
			}
		}
	}	
	
	/**
	 * Process CHANTYPES from 005.
	 */	
	protected void parseChanPrefix() {
		final String sDefaultModes = "#&";
		String[] Bits = null;
		String ModeStr;
		if (h005Info.containsKey("CHANTYPES")) { ModeStr = h005Info.get("CHANTYPES");	}
		else { ModeStr = sDefaultModes; h005Info.put("CHANTYPES", sDefaultModes); }
		
		// resetState
		hChanPrefix.clear();
		
		// Boolean Mode
		for (int i = 0; i < ModeStr.length(); ++i) {
			Character cMode = ModeStr.charAt(i);
			if (bDebug) { doDebug("Chan Prefix: %c\n",cMode); }
			if (!hChanPrefix.containsKey(cMode)) { hChanPrefix.put(cMode,true); }
		}
	}		
	
	/**
	 * Process PREFIX from 005.
	 */	
	public void parsePrefixModes() {
		final String sDefaultModes = "(ohv)@%+";
		String[] Bits = null;
		String ModeStr;
		if (h005Info.containsKey("PREFIX")) { ModeStr = h005Info.get("PREFIX");	}
		else { ModeStr = sDefaultModes; }
		if (ModeStr.substring(0,1).equals("(")) { ModeStr = ModeStr.substring(1); }
		else { ModeStr = sDefaultModes.substring(1); h005Info.put("PREFIX", sDefaultModes); }
		
		Bits = ModeStr.split("\\)",2);
		if (Bits.length != 2 || Bits[0].length() != Bits[1].length()) {
			ModeStr = sDefaultModes;
			callErrorInfo(new ParserError(errError, "PREFIX String not valid. Using default string of \""+ModeStr+"\""));
			h005Info.put("PREFIX",ModeStr);
			ModeStr = ModeStr.substring(1);
			Bits = ModeStr.split("\\)",2);
		}

		// resetState
		hPrefixModes.clear();
		hPrefixMap.clear();
		nNextKeyPrefix = 1;

//		for (int i = 0; i < Bits[0].length(); ++i) {
		for (int i = Bits[0].length()-1; i > -1; --i) {
			Character cMode = Bits[0].charAt(i);
			Character cPrefix = Bits[1].charAt(i);
			if (bDebug) { doDebug("Prefix Mode: %c => %c [%d]\n",cMode,cPrefix,nNextKeyPrefix); }
			if (!hPrefixModes.containsKey(cMode)) {
				hPrefixModes.put(cMode,nNextKeyPrefix);
				hPrefixMap.put(cMode,cPrefix);
				hPrefixMap.put(cPrefix,cMode);
				nNextKeyPrefix = nNextKeyPrefix*2;
			}
		}	
		
	}	

	/**
	 * Process an EndOfMOTD or No MOTD Found.
	 *
	 * @param nParam Integer representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void processEndOfMOTD(int nParam, String token[]) {
		parseChanModes();
		parseChanPrefix();
		parsePrefixModes();
		parseUserModes();
		callMOTDEnd();
	}

	/**
	 * Process a 001 message.
	 *
	 * @param nParam Integer representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void process001(int nParam, String token[]) {
		// << :demon1.uk.quakenet.org 001 Java-Test :Welcome to the QuakeNet IRC Network, Java-Test
		String sNick;
		sServerName = token[0].substring(1,token[0].length());
		sNick = token[2];
		/* Code below is here incase relying on token[2] breaks somewhere
		String[] temp = token[token.length-1].split(" ");
		sConfirmedNickname = temp[temp.length-1];
		// Some servers give a full host in 001
		temp = sNick.split("!",2);
		sNick = temp[0];  /* */
		
		cMyself = getClientInfo(sNick);
		if (cMyself == null) {
			cMyself = new ClientInfo(this, sNick);
			hClientList.put(cMyself.getNickname().toLowerCase(),cMyself);
		}
		
		callServerReady();
	}

	/**
	 * Process a NickInUse message.
	 * Parser implements handling of this if Pre-001 and no other handler found,
	 * adding the NickInUse handler (addNickInUse) after 001 is prefered over before.<br><br>
	 * <br>
	 * If the first nickname is in use, and a NickInUse message is recieved before 001, we
	 * will attempt to use the altnickname instead.<br>
	 * If this also fails, we will start prepending _ (or the value of me.cPrepend) to the main nickname.
	 *
	 * @param nParam Integer representation of parameter to parse
	 * @param token IRCTokenised Array of the incomming line
	 */	
	private void processNickInUse(int nParam, String token[]) {
		if (!callNickInUse()) {
			// Manually handle nick in use.
			callDebugInfo(ndInfo,"No Nick in use Handler.");
			if (!Got001) {
				callDebugInfo(ndInfo,"Using inbuilt handler");
				// If this is before 001 we will try and get a nickname, else we will leave the nick as-is
				if (!TriedAlt) { setNickname(me.sAltNickname); TriedAlt = true; }
				else {
					if (sThinkNickname.equalsIgnoreCase(me.sAltNickname)) { sThinkNickname = me.sNickname; }
					setNickname(me.cPrepend+sThinkNickname);
				}
			}
		}
	}
	
	/**
	 * Check if server is ready.
	 *
	 * @return true if 001 has been recieved, false otherwise.
	 */
	public boolean isReady() { return Got001; }	
	
	/**
	 * Join a Channel.
	 *
	 * @param sChannelName Name of channel to join
	 */
	public void joinChannel(String sChannelName) {
		if (!isValidChannelName(sChannelName)) { return; }
		sendString("JOIN "+sChannelName);
	}
	
	
	/**
	 * Leave a Channel.
	 *
	 * @param sChannelName Name of channel to part
	 * @param sReason Reason for leaving (Nothing sent if sReason is "")
	 */
	public void partChannel(String sChannelName, String sReason) {
		if (!isValidChannelName(sChannelName)) { return; }
		if (sReason.equals("")) { sendString("PART "+sChannelName); }
		else { sendString("PART "+sChannelName+" :"+sReason); }
	}	
	
	/**
	 * Set Nickname.
	 *
	 * @param sNewNickName New nickname wanted.
	 */
	public void setNickname(String sNewNickName) {
		if (this.getSocketState() != stateOpen) {
			me.sNickname = sNewNickName;
		} else {
			if (cMyself != null) {
				if (cMyself.getNickname().equals(sNewNickName)) { return; }
			}
			sendString("NICK "+sNewNickName);
		}
		sThinkNickname = sNewNickName;
	}
	
	/**
	 * Send a private message to a target.
	 *
	 * @param sTarget Target
	 * @param sMessage Message to send
	 */
	public void sendMessage(String sTarget, String sMessage) { 
		if (sTarget.equals("") || sMessage.equals("")) { return; }
		
		sendString("PRIVMSG "+sTarget+" :"+sMessage);	
	}
	
	/**
	 * Send a notice message to a target.
	 *
	 * @param sTarget Target
	 * @param sMessage Message to send
	 */
	public void sendNotice(String sTarget, String sMessage) { 
		if (sTarget.equals("") || sMessage.equals("")) { return; }
		
		sendString("NOTICE "+sTarget+" :"+sMessage);	
	}

	/**
	 * Send a private message to a target.
	 *
	 * @param sTarget Target
	 * @param sMessage Message to send
	 */
	public void sendAction(String sTarget, String sMessage) { 
		if (sTarget.equals("") || sMessage.equals("")) { return; }
		sendCTCP(sTarget, "ACTION", sMessage);
	}
	
	/**
	 * Send a CTCP to a target.
	 *
	 * @param sTarget Target
	 * @param sType Type of CTCP
	 * @param sMessage Optional Additional Parameters
	 */
	public void sendCTCP(String sTarget, String sType, String sMessage) { 
		if (sTarget.equals("") || sType.equals("")) { return; }
		Character Char1 = Character.valueOf((char)1);
		if (!sMessage.equals("")) { sMessage = " "+sMessage; }
		sendString("PRIVMSG "+sTarget+" :"+Char1+sType.toUpperCase()+sMessage+Char1);	
	}
	
	/**
	 * Send a CTCPReply to a target.
	 *
	 * @param sTarget Target
	 * @param sType Type of CTCP
	 * @param sMessage Optional Additional Parameters
	 */
	public void sendCTCPReply(String sTarget, String sType, String sMessage) { 
		if (sTarget.equals("") || sType.equals("")) { return; }
		Character Char1 = Character.valueOf((char)1);
		if (!sMessage.equals("")) { sMessage = " "+sMessage; }
		sendString("NOTICE "+sTarget+" :"+Char1+sType.toUpperCase()+sMessage+Char1);	
	}
	
	/**
	 * Quit server. This method will wait for the server to close the socket.
	 *
	 * @param sReason Reason for quitting.
	 */
	public void quit(String sReason) { 
		if (sReason.equals("")) { sendString("QUIT"); }
		else { sendString("QUIT :"+sReason); }	
	}
	/**
	 * Disconnect from server. This method will quit and automatically close the
	 * socket without waiting for the server
	 *
	 * @param sReason Reason for quitting.
	 */
	public void disconnect(String sReason) {
		quit(sReason);
		try { socket.close(); } catch (Exception e) { /* Meh */ };
	}
	
	/**
	 * Check if a channel name is valid in a certain parser object.
	 *
	 * @param sChannelName Channel name to test
	 */
	public boolean isValidChannelName(String sChannelName) {
		return hChanPrefix.containsKey(sChannelName.charAt(0));
	}	
	
	/**
	 * Get a reference to the cMyself object.
	 *
	 * @return cMyself reference
	 */
	public ClientInfo getMyself() { return cMyself; }
	
	/**
	 * Get SVN Version information
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}

// eof
