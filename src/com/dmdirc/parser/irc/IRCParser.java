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

import com.dmdirc.parser.irc.callbacks.CallbackManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.Queue;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * IRC Parser.
 *
 * @author Shane Mc Cormack
 */
public class IRCParser implements Runnable {

	/** Max length an outgoing line should be (NOT including \r\n). */
	public static final int MAX_LINELENGTH = 510;

	/** General Debug Information. */
	public static final int DEBUG_INFO = 1;
	/** Socket Debug Information. */
	public static final int DEBUG_SOCKET = 2;
	/** Processing Manager Debug Information. */
	public static final int DEBUG_PROCESSOR = 4;
	/** List Mode Queue Debug Information. */
	public static final int DEBUG_LMQ = 8;
//	public static final int DEBUG_SOMETHING = 16; //Next thingy

	/** Attempt to update user host all the time, not just on Who/Add/NickChange. */
	static final boolean ALWAYS_UPDATECLIENT = true;

	/** Byte used to show that a non-boolean mode is a list (b). */
	static final byte MODE_LIST = 1;
	/** Byte used to show that a non-boolean mode is not a list, and requires a parameter to set (lk). */
	static final byte MODE_SET = 2;
	/** Byte used to show that a non-boolean mode is not a list, and requires a parameter to unset (k). */
	static final byte MODE_UNSET = 4;

	/**
	 * This is what the user wants settings to be.
	 * Nickname here is *not* always accurate.<br><br>
	 * ClientInfo variable tParser.getMyself() should be used for accurate info.
	 */
	public MyInfo me = new MyInfo();
	/**	Server Info requested by user. */
	public ServerInfo server = new ServerInfo();

	/** Timer for server ping. */
	private Timer pingTimer = null;
	/** Length of time to wait between ping stuff. */
	private long pingTimerLength = 10000;
	/** Is a ping needed? */
	private volatile AtomicBoolean pingNeeded = new AtomicBoolean(false);
	/** Time last ping was sent at. */
	private long pingTime;
	/** Current Server Lag. */
	private long serverLag;
	/** Last value sent as a ping argument. */
	private String lastPingValue = "";

	/**
	 * Count down to next ping.
	 * The timer fires every 10 seconds, this value is decreased every time the
	 * timer fires.<br>
	 * Once it reaches 0, we send a ping, and reset it to 6, this means we ping
	 * the server every minute.
	 *
	 * @see setPingCountDownLength
	 */
	private byte pingCountDown;
	/**
	 * Amount of times the timer has to fire for inactivity before sending a ping.
	 *
	 * @see setPingCountDownLength
	 */
	private byte pingCountDownLength = 6;

	/** Name the server calls itself. */
	String sServerName;

	/** Network name. This is "" if no network name is provided */
	String sNetworkName;

	/** This is what we think the nickname should be. */
	String sThinkNickname;

	/** When using inbuilt pre-001 NickInUse handler, have we tried our AltNick. */
	boolean triedAlt;

	/** Have we recieved the 001. */
	boolean got001;
	/** Have we fired post005? */
	boolean post005;
	/** Has the thread started execution yet, (Prevents run() being called multiple times). */
	boolean hasBegan;

	/** Hashtable storing known prefix modes (ohv). */
	final Map<Character, Long> hPrefixModes = new Hashtable<Character, Long>();
	/**
	 * Hashtable maping known prefix modes (ohv) to prefixes (@%+) - Both ways.
	 * Prefix map contains 2 pairs for each mode. (eg @ => o and o => @)
	 */
	final Map<Character, Character> hPrefixMap = new Hashtable<Character, Character>();
	/** Integer representing the next avaliable integer value of a prefix mode. */
	long nNextKeyPrefix = 1;
	/** Hashtable storing known user modes (owxis etc). */
	final Map<Character, Long> hUserModes = new Hashtable<Character, Long>();
	/** Integer representing the next avaliable integer value of a User mode. */
	long nNextKeyUser = 1;
	/**
	 * Hashtable storing known boolean chan modes (cntmi etc).
	 * Valid Boolean Modes are stored as Hashtable.pub('m',1); where 'm' is the mode and 1 is a numeric value.<br><br>
	 * Numeric values are powers of 2. This allows up to 32 modes at present (expandable to 64)<br><br>
	 * ChannelInfo/ChannelClientInfo etc provide methods to view the modes in a human way.<br><br>
	 * <br>
	 * Channel modes discovered but not listed in 005 are stored as boolean modes automatically (and a ERROR_WARNING Error is called)
	 */
	final Map<Character, Long> hChanModesBool = new Hashtable<Character, Long>();
	/** Integer representing the next avaliable integer value of a Boolean mode. */

	long nNextKeyCMBool = 1;

	/**
	 * Hashtable storing known non-boolean chan modes (klbeI etc).
	 * Non Boolean Modes (for Channels) are stored together in this hashtable, the value param
	 * is used to show the type of variable. (List (1), Param just for set (2), Param for Set and Unset (2+4=6))<br><br>
	 * <br>
	 * see MODE_LIST<br>
	 * see MODE_SET<br>
	 * see MODE_UNSET<br>
	 */
	final Map<Character, Byte> hChanModesOther = new Hashtable<Character, Byte>();

	/** The last line of input recieved from the server */
	String lastLine = "";
	/** Should the lastline (where given) be appended to the "data" part of any onErrorInfo call? */
	boolean addLastLine = false;

	/**
	* Channel Prefixes (ie # + etc).
	* The "value" for these is always true.
	*/
	final Map<Character, Boolean> hChanPrefix = new Hashtable<Character, Boolean>();
	/** Hashtable storing all known clients based on nickname (in lowercase). */
	private final Map<String, ClientInfo> hClientList = new Hashtable<String, ClientInfo>();
	/** Hashtable storing all known channels based on chanel name (inc prefix - in lowercase). */
	private final Map<String, ChannelInfo> hChannelList = new Hashtable<String, ChannelInfo>();
	/** Reference to the ClientInfo object that references ourself. */
	private ClientInfo cMyself = new ClientInfo(this, "myself").setFake(true);
	/** Hashtable storing all information gathered from 005. */
	final Map<String, String> h005Info = new Hashtable<String, String>();

	/** Ignore List. */
	RegexStringList myIgnoreList = new RegexStringList();

	/** Reference to the callback Manager. */
	CallbackManager myCallbackManager = new CallbackManager(this);
	/** Reference to the Processing Manager. */
	ProcessingManager myProcessingManager = new ProcessingManager(this);

	/** Should we automatically disconnect on fatal errors?. */
	private boolean disconnectOnFatal = true;

	/** Current Socket State. */
	protected SocketState currentSocketState = SocketState.NULL;

	/** This is the socket used for reading from/writing to the IRC server. */
	private Socket socket;
	/** Used for writing to the server. */
	private PrintWriter out;
	/** Used for reading from the server. */
	private BufferedReader in;

