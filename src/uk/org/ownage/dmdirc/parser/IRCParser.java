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

import uk.org.ownage.dmdirc.parser.callbacks.CallbackManager;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnDataIn;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnDataOut;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnDebugInfo;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnErrorInfo;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackOnSocketClosed;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IDataIn;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IDataOut;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IDebugInfo;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IErrorInfo;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.ISocketClosed;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;

/**
 * IRC Parser.
 *
 * @author            Shane Mc Cormack
 * @version           $Id$
 */
public class IRCParser implements Runnable {
	
	/** General Debug Information. */
	public static final int DEBUG_INFO = 1;
	/** Socket Debug Information. */	
	public static final int DEBUG_SOCKET = 2;
	/** Processing Manager Debug Information. */	
	public static final int DEBUG_PROCESSOR = 4;
//	public static final int DEBUG_SOMETHING = 8; //Next thingy

	/** Socket is not created yet. */
	public static final byte STATE_NULL = 0;
	/** Socket is closed. */	
	public static final byte STATE_CLOSED = 1;
	/** Socket is Open. */	
	public static final byte STATE_OPEN = 2;
	
	/** Attempt to update user host all the time, not just on Who/Add/NickChange. */	
	protected static final boolean ALWAYS_UPDATECLIENT = true;

	/** Current Socket State */
	private byte currentSocketState = 0;
	
	/** Max length an outgoing line should be (NOT including \r\n). */
	public static final int MAX_LINELENGTH = 510;
	
	/**
	 * Get the current socket State.
	 *
	 * @return Current SocketState (STATE_NULL, STATE_CLOSED or STATE_OPEN)
	 */
	public byte getSocketState() { return currentSocketState; }
	
	/** This is the socket used for reading from/writing to the IRC server. */
	private Socket socket = null;
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
	
	/** Network name. This is "" if no network name is provided */
	protected String sNetworkName;

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
	
	/** Reference to the Processing Manager */
	private ProcessingManager myProcessingManager = new ProcessingManager(this);
	
	/**
	 * Get a reference to the Processing Manager
	 *
	 * @return Reference to the CallbackManager
	 */
	public ProcessingManager getProcessingManager() { return myProcessingManager;	}
	
	/** Reference to the callback Manager */
	private CallbackManager myCallbackManager = new CallbackManager(this);
	
