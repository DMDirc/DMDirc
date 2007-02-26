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

package org.ownage.dmdirc.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.SocketFactory;
import java.util.ArrayList;
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

	/** Used in Error Reporting, Error is potentially Fatal, Desync 99% Guarenteed! */
	public static final int errFatal = 1;
	/** Used in Error Reporting, Error is not fatal, but is more severe than a warning. */
	public static final int errError = 2;
	/** Used in Error Reporting, Error was an unexpected occurance, but shouldn't be anything to worry about. */
	public static final int errWarning = 4;
	/**
	 * Used in Error Reporting.
	 * If an Error has this flag, it means the parser is able to continue running<br>
	 * Most errWarnings should have this flag. if Fatal or Error are not accomanied<br>
	 * with this flag, you should disconnect or risk problems further on.	 
	 */
	public static final int errCanContinue = 8;
	
	/**
	 * Enable Development Debugging info - Outputs directly to console.
	 *
	 * This is used for debugging info that is generally of no use to most people<br>
	 * If this is set to false, self-test and any the "useless" debugging that relies on <br>
	 * this being true are not compiled.
	 */
	protected static final boolean bDebug = true;
	
	private Socket socket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	
	/**
	 * This is what the user wants settings to be. 
	 * Nickname here is *not* always accurate.<br>
	 * ClientInfo variable cMyself should be used for accurate info.<br>
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
	
	// Better alternative to hashtable?
	
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
	 * Valid Boolean Modes are stored as Hashtable.pub('m',1); where 'm' is the mode and 1 is a numeric value<br>
	 * Numeric values are powers of 2. This allows up to 32 modes at present (expandable to 64)<br>
	 * ChannelInfo/ChannelClientInfo etc provide methods to view the modes in a human way.<br>
	 *<br>
	 * Channel modes discovered but not listed in 005 are stored as boolean modes automatically (and a errWarning Error is called)
	 */
	protected Hashtable<Character,Integer> hChanModesBool = new Hashtable<Character,Integer>();
	/** Integer representing the next avaliable integer value of a Boolean mode. */	
	protected int nNextKeyCMBool = 1;
	
	/**
	 * Hashtable storing known non-boolean chan modes (klbeI etc).
	 * Non Boolean Modes (for Channels) are stored together in this arraylist, the value param<br>
	 * is used to show the type of variable. (List (1), Param just for set (2), Param for Set and Unset (2+4=6))<br>
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
	
	// Callbacks
	// TODO: This would probably be more efficient (for deleting anyway) as hashtables
	// with the key being the callback-to object?
	/**
	 * Used to give Debug Information.
	 */
	public interface IDebugInfo { 
		/**
		 * This callback is used to provide occasional debug information from the parser.
		 *
		 * @param tParser Reference to the parser object that made the callback.
		 * @param nLevel Debugging Level (ndInfo, ndSocket etc)
		 * @param sData Debugging Information
		 * @see IRCParser#AddDebugInfo
		 * @see IRCParser#DelDebugInfo
		 * @see IRCParser#CallDebugInfo
		 */
		public void onDebug(IRCParser tParser, int nLevel, String sData);
	}
	private ArrayList<IDebugInfo> cbDebugInfo = new ArrayList<IDebugInfo>();
	
	/**
	 * Called when "End of MOTD" or "No MOTD".
	 */
	public interface IMOTDEnd {
		/**
		 * Called when "End of MOTD" or "No MOTD".
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @see IRCParser#AddMOTDEnd
		 * @see IRCParser#DelMOTDEnd
		 * @see IRCParser#CallMOTDEnd
		 */
		public void onMOTDEnd(IRCParser tParser);
	}
	private ArrayList<IMOTDEnd> cbEndOfMOTD = new ArrayList<IMOTDEnd>();
	
	/**
	 * Called after 001.
	 */
	public interface IServerReady {
		/**
		 * Called after 001.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @see IRCParser#AddServerReady
		 * @see IRCParser#DelServerReady
		 * @see IRCParser#CallServerReady
		 */
		public void onServerReady(IRCParser tParser);
	}
	private ArrayList<IServerReady> cbServerReady = new ArrayList<IServerReady>();	
	
	/** 
	 * Called on every incomming line BEFORE parsing.
	 */
	public interface IDataIn { 
		/**
		 * Called on every incomming line BEFORE parsing.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param sData Incomming Line.
		 * @see IRCParser#AddDataIn
		 * @see IRCParser#DelDataIn
		 * @see IRCParser#CallDataIn
		 */
		public void onDataIn(IRCParser tParser, String sData);
	}
	private ArrayList<IDataIn> cbDataIn = new ArrayList<IDataIn>();
	
	/**
	 * Called on every incomming line BEFORE being sent.
	 */
	public interface IDataOut {
		/**
		 * Called on every incomming line BEFORE being sent.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param sData Outgoing Data
		 * @param FromParser True if parser sent the data, false if sent using .SendLine
		 * @see IRCParser#AddDataOut
		 * @see IRCParser#DelDataOut
		 * @see IRCParser#CallDataOut
		 */
		public void onDataOut(IRCParser tParser, String sData, boolean FromParser);
	}
	private ArrayList<IDataOut> cbDataOut = new ArrayList<IDataOut>();
	
	/**
	 * Called when requested nickname is in use.
	 */
	public interface INickInUse {
		/**
		 * Called when requested nickname is in use.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @see IRCParser#AddNickInUse
		 * @see IRCParser#DelNickInUse
		 * @see IRCParser#CallNickInUse
		 */
		public void onNickInUse(IRCParser tParser);
	}
	private ArrayList<INickInUse> cbNickInUse = new ArrayList<INickInUse>();
	
	/**
	 * Called to give Error Information.
	 */
	public interface IErrorInfo {
		/**
		 * Called to give Error Information.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param nLevel Debugging Level (errFatal, errWarning etc)
		 * @param sData Error Information
		 * @see IRCParser#AddErrorInfo
		 * @see IRCParser#DelErrorInfo
		 * @see IRCParser#CallErrorInfo
		 */
		public void onError(IRCParser tParser, int nLevel, String sData);
	}
	private ArrayList<IErrorInfo> cbErrorInfo = new ArrayList<IErrorInfo>();
	
	/** 
	 * Called When we, or another client joins a channel.
	 * This is called AFTER client has been added to channel as a ChannelClientInfo
	 */
	public interface IChannelJoin {
		/**
		 * Called When we, or another client joins a channel.
		 * This is called AFTER client has been added to channel as a ChannelClientInfo
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cChannel Channel Object
		 * @param cChannelClient ChannelClient object for new person
		 * @see IRCParser#AddChannelJoin
		 * @see IRCParser#DelChannelJoin
		 * @see IRCParser#CallChannelJoin
		 */
		public void onJoinChannel(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient );
	}
	private ArrayList<IChannelJoin> cbChannelJoin = new ArrayList<IChannelJoin>();
	
	/** 
	 * Called When we, or another client parts a channel.
	 * This is called BEFORE client has been removed from the channel.
	 */
	public interface IChannelPart {
		/**
		 * Called When we, or another client parts a channel.
		 * This is called BEFORE client has been removed from the channel.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cChannel Channel that the user parted
		 * @param cChannelClient Client that parted
		 * @param sReason Reason given for parting (May be "")
		 * @see IRCParser#AddChannelPart
		 * @see IRCParser#DelChannelPart
		 * @see IRCParser#CallChannelPart
		 */
		public void onPartChannel(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason );
	}
	private ArrayList<IChannelPart> cbChannelPart = new ArrayList<IChannelPart>();
	
	/** 
	 * Called When we, or another client quits IRC (Called once per channel the user was on).
	 * This is called BEFORE client has been removed from the channel.
	 */
	public interface IChannelQuit {
		/**
		 * Called When we, or another client quits IRC (Called once per channel the user was on).
		 * This is called BEFORE client has been removed from the channel.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cChannel Channel that user was on
		 * @param cChannelClient User thats quitting
		 * @param sReason Quit reason
		 * @see IRCParser#AddChannelQuit
		 * @see IRCParser#DelChannelQuit
		 * @see IRCParser#CallChannelQuit
		 */
		public void onQuitChannel(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason );
	}
	private ArrayList<IChannelQuit> cbChannelQuit = new ArrayList<IChannelQuit>();
	
	/** 
	 * Called when the topic is changed or discovered for the first time.
	 * bIsNewTopic is true if someone sets the topic, false if the topic is discovered on join
	 */
	public interface IChannelTopic {
		/**
		 * Called when the topic is changed or discovered for the first time.
		 * bIsNewTopic is true if someone sets the topic, false if the topic is discovered on join
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cChannel Channel that topic was set on
		 * @param bIsJoinTopic True when getting topic on join, false if set by user/server
		 * @see IRCParser#AddTopic
		 * @see IRCParser#DelTopic
		 * @see IRCParser#CallTopic
		 */
		public void onTopic(IRCParser tParser, ChannelInfo cChannel, boolean bIsJoinTopic);
	}
	private ArrayList<IChannelTopic> cbChannelTopic = new ArrayList<IChannelTopic>();
	
	/** 
	 * Called when the channel modes are changed or discovered.
	 * cChannelClient is null if the modes were found from raw 324 (/MODE #Chan reply) or if a server set the mode<br>
	 * If a Server set the mode, sHost is the servers name, else it is the full host of the user who set it
	 */
	public interface IChannelModesChanged {
		/**
		 * Called when the channel modes are changed or discovered.
		 * cChannelClient is null if the modes were found from raw 324 (/MODE #Chan reply) or if a server set the mode<br>
		 * If a Server set the mode, sHost is the servers name, else it is the full host of the user who set it
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cChannel Channel where modes were changed
		 * @param cChannelClient Client chaning the modes (null if server)
		 * @param sHost Host doing the mode changing (User host or server name)
		 * @see IRCParser#AddModesChanged
		 * @see IRCParser#DelModesChanged
		 * @see IRCParser#CallModesChanged
		 */
		public void onModeChange(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sHost);
	}
	private ArrayList<IChannelModesChanged> cbChannelModesChanged = new ArrayList<IChannelModesChanged>();
	
	/** 
	 * Called when user modes are changed.
	 * cClient represents the user who's modes were changed (should ALWAYS be us)<br>
	 * sSetby is the host of the person who set the mode (usually us, may be an oper or server in some cases)
	 */
	public interface IUserModesChanged {
		/**
		 * Called when user modes are changed.
		 * cClient represents the user who's modes were changed (should ALWAYS be us)<br>
		 * sSetby is the host of the person who set the mode (usually us, may be an oper or server in some cases)
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cClient Client that had the mode changed (almost always us)
		 * @param sSetBy Host that set the mode (us or servername)
		 * @see IRCParser#AddUserModesChanged
		 * @see IRCParser#DelUserModesChanged
		 * @see IRCParser#CallUserModesChanged
		 */
		public void onUserModeChange(IRCParser tParser, ClientInfo cClient, String sSetBy);
	}
	private ArrayList<IUserModesChanged> cbUserModesChanged = new ArrayList<IUserModesChanged>();
	
	/**
	 * Called when we or another user change nickname.
	 * This is called after the nickname change has been done internally
	 */
	public interface INickChanged {
		/**
		 * Called when we or another user change nickname.
		 * This is called after the nickname change has been done internally
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cClient Client changing nickname
		 * @param sOldNick Nickname before change
		 * @see IRCParser#AddNickChanged
		 * @see IRCParser#DelNickChanged
		 * @see IRCParser#CallNickChanged
		 */
		public void onNickChanged(IRCParser tParser, ClientInfo cClient, String sOldNick);
	}
	private ArrayList<INickChanged> cbNickChanged = new ArrayList<INickChanged>();
	
	/**
	 * Called when a person is kicked.
	 * cKickedByClient can be null if kicked by a server. sKickedByHost is the hostname of the person/server doing the kicking.
	 */
	public interface IChannelKick {
		/**
		 * Called when a person is kicked.
		 * cKickedByClient can be null if kicked by a server. sKickedByHost is the hostname of the person/server doing the kicking.
		 *
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cChannel Channel where the kick took place
		 * @param cKickedClient ChannelClient that got kicked
		 * @param cKickedByClient ChannelClient that did the kicking (may be null if server)
		 * @param sReason Reason for kick (may be "")
		 * @param sKickedByHost Hostname of Kicker (or servername)
		 * @see IRCParser#AddChannelKick
		 * @see IRCParser#DelChannelKick
		 * @see IRCParser#CallChannelKick
		 */
		public void onChannelKick(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cKickedClient, ChannelClientInfo cKickedByClient, String sReason, String sKickedByHost);
	}
	private ArrayList<IChannelKick> cbChannelKick = new ArrayList<IChannelKick>();
	
	/**
	 * Called when a person sends a message to a channel.
	 * sHost is the hostname of the person sending the message. (Can be a server or a person)<br>
	 * cChannelClient is null if user is a server, or not on the channel.
	 */
	public interface IChannelMessage {
		/**
		 * Called when a person sends a message to a channel.
		 * sHost is the hostname of the person sending the message. (Can be a server or a person)<br>
		 * cChannelClient is null if user is a server, or not on the channel.
		 *
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cChannel Channel where the message was sent to
		 * @param cChannelClient ChannelClient who sent the message (may be null if server)
		 * @param sMessage Message contents
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddChannelMessage
		 * @see IRCParser#DelChannelMessage
		 * @see IRCParser#CallChannelMessage
		 */
		public void onChannelMessage(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost );
	}
	private ArrayList<IChannelMessage> cbChannelMessage = new ArrayList<IChannelMessage>();
	
	/**
	 * Called when a person does an action in a channel.
	 * sHost is the hostname of the person sending the action. (Can be a server or a person)<br>
	 * cChannelClient is null if user is a server, or not on the channel.
	 */
	public interface IChannelAction {
		/**
		 * Called when a person does an action in a channel.
		 * sHost is the hostname of the person sending the action. (Can be a server or a person)<br>
		 * cChannelClient is null if user is a server, or not on the channel.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cChannel Channel where the action was sent to
		 * @param cChannelClient ChannelClient who sent the action (may be null if server)
		 * @param sMessage action contents
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddChannelAction
		 * @see IRCParser#DelChannelAction
		 * @see IRCParser#CallChannelAction
		 */
		public void onChannelAction(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost );
	}
	private ArrayList<IChannelAction> cbChannelAction = new ArrayList<IChannelAction>();
	
	/**
	 * Called when a person sends a notice to a channel.
	 * sHost is the hostname of the person sending the notice. (Can be a server or a person)<br>
	 * cChannelClient is null if user is a server, or not on the channel.
	 */
	public interface IChannelNotice {
		/**
		 * Called when a person sends a notice to a channel.
		 * sHost is the hostname of the person sending the notice. (Can be a server or a person)<br>
		 * cChannelClient is null if user is a server, or not on the channel.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cChannel Channel where the notice was sent to
		 * @param cChannelClient ChannelClient who sent the notice (may be null if server)
		 * @param sMessage notice contents
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddChannelNotice
		 * @see IRCParser#DelChannelNotice
		 * @see IRCParser#CallChannelNotice
		 */
		public void onChannelNotice(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost );
	}
	private ArrayList<IChannelNotice> cbChannelNotice = new ArrayList<IChannelNotice>();
	
	/**
	 * Called when a person sends a message to you directly (PM). 
	 * sHost is the hostname of the person sending the message. (Can be a server or a person)<br>
	 * cClient is null if user is a server, or not on any common channel.
	 */
	public interface IPrivateMessage {
		/**
		 * Called when a person sends a message to you directly (PM). 
		 * sHost is the hostname of the person sending the message. (Can be a server or a person)<br>
		 * cClient is null if user is a server, or not on any common channel.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cClient Client who sent the message (may be null if no common channels or server)
		 * @param sMessage Message contents
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddPrivateMessage
		 * @see IRCParser#DelPrivateMessage
		 * @see IRCParser#CallPrivateMessage
		 */
		public void onPrivateMessage(IRCParser tParser, ClientInfo cClient, String sMessage, String sHost );
	}
	private ArrayList<IPrivateMessage> cbPrivateMessage = new ArrayList<IPrivateMessage>();
	
	/**
	 * Called when a person does an action to you (PM).
	 * sHost is the hostname of the person sending the action. (Can be a server or a person)<br>
	 * cClient is null if user is a server, or not on any common channel.
	 */
	public interface IPrivateAction {
		/**
		 * Called when a person does an action to you (PM).
		 * sHost is the hostname of the person sending the action. (Can be a server or a person)<br>
		 * cClient is null if user is a server, or not on any common channel.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
	 	 * @param cClient Client who sent the action (may be null if no common channels or server)
		 * @param sMessage action contents
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddPrivateAction
		 * @see IRCParser#DelPrivateAction
		 * @see IRCParser#CallPrivateAction
		 */
		public void onPrivateAction(IRCParser tParser, ClientInfo cClient, String sMessage, String sHost );
	}
	private ArrayList<IPrivateAction> cbPrivateAction = new ArrayList<IPrivateAction>();
	
	/**
	 * Called when a person sends a notice to you.
	 * sHost is the hostname of the person sending the notice. (Can be a server or a person)<br>
	 * cClient is null if user is a server, or not on any common channel.
	 */
	public interface IPrivateNotice {
		/**
		 * Called when a person sends a notice to you.
		 * sHost is the hostname of the person sending the notice. (Can be a server or a person)<br>
		 * cClient is null if user is a server, or not on any common channel.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cClient Client who sent the notice (may be null if no common channels or server)
		 * @param sMessage Notice contents
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddPrivateNotice
		 * @see IRCParser#DelPrivateNotice
		 * @see IRCParser#CallPrivateNotice
		 */
		public void onPrivateNotice(IRCParser tParser, ClientInfo cClient, String sMessage, String sHost );
	}
	private ArrayList<IPrivateNotice> cbPrivateNotice = new ArrayList<IPrivateNotice>();
	
	/**
	 * Called when a person sends a message not aimed specifically at you or a channel (ie $*).
	 * sHost is the hostname of the person sending the message. (Can be a server or a person)<br>
	 * cClient is null if user is a server, or not on any common channel.
	 */
	public interface IUnknownMessage {
		/**
		 * Called when a person sends a message not aimed specifically at you or a channel (ie $*).
		 * sHost is the hostname of the person sending the message. (Can be a server or a person)<br>
		 * cClient is null if user is a server, or not on any common channel.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cClient Client who sent the message (may be null if no common channels or server)
		 * @param sMessage Message contents
		 * @param sTarget Actual target of message
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddUnknownMessage
		 * @see IRCParser#DelUnknownMessage
		 * @see IRCParser#CallUnknownMessage
		 */
		public void onUnknownMessage(IRCParser tParser, ClientInfo cClient, String sMessage, String sTarget, String sHost );
	}
	private ArrayList<IUnknownMessage> cbUnknownMessage = new ArrayList<IUnknownMessage>();
	
	/**
	 * Called when a person sends an action not aimed specifically at you or a channel (ie $*).
	 * sHost is the hostname of the person sending the message. (Can be a server or a person)<br>
	 * cClient is null if user is a server, or not on any common channel.
	 */
	public interface IUnknownAction {
		/**
		 * Called when a person sends an action not aimed specifically at you or a channel (ie $*).
		 * sHost is the hostname of the person sending the message. (Can be a server or a person)<br>
		 * cClient is null if user is a server, or not on any common channel.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cClient Client who sent the action (may be null if no common channels or server)
		 * @param sMessage Action contents
		 * @param sTarget Actual target of action
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddUnknownAction
		 * @see IRCParser#DelUnknownAction
		 * @see IRCParser#CallUnknownAction
		 */
		public void onUnknownAction(IRCParser tParser, ClientInfo cClient, String sMessage, String sTarget, String sHost );
	}
	private ArrayList<IUnknownAction> cbUnknownAction = new ArrayList<IUnknownAction>();
	
	/**
	 * Called when a person sends a notice not aimed specifically at you or a channel (ie $*).
	 * sHost is the hostname of the person sending the message. (Can be a server or a person)<br>
	 * cClient is null if user is a server, or not on any common channel.
	 */
	public interface IUnknownNotice { 
		/**
		 * Called when a person sends a notice not aimed specifically at you or a channel (ie $*).
		 * sHost is the hostname of the person sending the message. (Can be a server or a person)<br>
		 * cClient is null if user is a server, or not on any common channel.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cClient Client who sent the notice (may be null if no common channels or server)
		 * @param sMessage Notice contents
		 * @param sTarget Actual target of notice
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddUnknownNotice
		 * @see IRCParser#DelUnknownNotice
		 * @see IRCParser#CallUnknownNotice
		 */
		public void onUnknownNotice(IRCParser tParser, ClientInfo cClient, String sMessage, String sTarget, String sHost );
	}
	private ArrayList<IUnknownNotice> cbUnknownNotice = new ArrayList<IUnknownNotice>();
	
	/**
	 * Called when a person sends a CTCP to a channel.
	 * sHost is the hostname of the person sending the CTCP. (Can be a server or a person)<br>
	 * cChannelClient is null if user is a server.
	 */
	public interface IChannelCTCP {
		/**
		 * Called when a person sends a CTCP to a channel.
		 * sHost is the hostname of the person sending the CTCP. (Can be a server or a person)<br>
		 * cChannelClient is null if user is a server.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cChannel Channel where CTCP was sent
		 * @param cChannelClient ChannelClient who sent the message (may be null if server)
		 * @param sType Type of CTCP (VERSION, TIME etc)
		 * @param sMessage Additional contents
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddChannelCTCP
		 * @see IRCParser#DelChannelCTCP
		 * @see IRCParser#CallChannelCTCP
		 */
		public void onChannelCTCP(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sType, String sMessage, String sHost );
	}
	private ArrayList<IChannelCTCP> cbChannelCTCP = new ArrayList<IChannelCTCP>();
	
	/**
	 * Called when a person sends a CTCP to you directly.
	 * sHost is the hostname of the person sending the CTCP. (Can be a server or a person)<br>
	 * cClient is null if user is a server, or not on any common channels.
	 */
	public interface IPrivateCTCP {
		/**
		 * Called when a person sends a CTCP to you directly.
		 * sHost is the hostname of the person sending the CTCP. (Can be a server or a person)<br>
		 * cClient is null if user is a server, or not on any common channels.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
	 	 * @param cClient Client who sent the CTCP (may be null if no common channels or server)
		 * @param sType Type of CTCP (VERSION, TIME etc)
		 * @param sMessage Additional contents
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddPrivateCTCP
		 * @see IRCParser#DelPrivateCTCP
		 * @see IRCParser#CallPrivateCTCP
		 */
		public void onPrivateCTCP(IRCParser tParser, ClientInfo cClient, String sType, String sMessage, String sHost );
	}
	private ArrayList<IPrivateCTCP> cbPrivateCTCP = new ArrayList<IPrivateCTCP>();
	
	/**
	 * Called when a person sends a CTCP not aimed at you or a channel (ie $*).
	 * sHost is the hostname of the person sending the CTCP. (Can be a server or a person)<br>
	 * cClient is null if user is a server, or not on any common channels.
	 */
	public interface IUnknownCTCP {
		/**
		 * Called when a person sends a CTCP not aimed at you or a channel (ie $*).
		 * sHost is the hostname of the person sending the CTCP. (Can be a server or a person)<br>
		 * cClient is null if user is a server, or not on any common channels.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
	 	 * @param cClient Client who sent the CTCP (may be null if no common channels or server)
		 * @param sType Type of CTCP (VERSION, TIME etc)
		 * @param sMessage Additional contents
		 * @param sTarget Actual Target of CTCP
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddUnknownCTCP
		 * @see IRCParser#DelUnknownCTCP
		 * @see IRCParser#CallUnknownCTCP
		 */
		public void onUnknownCTCP(IRCParser tParser, ClientInfo cClient, String sType, String sMessage, String sTarget, String sHost );
	}
	private ArrayList<IUnknownCTCP> cbUnknownCTCP = new ArrayList<IUnknownCTCP>();
	
	/**
	 * Called when a person sends a CTCPRReply to a channel.
	 * sHost is the hostname of the person sending the CTCPReply. (Can be a server or a person)<br>
	 * cChannelClient is null if user is a server.
	 */
	public interface IChannelCTCPReply {
		/**
		 * Called when a person sends a CTCPRReply to a channel.
		 * sHost is the hostname of the person sending the CTCPReply. (Can be a server or a person)<br>
		 * cChannelClient is null if user is a server.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cChannel Channel where CTCPReply was sent
		 * @param cChannelClient ChannelClient who sent the message (may be null if server)
		 * @param sType Type of CTCPRReply (VERSION, TIME etc)
		 * @param sMessage Reply Contents
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddChannelCTCPReply
		 * @see IRCParser#DelChannelCTCPReply
		 * @see IRCParser#CallChannelCTCPReply
		 */
		public void onChannelCTCPReply(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sType, String sMessage, String sHost );
	}
	private ArrayList<IChannelCTCPReply> cbChannelCTCPReply = new ArrayList<IChannelCTCPReply>();
	
	/**
	 * Called when a person sends a CTCPRReply to you directly.
	 * sHost is the hostname of the person sending the CTCPRReply. (Can be a server or a person)<br>
	 * cClient is null if user is a server, or not on any common channels.
	 */
	public interface IPrivateCTCPReply {
		/**
		 * Called when a person sends a CTCPRReply to you directly.
		 * sHost is the hostname of the person sending the CTCPRReply. (Can be a server or a person)<br>
		 * cClient is null if user is a server, or not on any common channels.
		 * 
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cClient Client who sent the CTCPReply (may be null if no common channels or server)
		 * @param sType Type of CTCPRReply (VERSION, TIME etc)
		 * @param sMessage Reply Contents
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddPrivateCTCPReply
		 * @see IRCParser#DelPrivateCTCPReply
		 * @see IRCParser#CallPrivateCTCPReply
		 */
		public void onPrivateCTCPReply(IRCParser tParser, ClientInfo cClient, String sType, String sMessage, String sHost );
	}
	private ArrayList<IPrivateCTCPReply> cbPrivateCTCPReply = new ArrayList<IPrivateCTCPReply>();
	
	/**
	 * Called when a person sends a CTCP not aimed at you or a channel (ie $*).
	 * sHost is the hostname of the person sending the CTCP. (Can be a server or a person)<br>
	 * cClient is null if user is a server, or not on any common channels.
	 */
	public interface IUnknownCTCPReply {
		/**
		 * Called when a person sends a CTCP not aimed at you or a channel (ie $*).
		 * sHost is the hostname of the person sending the CTCP. (Can be a server or a person)<br>
		 * cClient is null if user is a server, or not on any common channels.
		 *
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cClient Client who sent the CTCPReply (may be null if no common channels or server)
		 * @param sType Type of CTCPRReply (VERSION, TIME etc)
		 * @param sMessage Reply Contents
		 * @param sTarget Actual Target of CTCPReply
		 * @param sHost Hostname of sender (or servername)
		 * @see IRCParser#AddUnknownCTCPReply
		 * @see IRCParser#DelUnknownCTCPReply
		 * @see IRCParser#CallUnknownCTCPReply
		 */
		public void onUnknownCTCPReply(IRCParser tParser, ClientInfo cClient, String sType, String sMessage, String sTarget, String sHost );
	}
	private ArrayList<IUnknownCTCPReply> cbUnknownCTCPReply = new ArrayList<IUnknownCTCPReply>();
	
	/** 
	 * Called When we, or another client quits IRC (Called once in total).
	 * This is called BEFORE client has been removed from the channel.
	 */
	public interface IQuit {
		/**
		 * Called When we, or another client quits IRC (Called once in total).
		 * This is called BEFORE client has been removed from the channel.
		 *
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cClient Client Quitting
		 * @param sReason Reason for quitting (may be "")
		 * @see IRCParser#AddQuit
		 * @see IRCParser#DelQuit
		 * @see IRCParser#CallQuit
		 */
		public void onQuit(IRCParser tParser, ClientInfo cClient, String sReason );
	}
	private ArrayList<IQuit> cbQuit = new ArrayList<IQuit>();
	
	/**
	 * Called when a names reply is parsed.
	 */
	public interface IGotNames {
		/**
		 * Called when a names reply is parsed.
		 *
		 * @param tParser Reference to the parser object that made the callback.
		 * @param cChannel Channel which the names reply is for
		 * @see IRCParser#AddGotNames
		 * @see IRCParser#DelGotNames
		 * @see IRCParser#CallGotNames
		 */
		public void onGotNames(IRCParser tParser, ChannelInfo cChannel);
	}
	private ArrayList<IGotNames> cbGotNames = new ArrayList<IGotNames>();
	

	/** Add a callback pointer to the appropriate ArrayList. */
	@SuppressWarnings("unchecked")
	private void AddCallback(Object eMethod, ArrayList CallbackList) {
		for (int i = 0; i < CallbackList.size(); i++) {
			if (eMethod.equals(CallbackList.get(i))) { return; }
		}
		CallbackList.add(eMethod);
	}
	/** Delete a callback pointer from the appropriate ArrayList. */
	private void DelCallback(Object eMethod, ArrayList CallbackList) {
		for (int i = 0; i < CallbackList.size(); i++) {
			if (eMethod.equals(CallbackList.get(i))) { CallbackList.remove(i); break; }
		}
	}
	
	/**
	 * Add callback for DebugInfo (onDebug).
	 *
	 * @see IRCParser.IDebugInfo
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddDebugInfo(Object eMethod) { AddCallback(eMethod, cbDebugInfo); }
	/**
	 * Delete callback for DebugInfo (onDebug).
	 *
	 * @see IRCParser.IDebugInfo
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelDebugInfo(Object eMethod) { DelCallback(eMethod, cbDebugInfo); }
	/**
	 * Callback to all objects implementing the IRCParser.IDebugInfo Interface.
	 *
	 * @see IRCParser.IDebugInfo
	 * @param level Debugging Level (ndInfo, ndSocket etc)
	 * @param data Debugging Information
	 */
	protected boolean CallDebugInfo(int level, String data) {
		if (bDebug) { System.out.printf("[DEBUG] {%d} %s\n", level, data); }
		boolean bResult = false;
		for (int i = 0; i < cbDebugInfo.size(); i++) {
			cbDebugInfo.get(i).onDebug(this, level, data);
			bResult = true;
		}
		return bResult;
	}
	
	/**
	 * Add callback for MOTDEnd (onMOTDEnd).
	 *
	 * @see IRCParser.IMOTDEnd
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddMOTDEnd(Object eMethod) { AddCallback(eMethod, cbEndOfMOTD); }
	/**
	 * Delete callback for MOTDEnd (onMOTDEnd).
	 *
	 * @see IRCParser.IMOTDEnd
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelMOTDEnd(Object eMethod) { DelCallback(eMethod, cbEndOfMOTD); }
	/**
	 * Callback to all objects implementing the IRCParser.IMotdEnd Interface.
	 *
	 * @see IRCParser.IMOTDEnd
	 */
	protected boolean CallMOTDEnd() {
		boolean bResult = false;
		for (int i = 0; i < cbEndOfMOTD.size(); i++) {
			cbEndOfMOTD.get(i).onMOTDEnd(this);
			bResult = true;
		}
		return bResult;
	}
	
	/**
	 * Add callback for ServerReady (onServerReady).
	 *
	 * @see IRCParser.IServerReady
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddServerReady(Object eMethod) { AddCallback(eMethod, cbServerReady); }
	/**
	 * Delete callback for ServerReady (onServerReady).
	 *
	 * @see IRCParser.IServerReady
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelServerReady(Object eMethod) { DelCallback(eMethod, cbServerReady); }
	/**
	 * Callback to all objects implementing the IRCParser.IServerReady Interface.
	 *
	 * @see IRCParser.IServerReady
	 */	
	protected boolean CallServerReady() {
		boolean bResult = false;
		for (int i = 0; i < cbServerReady.size(); i++) {
			cbServerReady.get(i).onServerReady(this);
			bResult = true;
		}
		return bResult;
	}
	
	/**
	 * Add callback for DataIn (onDataIn).
	 *
	 * @see IRCParser.IDataIn
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddDataIn(Object eMethod) { AddCallback((IDataIn)eMethod, cbDataIn); }
	/**
	 * Delete callback for DataIn (onDebug).
	 *
	 * @see IRCParser.IDataIn
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelDataIn(Object eMethod) { DelCallback((IDataIn)eMethod, cbDataIn); }
	/**
	 * Callback to all objects implementing the IRCParser.IDataIn Interface.
	 *
	 * @see IRCParser.IDataIn
	 * @param data Incomming Line.
	 */
	protected boolean CallDataIn(String data) {
		boolean bResult = false;
		for (int i = 0; i < cbDataIn.size(); i++) {
			cbDataIn.get(i).onDataIn(this, data);
			bResult = true;
		}
		return bResult;
	}
	
	/**
	 * Add callback for DataOut (onDataOut).
	 *
	 * @see IRCParser.IDataOut
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddDataOut(Object eMethod) { AddCallback((IDataOut)eMethod, cbDataOut); }
	/**
	 * Delete callback for DataOut (onDataOut).
	 *
	 * @see IRCParser.IDataOut
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelDataOut(Object eMethod) { DelCallback((IDataOut)eMethod, cbDataOut); }
	/**
	 * Callback to all objects implementing the IRCParser.IDataOut Interface.
	 *
	 * @see IRCParser.IDataOut
	 * @param data Outgoing Data
	 * @param FromParser True if parser sent the data, false if sent using .SendLine	 
	 */
	protected boolean CallDataOut(String data, boolean FromParser) {
		boolean bResult = false;
		for (int i = 0; i < cbDataOut.size(); i++) {
			cbDataOut.get(i).onDataOut(this, data, FromParser);
			bResult = true;
		}
		return bResult;
	}
	
	/**
	 * Add callback for NickInUse (onNickInUse).
	 *
	 * @see IRCParser.INickInUse
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddNickInUse(Object eMethod) { AddCallback(eMethod, cbNickInUse); }
	/**
	 * Delete callback for NickInUse (onNickInUse).
	 *
	 * @see IRCParser.INickInUse
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelNickInUse(Object eMethod) { DelCallback(eMethod, cbNickInUse); }
	/**
	 * Callback to all objects implementing the IRCParser.INickInUse Interface.
	 *
	 * @see IRCParser.INickInUse
	 */
	protected boolean CallNickInUse() {
		boolean bResult = false;
		for (int i = 0; i < cbNickInUse.size(); i++) {
			cbNickInUse.get(i).onNickInUse(this);
			bResult = true;
		}
		return bResult;
	}
	
	/**
	 * Add callback for ErrorInfo (onError).
	 *
	 * @see IRCParser.IErrorInfo
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddErrorInfo(Object eMethod) { AddCallback(eMethod, cbErrorInfo); }
	/**
	 * Delete callback for ErrorInfo (onError).
	 *
	 * @see IRCParser.IErrorInfo
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelErrorInfo(Object eMethod) { DelCallback(eMethod, cbErrorInfo); }
	/**
	 * Callback to all objects implementing the IRCParser.IErrorInfo Interface.
	 *
	 * @see IRCParser.IErrorInfo
	 * @param level Debugging Level (errFatal, errWarning etc)
	 * @param data Error Information
	 */
	protected boolean CallErrorInfo(int level, String data) {
		if (bDebug) { System.out.printf("[ERROR] {%d} %s\n", level, data); }
		boolean bResult = false;
		for (int i = 0; i < cbErrorInfo.size(); i++) {
			cbErrorInfo.get(i).onError(this, level, data);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for ChannelJoin (onJoinChannel).
	 *
	 * @see IRCParser.IChannelJoin
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelJoin(Object eMethod) { AddCallback(eMethod, cbChannelJoin); }
	/**
	 * Delete callback for ChannelJoin (onJoinChannel).
	 *
	 * @see IRCParser.IChannelJoin
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelJoin(Object eMethod) { DelCallback(eMethod, cbChannelJoin); }
	/**
	 * Callback to all objects implementing the IRCParser.IChannelJoin Interface.
	 *
	 * @see IRCParser.IChannelJoin
	 * @param cChannel Channel Object
	 * @param cChannelClient ChannelClient object for new person
	 */
	protected boolean CallChannelJoin(ChannelInfo cChannel, ChannelClientInfo cChannelClient) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelJoin.size(); i++) {
			cbChannelJoin.get(i).onJoinChannel(this, cChannel, cChannelClient);
			bResult = true;
		}
		return bResult;
	}	
	
	
	/**
	 * Add callback for ChannelPart (onPartChannel).
	 *
	 * @see IRCParser.IChannelPart
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelPart(Object eMethod) { AddCallback(eMethod, cbChannelPart); }
	/**
	 * Delete callback for ChannelPart (onPartChannel).
	 *
	 * @see IRCParser.IChannelPart
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelPart(Object eMethod) { DelCallback(eMethod, cbChannelPart); }
	/**
	 * Callback to all objects implementing the IRCParser.IChannelPart Interface.
	 *
	 * @see IRCParser.IChannelPart
	 * @param cChannel Channel that the user parted
	 * @param cChannelClient Client that parted
	 * @param sReason Reason given for parting (May be "")
	 */
	protected boolean CallChannelPart(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelPart.size(); i++) {
			cbChannelPart.get(i).onPartChannel(this, cChannel, cChannelClient, sReason);
			bResult = true;
		}
		return bResult;
	}
	
	/**
	 * Add callback for ChannelQuit (onQuitChannel).
	 *
	 * @see IRCParser.IChannelQuit
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelQuit(Object eMethod) { AddCallback(eMethod, cbChannelQuit); }
	/**
	 * Delete callback for ChannelQuit (onQuitChannel).
	 *
	 * @see IRCParser.IChannelQuit
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelQuit(Object eMethod) { DelCallback(eMethod, cbChannelQuit); }
	/**
	 * Callback to all objects implementing the IRCParser.IChannelQuit Interface.
	 *
	 * @see IRCParser.IChannelQuit
	 * @param cChannel Channel that user was on
	 * @param cChannelClient User thats quitting
	 * @param sReason Quit reason
	 */
	protected boolean CallChannelQuit(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelQuit.size(); i++) {
			cbChannelQuit.get(i).onQuitChannel(this, cChannel, cChannelClient, sReason);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for Quit (onQuit).
	 *
	 * @see IRCParser.IQuit
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddQuit(Object eMethod) { AddCallback(eMethod, cbQuit); }
	/**
	 * Delete callback for Quit (onQuit).
	 *
	 * @see IRCParser.IQuit
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelQuit(Object eMethod) { DelCallback(eMethod, cbQuit); }
	/**
	 * Callback to all objects implementing the IRCParser.IQuit Interface.
	 *
	 * @see IRCParser.IQuit
	 * @param cClient Client Quitting
	 * @param sReason Reason for quitting (may be "")
	 */
	protected boolean CallQuit(ClientInfo cClient, String sReason) {
		boolean bResult = false;
		for (int i = 0; i < cbQuit.size(); i++) {
			cbQuit.get(i).onQuit(this, cClient, sReason);
			bResult = true;
		}
		return bResult;
	}
	
	/**
	 * Add callback for ChannelTopic (onTopic).
	 *
	 * @see IRCParser.IChannelTopic
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddTopic(Object eMethod) { AddCallback(eMethod, cbChannelTopic); }
	/**
	 * Delete callback for ChannelTopic (onTopic).
	 *
	 * @see IRCParser.IChannelTopic
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelTopic(Object eMethod) { DelCallback(eMethod, cbChannelTopic); }
	/**
	 * Callback to all objects implementing the IRCParser.IChannelTopic Interface.
	 *
	 * @see IRCParser.IChannelTopic
	 * @param cChannel Channel that topic was set on
	 * @param bIsJoinTopic True when getting topic on join, false if set by user/server
	 */
	protected boolean CallTopic(ChannelInfo cChannel, boolean bIsJoinTopic) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelTopic.size(); i++) {
			cbChannelTopic.get(i).onTopic(this, cChannel, bIsJoinTopic);
			bResult = true;
		}
		return bResult;
	}
	
	/**
	 * Add callback for ChannelModesChanged (onModeChange).
	 *
	 * @see IRCParser.IChannelModesChanged
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddModesChanged(Object eMethod) { AddCallback(eMethod, cbChannelModesChanged); }
	/**
	 * Delete callback for ChannelModesChanged (onModeChange).
	 *
	 * @see IRCParser.IChannelModesChanged
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelModesChanged(Object eMethod) { DelCallback(eMethod, cbChannelModesChanged); }
	/**
	 * Callback to all objects implementing the IRCParser.IChannelModesChanged Interface.
	 *
	 * @see IRCParser.IChannelModesChanged
	 * @param cChannel Channel where modes were changed
	 * @param cChannelClient Client chaning the modes (null if server)
	 * @param sHost Host doing the mode changing (User host or server name)
	 */
	protected boolean CallModesChanged(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelModesChanged.size(); i++) {
			cbChannelModesChanged.get(i).onModeChange(this, cChannel, cChannelClient, sHost);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for UserModesChanged (onUserModeChange).
	 *
	 * @see IRCParser.IUserModesChanged
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddUserModesChanged(Object eMethod) { AddCallback(eMethod, cbUserModesChanged); }
	/**
	 * Delete callback for UserModesChanged (onUserModeChange).
	 *
	 * @see IRCParser.IUserModesChanged
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelUserModesChanged(Object eMethod) { DelCallback(eMethod, cbUserModesChanged); }
	/**
	 * Callback to all objects implementing the IRCParser.IUserModesChanged Interface.
	 *
	 * @see IRCParser.IUserModesChanged
	 * @param cClient Client that had the mode changed (almost always us)
	 * @param sSetby Host that set the mode (us or servername)
	 */
	protected boolean CallUserModesChanged(ClientInfo cClient, String sSetby) {
		boolean bResult = false;
		for (int i = 0; i < cbUserModesChanged.size(); i++) {
			cbUserModesChanged.get(i).onUserModeChange(this, cClient, sSetby);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for UserNickChanged (onNickChanged).
	 *
	 * @see IRCParser.INickChanged
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddNickChanged(Object eMethod) { AddCallback(eMethod, cbNickChanged); }
	/**
	 * Delete callback for UserNickChanged (onNickChanged).
	 *
	 * @see IRCParser.INickChanged
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelNickChanged(Object eMethod) { DelCallback(eMethod, cbNickChanged); }
	/**
	 * Callback to all objects implementing the IRCParser.INickChanged Interface.
	 *
	 * @see IRCParser.INickChanged
	 * @param cClient Client changing nickname
	 * @param sOldNick Nickname before change
	 */
	protected boolean CallNickChanged(ClientInfo cClient, String sOldNick) {
		boolean bResult = false;
		for (int i = 0; i < cbNickChanged.size(); i++) {
			cbNickChanged.get(i).onNickChanged(this, cClient, sOldNick);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for ChannelKick (onChannelKick).
	 *
	 * @see IRCParser.IChannelKick
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelKick(Object eMethod) { AddCallback(eMethod, cbChannelKick); }
	/**
	 * Delete callback for ChannelKick (onChannelKick).
	 *
	 * @see IRCParser.IChannelKick
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelKick(Object eMethod) { DelCallback(eMethod, cbChannelKick); }
	/**
	 * Callback to all objects implementing the IRCParser.IChannelKick Interface.
	 *
	 * @see IRCParser.IChannelKick
	 * @param cChannel Channel where the kick took place
	 * @param cKickedClient ChannelClient that got kicked
	 * @param cKickedByClient ChannelClient that did the kicking (may be null if server)
	 * @param sReason Reason for kick (may be "")
	 * @param sKickedByHost Hostname of Kicker (or servername)
	 */
	protected boolean CallChannelKick(ChannelInfo cChannel, ChannelClientInfo cKickedClient, ChannelClientInfo cKickedByClient, String sReason, String sKickedByHost) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelKick.size(); i++) {
			cbChannelKick.get(i).onChannelKick(this, cChannel, cKickedClient, cKickedByClient, sReason, sKickedByHost);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for ChannelMessage (onChannelMessage).
	 *
	 * @see IRCParser.IChannelMessage
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelMessage(Object eMethod) { AddCallback(eMethod, cbChannelMessage); }
	/**
	 * Delete callback for ChannelMessage (onChannelMessage).
	 *
	 * @see IRCParser.IChannelMessage
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelMessage(Object eMethod) { DelCallback(eMethod, cbChannelMessage); }
	/**
	 * Callback to all objects implementing the IRCParser.IChannelMessage Interface.
	 *
	 * @see IRCParser.IChannelMessage
	 * @param cChannel Channel where the message was sent to
	 * @param cChannelClient ChannelClient who sent the message (may be null if server)
	 * @param sMessage Message contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallChannelMessage(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelMessage.size(); i++) {
			cbChannelMessage.get(i).onChannelMessage(this, cChannel, cChannelClient, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for ChannelAction (onChannelAction).
	 *
	 * @see IRCParser.IChannelAction
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelAction(Object eMethod) { AddCallback(eMethod, cbChannelAction); }
	/**
	 * Delete callback for ChannelAction (onChannelAction).
	 *
	 * @see IRCParser.IChannelAction
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelAction(Object eMethod) { DelCallback(eMethod, cbChannelAction); }
	/**
	 * Callback to all objects implementing the IRCParser.IChannelAction Interface.
	 *
	 * @see IRCParser.IChannelAction
	 * @param cChannel Channel where the action was sent to
	 * @param cChannelClient ChannelClient who sent the action (may be null if server)
	 * @param sMessage action contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallChannelAction(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelAction.size(); i++) {
			cbChannelAction.get(i).onChannelAction(this, cChannel, cChannelClient, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}	

	/**
	 * Add callback for ChannelNotice (onChannelNotice).
	 *
	 * @see IRCParser.IChannelNotice
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelNotice(Object eMethod) { AddCallback(eMethod, cbChannelNotice); }
	/**
	 * Delete callback for ChannelNotice (onChannelNotice).
	 *
	 * @see IRCParser.IChannelNotice
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelNotice(Object eMethod) { DelCallback(eMethod, cbChannelNotice); }
	/**
	 * Callback to all objects implementing the IRCParser.IChannelNotice Interface.
	 *
	 * @see IRCParser.IChannelNotice
	 * @param cChannel Channel where the notice was sent to
	 * @param cChannelClient ChannelClient who sent the notice (may be null if server)
	 * @param sMessage notice contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallChannelNotice(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelNotice.size(); i++) {
			cbChannelNotice.get(i).onChannelNotice(this, cChannel, cChannelClient, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}
	
	/**
	 * Add callback for PrivateMessage (onPrivateMessage).
	 *
	 * @see IRCParser.IPrivateMessage
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddPrivateMessage(Object eMethod) { AddCallback(eMethod, cbPrivateMessage); }
	/**
	 * Delete callback for PrivateMessage (onPrivateMessage).
	 *
	 * @see IRCParser.IPrivateMessage
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelPrivateMessage(Object eMethod) { DelCallback(eMethod, cbPrivateMessage); }
	/**
	 * Callback to all objects implementing the IRCParser.IPrivateMessage Interface.
	 *
	 * @see IRCParser.IPrivateMessage
	 * @param cClient Client who sent the message (may be null if no common channels or server)
	 * @param sMessage Message contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallPrivateMessage(ClientInfo cClient, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbPrivateMessage.size(); i++) {
			cbPrivateMessage.get(i).onPrivateMessage(this, cClient, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for PrivateAction (onPrivateAction).
	 *
	 * @see IRCParser.IPrivateAction
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddPrivateAction(Object eMethod) { AddCallback(eMethod, cbPrivateAction); }
	/**
	 * Delete callback for PrivateAction (onPrivateAction).
	 *
	 * @see IRCParser.IPrivateAction
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelPrivateAction(Object eMethod) { DelCallback(eMethod, cbPrivateAction); }
	/**
	 * Callback to all objects implementing the IRCParser.IPrivateAction Interface.
	 *
	 * @see IRCParser.IPrivateAction
 	 * @param cClient Client who sent the action (may be null if no common channels or server)
	 * @param sMessage action contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallPrivateAction(ClientInfo cClient, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbPrivateAction.size(); i++) {
			cbPrivateAction.get(i).onPrivateAction(this, cClient, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}	

	/**
	 * Add callback for PrivateNotice (onPrivateNotice).
	 *
	 * @see IRCParser.IPrivateNotice
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddPrivateNotice(Object eMethod) { AddCallback(eMethod, cbPrivateNotice); }
	/**
	 * Delete callback for PrivateNotice (onPrivateNotice).
	 *
	 * @see IRCParser.IPrivateNotice
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelPrivateNotice(Object eMethod) { DelCallback(eMethod, cbPrivateNotice); }
	/**
	 * Callback to all objects implementing the IRCParser.IPrivateNotice Interface.
	 *
	 * @see IRCParser.IPrivateNotice
	 * @param cClient Client who sent the notice (may be null if no common channels or server)
	 * @param sMessage Notice contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallPrivateNotice(ClientInfo cClient, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbPrivateNotice.size(); i++) {
			cbPrivateNotice.get(i).onPrivateNotice(this, cClient, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}		

	/**
	 * Add callback for UnknownMessage (onUnknownMessage).
	 *
	 * @see IRCParser.IUnknownMessage
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddUnknownMessage(Object eMethod) { AddCallback(eMethod, cbUnknownMessage); }
	/**
	 * Delete callback for UnknownMessage (onUnknownMessage).
	 *
	 * @see IRCParser.IUnknownMessage
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelUnknownMessage(Object eMethod) { DelCallback(eMethod, cbUnknownMessage); }
	/**
	 * Callback to all objects implementing the IRCParser.IUnknownMessage Interface.
	 *
	 * @see IRCParser.IUnknownMessage
	 * @param cClient Client who sent the message (may be null if no common channels or server)
	 * @param sMessage Message contents
	 * @param sTarget Actual target of message
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallUnknownMessage(ClientInfo cClient, String sMessage, String sTarget, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbUnknownMessage.size(); i++) {
			cbUnknownMessage.get(i).onUnknownMessage(this, cClient, sMessage, sTarget, sHost);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for UnknownAction (onUnknownAction).
	 *
	 * @see IRCParser.IUnknownAction
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddUnknownAction(Object eMethod) { AddCallback(eMethod, cbUnknownAction); }
	/**
	 * Delete callback for UnknownAction (onUnknownAction).
	 *
	 * @see IRCParser.IUnknownAction
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelUnknownAction(Object eMethod) { DelCallback(eMethod, cbUnknownAction); }
	/**
	 * Callback to all objects implementing the IRCParser.IUnknownAction Interface.
	 *
	 * @see IRCParser.IUnknownAction
	 * @param cClient Client who sent the action (may be null if no common channels or server)
	 * @param sMessage Action contents
	 * @param sTarget Actual target of action
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallUnknownAction(ClientInfo cClient, String sMessage, String sTarget, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbUnknownAction.size(); i++) {
			cbUnknownAction.get(i).onUnknownAction(this, cClient, sMessage, sTarget, sHost);
			bResult = true;
		}
		return bResult;
	}	

	/**
	 * Add callback for UnknownNotice (onUnknownNotice).
	 *
	 * @see IRCParser.IUnknownNotice
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddUnknownNotice(Object eMethod) { AddCallback(eMethod, cbUnknownNotice); }
	/**
	 * Delete callback for UnknownNotice (onUnknownNotice).
	 *
	 * @see IRCParser.IUnknownNotice
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelUnknownNotice(Object eMethod) { DelCallback(eMethod, cbUnknownNotice); }
	/**
	 * Callback to all objects implementing the IRCParser.IUnknownNotice Interface.
	 *
	 * @see IRCParser.IUnknownNotice
	 * @param cClient Client who sent the notice (may be null if no common channels or server)
	 * @param sMessage Notice contents
	 * @param sTarget Actual target of notice
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallUnknownNotice(ClientInfo cClient, String sMessage, String sTarget, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbUnknownNotice.size(); i++) {
			cbUnknownNotice.get(i).onUnknownNotice(this, cClient, sMessage, sTarget, sHost);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for ChannelCTCP (onChannelCTCP).
	 *
	 * @see IRCParser.IChannelCTCP
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelCTCP(Object eMethod) { AddCallback(eMethod, cbChannelCTCP); }
	/**
	 * Delete callback for ChannelCTCP (onChannelCTCP).
	 *
	 * @see IRCParser.IChannelCTCP
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelCTCP(Object eMethod) { DelCallback(eMethod, cbChannelCTCP); }
	/**
	 * Callback to all objects implementing the IRCParser.IChannelCTCP Interface.
	 *
	 * @see IRCParser.IChannelCTCP
	 * @param cChannel Channel where CTCP was sent
	 * @param cChannelClient ChannelClient who sent the message (may be null if server)
	 * @param sType Type of CTCP (VERSION, TIME etc)
	 * @param sMessage Additional contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallChannelCTCP(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sType, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelCTCP.size(); i++) {
			cbChannelCTCP.get(i).onChannelCTCP(this, cChannel, cChannelClient, sType, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}	

	/**
	 * Add callback for PrivateCTCP (onPrivateCTCP).
	 *
	 * @see IRCParser.IPrivateCTCP
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddPrivateCTCP(Object eMethod) { AddCallback(eMethod, cbPrivateCTCP); }
	/**
	 * Delete callback for PrivateCTCP (onPrivateCTCP).
	 *
	 * @see IRCParser.IPrivateCTCP
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelPrivateCTCP(Object eMethod) { DelCallback(eMethod, cbPrivateCTCP); }
	/**
	 * Callback to all objects implementing the IRCParser.IPrivateCTCP Interface.
	 *
	 * @see IRCParser.IPrivateCTCP
 	 * @param cClient Client who sent the CTCP (may be null if no common channels or server)
	 * @param sType Type of CTCP (VERSION, TIME etc)
	 * @param sMessage Additional contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallPrivateCTCP(ClientInfo cClient, String sType, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbPrivateCTCP.size(); i++) {
			cbPrivateCTCP.get(i).onPrivateCTCP(this, cClient, sType, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}	

	/**
	 * Add callback for UnknownCTCP (onUnknownCTCP).
	 *
	 * @see IRCParser.IUnknownCTCP
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddUnknownCTCP(Object eMethod) { AddCallback(eMethod, cbUnknownCTCP); }
	/**
	 * Delete callback for UnknownCTCP (onUnknownCTCP).
	 *
	 * @see IRCParser.IUnknownCTCP
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelUnknownCTCP(Object eMethod) { DelCallback(eMethod, cbUnknownCTCP); }
	/**
	 * Callback to all objects implementing the IRCParser.IUnknownCTCP Interface.
	 *
	 * @see IRCParser.IUnknownCTCP
 	 * @param cClient Client who sent the CTCP (may be null if no common channels or server)
	 * @param sType Type of CTCP (VERSION, TIME etc)
	 * @param sMessage Additional contents
	 * @param sTarget Actual Target of CTCP
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallUnknownCTCP(ClientInfo cClient, String sType, String sMessage, String sTarget, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbUnknownCTCP.size(); i++) {
			cbUnknownCTCP.get(i).onUnknownCTCP(this, cClient, sType, sMessage, sTarget, sHost);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for ChannelCTCPReply (onChannelCTCPReply).
	 *
	 * @see IRCParser.IChannelCTCPReply
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelCTCPReply(Object eMethod) { AddCallback(eMethod, cbChannelCTCPReply); }
	/**
	 * Delete callback for ChannelCTCPReply (onChannelCTCPReply).
	 *
	 * @see IRCParser.IChannelCTCPReply
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelCTCPReply(Object eMethod) { DelCallback(eMethod, cbChannelCTCPReply); }
	/**
	 * Callback to all objects implementing the IRCParser.IChannelCTCPReply Interface.
	 *
	 * @see IRCParser.IChannelCTCPReply
	 * @param cChannel Channel where CTCPReply was sent
	 * @param cChannelClient ChannelClient who sent the message (may be null if server)
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallChannelCTCPReply(ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sType, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelCTCPReply.size(); i++) {
			cbChannelCTCPReply.get(i).onChannelCTCPReply(this, cChannel, cChannelClient, sType, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}	

	/**
	 * Add callback for PrivateCTCPReply (onPrivateCTCPReply).
	 *
	 * @see IRCParser.IPrivateCTCPReply
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddPrivateCTCPReply(Object eMethod) { AddCallback(eMethod, cbPrivateCTCPReply); }
	/**
	 * Delete callback for PrivateCTCPReply (onPrivateCTCPReply).
	 *
	 * @see IRCParser.IPrivateCTCPReply
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelPrivateCTCPReply(Object eMethod) { DelCallback(eMethod, cbPrivateCTCPReply); }
	/**
	 * Callback to all objects implementing the IRCParser.IPrivateCTCPReply Interface.
	 *
	 * @see IRCParser.IPrivateCTCPReply
 	 * @param cClient Client who sent the CTCPReply (may be null if no common channels or server)
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallPrivateCTCPReply(ClientInfo cClient, String sType, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbPrivateCTCPReply.size(); i++) {
			cbPrivateCTCPReply.get(i).onPrivateCTCPReply(this, cClient, sType, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}	

	/**
	 * Add callback for UnknownCTCPReply (onUnknownCTCPReply).
	 *
	 * @see IRCParser.IUnknownCTCPReply
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddUnknownCTCPReply(Object eMethod) { AddCallback(eMethod, cbUnknownCTCPReply); }
	/**
	 * Delete callback for UnknownCTCPReply (onUnknownCTCPReply).
	 *
	 * @see IRCParser.IUnknownCTCPReply
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelUnknownCTCPReply(Object eMethod) { DelCallback(eMethod, cbUnknownCTCPReply); }
	/**
	 * Callback to all objects implementing the IRCParser.IUnknownCTCPReply Interface.
	 *
	 * @see IRCParser.IUnknownCTCPReply
 	 * @param cClient Client who sent the CTCPReply (may be null if no common channels or server)
	 * @param sType Type of CTCPRReply (VERSION, TIME etc)
	 * @param sMessage Reply Contents
	 * @param sTarget Actual Target of CTCPReply
	 * @param sHost Hostname of sender (or servername)
	 */
	protected boolean CallUnknownCTCPReply(ClientInfo cClient, String sType, String sMessage, String sTarget, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbUnknownCTCPReply.size(); i++) {
			cbUnknownCTCPReply.get(i).onUnknownCTCPReply(this, cClient, sType, sMessage, sTarget, sHost);
			bResult = true;
		}
		return bResult;
	}		
	
	/**
	 * Add callback for GotNames (onGotNames).
	 *
	 * @see IRCParser.IGotNames
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddGotNames(Object eMethod) { AddCallback(eMethod, cbGotNames); }
	/**
	 * Delete callback for GotNames (onGotNames).
	 *
	 * @see IRCParser.IGotNames
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelGotNames(Object eMethod) { DelCallback(eMethod, cbGotNames); }
	/**
	 * Callback to all objects implementing the IRCParser.IGotNames Interface.
	 *
	 * @see IRCParser.IGotNames
	 * @param cChannel Channel which the names reply is for
	 */
	protected boolean CallGotNames(ChannelInfo cChannel) {
		boolean bResult = false;
		for (int i = 0; i < cbGotNames.size(); i++) {
			cbGotNames.get(i).onGotNames(this, cChannel);
			bResult = true;
		}
		return bResult;
	}		
	
	/**
	 * Perform a silent test on certain functions.
	 *
	 * @return Boolean result of test. (True only if ALL tests pass)
	 */
	public boolean DoSelfTest() {
		return DoSelfTest(true);
	} 
	
	/**
	 * Perform a test on certain functions.
	 *
	 * @param bSilent Should output be given? (Sent to Console)
	 * @return Boolean result of test. (True only if ALL tests pass)
	 */	
	public boolean DoSelfTest(boolean bSilent) {
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
	private void ResetState() {
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
			ResetState();
			CallDebugInfo(ndSocket,"Connecting to "+server.sHost+":"+server.nPort);

			if (server.bSSL) {
				SocketFactory socketFactory = SSLSocketFactory.getDefault();
				socket = socketFactory.createSocket(server.sHost,server.nPort);
			} else {
				socket = new Socket(server.sHost,server.nPort);
			}
			if (bDebug) { System.out.printf("\t\t-> 1\n"); }
			out = new PrintWriter(socket.getOutputStream(), true);
			if (bDebug) { System.out.printf("\t\t-> 2\n"); }
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			if (bDebug) { System.out.printf("\t\t-> 3\n"); }
		} catch (Exception e) { throw e; }
	}
	
	/**
	 * Begin execution.
	 * Connect to server, and start parsing incomming lines
	 */
	public void run() {
		CallDebugInfo(ndInfo,"Begin Thread Execution");
		if (HasBegan) { return; } else { HasBegan = true; }
		try { connect(); } catch (Exception e) { CallDebugInfo(ndSocket,"Error Connecting, Aborted"); return; }
		// :HACK: While true loops really really suck.
		CallDebugInfo(ndSocket,"Socket Connected");
		String line = "";
		while(true) {
			try {
				line = in.readLine(); // Blocking :/
				if (line == null) {
					CallDebugInfo(ndSocket,"Socket Closed");
					break;
				} else {
					if (IsFirst) {
						if (!server.sPassword.equals("")) {
							SendString("PASS "+server.sPassword);
						}
						SetNickname(me.sNickname);
						SendString("USER "+me.sUsername.toLowerCase()+" * * :"+me.sRealname);
						IsFirst = false;
					}
					ProcessLine(line);
				}
			} catch (IOException e) {
				CallDebugInfo(ndSocket,"Socket Closed");
				break;
			}
		}
		CallDebugInfo(ndInfo,"End Thread Execution");
	}
	
	/** Close socket on destroy. */
	protected void finalize(){
		try {
			socket.close();
		} catch (IOException e) {
			CallDebugInfo(ndInfo,"Could not close socket");
		}
	}

	/**
	 * Get the trailing parameter for a line.
	 * The parameter is everything after the first occurance of " :" ot the last token in the line after a space.
	 *
	 * @param line Line to get parameter for
	 * @return Parameter of the line
	 */
	protected String GetParam(String line) {
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
	protected String[] IRCTokenise(String line) {
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
	public ClientInfo GetClientInfo(String sWho) {
		if (bDebug) { System.out.printf("\t\tInput: %s | ",sWho); }
		sWho = ClientInfo.ParseHost(sWho);
		if (bDebug) { System.out.printf("Client Name: %s\n",sWho); }
		sWho = sWho.toLowerCase();
		if (hClientList.containsKey(sWho)) { return hClientList.get(sWho); } else { return null; }
	}
	
	/**
	 * Get the ChannelInfo object for a channel.
	 *
	 * @param sWhat This is the name of the channel.
	 * @return ChannelInfo Object for the channel, or null
	 */
	public ChannelInfo GetChannelInfo(String sWhat) {
		sWhat = sWhat.toLowerCase();
		if (hChannelList.containsKey(sWhat)) { return hChannelList.get(sWhat); } else { return null; }
	}	
	
	// TODO: This should do some checks on stuff?
	/**
	 * Send a line to the server.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 */
	public void SendLine(String line) { CallDataOut(line,false); out.printf("%s\r\n",line);}
	
	/** Send a line to the server and add proper line ending. */
	protected void SendString(String line) {
		CallDataOut(line,true);
		out.printf("%s\r\n",line);
	}
	
	/**
	 * Process a line and call relevent methods for handling.
	 *
	 * @param line IRC Line to process
	 */
	private void ProcessLine(String line) {
		String[] token = IRCTokenise(line);
//		String mainParam = token[token.length-1];
		
		int nParam;
		CallDataIn(line);
		String sParam = token[1];
		try {nParam = Integer.parseInt(token[1]);} catch (Exception e) { nParam = -1;}
		
		try {
			if (token[0].equals("PING") || token[1].equals("PING")) { SendString("PONG :"+sParam); }
			else {
				if (token[0].substring(0,1).equals(":")) {
					// Post Connect
					switch (nParam) {
						case -1:
							ProcessStringParam(sParam,token);
							break;
						case 1: // 001 - Welcome to IRC
							Got001 = true;
							Process001(nParam,token);
							break;
						case 4: // 004 - ISUPPORT
						case 5: // 005 - ISUPPORT
							Process004_005(nParam,token);
							break;
						case 332: // Topic on Join
						case 333: // Topic Setter On Join
							ProcessTopic(sParam,token);
							break;
						case 375: // MOTD Start
							break;
						case 353: // Names
						case 366: // End of Names
							ProcessNames(nParam,token);
							break;
						case 324: // Modes
							ProcessMode(sParam,token);
						case 329: // Channel Time
						case 368: // End of ban list
							break;
						case 376: // End of MOTD
						case 422: // No MOTD
							ProcessEndOfMOTD(nParam,token);
							break;
						case 433: // Nick In Use
							ProcessNickInUse(nParam,token);
							break;
						default: // Unknown
							break;
					}
				} else {
					// Pre Connect
				}
			}
		} catch (Exception e) { CallErrorInfo(errFatal,"Exception in Parser. {"+line+"} ["+e.getMessage()+"]"); e.getStackTrace(); }
	}
		
	/**
	 * Process an IRC Line with a string parameter rather than a Numeric.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token[] IRCTokenised verison of the incomming line
	 */
	private void ProcessStringParam(String sParam, String token[]) {
		// Process a line where the parameter is a string (IE PRIVMSG, NOTICE etc - Not including PING!)
		if (sParam.equalsIgnoreCase("PRIVMSG") || sParam.equalsIgnoreCase("NOTICE")) { ProcessIRCMessage(sParam,token); }
		else if (sParam.equalsIgnoreCase("JOIN")) { ProcessJoinChannel(sParam,token); }
		else if (sParam.equalsIgnoreCase("NICK")) { ProcessNickChange(sParam,token); }
		else if (sParam.equalsIgnoreCase("KICK")) { ProcessKickChannel(sParam,token); }
		else if (sParam.equalsIgnoreCase("PART")) { ProcessPartChannel(sParam,token); }
		else if (sParam.equalsIgnoreCase("QUIT")) { ProcessQuit(sParam,token); }
		else if (sParam.equalsIgnoreCase("TOPIC")) { ProcessTopic(sParam,token); }
		else if (sParam.equalsIgnoreCase("MODE")) { ProcessMode(sParam,token); }
	}
	
	/**
	 * Process a Nickname change.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void ProcessNickChange(String sParam, String token[]) {
		ClientInfo iClient;
		
		iClient = GetClientInfo(token[0]);
		if (iClient != null) {
			if (iClient.getHost().equals("")) { iClient.setUserBits(token[0],false); }
			hClientList.remove(iClient.getNickname().toLowerCase());
			iClient.setUserBits(token[2],true);
			hClientList.put(iClient.getNickname().toLowerCase(),iClient);
			CallNickChanged(iClient, ClientInfo.ParseHost(token[0]));
		}
	}
	
	/**
	 * Process a kick.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void ProcessKickChannel(String sParam, String token[]) {
		ChannelClientInfo iChannelClient;
		ChannelClientInfo iChannelKicker;
		ChannelInfo iChannel;
		ClientInfo iClient;
		ClientInfo iKicker;
		String sReason = "";
		
		iClient = GetClientInfo(token[3]);
		iKicker = GetClientInfo(token[0]);
		iChannel = GetChannelInfo(token[2]);
		
		if (iClient == null) { return; }
		if (iChannel == null) { 
			if (iClient != cMyself) {
				CallErrorInfo(errWarning+errCanContinue, "Got kick for channel ("+token[2]+") that I am not on. [User: "+token[3]+"]");
			}
			return;
		} else {
			if (token.length > 4) { sReason = token[token.length-1]; }
			iChannelClient = iChannel.getUser(iClient);
			iChannelKicker = iChannel.getUser(iClient);
			CallChannelKick(iChannel,iChannelClient,iChannelKicker,sReason,token[0]);
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
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void ProcessMode(String sParam, String token[]) {
		String[] sModestr;
		String sChannelName;
		String sModeParam;
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

		if (!ChannelInfo.isValidChannelName(this, sChannelName)) { ProcessUserMode(sParam, token, sModestr); return; }
		
		iChannel = GetChannelInfo(sChannelName);
		if (iChannel == null) { 
			CallErrorInfo(errWarning+errCanContinue, "Got modes for channel ("+sChannelName+") that I am not on.");
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
					if (bDebug) { System.out.printf("User Mode: %c [%s] {Positive: %b}\n",cMode, sModeParam, bPositive); }
					iChannelClientInfo = iChannel.getUser(sModeParam);
					if (iChannelClientInfo == null) {
						// Client not known?
						CallErrorInfo(errWarning+errCanContinue, "Got mode for client not known on channel - Added");
						iClient = GetClientInfo(sModeParam);
						if (iClient == null) { 
							CallErrorInfo(errWarning+errCanContinue, "Got mode for client not known at all - Added");
							iClient = new ClientInfo(this, sModeParam);
							hClientList.put(iClient.getNickname().toLowerCase(),iClient);
						}
						iChannelClientInfo = iChannel.addClient(iClient);
					}
					if (bPositive) { iChannelClientInfo.setChanMode(iChannelClientInfo.getChanMode() + nValue); }
					else { iChannelClientInfo.setChanMode(iChannelClientInfo.getChanMode() - nValue); }
					continue;
				} else {
					CallErrorInfo(errWarning+errCanContinue, "Got unknown mode "+cMode+" - Added as boolean mode");
					hChanModesBool.put(cMode,nNextKeyCMBool);
					nValue = nNextKeyCMBool;
					bBooleanMode = true;
					nNextKeyCMBool = nNextKeyCMBool*2;
				}
				
				if (bBooleanMode) {
					if (bDebug) { System.out.printf("Boolean Mode: %c [%d] {Positive: %b}\n",cMode, nValue, bPositive); }
					if (bPositive) { nCurrent = nCurrent + nValue; }
					else { nCurrent = nCurrent - nValue; }
				} else {
					if (nValue == cmList) {
						sModeParam = sModestr[nParam++];
						iChannel.setListModeParam(cMode, sModeParam, bPositive);
						if (bDebug) { System.out.printf("List Mode: %c [%s] {Positive: %b}\n",cMode, sModeParam, bPositive); }
					} else {
						if (bPositive) { 
							sModeParam = sModestr[nParam++];
							if (bDebug) { System.out.printf("Set Mode: %c [%s] {Positive: %b}\n",cMode, sModeParam, bPositive); }
							iChannel.setModeParam(cMode,sModeParam);
						} else {
							if ((nValue & cmUnset) == cmUnset) { sModeParam = sModestr[nParam++]; } else { sModeParam = ""; }
							if (bDebug) { System.out.printf("Unset Mode: %c [%s] {Positive: %b}\n",cMode, sModeParam, bPositive); }
							iChannel.setModeParam(cMode,"");
						}
					}
				}
			}
		}
		
		iChannel.setMode(nCurrent);
		if (sParam.equals("324")) { CallModesChanged(iChannel, null, ""); }
		else { CallModesChanged(iChannel, iChannel.getUser(token[0]), token[0]); }
	}
	
	/**
	 * Process user modes.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void ProcessUserMode(String sParam, String token[], String sModestr[]) {
		int nCurrent = 0, nValue = 0;
		boolean bPositive = true;
		
		ClientInfo iClient;
		
		iClient = GetClientInfo(token[2]);
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
					CallErrorInfo(errWarning+errCanContinue, "Got unknown user mode "+cMode+" - Added");
					hUserModes.put(cMode,nNextKeyUser);
					nValue = nNextKeyUser;
					nNextKeyUser = nNextKeyUser*2;
				}
				
				if (bDebug) { System.out.printf("User Mode: %c [%d] {Positive: %b}\n",cMode, nValue, bPositive); }
				if (bPositive) { nCurrent = nCurrent + nValue; }
				else { nCurrent = nCurrent - nValue; }
			}
		}
		
		iClient.setUserMode(nCurrent);
		CallUserModesChanged(iClient, token[0]);
	}	
	
	/**
	 * Process a Names reply.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void ProcessNames(int nParam, String token[]) {
		ChannelInfo iChannel;
		if (nParam == 366) {
			// End of names
			iChannel = GetChannelInfo(token[2]);
			if (iChannel != null) {
				iChannel.bAddingNames = false;
				CallGotNames(iChannel);
			}
		} else {
			// Names
			
			ClientInfo iClient;
			ChannelClientInfo iChannelClient;
			
			iChannel = GetChannelInfo(token[4]);
		
			if (iChannel == null) { 
				CallErrorInfo(errWarning+errCanContinue, "Got names for channel ("+token[4]+") that I am not on.");
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
				System.out.printf("Name: %s Modes: \"%s\" [%d]\n",sName,sModes,nPrefix);
				
				iClient = GetClientInfo(sName);
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
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void ProcessIRCMessage(String sParam, String token[]) {
		ChannelClientInfo iChannelClient = null;
		ChannelInfo iChannel = null;
		ClientInfo iClient = null;
		String sMessage = token[token.length-1];
		String[] bits = sMessage.split(" ", 2);
		Character Char1 = Character.valueOf((char)1);
		String sCTCP = "";
		boolean isAction = false;
		boolean isCTCP = false;
		
		iClient = GetClientInfo(token[0]);

		if (sParam.equalsIgnoreCase("PRIVMSG")) {
			if (bits[0].equalsIgnoreCase(Char1+"ACTION") && Character.valueOf(sMessage.charAt(sMessage.length()-1)).equals(Char1)) {
				isAction = true;
			} else if (Character.valueOf(sMessage.charAt(0)).equals(Char1) && Character.valueOf(sMessage.charAt(sMessage.length()-1)).equals(Char1)) {
				isCTCP = true;
				if (bits.length > 1) { sMessage = bits[1]; } else { sMessage = ""; }
				bits = bits[0].split(Char1.toString());
				sCTCP = bits[1];
				if (bDebug) { System.out.printf("CTCP: \"%s\" \"%s\"\n",sCTCP,sMessage); }
			}
		}

		if (ChannelInfo.isValidChannelName(this, token[2])) {
			iChannel = GetChannelInfo(token[2]);
			if (iClient != null && iChannel != null) { iChannelClient = iChannel.getUser(iClient); }
			if (sParam.equalsIgnoreCase("PRIVMSG")) {
				if (!isAction) {
					if (isCTCP) {
						CallChannelCTCP(iChannel, iChannelClient, sCTCP, sMessage, token[0]);
					} else {
						CallChannelMessage(iChannel, iChannelClient, sMessage, token[0]);
					}
				} else {
					CallChannelAction(iChannel, iChannelClient, sMessage, token[0]);
				}
			} else if (sParam.equalsIgnoreCase("NOTICE")) {
				if (isCTCP) {
					CallChannelCTCPReply(iChannel, iChannelClient, sCTCP, sMessage, token[0]);
				} else {
					CallChannelNotice(iChannel, iChannelClient, sMessage, token[0]);
				}
			}
		} else if (token[2].equalsIgnoreCase(cMyself.getNickname())) {
			if (sParam.equalsIgnoreCase("PRIVMSG")) {
				if (!isAction) {
					if (isCTCP) {
						CallPrivateCTCP(iClient, sCTCP, sMessage, token[0]);
					} else {
						CallPrivateMessage(iClient, sMessage, token[0]);
					}
				} else {
					CallPrivateAction(iClient, sMessage, token[0]);
				}
			} else if (sParam.equalsIgnoreCase("NOTICE")) {
				if (isCTCP) {
					CallPrivateCTCPReply(iClient, sCTCP, sMessage, token[0]);
				} else {
					CallPrivateNotice(iClient, sMessage, token[0]);
				}
			}
		} else {
			if (bDebug) { System.out.printf("Message for Other ("+token[2]+")\n"); }
			if (sParam.equalsIgnoreCase("PRIVMSG")) {
				if (!isAction) {
					if (isCTCP) {
						CallUnknownCTCP(iClient, sCTCP, sMessage, token[2], token[0]);
					} else {
						CallUnknownMessage(iClient, sMessage, token[2], token[0]);
					}
				} else {
					CallUnknownAction(iClient, sMessage, token[2], token[0]);
				}
			} else if (sParam.equalsIgnoreCase("NOTICE")) {
				if (isCTCP) {
					CallUnknownCTCPReply(iClient, sCTCP, sMessage, token[2], token[0]);
				} else {
					CallUnknownNotice(iClient, sMessage, token[2], token[0]);
				}
			}
		}
	}
	
	/**
	 * Process a topic change.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void ProcessTopic(String sParam, String token[]) {
		ChannelInfo iChannel;
		if (sParam.equals("332")) {
			iChannel = GetChannelInfo(token[3]);
			if (iChannel == null) { return; };
			iChannel.setTopic(token[token.length-1]);
		} else if (sParam.equals("333")) {
			iChannel = GetChannelInfo(token[3]);
			if (iChannel == null) { return; };
			iChannel.setTopicTime(Long.parseLong(token[5]));
			iChannel.setTopicUser(token[5]);
			CallTopic(iChannel,false);
		} else {
			iChannel = GetChannelInfo(token[2]);
			if (iChannel == null) { return; };
			iChannel.setTopicTime(java.util.Calendar.getInstance().getTimeInMillis() / 1000);
			String sTemp[] = token[0].split(":",2);
			if (sTemp.length > 1) { token[0] = sTemp[1]; }
			iChannel.setTopicUser(token[0]);
			CallTopic(iChannel,true);
		}
	}
	
	/**
	 * Process a channel join.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void ProcessJoinChannel(String sParam, String token[]) {
		// :nick!ident@host JOIN (:)#Channel
		Character cTemp;
		Byte nTemp;
		if (token.length < 3) { return; }
		ClientInfo iClient;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		iClient = GetClientInfo(token[0]);
		iChannel = GetChannelInfo(token[token.length-1]);
		
		if (iClient == null) { iClient = new ClientInfo(this, token[0]); hClientList.put(iClient.getNickname().toLowerCase(),iClient); }
		if (iChannel == null) { 
			if (iClient != cMyself) {
				CallErrorInfo(errWarning+errCanContinue, "Got join for channel ("+token[token.length-1]+") that I am not on. [User: "+token[0]+"]");
			}
			iChannel = new ChannelInfo(this, token[token.length-1]);
			hChannelList.put(iChannel.getName().toLowerCase(),iChannel);
			SendString("MODE "+iChannel.getName());
			
			for (Enumeration e = hChanModesOther.keys(); e.hasMoreElements();) {
				cTemp = (Character)e.nextElement();
				nTemp = hChanModesOther.get(cTemp);
				if (nTemp == cmList) { SendString("MODE "+iChannel.getName()+" "+cTemp); }
			}
			
		} else {
			// This is only done if we are on the channel. Else we wait for names.
			iChannelClient = iChannel.addClient(iClient);
			CallChannelJoin(iChannel, iChannelClient);	
		}
	}	
	
	/**
	 * Process a channel part.
	 *
	 * @param sParam String representation of parameter to parse
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void ProcessPartChannel(String sParam, String token[]) {
		// :nick!ident@host PART #Channel
		// :nick!ident@host PART #Channel :reason
		if (token.length < 3) { return; }
		ClientInfo iClient;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		iClient = GetClientInfo(token[0]);
		iChannel = GetChannelInfo(token[2]);
		
		if (iClient == null) { return; }
		if (iChannel == null) { 
			if (iClient != cMyself) {
				CallErrorInfo(errWarning+errCanContinue, "Got part for channel ("+token[2]+") that I am not on. [User: "+token[0]+"]");
			}
			return;
		} else {
			String sReason = "";
			if (token.length > 3) { sReason = token[token.length-1]; }
			iChannelClient = iChannel.getUser(iClient);
			CallChannelPart(iChannel,iChannelClient,sReason);
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
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void ProcessQuit(String sParam, String token[]) {
		// :nick!ident@host QUIT
		// :nick!ident@host QUIT :reason
		if (token.length < 2) { return; }
		ClientInfo iClient;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		iClient = GetClientInfo(token[0]);
		
		if (iClient == null) { return; }

		String sReason = "";
		if (token.length > 2) { sReason = token[token.length-1]; }
		
		for (Enumeration e = hChannelList.keys(); e.hasMoreElements();) {
			iChannel = hChannelList.get(e.nextElement());
			iChannelClient = iChannel.getUser(iClient);
			if (iChannelClient != null) {
				CallChannelQuit(iChannel,iChannelClient,sReason);
				if (iClient == cMyself) {
					iChannel.emptyChannel();
					hChannelList.remove(iChannel.getName().toLowerCase());
				} else {
					iChannel.delClient(iClient);
				}
			}
		}

		CallQuit(iClient,sReason);
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
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void Process004_005(int nParam, String token[]) {
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
				if (bDebug) { System.out.printf("%s => %s \r\n",sKey,sValue); }
				h005Info.put(sKey,sValue);
			}
		}
	}
	
	/**
	 * Process CHANMODES from 005.
	 */	
	protected void ParseChanModes() {
		final String sDefaultModes = "b,k,l,imnpstrc";
		String[] Bits = null;
		String ModeStr;
		if (h005Info.containsKey("CHANMODES")) { ModeStr = h005Info.get("CHANMODES");	}
		else { ModeStr = sDefaultModes; h005Info.put("CHANMODES",ModeStr); }
		Bits = ModeStr.split(",",4);
		if (Bits.length != 4) {
			ModeStr = sDefaultModes;
			CallErrorInfo(errError+errCanContinue, "CHANMODES String not valid. Using default string of \""+ModeStr+"\"");
			h005Info.put("CHANMODES",ModeStr);
			Bits = ModeStr.split(",",4);
		}
		
		// ResetState
		hChanModesOther.clear();
		hChanModesBool.clear();
		nNextKeyCMBool = 1;
		
		// List modes.
		for (int i = 0; i < Bits[0].length(); ++i) {
			Character cMode = Bits[0].charAt(i);
			if (bDebug) { System.out.printf("List Mode: %c\n",cMode); }
			if (!hChanModesOther.containsKey(cMode)) { hChanModesOther.put(cMode,cmList); }
		}
		
		// Param for Set and Unset.
		Byte nBoth = (cmSet+cmUnset);
		for (int i = 0; i < Bits[1].length(); ++i) {
			Character cMode = Bits[1].charAt(i);
			if (bDebug) { System.out.printf("Set/Unset Mode: %c\n",cMode); }
			if (!hChanModesOther.containsKey(cMode)) { hChanModesOther.put(cMode,nBoth); }
		}
		
		// Param just for Set
		for (int i = 0; i < Bits[2].length(); ++i) {
			Character cMode = Bits[2].charAt(i);
			if (bDebug) { System.out.printf("Set Only Mode: %c\n",cMode); }
			if (!hChanModesOther.containsKey(cMode)) { hChanModesOther.put(cMode,cmSet); }
		}
		
		// Boolean Mode
		for (int i = 0; i < Bits[3].length(); ++i) {
			Character cMode = Bits[3].charAt(i);
			if (bDebug) { System.out.printf("Boolean Mode: %c [%d]\n",cMode,nNextKeyCMBool); }
			if (!hChanModesBool.containsKey(cMode)) {
				hChanModesBool.put(cMode,nNextKeyCMBool);
				nNextKeyCMBool = nNextKeyCMBool*2;
			}
		}
	}
	
	/**
	 * Process USERMODES from 004.
	 */	
	protected void ParseUserModes() {
		final String sDefaultModes = "nwdoi";
		String[] Bits = null;
		String ModeStr;
		if (h005Info.containsKey("USERMODES")) { ModeStr = h005Info.get("USERMODES");	}
		else { ModeStr = sDefaultModes; h005Info.put("USERMODES", sDefaultModes); }
		
		// ResetState
		hUserModes.clear();
		nNextKeyUser = 1;
		
		// Boolean Mode
		for (int i = 0; i < ModeStr.length(); ++i) {
			Character cMode = ModeStr.charAt(i);
			if (bDebug) { System.out.printf("User Mode: %c [%d]\n",cMode,nNextKeyUser); }
			if (!hUserModes.containsKey(cMode)) {
				hUserModes.put(cMode,nNextKeyUser);
				nNextKeyUser = nNextKeyUser*2;
			}
		}
	}	
	
	/**
	 * Process CHANTYPES from 005.
	 */	
	protected void ParseChanPrefix() {
		final String sDefaultModes = "#&";
		String[] Bits = null;
		String ModeStr;
		if (h005Info.containsKey("CHANTYPES")) { ModeStr = h005Info.get("CHANTYPES");	}
		else { ModeStr = sDefaultModes; h005Info.put("CHANTYPES", sDefaultModes); }
		
		// ResetState
		hChanPrefix.clear();
		
		// Boolean Mode
		for (int i = 0; i < ModeStr.length(); ++i) {
			Character cMode = ModeStr.charAt(i);
			if (bDebug) { System.out.printf("Chan Prefix: %c\n",cMode); }
			if (!hChanPrefix.containsKey(cMode)) { hChanPrefix.put(cMode,true); }
		}
	}		
	
	/**
	 * Process PREFIX from 005.
	 */	
	protected void ParsePrefixModes() {
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
			CallErrorInfo(errError+errCanContinue, "PREFIX String not valid. Using default string of \""+ModeStr+"\"");
			h005Info.put("PREFIX",ModeStr);
			ModeStr = ModeStr.substring(1);
			Bits = ModeStr.split("\\)",2);
		}

		// ResetState
		hPrefixModes.clear();
		hPrefixMap.clear();
		nNextKeyPrefix = 1;

		for (int i = 0; i < Bits[0].length(); ++i) {
			Character cMode = Bits[0].charAt(i);
			Character cPrefix = Bits[1].charAt(i);
			if (bDebug) { System.out.printf("Prefix Mode: %c => %c [%d]\n",cMode,cPrefix,nNextKeyPrefix); }
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
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void ProcessEndOfMOTD(int nParam, String token[]) {
		ParseChanModes();
		ParseChanPrefix();
		ParsePrefixModes();
		ParseUserModes();
		CallMOTDEnd();
	}

	/**
	 * Process a 001 message.
	 *
	 * @param nParam Integer representation of parameter to parse
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void Process001(int nParam, String token[]) {
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
		
		cMyself = GetClientInfo(sNick);
		if (cMyself == null) { cMyself = new ClientInfo(this, sNick); }
		
		CallServerReady();
	}

	/**
	 * Process a NickInUse message.
	 * Parser implements handling of this if Pre-001 and no other handler found,<br>
	 * adding the NickInUse handler (AddNickInUse) after 001 is prefered over before.
	 *
	 * @param nParam Integer representation of parameter to parse
	 * @param token[] IRCTokenised verison of the incomming line
	 */	
	private void ProcessNickInUse(int nParam, String token[]) {
		if (!CallNickInUse()) {
			// Manually handle nick in use.
			CallDebugInfo(ndInfo,"No Nick in use Handler.");
			if (!Got001) {
				CallDebugInfo(ndInfo,"Using inbuilt handler");
				// If this is before 001 we will try and get a nickname, else we will leave the nick as-is
				if (!TriedAlt) { SetNickname(me.sAltNickname); TriedAlt = true; }
				else {
					if (sThinkNickname.equalsIgnoreCase(me.sAltNickname)) { sThinkNickname = me.sNickname; }
					SetNickname('_'+sThinkNickname);
				}
			}
		}
	}
	
	
	/**
	 * Join a Channel.
	 *
	 * @param sChannelName Name of channel to join
	 */
	public void JoinChannel(String sChannelName) {
		if (!ChannelInfo.isValidChannelName(this,sChannelName)) { return; }
		SendString("JOIN "+sChannelName);
	}
	
	
	/**
	 * Leave a Channel.
	 *
	 * @param sChannelName Name of channel to part
	 * @param sReason Reason for leaving (Nothing sent if sReason is "")
	 */
	public void PartChannel(String sChannelName, String sReason) {
		if (!ChannelInfo.isValidChannelName(this,sChannelName)) { return; }
		if (sReason.equals("")) { SendString("PART "+sChannelName); }
		else { SendString("PART "+sChannelName+" :"+sReason); }
	}	
	
	/**
	 * Set Nickname.
	 *
	 * @param sNewNickName New nickname wanted.
	 */
	public void SetNickname(String sNewNickName) {
		sThinkNickname = sNewNickName;
		SendString("NICK "+sNewNickName);
	}
	
	/**
	 * Quit server. This method will wait for the server to close the socket.
	 *
	 * @param sReason Reason for quitting.
	 */
	public void Quit(String sReason) { 
		if (sReason.equals("")) { SendString("QUIT"); }
		else { SendString("QUIT :"+sReason); }	
	}
	/**
	 * Disconnect from server. This method will quit and automatically close the
	 * socket without waiting for the server
	 *
	 * @param sReason Reason for quitting.
	 */
	public void Disconnect(String sReason) {
		Quit(sReason);
		try { socket.close(); } catch (Exception e) { /* Meh */ };
	}
	
	/**
	 * Get SVN Version information
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}

// eof