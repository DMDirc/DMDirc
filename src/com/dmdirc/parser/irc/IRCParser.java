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

import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.SecureParser;
import com.dmdirc.parser.interfaces.callbacks.ConnectErrorListener;
import com.dmdirc.parser.interfaces.callbacks.DataInListener;
import com.dmdirc.parser.interfaces.callbacks.DataOutListener;
import com.dmdirc.parser.interfaces.callbacks.DebugInfoListener;
import com.dmdirc.parser.interfaces.callbacks.ErrorInfoListener;
import com.dmdirc.parser.interfaces.callbacks.PingFailureListener;
import com.dmdirc.parser.interfaces.callbacks.PingSentListener;
import com.dmdirc.parser.interfaces.callbacks.PingSuccessListener;
import com.dmdirc.parser.interfaces.callbacks.Post005Listener;
import com.dmdirc.parser.interfaces.callbacks.ServerErrorListener;
import com.dmdirc.parser.interfaces.callbacks.SocketCloseListener;
import com.dmdirc.parser.common.CallbackManager;

import com.dmdirc.parser.irc.callbacks.IRCCallbackManager;
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
public class IRCParser implements SecureParser, Runnable {

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

	/** Should PINGs be sent to the server to check if its alive? */
	private boolean checkServerPing = true;
	/** Timer for server ping. */
	private Timer pingTimer = null;
	/** Semaphore for access to pingTimer. */
	private Semaphore pingTimerSem = new Semaphore(1);
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
	private int pingCountDown;
	/**
	 * Amount of times the timer has to fire for inactivity before sending a ping.
	 *
	 * @see setPingCountDownLength
	 */
	private int pingCountDownLength = 6;

	/** Name the server calls itself. */
	String serverName;

	/** Network name. This is "" if no network name is provided */
	String networkName;

	/** This is what we think the nickname should be. */
	String thinkNickname;

	/** When using inbuilt pre-001 NickInUse handler, have we tried our AltNick. */
	boolean triedAlt;

	/** Have we recieved the 001. */
	boolean got001;
	/** Have we fired post005? */
	boolean post005;
	/** Has the thread started execution yet, (Prevents run() being called multiple times). */
	boolean hasBegan;

	/** Hashtable storing known prefix modes (ohv). */
	final Map<Character, Long> prefixModes = new Hashtable<Character, Long>();
	/**
	 * Hashtable maping known prefix modes (ohv) to prefixes (@%+) - Both ways.
	 * Prefix map contains 2 pairs for each mode. (eg @ => o and o => @)
	 */
	final Map<Character, Character> prefixMap = new Hashtable<Character, Character>();
	/** Integer representing the next avaliable integer value of a prefix mode. */
	long nextKeyPrefix = 1;
	/** Hashtable storing known user modes (owxis etc). */
	final Map<Character, Long> userModes = new Hashtable<Character, Long>();
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
	final Map<Character, Long> chanModesBool = new Hashtable<Character, Long>();
	/** Integer representing the next avaliable integer value of a Boolean mode. */

	long nextKeyCMBool = 1;

	/**
	 * Hashtable storing known non-boolean chan modes (klbeI etc).
	 * Non Boolean Modes (for Channels) are stored together in this hashtable, the value param
	 * is used to show the type of variable. (List (1), Param just for set (2), Param for Set and Unset (2+4=6))<br><br>
	 * <br>
	 * see MODE_LIST<br>
	 * see MODE_SET<br>
	 * see MODE_UNSET<br>
	 */
	final Map<Character, Byte> chanModesOther = new Hashtable<Character, Byte>();

	/** The last line of input recieved from the server */
	String lastLine = "";
	/** Should the lastline (where given) be appended to the "data" part of any onErrorInfo call? */
	boolean addLastLine = false;

	/**
	* Channel Prefixes (ie # + etc).
	* The "value" for these is always true.
	*/
	final Map<Character, Boolean> chanPrefix = new Hashtable<Character, Boolean>();
	/** Hashtable storing all known clients based on nickname (in lowercase). */
	private final Map<String, IRCClientInfo> clientList = new Hashtable<String, IRCClientInfo>();
	/** Hashtable storing all known channels based on chanel name (inc prefix - in lowercase). */
	private final Map<String, IRCChannelInfo> channelList = new Hashtable<String, IRCChannelInfo>();
	/** Reference to the ClientInfo object that references ourself. */
	private IRCClientInfo myself = new IRCClientInfo(this, "myself").setFake(true);
	/** Hashtable storing all information gathered from 005. */
	final Map<String, String> h005Info = new Hashtable<String, String>();