	/**
	 * Get a reference to the CallbackManager
	 *
	 * @return Reference to the CallbackManager
	 */
	public CallbackManager getCallbackManager() { return myCallbackManager;	}
	
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
	 * Channel modes discovered but not listed in 005 are stored as boolean modes automatically (and a ERROR_WARNING Error is called)
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
					public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null;	}
					public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
					public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
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

	/** Ignore List */
	protected RegexStringList myIgnoreList = new RegexStringList();
	
	/**
	 * Get a reference to the ignorelist
	 *
	 * @return a reference to the ignorelist
	 */
	public RegexStringList getIgnoreList() { return myIgnoreList; }
	
	//---------------------------------------------------------------------------
	// Start Callbacks
	//---------------------------------------------------------------------------
	
	/**
	 * Callback to all objects implementing the DataIn Callback.
	 *
	 * @see IDataIn
	 * @param data Incomming Line.
	 * @return true if a method was called, false otherwise
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
	 * @return true if a method was called, false otherwise
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
	 * @param level Debugging Level (DEBUG_INFO, DEBUG_SOCKET etc)
	 * @param data Debugging Information as a format string
	 * @param args Formatting String Options
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callDebugInfo(int level, String data, Object... args) {
		return callDebugInfo(level, String.format(data, args));
	}
	/**
	 * Callback to all objects implementing the DebugInfo Callback.
	 *
	 * @see IDebugInfo
	 * @param level Debugging Level (DEBUG_INFO, DEBUG_SOCKET etc)
	 * @param data Debugging Information
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callDebugInfo(int level, String data) {
		CallbackOnDebugInfo cb = (CallbackOnDebugInfo)myCallbackManager.getCallbackType("OnDebugInfo");
		if (cb != null) { return cb.call(level, String.format(data)); }
		return false;
	}

	/**
	 * Callback to all objects implementing the IErrorInfo Interface.
	 *
	 * @see IErrorInfo
	 * @param errorInfo ParserError object representing the error.
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callErrorInfo(ParserError errorInfo) {
		CallbackOnErrorInfo cb = (CallbackOnErrorInfo)myCallbackManager.getCallbackType("OnErrorInfo");
		if (cb != null) { return cb.call(errorInfo); }
		return false;
	}

	/**
	 * Callback to all objects implementing the SocketClosed Callback.
	 *
	 * @see ISocketClosed
	 * @return true if a method was called, false otherwise
	 */	
	protected boolean callSocketClosed() {
		CallbackOnSocketClosed cb = (CallbackOnSocketClosed)myCallbackManager.getCallbackType("OnSocketClosed");
		if (cb != null) { return cb.call(); }
		return false;
	}
	
	//---------------------------------------------------------------------------
	// End Callbacks
	//---------------------------------------------------------------------------

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
		sServerName = "";
		sNetworkName = "";
	}

	
	/**
	 * Connect to IRC.
	 * 
	 * @throws IOException if the socket can not be connected
	 * @throws UnknownHostException if the hostname can not be resolved
	 * @throws NoSuchAlgorithmException if SSL is not available
	 * @throws KeyManagementException if the trustManager is invalid
	 */
	private void connect() throws IOException, UnknownHostException, NoSuchAlgorithmException, KeyManagementException {
			resetState();
			callDebugInfo(DEBUG_SOCKET, "Connecting to "+server.getHost()+":"+server.getPort());
			
			if (server.getUseSocks()) {
				callDebugInfo(DEBUG_SOCKET, "Using Proxy");
				Proxy.Type proxyType = Proxy.Type.SOCKS;
				socket = new Socket(new Proxy(proxyType, new InetSocketAddress(server.getProxyHost(), server.getProxyPort())));
				
				socket.connect(new InetSocketAddress(server.getHost(),server.getPort()));
			} else {
				callDebugInfo(DEBUG_SOCKET, "Not using Proxy");
				if (!server.getSSL()) {
					socket = new Socket(server.getHost(),server.getPort());
				}
			}
			
			if (server.getSSL()) {
				callDebugInfo(DEBUG_SOCKET, "Server is SSL.");
				
				if (myTrustManager == null) { myTrustManager = trustAllCerts; }
				
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, myTrustManager, new java.security.SecureRandom());
				
				SSLSocketFactory socketFactory = sc.getSocketFactory();
				if (server.getUseSocks()) {
					socket = socketFactory.createSocket(socket, server.getHost(), server.getPort(), false);
				} else {
					socket = socketFactory.createSocket(server.getHost(), server.getPort());
				}
			}
			
			callDebugInfo(DEBUG_SOCKET, "\t-> Opening socket output stream PrintWriter");
			out = new PrintWriter(socket.getOutputStream(), true);
			currentSocketState = STATE_OPEN;
			callDebugInfo(DEBUG_SOCKET, "\t-> Opening socket input stream BufferedReader");
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			callDebugInfo(DEBUG_SOCKET, "\t-> Socket Opened");
	}
	
	/**
	 * Send server connection strings (NICK/USER/PASS)
	 */
	private void sendConnectionStrings() {
		if (!server.getPassword().equals("")) {
			sendString("PASS "+server.getPassword());
		}
		setNickname(me.getNickname());
		sendString("USER "+me.getUsername().toLowerCase()+" * * :"+me.getRealname());
		IsFirst = false;	
	}
	
	/**
	 * Begin execution.
	 * Connect to server, and start parsing incomming lines
	 */
	public void run() {
		callDebugInfo(DEBUG_INFO, "Begin Thread Execution");
		if (HasBegan) { return; } else { HasBegan = true; }
		try {connect(); }
		catch (Exception e) {
			callDebugInfo(DEBUG_SOCKET, "Error Connecting ("+e.getMessage()+"), Aborted");
			ParserError ei = new ParserError(ParserError.ERROR_ERROR, "Error connecting to server");
			ei.setException(e);
			callErrorInfo(ei);
			return;
		}
		
		callDebugInfo(DEBUG_SOCKET, "Socket Connected");
		
//This has been removed, seems pointless
//		if (!server.waitForFirst) { sendConnectionStrings(); }
		
		// Prepare the ProcessingManager
		myProcessingManager.init();
		
		String line = "";
		while(true) {
			try {
				line = in.readLine(); // Blocking :/
				if (line == null) {
					currentSocketState = STATE_CLOSED;
					// Empty the ProcessingManager
					myProcessingManager.empty();
					callSocketClosed();
					break;
				} else {
					if (IsFirst) { sendConnectionStrings(); }
					processLine(line);
				}
			} catch (IOException e) {
				currentSocketState = STATE_CLOSED;
				// Empty the ProcessingManager
				myProcessingManager.empty();
				callSocketClosed();
				break;
			}
		}
		callDebugInfo(DEBUG_INFO, "End Thread Execution");
	}
	
	/** Close socket on destroy. */
	protected void finalize(){
		try { socket.close(); }
		catch (IOException e) { callDebugInfo(DEBUG_SOCKET, "Could not close socket"); }
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
		sWho = ClientInfo.parseHost(sWho);
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
	
	/**
	 * Send a line to the server.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 */
	public void sendLine(String line) { doSendString(line,false); }
	
	/** 
	 * Send a line to the server and add proper line ending.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 */
	protected void sendString(String line) { doSendString(line,true); }
	
	/** 
	 * Send a line to the server and add proper line ending.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 * @param fromParser is this line from the parser? (used for callDataOut)
	 */
	protected void doSendString(String line, boolean fromParser) {
		if (out == null) { return; }
		callDataOut(line,fromParser);
		out.printf("%s\r\n",line);
		String[] newLine = tokeniseLine(line);
		if (newLine[0].equalsIgnoreCase("away")) {
			if (newLine.length > 1) {
				// AWAY blah blah blah
				// 01234^-5
				cMyself.setAwayReason(line.substring(5));
			} else {
				cMyself.setAwayReason("");
			}
		}
	}
	
	/**
	 * Get the network name given in 005.
	 *
	 * @return network name from 005
	 */
	public String getNetworkName() {
		return sNetworkName;
	}
	
	/**
	 * Get the server name given in 001.
	 *
	 * @return server name from 001
	 */
	public String getServerName() {
		return sServerName;
	}
	
	/**
	 * Process a line and call relevent methods for handling.
	 *
	 * @param line IRC Line to process
	 */
	private void processLine(String line) {
		String[] token = tokeniseLine(line);
		
		int nParam;
		callDataIn(line);
		String sParam = token[1];
		
		try {
			if (token[0].equalsIgnoreCase("PING") || token[1].equalsIgnoreCase("PING")) { sendString("PONG :"+sParam); }
			else {
				if (!Got001) {
					// Before 001 we don't care about much.
					try { nParam = Integer.parseInt(token[1]); } catch (Exception e) { nParam = -1; }
					switch (nParam) {
						case 1: // 001 - Welcome to IRC
						case 464: // Password Required
						case 433: // Nick In Use
							try { myProcessingManager.process(sParam, token); } catch (Exception e) { }
							break;
						default: // Unknown - Send to Notice Auth
							try { myProcessingManager.process("Notice Auth", token); } catch (Exception e) { }
							break;
					}
				} else {
					// After 001 we potentially care about everything!
					try { myProcessingManager.process(sParam, token); }
					catch (Exception e) { /* No Processor found */  }
				}
			}
		} catch (Exception e) {
			ParserError ei = new ParserError(ParserError.ERROR_FATAL, "Exception in Parser. {"+line+"}");
			ei.setException(e);
			callErrorInfo(ei);
		}
	}
	
	/**
	 * Get the known boolean chanmodes in 005 order.
	 * Modes are returned in the order that the ircd specifies the modes in 005
	 * with any newly-found modes (mode being set that wasn't specified in 005)
	 * being added at the end.
	 *
	 * @return All the currently known boolean modes
	 */	
	public String getBoolChanModes005() {
		// This code isn't the nicest, as Hashtable's don't lend themselves to being
		// ordered. 
		// Order isn't really important, and this code only takes 3 lines of we
		// don't care about it but ordered guarentees that on a specific ircd this
		// method will ALWAYs return the same value.
		char[] modes = new char[hChanModesBool.size()];
		Character cTemp;
		Integer nTemp;
		
		for (Enumeration e = hChanModesBool.keys(); e.hasMoreElements();) {
			cTemp = (Character)e.nextElement();
			nTemp = hChanModesBool.get(cTemp);
			// Is there an easier way to find out the power of 2 value for a number?
			// ie 1024 = 10, 512 = 9 ?
			for (int i = 0; i < modes.length; i++) {
				if (Math.pow(2, i) == (double)nTemp) {
					modes[i] = cTemp;
					break;
				}
			}
		}
		return new String(modes);
	}
	
	/**
	 * Process CHANMODES from 005.
	 */	
	protected void parseChanModes() {
		StringBuilder sDefaultModes = new StringBuilder("b,k,l,");
		String[] Bits = null;
		String ModeStr;
		if (h005Info.containsKey("USERCHANMODES")) {
			if (getIRCD(true).equalsIgnoreCase("dancer")) { sDefaultModes.insert(0, "dqeI"); }
			else if (getIRCD(true).equalsIgnoreCase("austirc")) { sDefaultModes.insert(0, "e"); }
			ModeStr = h005Info.get("USERCHANMODES");
			char mode;
			for (int i = 0; i < ModeStr.length(); ++i) {
				mode = ModeStr.charAt(i);
				if (!hPrefixModes.containsKey(mode)) {
					if (sDefaultModes.indexOf(Character.toString(mode)) < 0) {
						sDefaultModes.append(mode);
					}
				}
			}
		} else {
			sDefaultModes.append("imnpstrc");
		}
		if (h005Info.containsKey("CHANMODES")) { ModeStr = h005Info.get("CHANMODES");	}
		else { ModeStr = sDefaultModes.toString(); h005Info.put("CHANMODES",ModeStr); }
		Bits = ModeStr.split(",",5);
		if (Bits.length < 4) {
			ModeStr = sDefaultModes.toString();
			callErrorInfo(new ParserError(ParserError.ERROR_ERROR, "CHANMODES String not valid. Using default string of \""+ModeStr+"\""));
			h005Info.put("CHANMODES",ModeStr);
			Bits = ModeStr.split(",",5);
		}
		
		// resetState
		hChanModesOther.clear();
		hChanModesBool.clear();
		nNextKeyCMBool = 1;
		
		// List modes.
		for (int i = 0; i < Bits[0].length(); ++i) {
			Character cMode = Bits[0].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found List Mode: %c",cMode);
			if (!hChanModesOther.containsKey(cMode)) { hChanModesOther.put(cMode,cmList); }
		}
		
		// Param for Set and Unset.
		Byte nBoth = (cmSet+cmUnset);
		for (int i = 0; i < Bits[1].length(); ++i) {
			Character cMode = Bits[1].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Set/Unset Mode: %c",cMode);
			if (!hChanModesOther.containsKey(cMode)) { hChanModesOther.put(cMode,nBoth); }
		}
		
		// Param just for Set
		for (int i = 0; i < Bits[2].length(); ++i) {
			Character cMode = Bits[2].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Set Only Mode: %c",cMode);
			if (!hChanModesOther.containsKey(cMode)) { hChanModesOther.put(cMode,cmSet); }
		}
		
		// Boolean Mode
		for (int i = 0; i < Bits[3].length(); ++i) {
			Character cMode = Bits[3].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Boolean Mode: %c [%d]",cMode,nNextKeyCMBool);
			if (!hChanModesBool.containsKey(cMode)) {
				hChanModesBool.put(cMode,nNextKeyCMBool);
				nNextKeyCMBool = nNextKeyCMBool*2;
			}
		}
	}
	
	/**
	 * Get the known boolean chanmodes in alphabetical order.
	 * Modes are returned in alphabetic order
	 *
	 * @return All the currently known boolean modes
	 */	
	public String getBoolChanModes() {
		char[] modes = new char[hChanModesBool.size()];
		int i = 0;
		for (Enumeration e = hChanModesBool.keys(); e.hasMoreElements();) {
			modes[i++] = (Character)e.nextElement();
		}
		// Alphabetically sort the array
		Arrays.sort(modes);
		return new String(modes);
	}
	
	/**
	 * Get the known List chanmodes.
	 * Modes are returned in alphabetical order
	 *
	 * @return All the currently known List modes
	 */	
	public String getListChanModes() {
		return getOtherModeString(cmList);
	}
	
	/**
	 * Get the known Set-Only chanmodes.
	 * Modes are returned in alphabetical order
	 *
	 * @return All the currently known Set-Only modes
	 */	
	public String getSetOnlyChanModes() {
		return getOtherModeString(cmSet);
	}
	
	/**
	 * Get the known Set-Unset chanmodes.
	 * Modes are returned in alphabetical order
	 *
	 * @return All the currently known Set-Unset modes
	 */	
	public String getSetUnsetChanModes() {
		return getOtherModeString((byte)(cmSet+cmUnset));
	}

	/**
	 * Get modes from hChanModesOther that have a specific value.
	 * Modes are returned in alphabetical order
	 *
	 * @param nValue Value mode must have to be included
	 * @return All the currently known Set-Unset modes
	 */	
	protected String getOtherModeString(byte nValue) {
		char[] modes = new char[hChanModesOther.size()];
		Character cTemp;
		Byte nTemp;
		int i = 0;
		for (Enumeration e = hChanModesOther.keys(); e.hasMoreElements();) {
			cTemp = (Character)e.nextElement();
			nTemp = hChanModesOther.get(cTemp);
			if (nTemp == nValue) { modes[i++] = cTemp; }
		}
		// Alphabetically sort the array
		Arrays.sort(modes);
		return (new String(modes)).trim();
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
			callDebugInfo(DEBUG_INFO, "Found User Mode: %c [%d]",cMode,nNextKeyUser);
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
			callDebugInfo(DEBUG_INFO, "Found Chan Prefix: %c",cMode);
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
			callErrorInfo(new ParserError(ParserError.ERROR_ERROR, "PREFIX String not valid. Using default string of \""+ModeStr+"\""));
			h005Info.put("PREFIX",ModeStr);
			ModeStr = ModeStr.substring(1);
			Bits = ModeStr.split("\\)",2);
		}

		// resetState
		hPrefixModes.clear();
		hPrefixMap.clear();
		nNextKeyPrefix = 1;

		for (int i = Bits[0].length()-1; i > -1; --i) {
			Character cMode = Bits[0].charAt(i);
			Character cPrefix = Bits[1].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Prefix Mode: %c => %c [%d]",cMode,cPrefix,nNextKeyPrefix);
			if (!hPrefixModes.containsKey(cMode)) {
				hPrefixModes.put(cMode,nNextKeyPrefix);
				hPrefixMap.put(cMode,cPrefix);
				hPrefixMap.put(cPrefix,cMode);
				nNextKeyPrefix = nNextKeyPrefix*2;
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
		if (getChannelInfo(sChannelName) == null) { return; }
		if (sReason.equals("")) { sendString("PART "+sChannelName); }
		else { sendString("PART "+sChannelName+" :"+sReason); }
	}	
	
	/**
	 * Set Nickname.
	 *
	 * @param sNewNickName New nickname wanted.
	 */
	public void setNickname(String sNewNickName) {
		if (this.getSocketState() != STATE_OPEN) {
			me.setNickname(sNewNickName);
		} else {
			if (cMyself != null) {
				if (cMyself.getNickname().equals(sNewNickName)) { return; }
			}
			sendString("NICK "+sNewNickName);
		}
		sThinkNickname = sNewNickName;
	}
	
	/**
	 * Get the max length a message can be.
	 *
	 * @param sType Type of message (ie PRIVMSG)
	 * @param sTarget Target for message (eg #DMDirc)
	 * @return Max Length message should be.
	 */
	public int getMaxLength(String sType, String sTarget) { 
		// If my host is "nick!user@host" and we are sending "#Channel"
		// a "PRIVMSG" this will find the length of ":nick!user@host PRIVMSG #channel :"
		// and subtract it from the MAX_LINELENGTH. This should be sufficient in most cases.
		// Lint = the 2 ":" at the start and end and the 3 separating " "s
		final int LINE_LINT = 5;
		return MAX_LINELENGTH - getMyself().toString().length() - sType.length() - sTarget.length() - LINE_LINT;
	}
	
	/**
	 * Send a private message to a target.
	 *
	 * @param sTarget Target
	 * @param sMessage Message to send
	 */
	public void sendMessage(final String sTarget, final String sMessage) { 
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
	 * Send a Action to a target.
	 *
	 * @param sTarget Target
	 * @param sMessage Action to send
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
	 * Quit IRC.
	 * This method will wait for the server to close the socket.
	 *
	 * @param sReason Reason for quitting.
	 */
	public void quit(String sReason) { 
		if (sReason.equals("")) { sendString("QUIT"); }
		else { sendString("QUIT :"+sReason); }	
	}
	/**
	 * Disconnect from server.
	 * This method will quit and automatically close the socket without waiting for
	 * the server.
	 *
	 * @param sReason Reason for quitting.
	 */
	public void disconnect(String sReason) {
		quit(sReason);
		try { socket.close(); } catch (Exception e) { };
	}
	
	/**
	 * Check if a channel name is valid.
	 *
	 * @param sChannelName Channel name to test
	 * @return true if name is valid on the current connection, false otherwise. (Always false before noMOTD/MOTDEnd)
	 */
	public boolean isValidChannelName(String sChannelName) {
		return hChanPrefix.containsKey(sChannelName.charAt(0));
	}
	
	/**
	 * Check if a given chanmode is user settable
	 *
	 * @param mode Mode to test
	 * @return true if mode is settable by users, false if servers only
	 */
	public boolean isUserSettable(final Character mode) {
		String validmodes;
		if (h005Info.containsKey("USERCHANMODES")) { validmodes = h005Info.get("USERCHANMODES"); }
		else { validmodes = "bklimnpstrc"; }
		return validmodes.matches(".*"+mode+".*");
	}
	
	/**
	 * Get the 005 info
	 *
	 * @return 005Info hashtable.
	 */
	public Hashtable<String,String> get005() { return h005Info; }
	
	/**
	 * Get the name of the ircd
	 *
	 * @param getType if this is false the string frmo 004 is returned. Else a guess of the type (ircu, hybrid, ircnet)
	 * @return IRCD Version or Type
	 */ 
	public String getIRCD(final boolean getType) {
		if (h005Info.containsKey("004IRCD")) {
			String version = h005Info.get("004IRCD");
			if (getType) {
				if (version.matches("(?i).*asuka.*")) { return "asuka"; }
				else if (version.matches("(?i).*hyperion.*")) { return "hyperion"; }
				else if (version.matches("(?i).*dancer.*")) { return "dancer"; }
				else if (version.matches("(?i).*austhex.*")) { return "austhex"; }
				else if (version.matches("(?i).*austirc.*")) { return "austirc"; }
				else if (version.matches("(?i).*ratbox.*")) { return "ratbox"; }
				else if (version.matches("(?i).*ircd.hybrid.*")) { return "hybrid7"; }
				else if (version.matches("(?i).*hybrid.*")) { return "hybrid"; }
				else if (version.matches("(?i).*beware.*")) { return "bircd"; }
				else if (version.matches("(?i).*ircu.*")) { return "ircu"; }
				else if (version.matches("(?i).*unreal.*")) { return "unreal"; }
				else {
					// Stupid networks go here...
					if (sNetworkName.equalsIgnoreCase("ircnet")) { return "ircnet"; }
					else { return "generic"; }
				}
			} else { return version; }
		} else {
			if (getType) { return "generic"; }
			else { return ""; }
		}
	}
	
	/**
	 * Get a reference to the cMyself object.
	 *
	 * @return cMyself reference
	 */
	public ClientInfo getMyself() { return cMyself; }
	
	/**
	 * Get SVN Version information.
	 *
	 * @return SVN Version String
	 */
	public static String getSvnInfo () { return "$Id$"; }	
}