	/** This is the default TrustManager for SSL Sockets, it trusts all ssl certs. */
	private final TrustManager[] trustAllCerts = {
		new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() { return null; }
			@Override
			public void checkClientTrusted(final X509Certificate[] certs, final String authType) { }
			@Override
			public void checkServerTrusted(final X509Certificate[] certs, final String authType) { }
		},
	};

	/** Should fake (channel)clients be created for callbacks where they do not exist? */
	boolean createFake = false;

	/** Should channels automatically request list modes? */
	boolean autoListMode = true;

	/** Should part/quit/kick callbacks be fired before removing the user internally? */
	boolean removeAfterCallback = true;

	/** This is the TrustManager used for SSL Sockets. */
	private TrustManager[] myTrustManager = trustAllCerts;

	/** The KeyManagers used for client certificates for SSL sockets. */
	private KeyManager[] myKeyManagers;

	/** This is the IP we want to bind to. */
	private String bindIP = "";

	/**
	 * Default constructor, ServerInfo and MyInfo need to be added separately (using IRC.me and IRC.server).
	 */
	public IRCParser() { this(null, null); }
	/**
	 * Constructor with ServerInfo, MyInfo needs to be added separately (using IRC.me).
	 *
	 * @param serverDetails Server information.
	 */
	public IRCParser(final ServerInfo serverDetails) { this(null, serverDetails); }
	/**
	 * Constructor with MyInfo, ServerInfo needs to be added separately (using IRC.server).
	 *
	 * @param myDetails Client information.
	 */
	public IRCParser(final MyInfo myDetails) { this(myDetails, null); }
	/**
	 * Constructor with ServerInfo and MyInfo.
	 *
	 * @param serverDetails Server information.
	 * @param myDetails Client information.
	 */
	public IRCParser(final MyInfo myDetails, final ServerInfo serverDetails) {
		if (myDetails != null) { this.me = myDetails; }
		if (serverDetails != null) { this.server = serverDetails; }
		resetState();
	}

	/**
	 * Get the current Value of bindIP.
	 *
	 * @return Value of bindIP ("" for default IP)
	 */
	public String getBindIP() { return bindIP; }

	/**
	 * Set the current Value of bindIP.
	 *
	 * @param newValue New value to set bindIP
	 */
	public void setBindIP(final String newValue) { bindIP = newValue; }

	/**
	 * Get the current Value of createFake.
	 *
	 * @return Value of createFake (true if fake clients will be added for callbacks, else false)
	 */
	public boolean getCreateFake() { return createFake; }

	/**
	 * Set the current Value of createFake.
	 *
	 * @param newValue New value to set createFake
	 */
	public void setCreateFake(final boolean newValue) { createFake = newValue; }

	/**
	 * Get the current Value of autoListMode.
	 *
	 * @return Value of autoListMode (true if channels automatically ask for list modes on join, else false)
	 */
	public boolean getAutoListMode() { return autoListMode; }

	/**
	 * Set the current Value of autoListMode.
	 *
	 * @param newValue New value to set autoListMode
	 */
	public void setAutoListMode(final boolean newValue) { autoListMode = newValue; }

	/**
	 * Get the current Value of removeAfterCallback.
	 *
	 * @return Value of removeAfterCallback (true if kick/part/quit callbacks are fired before internal removal)
	 */
	public boolean getRemoveAfterCallback() { return removeAfterCallback; }

	/**
	 * Get the current Value of removeAfterCallback.
	 *
	 * @param newValue New value to set removeAfterCallback
	 */
	public void setRemoveAfterCallback(final boolean newValue) { removeAfterCallback = newValue; }

	/**
	 * Get the current Value of addLastLine.
	 *
	 * @return Value of addLastLine (true if lastLine info will be automatically
	 *         added to the errorInfo data line). This should be true if lastLine
	 *         isn't handled any other way.
	 */
	public boolean getAddLastLine() { return addLastLine; }

	/**
	 * Get the current Value of addLastLine.
	 * This returns "this" and thus can be used in the construction line.
	 *
	 * @param newValue New value to set addLastLine
	 */
	public void setAddLastLine(final boolean newValue) { addLastLine = newValue; }


	/**
	 * Get the current socket State.
	 *
     * @since 0.6.3
	 * @return Current {@link SocketState}
	 */
	public SocketState getSocketState() { return currentSocketState; }

	/**
	 * Get a reference to the Processing Manager.
	 *
	 * @return Reference to the CallbackManager
	 */
	public ProcessingManager getProcessingManager() { return myProcessingManager;	}

	/**
	 * Get a reference to the CallbackManager.
	 *
	 * @return Reference to the CallbackManager
	 */
	public CallbackManager getCallbackManager() { return myCallbackManager;	}

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
	public void setTrustManager(final TrustManager[] newTrustManager) { myTrustManager = newTrustManager; }

	/**
	 * Replace the current KeyManagers for SSL Sockets with a new set.
	 *
	 * @param newKeyManagers Replacement KeyManagers for SSL Sockets.
	 */
	public void setKeyManagers(final KeyManager[] newKeyManagers) { myKeyManagers = newKeyManagers; }

	/**
	 * Get a reference to the ignorelist.
	 *
	 * @return a reference to the ignorelist
	 */
	public RegexStringList getIgnoreList() { return myIgnoreList; }

	/**
	 * Replaces the current ignorelist with a new one.
	 *
	 * @param ignoreList Replacement ignorelist
	 */
	public void setIgnoreList(final RegexStringList ignoreList) { myIgnoreList = ignoreList; }

	//---------------------------------------------------------------------------
	// Start Callbacks
	//---------------------------------------------------------------------------

	/**
	 * Callback to all objects implementing the ServerError Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IServerError
	 * @param message The error message
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callServerError(final String message) {
		return myCallbackManager.getCallbackType("OnServerError").call(message);
	}

	/**
	 * Callback to all objects implementing the DataIn Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IDataIn
	 * @param data Incomming Line.
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callDataIn(final String data) {
		return myCallbackManager.getCallbackType("OnDataIn").call(data);
	}

	/**
	 * Callback to all objects implementing the DataOut Callback.
	 *
	 * @param data Outgoing Data
	 * @param fromParser True if parser sent the data, false if sent using .sendLine
	 * @return true if a method was called, false otherwise
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IDataOut
	 */
	protected boolean callDataOut(final String data, final boolean fromParser) {
		return myCallbackManager.getCallbackType("OnDataOut").call(data, fromParser);
	}

	/**
	 * Callback to all objects implementing the DebugInfo Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IDebugInfo
	 * @param level Debugging Level (DEBUG_INFO, DEBUG_SOCKET etc)
	 * @param data Debugging Information as a format string
	 * @param args Formatting String Options
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callDebugInfo(final int level, final String data, final Object... args) {
		return callDebugInfo(level, String.format(data, args));
	}
	/**
	 * Callback to all objects implementing the DebugInfo Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IDebugInfo
	 * @param level Debugging Level (DEBUG_INFO, DEBUG_SOCKET etc)
	 * @param data Debugging Information
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callDebugInfo(final int level, final String data) {
		return myCallbackManager.getCallbackType("OnDebugInfo").call(level, data);
	}

	/**
	 * Callback to all objects implementing the IErrorInfo Interface.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IErrorInfo
	 * @param errorInfo ParserError object representing the error.
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callErrorInfo(final ParserError errorInfo) {
		return myCallbackManager.getCallbackType("OnErrorInfo").call(errorInfo);
	}

	/**
	 * Callback to all objects implementing the IConnectError Interface.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IConnectError
	 * @param errorInfo ParserError object representing the error.
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callConnectError(final ParserError errorInfo) {
		return myCallbackManager.getCallbackType("OnConnectError").call(errorInfo);
	}

	/**
	 * Callback to all objects implementing the SocketClosed Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.ISocketClosed
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callSocketClosed() {
		return myCallbackManager.getCallbackType("OnSocketClosed").call();
	}

	/**
	 * Callback to all objects implementing the PingFailed Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IPingFailed
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callPingFailed() {
		return myCallbackManager.getCallbackType("OnPingFailed").call();
	}

	/**
	 * Callback to all objects implementing the PingSent Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IPingSent
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callPingSent() {
		return myCallbackManager.getCallbackType("OnPingSent").call();
	}

	/**
	 * Callback to all objects implementing the PingSuccess Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IPingSuccess
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callPingSuccess() {
		return myCallbackManager.getCallbackType("OnPingSuccess").call();
	}

	/**
	 * Callback to all objects implementing the Post005 Callback.
	 *
	 * @return true if any callbacks were called.
	 * @see IPost005
	 */
	protected synchronized boolean callPost005() {
		if (post005) { return false; }
		post005 = true;

		return getCallbackManager().getCallbackType("OnPost005").call();
	}

	//---------------------------------------------------------------------------
	// End Callbacks
	//---------------------------------------------------------------------------

	/** Reset internal state (use before connect). */
	private void resetState() {
		// Reset General State info
		triedAlt = false;
		got001 = false;
		post005 = false;
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
		lastLine = "";
		cMyself = new ClientInfo(this, "myself").setFake(true);
		if (pingTimer != null) {
			pingTimer.cancel();
			pingTimer = null;
		}
		currentSocketState = SocketState.CLOSED;
		// Char Mapping
		updateCharArrays((byte)4);
	}

	/**
	 * Called after other error callbacks.
	 * CallbackOnErrorInfo automatically calls this *AFTER* any registered callbacks
	 * for it are called.
	 *
	 * @param errorInfo ParserError object representing the error.
	 * @param called True/False depending on the the success of other callbacks.
	 */
	public void onPostErrorInfo(final ParserError errorInfo, final boolean called) {
		if (errorInfo.isFatal() && disconnectOnFatal) {
			disconnect("Fatal Parser Error");
		}
	}

	/**
	 * Get the current Value of disconnectOnFatal.
	 *
	 * @return Value of disconnectOnFatal (true if the parser automatically disconnects on fatal errors, else false)
	 */
	public boolean getDisconnectOnFatal() { return disconnectOnFatal; }

	/**
	 * Set the current Value of disconnectOnFatal.
	 *
	 * @param newValue New value to set disconnectOnFatal
	 */
	public void setDisconnectOnFatal(final boolean newValue) { disconnectOnFatal = newValue; }

	/**
	 * Connect to IRC.
	 *
	 * @throws IOException if the socket can not be connected
	 * @throws UnknownHostException if the hostname can not be resolved
	 * @throws NoSuchAlgorithmException if SSL is not available
	 * @throws KeyManagementException if the trustManager is invalid
	 */
	private void connect() throws UnknownHostException, IOException, NoSuchAlgorithmException, KeyManagementException {
		resetState();
		callDebugInfo(DEBUG_SOCKET, "Connecting to " + server.getHost() + ":" + server.getPort());

		if (server.getPort() > 65535 || server.getPort() <= 0) {
			throw new IOException("Server port ("+server.getPort()+") is invalid.");
		}

		if (server.getUseSocks()) {
			callDebugInfo(DEBUG_SOCKET, "Using Proxy");
			if (bindIP != null && !bindIP.isEmpty()) {
				callDebugInfo(DEBUG_SOCKET, "IP Binding is not possible when using a proxy.");
			}
			if (server.getProxyPort() > 65535 || server.getProxyPort() <= 0) {
				throw new IOException("Proxy port ("+server.getProxyPort()+") is invalid.");
			}

			final Proxy.Type proxyType = Proxy.Type.SOCKS;
			socket = new Socket(new Proxy(proxyType, new InetSocketAddress(server.getProxyHost(), server.getProxyPort())));
			currentSocketState = SocketState.OPEN;
			if (server.getProxyUser() != null && !server.getProxyUser().isEmpty()) {
				IRCAuthenticator.getIRCAuthenticator().addAuthentication(server);
			}
			socket.connect(new InetSocketAddress(server.getHost(), server.getPort()));
		} else {
			callDebugInfo(DEBUG_SOCKET, "Not using Proxy");
			if (!server.getSSL()) {
				socket = new Socket();

				if (bindIP != null && !bindIP.isEmpty()) {
					callDebugInfo(DEBUG_SOCKET, "Binding to IP: "+bindIP);
					try {
						socket.bind(new InetSocketAddress(InetAddress.getByName(bindIP), 0));
					} catch (IOException e) {
						callDebugInfo(DEBUG_SOCKET, "Binding failed: "+e.getMessage());
					}
				}

				currentSocketState = SocketState.OPEN;
				socket.connect(new InetSocketAddress(server.getHost(), server.getPort()));
			}
		}

		if (server.getSSL()) {
			callDebugInfo(DEBUG_SOCKET, "Server is SSL.");

			if (myTrustManager == null) { myTrustManager = trustAllCerts; }

			final SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(myKeyManagers, myTrustManager, new java.security.SecureRandom());

			final SSLSocketFactory socketFactory = sc.getSocketFactory();
			if (server.getUseSocks()) {
				socket = socketFactory.createSocket(socket, server.getHost(), server.getPort(), false);
			} else {
				if (bindIP == null || bindIP.isEmpty()) {
					socket = socketFactory.createSocket(server.getHost(), server.getPort());
				} else {
					callDebugInfo(DEBUG_SOCKET, "Binding to IP: "+bindIP);
					try {
						socket = socketFactory.createSocket(server.getHost(), server.getPort(), InetAddress.getByName(bindIP), 0);
					} catch (UnknownHostException e) {
						callDebugInfo(DEBUG_SOCKET, "Bind failed: "+e.getMessage());
						socket = socketFactory.createSocket(server.getHost(), server.getPort());
					}
				}
			}

			currentSocketState = SocketState.OPEN;
		}

		callDebugInfo(DEBUG_SOCKET, "\t-> Opening socket output stream PrintWriter");
		out = new PrintWriter(socket.getOutputStream(), true);
		callDebugInfo(DEBUG_SOCKET, "\t-> Opening socket input stream BufferedReader");
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		callDebugInfo(DEBUG_SOCKET, "\t-> Socket Opened");
	}

	/**
	 * Send server connection strings (NICK/USER/PASS).
	 */
	protected void sendConnectionStrings() {
		if (!server.getPassword().isEmpty()) {
			sendString("PASS " + server.getPassword());
		}
		setNickname(me.getNickname());
		String localhost;
		try {
			localhost = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException uhe) {
			localhost = "*";
		}
		sendString("USER " + me.getUsername() + " "+localhost+" "+server.getHost()+" :" + me.getRealname());
	}

	/**
	 * Handle an onConnect error.
	 *
	 * @param e Exception to handle
	 */
	private void handleConnectException(final Exception e) {
		callDebugInfo(DEBUG_SOCKET, "Error Connecting (" + e.getMessage() + "), Aborted");
		final ParserError ei = new ParserError(ParserError.ERROR_ERROR, "Exception with server socket", getLastLine());
		ei.setException(e);
		callConnectError(ei);
		
		if (currentSocketState != SocketState.CLOSED) {
			currentSocketState = SocketState.CLOSED;
			callSocketClosed();
		}
		resetState();
	}

	/**
	 * Begin execution.
	 * Connect to server, and start parsing incomming lines
	 */
	@Override
	public void run() {
		callDebugInfo(DEBUG_INFO, "Begin Thread Execution");
		if (hasBegan) { return; } else { hasBegan = true; }
		try {
		 connect();
		} catch (UnknownHostException e) {
			handleConnectException(e);
			return;
		} catch (IOException e) {
			handleConnectException(e);
			return;
		} catch (NoSuchAlgorithmException e) {
			handleConnectException(e);
			return;
		} catch (KeyManagementException e) {
			handleConnectException(e);
			return;
		}

		callDebugInfo(DEBUG_SOCKET, "Socket Connected");

		sendConnectionStrings();

		while (true) {
			try {
				lastLine = in.readLine(); // Blocking :/
				if (lastLine == null) {
					if (currentSocketState != SocketState.CLOSED) {
						currentSocketState = SocketState.CLOSED;
						callSocketClosed();
					}
					resetState();
					break;
				} else {
					processLine(lastLine);
				}
			} catch (IOException e) {
				callDebugInfo(DEBUG_SOCKET, "Exception in main loop (" + e.getMessage() + "), Aborted");
				
				if (currentSocketState != SocketState.CLOSED) {
					currentSocketState = SocketState.CLOSED;
					callSocketClosed();
				}
				resetState();
				break;
			}
		}
		callDebugInfo(DEBUG_INFO, "End Thread Execution");
	}

	/**
	 * Get the current local port number.
	 *
	 * @return 0 if not connected, else the current local port number
	 */
	public int getLocalPort() {
		if (currentSocketState == SocketState.OPEN) {
			return socket.getLocalPort();
		} else {
			return 0;
		}
	}

	/** Close socket on destroy. */
	@Override
	protected void finalize() throws Throwable {
		try { socket.close(); }
		catch (IOException e) {
			callDebugInfo(DEBUG_SOCKET, "Could not close socket");
		}
		super.finalize();
	}

	/**
	 * Get the trailing parameter for a line.
	 * The parameter is everything after the first occurance of " :" ot the last token in the line after a space.
	 *
	 * @param line Line to get parameter for
	 * @return Parameter of the line
	 */
	public static String getParam(final String line) {
		String[] params = null;
		params = line.split(" :", 2);
		return params[params.length - 1];
	}

	/**
	 * Tokenise a line.
	 * splits by " " up to the first " :" everything after this is a single token
	 *
	 * @param line Line to tokenise
	 * @return Array of tokens
	 */
	public static String[] tokeniseLine(final String line) {
		if (line == null) {
			return new String[]{"", }; // Return empty string[]
		}

		final int lastarg = line.indexOf(" :");
		String[] tokens;

		if (lastarg > -1) {
			final String[] temp = line.substring(0, lastarg).split(" ");
			tokens = new String[temp.length + 1];
			System.arraycopy(temp, 0, tokens, 0, temp.length);
			tokens[temp.length] = line.substring(lastarg + 2);
		} else {
			tokens = line.split(" ");
		}

		return tokens;
	}

	/**
	 * Get the ClientInfo object for a person.
	 *
	 * @param sHost Who can be any valid identifier for a client as long as it contains a nickname (?:)nick(?!ident)(?@host)
	 * @return ClientInfo Object for the client, or null
	 */
	public ClientInfo getClientInfo(final String sHost) {
		final String sWho = getIRCStringConverter().toLowerCase(ClientInfo.parseHost(sHost));
		if (hClientList.containsKey(sWho)) { return hClientList.get(sWho); }
		else { return null; }
	}

	/**
	 * Get the ClientInfo object for a person, or create a fake client info object.
	 *
	 * @param sHost Who can be any valid identifier for a client as long as it contains a nickname (?:)nick(?!ident)(?@host)
	 * @return ClientInfo Object for the client.
	 */
	public ClientInfo getClientInfoOrFake(final String sHost) {
		final String sWho = getIRCStringConverter().toLowerCase(ClientInfo.parseHost(sHost));
		if (hClientList.containsKey(sWho)) { return hClientList.get(sWho); }
		else { return new ClientInfo(this, sHost).setFake(true); }
	}

	/**
	 * Get the ChannelInfo object for a channel.
	 *
	 * @param sWhat This is the name of the channel.
	 * @return ChannelInfo Object for the channel, or null
	 */
	public ChannelInfo getChannelInfo(String sWhat) {
		synchronized (hChannelList) {
			sWhat = getIRCStringConverter().toLowerCase(sWhat);
			if (hChannelList.containsKey(sWhat)) { return hChannelList.get(sWhat); } else { return null; }
		}
	}

	/**
	 * Send a line to the server.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 */
	public void sendLine(final String line) { doSendString(line, false); }

	/**
	 * Send a line to the server and add proper line ending.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 */
	protected void sendString(final String line) { doSendString(line, true); }

	/**
	 * Send a line to the server and add proper line ending.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 * @param fromParser is this line from the parser? (used for callDataOut)
	 */
	protected void doSendString(final String line, final boolean fromParser) {
		if (out == null) { return; }
		callDataOut(line, fromParser);
		out.printf("%s\r\n", line);
		final String[] newLine = tokeniseLine(line);
		if (newLine[0].equalsIgnoreCase("away") && newLine.length > 1) {
			cMyself.setAwayReason(newLine[newLine.length-1]);
		} else if (newLine[0].equalsIgnoreCase("mode") && newLine.length == 3) {
			// This makes sure we don't add the same item to the LMQ twice, even if its requested twice,
			// as the ircd will only reply once.
			final LinkedList<Character> foundModes = new LinkedList<Character>();

			final ChannelInfo channel = getChannelInfo(newLine[1]);
			if (channel != null) {
				final Queue<Character> listModeQueue = channel.getListModeQueue();
				for (int i = 0; i < newLine[2].length() ; ++i) {
					final Character mode = newLine[2].charAt(i);
					callDebugInfo(DEBUG_LMQ, "Intercepted mode request for "+channel+" for mode "+mode);
					if (hChanModesOther.containsKey(mode) && hChanModesOther.get(mode) == MODE_LIST) {
						if (foundModes.contains(mode)) {
							callDebugInfo(DEBUG_LMQ, "Already added to LMQ");
						} else {
							listModeQueue.offer(mode);
							foundModes.offer(mode);
							callDebugInfo(DEBUG_LMQ, "Added to LMQ");
						}
					}
				}
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
	 * Get the last line of input recieved from the server.
	 *
	 * @return the last line of input recieved from the server.
	 */
	public String getLastLine() {
		return lastLine;
	}

	/**
	 * Process a line and call relevent methods for handling.
	 *
	 * @param line IRC Line to process
	 */
	protected void processLine(final String line) {
		callDataIn(line);

		final String[] token = tokeniseLine(line);
		int nParam;
		setPingNeeded(false);
//		pingCountDown = pingCountDownLength;

		if (token.length < 2) {
			return;
		}
		try {
			final String sParam = token[1];
			if (token[0].equalsIgnoreCase("PING") || token[1].equalsIgnoreCase("PING")) {
				sendString("PONG :" + sParam);
			} else if (token[0].equalsIgnoreCase("PONG") || token[1].equalsIgnoreCase("PONG")) {
				if (!lastPingValue.isEmpty() && lastPingValue.equals(token[token.length-1])) {
					lastPingValue = "";
					serverLag = System.currentTimeMillis() - pingTime;
					callPingSuccess();
				}
			} else if (token[0].equalsIgnoreCase("ERROR")) {
				final StringBuilder errorMessage = new StringBuilder();
				for (int i = 1; i < token.length; ++i) { errorMessage.append(token[i]); }
				callServerError(errorMessage.toString());
			} else {
				if (got001) {
					// Freenode sends a random notice in a stupid place, others might do aswell
					// These shouldn't cause post005 to be fired, so handle them here.
					if (token[0].equalsIgnoreCase("NOTICE")) {
						try { myProcessingManager.process("Notice Auth", token); } catch (ProcessorNotFoundException e) { }
						return;
					}
					if (!post005) {
						try { nParam = Integer.parseInt(token[1]); } catch (NumberFormatException e) { nParam = -1; }
						if (nParam < 0 || nParam > 5) {
							callPost005();
						}
					}
					// After 001 we potentially care about everything!
					try { myProcessingManager.process(sParam, token); }
					catch (ProcessorNotFoundException e) { }
				} else {
					// Before 001 we don't care about much.
					try { nParam = Integer.parseInt(token[1]); } catch (NumberFormatException e) { nParam = -1; }
					switch (nParam) {
						case 1: // 001 - Welcome to IRC
						case 464: // Password Required
						case 433: // Nick In Use
							try { myProcessingManager.process(sParam, token); } catch (ProcessorNotFoundException e) { }
							break;
						default: // Unknown - Send to Notice Auth
							// Some networks send a CTCP during the auth process, handle it
							if (token.length > 3 && !token[3].isEmpty() && token[3].charAt(0) == (char)1 && token[3].charAt(token[3].length()-1) == (char)1) {
								try { myProcessingManager.process(sParam, token); } catch (ProcessorNotFoundException e) { }
								break;
							}
							// Some networks may send a NICK message if you nick change before 001
							// Eat it up so that it isn't treated as a notice auth.
							if (token[0].equalsIgnoreCase("NICK")) { break; }
							
							// Otherwise, send to Notice Auth
							try { myProcessingManager.process("Notice Auth", token); } catch (ProcessorNotFoundException e) { }
							break;
					}
				}
			}
		} catch (Exception e) {
			final ParserError ei = new ParserError(ParserError.ERROR_FATAL, "Fatal Exception in Parser.", getLastLine());
			ei.setException(e);
			callErrorInfo(ei);
		}
	}

	/** The IRCStringConverter for this parser */
	private IRCStringConverter stringConverter = null;

	/**
	 * Get the IRCStringConverter used by this parser.
	 *
	 * @return the IRCStringConverter used by this parser. (will create a default
	 *         one if none exists already);
	 */
	public IRCStringConverter getIRCStringConverter() {
		if (stringConverter == null) {
			stringConverter = new IRCStringConverter((byte)4);
		}
		return stringConverter;
	}

	/**
	 * Update the character arrays.
	 *
	 * @param limit Number of post-alphabetical characters to convert
	 *              0 = ascii encoding
	 *              3 = strict-rfc1459 encoding
	 *              4 = rfc1459 encoding
	 */
	protected void updateCharArrays(final byte limit) {
		stringConverter = new IRCStringConverter(limit);
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
		final char[] modes = new char[hChanModesBool.size()];
		long nTemp;
		double pos;

		for (char cTemp : hChanModesBool.keySet()) {
			nTemp = hChanModesBool.get(cTemp);
			// nTemp should never be less than 0
			if (nTemp > 0) {
				pos = Math.log(nTemp) / Math.log(2);
				modes[(int)pos] = cTemp;
			}
/*			// Is there an easier way to find out the power of 2 value for a number?
			// ie 1024 = 10, 512 = 9 ?
			for (int i = 0; i < modes.length; i++) {
				if (Math.pow(2, i) == (double) nTemp) {
					modes[i] = cTemp;
					break;
				}
			}*/
		}
		return new String(modes);
	}

	/**
	 * Process CHANMODES from 005.
	 */
	public void parseChanModes() {
		final StringBuilder sDefaultModes = new StringBuilder("b,k,l,");
		String[] bits = null;
		String modeStr;
		if (h005Info.containsKey("USERCHANMODES")) {
			if (getIRCD(true).equalsIgnoreCase("dancer")) {
				sDefaultModes.insert(0, "dqeI");
			} else if (getIRCD(true).equalsIgnoreCase("austirc")) {
				sDefaultModes.insert(0, "e");
			}
			modeStr = h005Info.get("USERCHANMODES");
			char mode;
			for (int i = 0; i < modeStr.length(); ++i) {
				mode = modeStr.charAt(i);
				if (!hPrefixModes.containsKey(mode) && sDefaultModes.indexOf(Character.toString(mode)) < 0) {
					sDefaultModes.append(mode);
				}
			}
		} else {
			sDefaultModes.append("imnpstrc");
		}
		if (h005Info.containsKey("CHANMODES")) {
			modeStr = h005Info.get("CHANMODES");
		} else {
			modeStr = sDefaultModes.toString();
			h005Info.put("CHANMODES", modeStr);
		}
		bits = modeStr.split(",", 5);
		if (bits.length < 4) {
			modeStr = sDefaultModes.toString();
			callErrorInfo(new ParserError(ParserError.ERROR_ERROR, "CHANMODES String not valid. Using default string of \"" + modeStr + "\"", getLastLine()));
			h005Info.put("CHANMODES", modeStr);
			bits = modeStr.split(",", 5);
		}

		// resetState
		hChanModesOther.clear();
		hChanModesBool.clear();
		nNextKeyCMBool = 1;

		// List modes.
		for (int i = 0; i < bits[0].length(); ++i) {
			final Character cMode = bits[0].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found List Mode: %c", cMode);
			if (!hChanModesOther.containsKey(cMode)) { hChanModesOther.put(cMode, MODE_LIST); }
		}

		// Param for Set and Unset.
		final Byte nBoth = MODE_SET + MODE_UNSET;
		for (int i = 0; i < bits[1].length(); ++i) {
			final Character cMode = bits[1].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Set/Unset Mode: %c", cMode);
			if (!hChanModesOther.containsKey(cMode)) { hChanModesOther.put(cMode, nBoth); }
		}

		// Param just for Set
		for (int i = 0; i < bits[2].length(); ++i) {
			final Character cMode = bits[2].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Set Only Mode: %c", cMode);
			if (!hChanModesOther.containsKey(cMode)) { hChanModesOther.put(cMode, MODE_SET); }
		}

		// Boolean Mode
		for (int i = 0; i < bits[3].length(); ++i) {
			final Character cMode = bits[3].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Boolean Mode: %c [%d]", cMode, nNextKeyCMBool);
			if (!hChanModesBool.containsKey(cMode)) {
				hChanModesBool.put(cMode, nNextKeyCMBool);
				nNextKeyCMBool = nNextKeyCMBool * 2;
			}
		}
	}

	/**
	 * Get the known prefixmodes in priority order.
	 *
	 * @return All the currently known usermodes
	 */
	public String getPrefixModes() {
		if (h005Info.containsKey("PREFIXSTRING")) {
			return h005Info.get("PREFIXSTRING");
		} else {
			return "";
		}
	}

	/**
	 * Get the known boolean chanmodes in alphabetical order.
	 * Modes are returned in alphabetic order
	 *
	 * @return All the currently known boolean modes
	 */
	public String getBoolChanModes() {
		final char[] modes = new char[hChanModesBool.size()];
		int i = 0;
		for (char mode : hChanModesBool.keySet()) {
			modes[i++] = mode;
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
		return getOtherModeString(MODE_LIST);
	}

	/**
	 * Get the known Set-Only chanmodes.
	 * Modes are returned in alphabetical order
	 *
	 * @return All the currently known Set-Only modes
	 */
	public String getSetOnlyChanModes() {
		return getOtherModeString(MODE_SET);
	}

	/**
	 * Get the known Set-Unset chanmodes.
	 * Modes are returned in alphabetical order
	 *
	 * @return All the currently known Set-Unset modes
	 */
	public String getSetUnsetChanModes() {
		return getOtherModeString((byte) (MODE_SET + MODE_UNSET));
	}

	/**
	 * Get modes from hChanModesOther that have a specific value.
	 * Modes are returned in alphabetical order
	 *
	 * @param nValue Value mode must have to be included
	 * @return All the currently known Set-Unset modes
	 */
	protected String getOtherModeString(final byte nValue) {
		final char[] modes = new char[hChanModesOther.size()];
		Byte nTemp;
		int i = 0;
		for (char cTemp : hChanModesOther.keySet()) {
			nTemp = hChanModesOther.get(cTemp);
			if (nTemp == nValue) { modes[i++] = cTemp; }
		}
		// Alphabetically sort the array
		Arrays.sort(modes);
		return new String(modes).trim();
	}

	/**
	 * Get the known usermodes.
	 * Modes are returned in the order specified by the ircd.
	 *
	 * @return All the currently known usermodes (returns "" if usermodes are unknown)
	 */
	public String getUserModeString() {
		if (h005Info.containsKey("USERMODES")) {
			return h005Info.get("USERMODES");
		} else {
			return "";
		}
	}

	/**
	 * Process USERMODES from 004.
	 */
	protected void parseUserModes() {
		final String sDefaultModes = "nwdoi";
		String modeStr;
		if (h005Info.containsKey("USERMODES")) {
			modeStr = h005Info.get("USERMODES");
		} else {
			modeStr = sDefaultModes;
			h005Info.put("USERMODES", sDefaultModes);
		}

		// resetState
		hUserModes.clear();
		nNextKeyUser = 1;

		// Boolean Mode
		for (int i = 0; i < modeStr.length(); ++i) {
			final Character cMode = modeStr.charAt(i);
			callDebugInfo(DEBUG_INFO, "Found User Mode: %c [%d]", cMode, nNextKeyUser);
			if (!hUserModes.containsKey(cMode)) {
				hUserModes.put(cMode, nNextKeyUser);
				nNextKeyUser = nNextKeyUser * 2;
			}
		}
	}

	/**
	 * Process CHANTYPES from 005.
	 */
	protected void parseChanPrefix() {
		final String sDefaultModes = "#&";
		String modeStr;
		if (h005Info.containsKey("CHANTYPES")) {
			modeStr = h005Info.get("CHANTYPES");
		} else {
			modeStr = sDefaultModes;
			h005Info.put("CHANTYPES", sDefaultModes);
		}

		// resetState
		hChanPrefix.clear();

		// Boolean Mode
		for (int i = 0; i < modeStr.length(); ++i) {
			final Character cMode = modeStr.charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Chan Prefix: %c", cMode);
			if (!hChanPrefix.containsKey(cMode)) { hChanPrefix.put(cMode, true); }
		}
	}

	/**
	 * Process PREFIX from 005.
	 */
	public void parsePrefixModes() {
		final String sDefaultModes = "(ohv)@%+";
		String[] bits;
		String modeStr;
		if (h005Info.containsKey("PREFIX")) {
			modeStr = h005Info.get("PREFIX");
		} else {
			modeStr = sDefaultModes;
		}
		if (modeStr.substring(0, 1).equals("(")) {
			modeStr = modeStr.substring(1);
		} else {
			modeStr = sDefaultModes.substring(1);
			h005Info.put("PREFIX", sDefaultModes);
		}

		bits = modeStr.split("\\)", 2);
		if (bits.length != 2 || bits[0].length() != bits[1].length()) {
			modeStr = sDefaultModes;
			callErrorInfo(new ParserError(ParserError.ERROR_ERROR, "PREFIX String not valid. Using default string of \"" + modeStr + "\"", getLastLine()));
			h005Info.put("PREFIX", modeStr);
			modeStr = modeStr.substring(1);
			bits = modeStr.split("\\)", 2);
		}

		// resetState
		hPrefixModes.clear();
		hPrefixMap.clear();
		nNextKeyPrefix = 1;

		for (int i = bits[0].length() - 1; i > -1; --i) {
			final Character cMode = bits[0].charAt(i);
			final Character cPrefix = bits[1].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Prefix Mode: %c => %c [%d]", cMode, cPrefix, nNextKeyPrefix);
			if (!hPrefixModes.containsKey(cMode)) {
				hPrefixModes.put(cMode, nNextKeyPrefix);
				hPrefixMap.put(cMode, cPrefix);
				hPrefixMap.put(cPrefix, cMode);
				nNextKeyPrefix = nNextKeyPrefix * 2;
			}
		}

		h005Info.put("PREFIXSTRING", bits[0]);
	}

	/**
	 * Check if server is ready.
	 *
	 * @return true if 001 has been recieved, false otherwise.
	 */
	public boolean isReady() { return got001; }

	/**
	 * Join a Channel.
	 *
	 * @param sChannelName Name of channel to join
	 */
	public void joinChannel(final String sChannelName) {
		joinChannel(sChannelName, "", true);
	}

	/**
	 * Join a Channel.
	 *
	 * @param sChannelName Name of channel to join
	 * @param autoPrefix Automatically prepend the first channel prefix defined
	 *                   in 005 if sChannelName is an invalid channel.
	 *                   **This only applies to the first channel if given a list**
	 */
	public void joinChannel(final String sChannelName, final boolean autoPrefix) {
		joinChannel(sChannelName, "", autoPrefix);
	}

	/**
	 * Join a Channel with a key.
	 *
	 * @param sChannelName Name of channel to join
	 * @param sKey Key to use to try and join the channel
	 */
	public void joinChannel(final String sChannelName, final String sKey) {
		joinChannel(sChannelName, sKey, true);
	}

	/**
	 * Join a Channel with a key.
	 *
	 * @param sChannelName Name of channel to join
	 * @param sKey Key to use to try and join the channel
	 * @param autoPrefix Automatically prepend the first channel prefix defined
	 *                   in 005 if sChannelName is an invalid channel.
	 *                   **This only applies to the first channel if given a list**
	 */
	public void joinChannel(final String sChannelName, final String sKey, final boolean autoPrefix) {
		final String channelName;
		if (isValidChannelName(sChannelName)) {
			channelName = sChannelName;
		} else {
			if (autoPrefix) {
				if (h005Info.containsKey("CHANTYPES")) {
					final String chantypes = h005Info.get("CHANTYPES");
					if (chantypes.isEmpty()) {
						channelName = "#" + sChannelName;
					} else {
						channelName = chantypes.charAt(0) + sChannelName;
					}
				} else {
					return;
				}
			} else {
				return;
			}
		}
		if (sKey.isEmpty()) {
			sendString("JOIN " + channelName);
		} else {
			sendString("JOIN " + channelName + " " + sKey);
		}
	}

	/**
	 * Leave a Channel.
	 *
	 * @param sChannelName Name of channel to part
	 * @param sReason Reason for leaving (Nothing sent if sReason is "")
	 */
	public void partChannel(final String sChannelName, final String sReason) {
		if (getChannelInfo(sChannelName) == null) { return; }
		if (sReason.isEmpty()) {
			sendString("PART " + sChannelName);
		} else {
			sendString("PART " + sChannelName + " :" + sReason);
		}
	}

	/**
	 * Set Nickname.
	 *
	 * @param sNewNickName New nickname wanted.
	 */
	public void setNickname(final String sNewNickName) {
		if (getSocketState() == SocketState.OPEN) {
			if (!cMyself.isFake() && cMyself.getNickname().equals(sNewNickName)) {
				return;
			}
			sendString("NICK " + sNewNickName);
		} else {
			me.setNickname(sNewNickName);
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
	public int getMaxLength(final String sType, final String sTarget) {
		// If my host is "nick!user@host" and we are sending "#Channel"
		// a "PRIVMSG" this will find the length of ":nick!user@host PRIVMSG #channel :"
		// and subtract it from the MAX_LINELENGTH. This should be sufficient in most cases.
		// Lint = the 2 ":" at the start and end and the 3 separating " "s
		int length = 0;
		if (sType != null) { length = length + sType.length(); }
		if (sTarget != null) { length = length + sTarget.length(); }
		return getMaxLength(length);
	}

	/**
	 * Get the max length a message can be.
	 *
	 * @param nLength Length of stuff. (Ie "PRIVMSG"+"#Channel")
	 * @return Max Length message should be.
	 */
	public int getMaxLength(final int nLength) {
		final int lineLint = 5;
		if (cMyself.isFake()) {
			callErrorInfo(new ParserError(ParserError.ERROR_ERROR + ParserError.ERROR_USER, "getMaxLength() called, but I don't know who I am?", getLastLine()));
			return MAX_LINELENGTH - nLength - lineLint;
		} else {
			return MAX_LINELENGTH - cMyself.toString().length() - nLength - lineLint;
		}
	}

	/**
	 * Get the max number of list modes.
	 *
	 * @param mode The mode to know the max number for
	 * @return The max number of list modes for the given mode.
	 *         - returns 0 if MAXLIST does not contain the mode, unless MAXBANS is
	 *           set, then this is returned instead.
	 *         - returns -1 if:
	 *           - MAXLIST or MAXBANS were not in 005
	 *           - Values for MAXLIST or MAXBANS were invalid (non integer, empty)
	 */
	public int getMaxListModes(final char mode) {
		// MAXLIST=bdeI:50
		// MAXLIST=b:60,e:60,I:60
		// MAXBANS=30
		int result = -2;
		callDebugInfo(DEBUG_INFO, "Looking for maxlistmodes for: "+mode);
		// Try in MAXLIST
		if (h005Info.get("MAXLIST") != null) {
			if (h005Info.get("MAXBANS") == null) {
				result = 0;
			}
			final String maxlist = h005Info.get("MAXLIST");
			callDebugInfo(DEBUG_INFO, "Found maxlist ("+maxlist+")");
			final String[] bits = maxlist.split(",");
			for (String bit : bits) {
				final String[] parts = bit.split(":", 2);
				callDebugInfo(DEBUG_INFO, "Bit: "+bit+" | parts.length = "+parts.length+" ("+parts[0]+" -> "+parts[0].indexOf(mode)+")");
				if (parts.length == 2 && parts[0].indexOf(mode) > -1) {
					callDebugInfo(DEBUG_INFO, "parts[0] = '"+parts[0]+"' | parts[1] = '"+parts[1]+"'");
					try {
						result = Integer.parseInt(parts[1]);
						break;
					} catch (NumberFormatException nfe) { result = -1; }
				}
			}
		}

		// If not in max list, try MAXBANS
		if (result == -2 && h005Info.get("MAXBANS") != null) {
			callDebugInfo(DEBUG_INFO, "Trying max bans");
			try {
				result = Integer.parseInt(h005Info.get("MAXBANS"));
			} catch (NumberFormatException nfe) { result = -1; }
		} else if (result == -2 && getIRCD(true).equalsIgnoreCase("weircd")) {
			// -_-
			result = 50;
		} else if (result == -2) {
			result = -1;
			callDebugInfo(DEBUG_INFO, "Failed");
			callErrorInfo(new ParserError(ParserError.ERROR_ERROR, "Unable to discover max list modes.", getLastLine()));
		}
		callDebugInfo(DEBUG_INFO, "Result: "+result);
		return result;
	}

	/**
	 * Send a private message to a target.
	 *
	 * @param sTarget Target
	 * @param sMessage Message to send
	 */
	public void sendMessage(final String sTarget, final String sMessage) {
		if (sTarget == null || sMessage == null) { return; }
		if (sTarget.isEmpty()/* || sMessage.isEmpty()*/) { return; }

		sendString("PRIVMSG " + sTarget + " :" + sMessage);
	}

	/**
	 * Send a notice message to a target.
	 *
	 * @param sTarget Target
	 * @param sMessage Message to send
	 */
	public void sendNotice(final String sTarget, final String sMessage) {
		if (sTarget == null || sMessage == null) { return; }
		if (sTarget.isEmpty()/* || sMessage.isEmpty()*/) { return; }

		sendString("NOTICE " + sTarget + " :" + sMessage);
	}

	/**
	 * Send a Action to a target.
	 *
	 * @param sTarget Target
	 * @param sMessage Action to send
	 */
	public void sendAction(final String sTarget, final String sMessage) {
		sendCTCP(sTarget, "ACTION", sMessage);
	}

	/**
	 * Send a CTCP to a target.
	 *
	 * @param sTarget Target
	 * @param sType Type of CTCP
	 * @param sMessage Optional Additional Parameters
	 */
	public void sendCTCP(final String sTarget, final String sType, final String sMessage) {
		if (sTarget == null || sMessage == null) { return; }
		if (sTarget.isEmpty() || sType.isEmpty()) { return; }
		final char char1 = (char) 1;
		sendString("PRIVMSG " + sTarget + " :" + char1 + sType.toUpperCase() + " " + sMessage + char1);
	}

	/**
	 * Send a CTCPReply to a target.
	 *
	 * @param sTarget Target
	 * @param sType Type of CTCP
	 * @param sMessage Optional Additional Parameters
	 */
	public void sendCTCPReply(final String sTarget, final String sType, final String sMessage) {
		if (sTarget == null || sMessage == null) { return; }
		if (sTarget.isEmpty() || sType.isEmpty()) { return; }
		final char char1 = (char) 1;
		sendString("NOTICE " + sTarget + " :" + char1 + sType.toUpperCase() + " " + sMessage + char1);
	}

	/**
	 * Quit IRC.
	 * This method will wait for the server to close the socket.
	 *
	 * @param sReason Reason for quitting.
	 */
	public void quit(final String sReason) {
		if (sReason.isEmpty()) {
			sendString("QUIT");
		} else {
			sendString("QUIT :" + sReason);
		}
	}
	/**
	 * Disconnect from server.
	 * This method will quit and automatically close the socket without waiting for
	 * the server.
	 *
	 * @param sReason Reason for quitting.
	 */
	public void disconnect(final String sReason) {
		if (currentSocketState == SocketState.OPEN && got001) { quit(sReason); }
		try {
			if (socket != null) { socket.close(); }
		} catch (IOException e) {
			/* Do Nothing */
		} finally {
			if (currentSocketState != SocketState.CLOSED) {
				currentSocketState = SocketState.CLOSED;
				callSocketClosed();
			}
			resetState();
		}
	}

	/**
	 * Check if a channel name is valid.
	 *
	 * @param sChannelName Channel name to test
	 * @return true if name is valid on the current connection, false otherwise.
	 *         - Before channel prefixes are known (005/noMOTD/MOTDEnd), this checks
	 *           that the first character is either #, &amp;, ! or +
	 *         - Assumes that any channel that is already known is valid, even if
	 *           005 disagrees.
	 */
	public boolean isValidChannelName(final String sChannelName) {
		// Check sChannelName is not empty or null
		if (sChannelName == null || sChannelName.isEmpty()) { return false; }
		// Check its not ourself (PM recieved before 005)
		if (getIRCStringConverter().equalsIgnoreCase(getMyNickname(), sChannelName)) { return false; }
		// Check if we are already on this channel
		if (getChannelInfo(sChannelName) != null) { return true; }
		// Check if we know of any valid chan prefixes
		if (hChanPrefix.isEmpty()) {
			// We don't. Lets check against RFC2811-Specified channel types
			final char first = sChannelName.charAt(0);
			return first == '#' || first == '&' || first == '!' || first == '+';
		}
		// Otherwise return true if:
		// Channel equals "0"
		// first character of the channel name is a valid channel prefix.
		return hChanPrefix.containsKey(sChannelName.charAt(0)) || sChannelName.equals("0");
	}

	/**
	 * Check if a given chanmode is user settable.
	 *
	 * @param mode Mode to test
	 * @return true if mode is settable by users, false if servers only
	 */
	public boolean isUserSettable(final Character mode) {
		String validmodes;
		if (h005Info.containsKey("USERCHANMODES")) {
			validmodes = h005Info.get("USERCHANMODES");
		} else {
			validmodes = "bklimnpstrc";
		}
		return validmodes.matches(".*" + mode + ".*");
	}

	/**
	 * Get the 005 info.
	 *
	 * @return 005Info hashtable.
	 */
	public Map<String, String> get005() { return h005Info; }

	/**
	 * Get the name of the ircd.
	 *
	 * @param getType if this is false the string from 004 is returned. Else a guess of the type (ircu, hybrid, ircnet)
	 * @return IRCD Version or Type
	 */
	public String getIRCD(final boolean getType) {
		if (h005Info.containsKey("004IRCD")) {
			final String version = h005Info.get("004IRCD");
			if (getType) {
				// This ilst is vaugly based on http://searchirc.com/ircd-versions,
				// but keeping groups of ircd's together (ie hybrid-based, ircu-based)
				if (version.matches("(?i).*unreal.*")) { return "unreal"; }
				else if (version.matches("(?i).*bahamut.*")) { return "bahamut"; }
				else if (version.matches("(?i).*nefarious.*")) { return "nefarious"; }
				else if (version.matches("(?i).*asuka.*")) { return "asuka"; }
				else if (version.matches("(?i).*snircd.*")) { return "snircd"; }
				else if (version.matches("(?i).*beware.*")) { return "bircd"; }
				else if (version.matches("(?i).*u2\\.[0-9]+\\.H\\..*")) { return "irchispano"; }
				else if (version.matches("(?i).*u2\\.[0-9]+\\..*")) { return "ircu"; }
				else if (version.matches("(?i).*ircu.*")) { return "ircu"; }
				else if (version.matches("(?i).*plexus.*")) { return "plexus"; }
				else if (version.matches("(?i).*hybrid.*oftc.*")) { return "oftc-hybrid"; }
				else if (version.matches("(?i).*ircd.hybrid.*")) { return "hybrid7"; }
				else if (version.matches("(?i).*hybrid.*")) { return "hybrid"; }
				else if (version.matches("(?i).*charybdis.*")) { return "charybdis"; }
				else if (version.matches("(?i).*inspircd.*")) { return "inspircd"; }
				else if (version.matches("(?i).*ultimateircd.*")) { return "ultimateircd"; }
				else if (version.matches("(?i).*critenircd.*")) { return "critenircd"; }
				else if (version.matches("(?i).*fqircd.*")) { return "fqircd"; }
				else if (version.matches("(?i).*conferenceroom.*")) { return "conferenceroom"; }
				else if (version.matches("(?i).*hyperion.*")) { return "hyperion"; }
				else if (version.matches("(?i).*dancer.*")) { return "dancer"; }
				else if (version.matches("(?i).*austhex.*")) { return "austhex"; }
				else if (version.matches("(?i).*austirc.*")) { return "austirc"; }
				else if (version.matches("(?i).*ratbox.*")) { return "ratbox"; }
				else if (version.matches("(?i).*euircd.*")) { return "euircd"; }
				else if (version.matches("(?i).*weircd.*")) { return "weircd"; }
				else if (version.matches("(?i).*swiftirc.*")) { return "swiftirc"; }
				else {
					// Stupid networks/ircds go here...
					if (sNetworkName.equalsIgnoreCase("ircnet")) { return "ircnet"; }
					else if (sNetworkName.equalsIgnoreCase("starchat")) { return "starchat"; }
					else if (sNetworkName.equalsIgnoreCase("bitlbee")) { return "bitlbee"; }
					else if (h005Info.containsKey("003IRCD") && h005Info.get("003IRCD").matches("(?i).*bitlbee.*")) { return "bitlbee"; } // Older bitlbee
					else { return "generic"; }
				}
			} else {
				return version;
			}
		} else {
			if (getType) { return "generic"; }
			else { return ""; }
		}
	}


	/**
	 * Get the time used for the ping Timer.
	 *
	 * @return current time used.
	 * @see setPingCountDownLength
	 */
	public long getPingTimerLength() { return pingTimerLength; }

	/**
	 * Set the time used for the ping Timer.
	 * This will also reset the pingTimer.
	 *
	 * @param newValue New value to use.
	 * @see setPingCountDownLength
	 */
	public void setPingTimerLength(final long newValue) {
		pingTimerLength = newValue;
		startPingTimer();
	}

	/**
	 * Get the time used for the pingCountdown.
	 *
	 * @return current time used.
	 * @see setPingCountDownLength
	 */
	public byte getPingCountDownLength() { return pingCountDownLength; }

	/**
	 * Set the time used for the ping countdown.
	 * The pingTimer fires every pingTimerLength/1000 seconds, whenever a line of data
	 * is received, the "waiting for ping" flag is set to false, if the line is
	 * a "PONG", then onPingSuccess is also called.
	 *
	 * When waiting for a ping reply, onPingFailed() is called every time the
	 * timer is fired.
	 *
	 * When not waiting for a ping reply, the pingCountDown is decreased by 1
	 * every time the timer fires, when it reaches 0 is is reset to
	 * pingCountDownLength and a PING is sent to the server.
	 *
	 * To ping the server after 30 seconds of inactivity you could use:
	 * pingTimerLength = 5000, pingCountDown = 6
	 * or
	 * pingTimerLength = 10000, pingCountDown = 3
	 *
	 * @param newValue New value to use.
	 * @see pingCountDown
	 * @see pingTimerLength
	 * @see pingTimerTask
	 */
	public void setPingCountDownLength(final byte newValue) {
		pingCountDownLength = newValue;
	}

	/**
	 * Start the pingTimer.
	 */
	protected void startPingTimer() {
		setPingNeeded(false);
		if (pingTimer != null) { pingTimer.cancel(); }
		pingTimer = new Timer("IRCParser pingTimer");
		pingTimer.schedule(new PingTimer(this, pingTimer), 0, pingTimerLength);
		pingCountDown = 1;
	}

	/**
	 * This is called when the ping Timer has been executed.
	 * As the timer is restarted on every incomming message, this will only be
	 * called when there has been no incomming line for 10 seconds.
	 *
	 * @param timer The timer that called this.
	 */
	protected void pingTimerTask(final Timer timer) {
		if (pingTimer == null || !pingTimer.equals(timer)) { return; }
		if (getPingNeeded()) {
			if (!callPingFailed()) {
				pingTimer.cancel();
				disconnect("Server not responding.");
			}
		} else {
			--pingCountDown;
			if (pingCountDown < 1) {
				pingTime = System.currentTimeMillis();
				setPingNeeded(true);
				pingCountDown = pingCountDownLength;
				callPingSent();
				lastPingValue = String.valueOf(System.currentTimeMillis());
				sendLine("PING " + lastPingValue);
			}
		}
	}

	/**
	 * Get the current server lag.
	 *
	 * @return Last time between sending a PING and recieving a PONG
	 */
	public long getServerLag() {
		return serverLag;
	}

	/**
	 * Get the current server lag.
	 *
	 * @param actualTime if True the value returned will be the actual time the ping was sent
	 *                   else it will be the amount of time sinse the last ping was sent.
	 * @return Time last ping was sent
	 */
	public long getPingTime(final boolean actualTime) {
		if (actualTime) { return pingTime; }
		else { return System.currentTimeMillis() - pingTime; }
	}

	/**
	 * Set if a ping is needed or not.
	 *
	 * @param newStatus new value to set pingNeeded to.
	 */
	private void setPingNeeded(final boolean newStatus)  {
		pingNeeded.set(newStatus);
	}

	/**
	 * Get if a ping is needed or not.
	 *
	 * @return value of pingNeeded.
	 */
	private boolean getPingNeeded()  {
		return pingNeeded.get();
	}

	/**
	 * Get a reference to the cMyself object.
	 *
	 * @return cMyself reference
	 */
	public ClientInfo getMyself() { return cMyself; }

	/**
	 * Get the current nickname.
	 * If after 001 this returns the exact same as getMyself.getNickname();
	 * Before 001 it returns the nickname that the parser Thinks it has.
	 *
	 * @return Current nickname.
	 */
	public String getMyNickname() {
		if (cMyself.isFake()) {
			return sThinkNickname;
		} else {
			return cMyself.getNickname();
		}
	}

	/**
	 * Get the current username (Specified in MyInfo on construction).
	 * Get the username given in MyInfo
	 *
	 * @return My username.
	 */
	public String getMyUsername() {
		return me.getUsername();
	}

	/**
	 * Add a client to the ClientList.
	 *
	 * @param client Client to add
	 */
	public void addClient(final ClientInfo client) {
		hClientList.put(getIRCStringConverter().toLowerCase(client.getNickname()),client);
	}

	/**
	 * Remove a client from the ClientList.
	 * This WILL NOT allow cMyself to be removed from the list.
	 *
	 * @param client Client to remove
	 */
	public void removeClient(final ClientInfo client) {
		if (client != cMyself) {
			forceRemoveClient(client);
		}
	}

	/**
	 * Remove a client from the ClientList.
.	 * This WILL allow cMyself to be removed from the list
	 *
	 * @param client Client to remove
	 */
	protected void forceRemoveClient(final ClientInfo client) {
		hClientList.remove(getIRCStringConverter().toLowerCase(client.getNickname()));
	}

	/**
	 * Get the number of known clients.
	 *
	 * @return Count of known clients
	 */
	public int knownClients() {
		return hClientList.size();
	}

	/**
	 * Get the known clients as a collection.
	 *
	 * @return Known clients as a collection
	 */
	public Collection<ClientInfo> getClients() {
		return hClientList.values();
	}

	/**
	 * Clear the client list.
	 */
	public void clearClients() {
		hClientList.clear();
		addClient(getMyself());
	}

	/**
	 * Add a channel to the ChannelList.
	 *
	 * @param channel Channel to add
	 */
	public void addChannel(final ChannelInfo channel) {
		synchronized (hChannelList) {
			hChannelList.put(getIRCStringConverter().toLowerCase(channel.getName()), channel);
		}
	}

	/**
	 * Remove a channel from the ChannelList.
	 *
	 * @param channel Channel to remove
	 */
	public void removeChannel(final ChannelInfo channel) {
		synchronized (hChannelList) {
			hChannelList.remove(getIRCStringConverter().toLowerCase(channel.getName()));
		}
	}

	/**
	 * Get the number of known channel.
	 *
	 * @return Count of known channel
	 */
	public int knownChannels() {
		synchronized (hChannelList) {
			return hChannelList.size();
		}
	}

	/**
	 * Get the known channels as a collection.
	 *
	 * @return Known channels as a collection
	 */
	public Collection<ChannelInfo> getChannels() {
		synchronized (hChannelList) {
			return hChannelList.values();
		}
	}

	/**
	 * Clear the channel list.
	 */
	public void clearChannels() {
		synchronized (hChannelList) {
			hChannelList.clear();
		}
	}

}