	/** Ignore List. */
	RegexStringList myIgnoreList = new RegexStringList();

	/** Reference to the callback Manager. */
	CallbackManager<IRCParser> myCallbackManager = new IRCCallbackManager(this);
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
	boolean createFake = true;

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

    /** {@inheritDoc} */
    @Override
	public void setBindIP(final String ip) { bindIP = ip; }

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
     * @since 0.6.3m1
	 * @return Current {@link SocketState}
	 */
	public SocketState getSocketState() { return currentSocketState; }

	/**
	 * Get a reference to the Processing Manager.
	 *
	 * @return Reference to the CallbackManager
	 */
	public ProcessingManager getProcessingManager() { return myProcessingManager;	}

	/** {@inheritDoc} */
        @Override
	public CallbackManager<IRCParser> getCallbackManager() { return myCallbackManager;	}

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

	/** {@inheritDoc} */
        @Override
	public void setTrustManagers(final TrustManager[] managers) { myTrustManager = managers; }

	/** {@inheritDoc} */
        @Override
	public void setKeyManagers(final KeyManager[] managers) { myKeyManagers = managers; }

	/** {@inheritDoc} */
        @Override
	public RegexStringList getIgnoreList() { return myIgnoreList; }

	/** {@inheritDoc} */
        @Override
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
		return myCallbackManager.getCallbackType(ServerErrorListener.class).call(message);
	}

	/**
	 * Callback to all objects implementing the DataIn Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IDataIn
	 * @param data Incomming Line.
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callDataIn(final String data) {
		return myCallbackManager.getCallbackType(DataInListener.class).call(data);
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
		return myCallbackManager.getCallbackType(DataOutListener.class).call(data, fromParser);
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
		return myCallbackManager.getCallbackType(DebugInfoListener.class).call(level, data);
	}

	/**
	 * Callback to all objects implementing the IErrorInfo Interface.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IErrorInfo
	 * @param errorInfo ParserError object representing the error.
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callErrorInfo(final ParserError errorInfo) {
		return myCallbackManager.getCallbackType(ErrorInfoListener.class).call(errorInfo);
	}

	/**
	 * Callback to all objects implementing the IConnectError Interface.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IConnectError
	 * @param errorInfo ParserError object representing the error.
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callConnectError(final ParserError errorInfo) {
		return myCallbackManager.getCallbackType(ConnectErrorListener.class).call(errorInfo);
	}

	/**
	 * Callback to all objects implementing the SocketClosed Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.ISocketClosed
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callSocketClosed() {
		return myCallbackManager.getCallbackType(SocketCloseListener.class).call();
	}

	/**
	 * Callback to all objects implementing the PingFailed Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IPingFailed
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callPingFailed() {
		return myCallbackManager.getCallbackType(PingFailureListener.class).call();
	}

	/**
	 * Callback to all objects implementing the PingSent Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IPingSent
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callPingSent() {
		return myCallbackManager.getCallbackType(PingSentListener.class).call();
	}

	/**
	 * Callback to all objects implementing the PingSuccess Callback.
	 *
	 * @see com.dmdirc.parser.irc.callbacks.interfaces.IPingSuccess
	 * @return true if a method was called, false otherwise
	 */
	protected boolean callPingSuccess() {
		return myCallbackManager.getCallbackType(PingSuccessListener.class).call();
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
		
		if (!h005Info.containsKey("CHANTYPES")) { parseChanPrefix(); }
		if (!h005Info.containsKey("PREFIX")) { parsePrefixModes(); }
		if (!h005Info.containsKey("USERMODES")) { parseUserModes(); }
		if (!h005Info.containsKey("CHANMODES")) { parseChanModes(); }

		return getCallbackManager().getCallbackType(Post005Listener.class).call();
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
		channelList.clear();
		clientList.clear();
		h005Info.clear();
		prefixModes.clear();
		prefixMap.clear();
		chanModesOther.clear();
		chanModesBool.clear();
		userModes.clear();
		chanPrefix.clear();
		// Reset the mode indexes
		nextKeyPrefix = 1;
		nextKeyCMBool = 1;
		nNextKeyUser = 1;
		serverName = "";
		networkName = "";
		lastLine = "";
		myself = new IRCClientInfo(this, "myself").setFake(true);

		stopPingTimer();

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
		getLocalClient().setNickname(me.getNickname());
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
				} else if (currentSocketState != SocketState.CLOSING) {
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

	/** {@inheritDoc} */
        @Override
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

	/** {@inheritDoc} */
        @Override
	public IRCClientInfo getClient(final String details) {
		final String sWho = getStringConverter().toLowerCase(IRCClientInfo.parseHost(details));
		if (clientList.containsKey(sWho)) { return clientList.get(sWho); }
		else { return new IRCClientInfo(this, details).setFake(true); }
	}

        public boolean isKnownClient(final String host) {
            final String sWho = getStringConverter().toLowerCase(IRCClientInfo.parseHost(host));
            return clientList.containsKey(sWho);
        }

	/** {@inheritDoc} */
        @Override
	public IRCChannelInfo getChannel(String channel) {
		synchronized (channelList) {
			channel = getStringConverter().toLowerCase(channel);
			if (channelList.containsKey(channel)) { return channelList.get(channel); } else { return null; }
		}
	}

	/** {@inheritDoc} */
        @Override
	public void sendRawMessage(final String message) { doSendString(message, false); }

	/**
	 * Send a line to the server and add proper line ending.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 * @return True if line was sent, else false.
	 */
	protected boolean sendString(final String line) { return doSendString(line, true); }

	/**
	 * Send a line to the server and add proper line ending.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 * @param fromParser is this line from the parser? (used for callDataOut)
	 * @return True if line was sent, else false.
	 */
	protected boolean doSendString(final String line, final boolean fromParser) {
		if (out == null || getSocketState() != SocketState.OPEN) { return false; }
		callDataOut(line, fromParser);
		out.printf("%s\r\n", line);
		final String[] newLine = tokeniseLine(line);
		if (newLine[0].equalsIgnoreCase("away") && newLine.length > 1) {
			myself.setAwayReason(newLine[newLine.length-1]);
		} else if (newLine[0].equalsIgnoreCase("mode") && newLine.length == 3) {
			// This makes sure we don't add the same item to the LMQ twice, even if its requested twice,
			// as the ircd will only reply once.
			final LinkedList<Character> foundModes = new LinkedList<Character>();

			final IRCChannelInfo channel = getChannel(newLine[1]);
			if (channel != null) {
				final Queue<Character> listModeQueue = channel.getListModeQueue();
				for (int i = 0; i < newLine[2].length() ; ++i) {
					final Character mode = newLine[2].charAt(i);
					callDebugInfo(DEBUG_LMQ, "Intercepted mode request for "+channel+" for mode "+mode);
					if (chanModesOther.containsKey(mode) && chanModesOther.get(mode) == MODE_LIST) {
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
		
		return true;
	}

	/** {@inheritDoc} */
        @Override
	public String getNetworkName() {
		return networkName;
	}

	/** {@inheritDoc} */
        @Override
	public String getServerName() {
		return serverName;
	}

	/** {@inheritDoc} */
        @Override
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
					if (token[0].equalsIgnoreCase("NOTICE") || (token.length > 2 && token[2].equalsIgnoreCase("NOTICE"))) {
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
							if (token[1].equalsIgnoreCase("NICK")) { break; }
							
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

	/** {@inheritDoc} */
        @Override
	public IRCStringConverter getStringConverter() {
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
		final char[] modes = new char[chanModesBool.size()];
		long nTemp;
		double pos;

		for (char cTemp : chanModesBool.keySet()) {
			nTemp = chanModesBool.get(cTemp);
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
				if (!prefixModes.containsKey(mode) && sDefaultModes.indexOf(Character.toString(mode)) < 0) {
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
		chanModesOther.clear();
		chanModesBool.clear();
		nextKeyCMBool = 1;

		// List modes.
		for (int i = 0; i < bits[0].length(); ++i) {
			final Character cMode = bits[0].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found List Mode: %c", cMode);
			if (!chanModesOther.containsKey(cMode)) { chanModesOther.put(cMode, MODE_LIST); }
		}

		// Param for Set and Unset.
		final Byte nBoth = MODE_SET + MODE_UNSET;
		for (int i = 0; i < bits[1].length(); ++i) {
			final Character cMode = bits[1].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Set/Unset Mode: %c", cMode);
			if (!chanModesOther.containsKey(cMode)) { chanModesOther.put(cMode, nBoth); }
		}

		// Param just for Set
		for (int i = 0; i < bits[2].length(); ++i) {
			final Character cMode = bits[2].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Set Only Mode: %c", cMode);
			if (!chanModesOther.containsKey(cMode)) { chanModesOther.put(cMode, MODE_SET); }
		}

		// Boolean Mode
		for (int i = 0; i < bits[3].length(); ++i) {
			final Character cMode = bits[3].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Boolean Mode: %c [%d]", cMode, nextKeyCMBool);
			if (!chanModesBool.containsKey(cMode)) {
				chanModesBool.put(cMode, nextKeyCMBool);
				nextKeyCMBool = nextKeyCMBool * 2;
			}
		}
	}

	/** {@inheritDoc} */
        @Override
	public String getChannelUserModes() {
		if (h005Info.containsKey("PREFIXSTRING")) {
			return h005Info.get("PREFIXSTRING");
		} else {
			return "";
		}
	}

	/** {@inheritDoc} */
        @Override
	public String getBooleanChannelModes() {
		final char[] modes = new char[chanModesBool.size()];
		int i = 0;
		for (char mode : chanModesBool.keySet()) {
			modes[i++] = mode;
		}
		// Alphabetically sort the array
		Arrays.sort(modes);
		return new String(modes);
	}

	/** {@inheritDoc} */
        @Override
	public String getListChannelModes() {
		return getOtherModeString(MODE_LIST);
	}

	/** {@inheritDoc} */
        @Override
	public String getParameterChannelModes() {
		return getOtherModeString(MODE_SET);
	}

	/** {@inheritDoc} */
        @Override
	public String getDoubleParameterChannelModes() {
		return getOtherModeString((byte) (MODE_SET + MODE_UNSET));
	}

	/**
	 * Get modes from hChanModesOther that have a specific value.
	 * Modes are returned in alphabetical order
	 *
	 * @param value Value mode must have to be included
	 * @return All the currently known Set-Unset modes
	 */
	protected String getOtherModeString(final byte value) {
		final char[] modes = new char[chanModesOther.size()];
		Byte nTemp;
		int i = 0;
		for (char cTemp : chanModesOther.keySet()) {
			nTemp = chanModesOther.get(cTemp);
			if (nTemp == value) { modes[i++] = cTemp; }
		}
		// Alphabetically sort the array
		Arrays.sort(modes);
		return new String(modes).trim();
	}

	/** {@inheritDoc} */
        @Override
	public String getUserModes() {
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
		userModes.clear();
		nNextKeyUser = 1;

		// Boolean Mode
		for (int i = 0; i < modeStr.length(); ++i) {
			final Character cMode = modeStr.charAt(i);
			callDebugInfo(DEBUG_INFO, "Found User Mode: %c [%d]", cMode, nNextKeyUser);
			if (!userModes.containsKey(cMode)) {
				userModes.put(cMode, nNextKeyUser);
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
		chanPrefix.clear();

		// Boolean Mode
		for (int i = 0; i < modeStr.length(); ++i) {
			final Character cMode = modeStr.charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Chan Prefix: %c", cMode);
			if (!chanPrefix.containsKey(cMode)) { chanPrefix.put(cMode, true); }
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
		prefixModes.clear();
		prefixMap.clear();
		nextKeyPrefix = 1;

		for (int i = bits[0].length() - 1; i > -1; --i) {
			final Character cMode = bits[0].charAt(i);
			final Character cPrefix = bits[1].charAt(i);
			callDebugInfo(DEBUG_INFO, "Found Prefix Mode: %c => %c [%d]", cMode, cPrefix, nextKeyPrefix);
			if (!prefixModes.containsKey(cMode)) {
				prefixModes.put(cMode, nextKeyPrefix);
				prefixMap.put(cMode, cPrefix);
				prefixMap.put(cPrefix, cMode);
				nextKeyPrefix = nextKeyPrefix * 2;
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

    /** {@inheritDoc} */
    @Override
    public void joinChannel(final String channel) {
        joinChannel(channel, "", true);
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

    /** {@inheritDoc} */
    @Override
    public void joinChannel(final String channel, final String key) {
        joinChannel(channel, key, true);
    }

	/**
	 * Join a Channel with a key.
	 *
	 * @param channel Name of channel to join
	 * @param key Key to use to try and join the channel
	 * @param autoPrefix Automatically prepend the first channel prefix defined
	 *                   in 005 if sChannelName is an invalid channel.
	 *                   **This only applies to the first channel if given a list**
	 */
	public void joinChannel(final String channel, final String key, final boolean autoPrefix) {
		final String channelName;
		if (isValidChannelName(channel)) {
			channelName = channel;
		} else {
			if (autoPrefix) {
				if (h005Info.containsKey("CHANTYPES")) {
					final String chantypes = h005Info.get("CHANTYPES");
					if (chantypes.isEmpty()) {
						channelName = "#" + channel;
					} else {
						channelName = chantypes.charAt(0) + channel;
					}
				} else {
					return;
				}
			} else {
				return;
			}
		}
		if (key.isEmpty()) {
			sendString("JOIN " + channelName);
		} else {
			sendString("JOIN " + channelName + " " + key);
		}
	}

	/**
	 * Leave a Channel.
	 *
	 * @param channel Name of channel to part
	 * @param reason Reason for leaving (Nothing sent if sReason is "")
	 */
	public void partChannel(final String channel, final String reason) {
		if (getChannel(channel) == null) { return; }
		if (reason.isEmpty()) {
			sendString("PART " + channel);
		} else {
			sendString("PART " + channel + " :" + reason);
		}
	}

	/**
	 * Set Nickname.
	 *
	 * @param nickname New nickname wanted.
	 */
	public void setNickname(final String nickname) {
		if (getSocketState() == SocketState.OPEN) {
			if (!myself.isFake() && myself.getNickname().equals(nickname)) {
				return;
			}
			sendString("NICK " + nickname);
		} else {
			me.setNickname(nickname);
		}
                
		thinkNickname = nickname;
	}

    /** {@inheritDoc} */
    @Override
	public int getMaxLength(final String type, final String target) {
		// If my host is "nick!user@host" and we are sending "#Channel"
		// a "PRIVMSG" this will find the length of ":nick!user@host PRIVMSG #channel :"
		// and subtract it from the MAX_LINELENGTH. This should be sufficient in most cases.
		// Lint = the 2 ":" at the start and end and the 3 separating " "s
		int length = 0;
		if (type != null) { length = length + type.length(); }
		if (target != null) { length = length + target.length(); }
		return getMaxLength(length);
	}

	/**
	 * Get the max length a message can be.
	 *
	 * @param length Length of stuff. (Ie "PRIVMSG"+"#Channel")
	 * @return Max Length message should be.
	 */
	public int getMaxLength(final int length) {
		final int lineLint = 5;
		if (myself.isFake()) {
			callErrorInfo(new ParserError(ParserError.ERROR_ERROR + ParserError.ERROR_USER, "getMaxLength() called, but I don't know who I am?", getLastLine()));
			return MAX_LINELENGTH - length - lineLint;
		} else {
			return MAX_LINELENGTH - myself.toString().length() - length - lineLint;
		}
	}

	/** {@inheritDoc} */
        @Override
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

	/** {@inheritDoc} */
        @Override
	public void sendMessage(final String target, final String message) {
		if (target == null || message == null) { return; }
		if (target.isEmpty()/* || sMessage.isEmpty()*/) { return; }

		sendString("PRIVMSG " + target + " :" + message);
	}

	/** {@inheritDoc} */
        @Override
	public void sendNotice(final String target, final String message) {
		if (target == null || message == null) { return; }
		if (target.isEmpty()/* || sMessage.isEmpty()*/) { return; }

		sendString("NOTICE " + target + " :" + message);
	}

	/** {@inheritDoc} */
        @Override
        public void sendAction(final String target, final String message) {
		sendCTCP(target, "ACTION", message);
	}

	/** {@inheritDoc} */
        @Override
	public void sendCTCP(final String target, final String type, final String message) {
		if (target == null || message == null) { return; }
		if (target.isEmpty() || type.isEmpty()) { return; }
		final char char1 = (char) 1;
		sendString("PRIVMSG " + target + " :" + char1 + type.toUpperCase() + " " + message + char1);
	}

	/** {@inheritDoc} */
        @Override
	public void sendCTCPReply(final String target, final String type, final String message) {
		if (target == null || message == null) { return; }
		if (target.isEmpty() || type.isEmpty()) { return; }
		final char char1 = (char) 1;
		sendString("NOTICE " + target + " :" + char1 + type.toUpperCase() + " " + message + char1);
	}

	/**
	 * Quit IRC.
	 * This method will wait for the server to close the socket.
	 *
	 * @param reason Reason for quitting.
	 */
	public void quit(final String reason) {
		if (reason.isEmpty()) {
			sendString("QUIT");
		} else {
			sendString("QUIT :" + reason);
		}
	}
    
	/** {@inheritDoc} */
    @Override
	public void disconnect(final String message) {
                if (currentSocketState == SocketState.OPEN) {
                        currentSocketState = SocketState.CLOSING;
                        if (got001) { quit(message); }
                }

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

	/** {@inheritDoc}
         *
	 * - Before channel prefixes are known (005/noMOTD/MOTDEnd), this checks
	 *   that the first character is either #, &amp;, ! or +
	 * - Assumes that any channel that is already known is valid, even if
	 *   005 disagrees.
	 */
        @Override
	public boolean isValidChannelName(final String name) {
		// Check sChannelName is not empty or null
		if (name == null || name.isEmpty()) { return false; }
		// Check its not ourself (PM recieved before 005)
		if (getStringConverter().equalsIgnoreCase(getMyNickname(), name)) { return false; }
		// Check if we are already on this channel
		if (getChannel(name) != null) { return true; }
		// Check if we know of any valid chan prefixes
		if (chanPrefix.isEmpty()) {
			// We don't. Lets check against RFC2811-Specified channel types
			final char first = name.charAt(0);
			return first == '#' || first == '&' || first == '!' || first == '+';
		}
		// Otherwise return true if:
		// Channel equals "0"
		// first character of the channel name is a valid channel prefix.
		return chanPrefix.containsKey(name.charAt(0)) || name.equals("0");
	}

	/** {@inheritDoc} */
        @Override
	public boolean isUserSettable(final char mode) {
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
				if (version.matches("(?i).*unreal[^4-9].*")) { return "unreal"; }
				else if (version.matches("(?i).*unreal[4-9].*")) { return "unreal4"; }
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
					if (networkName.equalsIgnoreCase("ircnet")) { return "ircnet"; }
					else if (networkName.equalsIgnoreCase("starchat")) { return "starchat"; }
					else if (networkName.equalsIgnoreCase("bitlbee")) { return "bitlbee"; }
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

    /** {@inheritDoc} */
    @Override
    public String getServerSoftware() {
        return getIRCD(false);
    }

    /** {@inheritDoc} */
    @Override
    public String getServerSoftwareType() {
        return getIRCD(true);
    }

	/**
	 * Get the value of checkServerPing.
	 *
	 * @return value of checkServerPing.
	 * @see setCheckServerPing
	 */
	public boolean getCheckServerPing() { return checkServerPing; }

	/**
	 * Set the value of checkServerPing.
	 *
	 * @param newValue New value to use.
	 * @see setCheckServerPing
	 */
	public void setCheckServerPing(final boolean newValue) {
		checkServerPing = newValue;
		if (checkServerPing) {
			startPingTimer();
		} else {
			stopPingTimer();
		}
	}

	/** {@inheritDoc} */
        @Override
	public long getPingTimerInterval() { return pingTimerLength; }

	/** {@inheritDoc} */
        @Override
	public void setPingTimerInterval(final long newValue) {
		pingTimerLength = newValue;
		startPingTimer();
	}

	/** {@inheritDoc} */
        @Override
	public int getPingTimerFraction() { return pingCountDownLength; }

	/** {@inheritDoc} */
        @Override
	public void setPingTimerFraction(final int newValue) {
		pingCountDownLength = newValue;
	}

	/**
	 * Start the pingTimer.
	 */
	protected void startPingTimer() {
		pingTimerSem.acquireUninterruptibly();

		setPingNeeded(false);
		if (pingTimer != null) { pingTimer.cancel(); }
		pingTimer = new Timer("IRCParser pingTimer");
		pingTimer.schedule(new PingTimer(this, pingTimer), 0, pingTimerLength);
		pingCountDown = 1;

		pingTimerSem.release();
	}
	
	/**
	 * Stop the pingTimer.
	 */
	protected void stopPingTimer() {
		pingTimerSem.acquireUninterruptibly();
		if (pingTimer != null) {
			pingTimer.cancel();
			pingTimer = null;
		}
		pingTimerSem.release();
	}

	/**
	 * This is called when the ping Timer has been executed.
	 * As the timer is restarted on every incomming message, this will only be
	 * called when there has been no incomming line for 10 seconds.
	 *
	 * @param timer The timer that called this.
	 */
	protected void pingTimerTask(final Timer timer) {
		if (!getCheckServerPing()) {
			pingTimerSem.acquireUninterruptibly();
			if (pingTimer != null && pingTimer.equals(timer)) {
				pingTimer.cancel();
			}
			pingTimerSem.release();
			
			return;
		}
		if (getPingNeeded()) {
			if (!callPingFailed()) {
				pingTimerSem.acquireUninterruptibly();
				
				if (pingTimer != null && pingTimer.equals(timer)) {
					pingTimer.cancel();
				}
				pingTimerSem.release();
				
				disconnect("Server not responding.");
			}
		} else {
			--pingCountDown;
			if (pingCountDown < 1) {
				pingTime = System.currentTimeMillis();
				setPingNeeded(true);
				pingCountDown = pingCountDownLength;
				lastPingValue = String.valueOf(System.currentTimeMillis());
				if (doSendString("PING " + lastPingValue, false)) {
					callPingSent();
				}
			}
		}
	}

	/** {@inheritDoc} */
        @Override
	public long getServerLatency() {
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

        /** {@inheritDoc} */
        @Override
        public long getPingTime() {
            return getPingTime(false);
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

	/** {@inheritDoc} */
        @Override
	public IRCClientInfo getLocalClient() { return myself; }

	/**
	 * Get the current nickname.
	 * If after 001 this returns the exact same as getMyself.getNickname();
	 * Before 001 it returns the nickname that the parser Thinks it has.
	 *
	 * @return Current nickname.
	 */
	public String getMyNickname() {
		if (myself.isFake()) {
			return thinkNickname;
		} else {
			return myself.getNickname();
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
	public void addClient(final IRCClientInfo client) {
		clientList.put(getStringConverter().toLowerCase(client.getNickname()),client);
	}

	/**
	 * Remove a client from the ClientList.
	 * This WILL NOT allow cMyself to be removed from the list.
	 *
	 * @param client Client to remove
	 */
	public void removeClient(final IRCClientInfo client) {
		if (client != myself) {
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
		clientList.remove(getStringConverter().toLowerCase(client.getNickname()));
	}

	/**
	 * Get the number of known clients.
	 *
	 * @return Count of known clients
	 */
	public int knownClients() {
		return clientList.size();
	}

	/**
	 * Get the known clients as a collection.
	 *
	 * @return Known clients as a collection
	 */
	public Collection<IRCClientInfo> getClients() {
		return clientList.values();
	}

	/**
	 * Clear the client list.
	 */
	public void clearClients() {
		clientList.clear();
		addClient(getLocalClient());
	}

	/**
	 * Add a channel to the ChannelList.
	 *
	 * @param channel Channel to add
	 */
	public void addChannel(final IRCChannelInfo channel) {
		synchronized (channelList) {
			channelList.put(getStringConverter().toLowerCase(channel.getName()), channel);
		}
	}

	/**
	 * Remove a channel from the ChannelList.
	 *
	 * @param channel Channel to remove
	 */
	public void removeChannel(final IRCChannelInfo channel) {
		synchronized (channelList) {
			channelList.remove(getStringConverter().toLowerCase(channel.getName()));
		}
	}

	/**
	 * Get the number of known channel.
	 *
	 * @return Count of known channel
	 */
	public int knownChannels() {
		synchronized (channelList) {
			return channelList.size();
		}
	}

	/** {@inheritDoc} */
        @Override
	public Collection<IRCChannelInfo> getChannels() {
		synchronized (channelList) {
                    return channelList.values();
		}
	}

	/**
	 * Clear the channel list.
	 */
	public void clearChannels() {
		synchronized (channelList) {
			channelList.clear();
		}
	}

    /** {@inheritDoc} */
    @Override
    public String[] parseHostmask(final String hostmask) {
        return IRCClientInfo.parseHostFull(hostmask);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxTopicLength() {
        if (h005Info.containsKey("TOPICLEN")) {
            try {
                return Integer.parseInt(h005Info.get("TOPICLEN"));
            } catch (NumberFormatException ex) {
                // Do nothing
            }
        }

        return 0;
    }

}
