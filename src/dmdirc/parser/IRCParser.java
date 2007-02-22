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

package dmdirc.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * IRC Parser.
 *
 * @author            Shane Mc Cormack
 * @version           $Id$
 */
public class IRCParser implements Runnable {
	
	public static final int ndInfo = 1;   // Information
	public static final int ndSocket = 2; // Socket Errors
//	public static final int ndSomething = 4; //Next thingy

	public static final int errFatal = 1; // Error is potentially Fatal. Desync 99% Guarenteed!
	public static final int errError = 2;	 // Error is not fatal, but is more severe than a warning
	public static final int errWarning = 4; // This was an unexpected occurance, but shouldn't be anything to worry about
	public static final int errCanContinue = 8; // If an Error has this flag, it means the parser is able to continue running
	                                            // Most errWarnings should have this flag. if Fatal or Error are not accomanied
	                                            // with this flag, you should disconnect or risk problems further on.
	
	// Development Debugging info - Outputs directly to console.
	// This is used for debugging info that is generally of no use to most people
	private static final boolean bDebug = true;
	
	private Socket socket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	
	public MyInfo me = new MyInfo(); // This is what the user wants, nickname here is *not* fact.
	public ServerInfo server = new ServerInfo(); // Server Info requested by user

	private String sServerName;

	private String sThinkNickname; // This is what we want the nickname to be.
	private boolean TriedAlt = false;
	
	private boolean Got001 = false;
	private boolean Got005 = false;
	private boolean HasBegan = false;
	private boolean IsFirst = true;
	
	private Hashtable<String,ChannelInfo> hChannelList = new Hashtable<String,ChannelInfo>();
	private Hashtable<String,ClientInfo> hClientList = new Hashtable<String,ClientInfo>();	
	private ClientInfo cMyself = null;
	
	private class Info005 {
		Hashtable<String,String> Info = new Hashtable<String,String>(); // Misc values from 005
	}
	private Info005 i005 = new Info005();

	// Events
	// TODO: This would probably be more efficient as hashtables.
	public interface IDebugInfo { public void onDebug(IRCParser tParser, int nLevel, String sData); }
	ArrayList<IDebugInfo> cbDebugInfo = new ArrayList<IDebugInfo>();
	public interface IMOTDEnd { public void onMOTDEnd(IRCParser tParser); }
	ArrayList<IMOTDEnd> cbEndOfMOTD = new ArrayList<IMOTDEnd>();
	public interface IDataIn { public void onDataIn(IRCParser tParser, String sData); }
	ArrayList<IDataIn> cbDataIn = new ArrayList<IDataIn>();
	public interface IDataOut { public void onDataOut(IRCParser tParser, String sData, boolean FromParser); }
	ArrayList<IDataOut> cbDataOut = new ArrayList<IDataOut>();
	public interface INickInUse { public void onNickInUse(IRCParser tParser); }
	ArrayList<INickInUse> cbNickInUse = new ArrayList<INickInUse>();
	public interface IErrorInfo { public void onError(IRCParser tParser, int nLevel, String sData); }
	ArrayList<IErrorInfo> cbErrorInfo = new ArrayList<IErrorInfo>();	
	public interface IChannelJoin { public void onJoinChannel(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient ); }
	ArrayList<IChannelJoin> cbChannelJoin = new ArrayList<IChannelJoin>();		
	

	@SuppressWarnings("unchecked")
	private void AddCallback(Object eMethod, ArrayList CallbackList) {
		for (int i = 0; i < CallbackList.size(); i++) {
			if (eMethod.equals(CallbackList.get(i))) { return; }
		}
		CallbackList.add(eMethod);
	}
	private void DelCallback(Object eMethod, ArrayList CallbackList) {
		for (int i = 0; i < CallbackList.size(); i++) {
			if (eMethod.equals(CallbackList.get(i))) { CallbackList.remove(i); break; }
		}
	}
	
	/**
	 * Add callback for DebugInfo (onDebug).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddDebugInfo(Object eMethod) { AddCallback(eMethod, cbDebugInfo); }
	/**
	 * Delete callback for DebugInfo (onDebug).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelDebugInfo(Object eMethod) { DelCallback(eMethod, cbDebugInfo); }
	private boolean CallDebugInfo(int level, String data) {
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddMOTDEnd(Object eMethod) { AddCallback(eMethod, cbEndOfMOTD); }
	/**
	 * Delete callback for MOTDEnd (onMOTDEnd).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelMOTDEnd(Object eMethod) { DelCallback(eMethod, cbEndOfMOTD); }
	private boolean CallMOTDEnd() {
		boolean bResult = false;
		for (int i = 0; i < cbEndOfMOTD.size(); i++) {
			cbEndOfMOTD.get(i).onMOTDEnd(this);
			bResult = true;
		}
		return bResult;
	}
	
	/**
	 * Add callback for DataIn (onDataIn).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddDataIn(Object eMethod) { AddCallback((IDataIn)eMethod, cbDataIn); }
	/**
	 * Delete callback for DataIn (onDebug).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelDataIn(Object eMethod) { DelCallback((IDataIn)eMethod, cbDataIn); }
	private boolean CallDataIn(String data) {
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddDataOut(Object eMethod) { AddCallback((IDataOut)eMethod, cbDataOut); }
	/**
	 * Delete callback for DataOut (onDataOut).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelDataOut(Object eMethod) { DelCallback((IDataOut)eMethod, cbDataOut); }
	private boolean CallDataOut(String data, boolean FromParser) {
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddNickInUse(Object eMethod) { AddCallback(eMethod, cbNickInUse); }
	/**
	 * Delete callback for NickInUse (onNickInUse).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelNickInUse(Object eMethod) { DelCallback(eMethod, cbNickInUse); }
	private boolean CallNickInUse() {
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddErrorInfo(Object eMethod) { AddCallback(eMethod, cbErrorInfo); }
	/**
	 * Delete callback for ErrorInfo (onError).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelErrorInfo(Object eMethod) { DelCallback(eMethod, cbErrorInfo); }
	private boolean CallErrorInfo(int level, String data) {
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelJoin(Object eMethod) { AddCallback(eMethod, cbChannelJoin); }
	/**
	 * Delete callback for ChannelJoin (onJoinChannel).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelJoin(Object eMethod) { DelCallback(eMethod, cbChannelJoin); }
	private boolean CallChannelJoin(ChannelInfo cChannel, ChannelClientInfo cChannelClient) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelJoin.size(); i++) {
			cbChannelJoin.get(i).onJoinChannel(this, cChannel, cChannelClient);
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
		if (!bDebug) { return true; }
		else {
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
		}
	}
	
	/**
	 * Default constructor.
	 */
	public IRCParser() { }
	/**
	 * Constructor with ServerInfo.
	 *
	 * @param serverDetails Server information.
	 */
	public IRCParser(ServerInfo serverDetails) { this(null,serverDetails); }
	/**
	 * Constructor with MyInfo.
	 *
	 * @param myDetails Client information.|
	 */
	public IRCParser(MyInfo myDetails) { this(myDetails,null); }
	/**
	 * Constructor with ServerInfo and MyInfo.
	 *
	 * @param serverDetails Server information.
	 * @param myDetails Client information.|
	 */
	public IRCParser(MyInfo myDetails, ServerInfo serverDetails) {
		if (myDetails != null) { this.me = myDetails; }
		if (serverDetails != null) { this.server = serverDetails; }
	}
	
	private void ResetState() {
		TriedAlt = false;
		Got001 = false;
		Got005 = false;
	};
	
	private void connect() throws Exception {
		try {
			ResetState();
			CallDebugInfo(ndSocket,"Connecting to "+server.sHost+":"+server.nPort);
			socket = new Socket(server.sHost,server.nPort);
			if (bDebug) { System.out.printf("\t\t-> 1\n"); }
			out = new PrintWriter(socket.getOutputStream(), true);
			if (bDebug) { System.out.printf("\t\t-> 2\n"); }
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			if (bDebug) { System.out.printf("\t\t-> 3\n"); }
		} catch (Exception e) { throw e; }
	}
	
	/**
	 * Begin execution. Connect to server, and start parsing incomming lines
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
	
	protected void finalize(){
		try {
			socket.close();
		} catch (IOException e) {
			CallDebugInfo(ndInfo,"Could not close socket");
		}
	}

	private String GetParam(String line) {
		String[] params = null;
		params = line.split(" :",2);
		return params[params.length-1];
	}
	
	private String[] IRCTokenise(String line) {
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
		
		if (hClientList.containsKey(sWho)) { return hClientList.get(sWho); } else { return null; }
	}
	
	/**
	 * Get the ChannelInfo object for a channel.
	 *
	 * @param sWhat This is the name of the channel.
	 * @return ChannelInfo Object for the channel, or null	 
	 */
	public ChannelInfo GetChannelInfo(String sWhat) {
		if (hChannelList.containsKey(sWhat)) { return hChannelList.get(sWhat); } else { return null; }
	}	
	
	/**
	 * Send a line to the server.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 */
	public void SendLine(String line) { CallDataOut(line,false); out.printf("%s\r\n",line);} // This should do some checks on stuff, public event!
	
	// Our Method
	private void SendString(String line) {
		CallDataOut(line,true);
		out.printf("%s\r\n",line);
	}
	
	
	private void ProcessLine(String line) {
		String[] token = IRCTokenise(line);
		String sParam = token[token.length-1];
		
		int nParam;
		CallDataIn(line);
		
		try {nParam = Integer.parseInt(token[1]);} catch (Exception e) { nParam = -1;}

		if (token[0].equals("PING") || token[1].equals("PING")) { SendString("PONG :"+sParam); }
		else {
			if (token[0].substring(0,1).equals(":")) {
				// Post Connect
				switch (nParam) {
					case -1:
						ProcessStringParam(sParam,token);
						break;
					case 1: // 001 - Welcome to IRC
						Process001(sParam,token);
						break;
					case 4: // 004 - ISUPPORT
					case 5: // 005 - ISUPPORT
						Process004_005(nParam,token);
						break;
					case 375: // MOTD Start
						break;
					case 422: // No MOTD
					case 376: // End of MOTD
						ProcessEndOfMOTD(sParam,token);
						break;
					case 433: // Nick In Use
						ProcessNickInUse(sParam,token);
						break;
					default: // Unknown
						break;
				}
			} else {
				// Pre Connect
			}
		}
	}
		
	private void ProcessStringParam(String sParam,String token[]) {
		// Process a line where the parameter is a string (IE PRIVMSG, NOTICE etc - Not including PING!)
		if (sParam.equalsIgnoreCase("JOIN")) { ProcessJoinChannel(sParam,token); }
		else if (sParam.equalsIgnoreCase("PART")) { ProcessPartChannel(sParam,token); }		
	}
	
	private void ProcessJoinChannel(String sParam,String token[]) {
		// :nick!ident@host JOIN #Channel
		if (token.length < 3) { return; }
		ClientInfo iClient;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		iClient = GetClientInfo(token[0]);
		iChannel = GetChannelInfo(token[2]);
		
		if (iClient == null) { iClient = new ClientInfo(token[0]); hClientList.put(iClient.GetNickname(),iClient); }
		if (iChannel == null) { 
			if (iClient != cMyself) {
				CallErrorInfo(errWarning+errCanContinue, "Got join for channel ("+token[2]+") that I am not on. [User: "+token[0]+"]");
			}
			iChannel = new ChannelInfo(token[2]);
			hChannelList.put(iChannel.GetName(),iChannel);
		}
		
		iChannelClient = iChannel.addClient(iClient);
		
		CallChannelJoin(iChannel, iChannelClient);		
	}	
	
	private void ProcessPartChannel(String sParam,String token[]) {
		//
	}		
	

	private void Process004_005(int nParam,String token[]) {
		if (nParam == 4) {
			// 004
		} else {
			// 005
			String[] Bits = null;
			String sKey = null, sValue = null;
			for (int i = 3; i < token.length ; i++) {
				Bits = token[i].split("=",2);
				sKey = Bits[0];
				if (Bits.length == 2) { sValue = Bits[1]; } else { sValue = ""; }
				if (bDebug) { System.out.printf("%s => %s \r\n",sKey,sValue); }
				i005.Info.put(sKey,sValue);
			}
		}
	}

	private void ProcessEndOfMOTD(String sParam,String token[]) { CallMOTDEnd(); }

	private void Process001(String sParam,String token[]) {
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
		if (cMyself == null) { cMyself = new ClientInfo(sNick); }
	}

	private void ProcessNickInUse(String sParam,String token[]) {
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
		SendString("JOIN "+sChannelName);
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
	public void Quit(String sReason) { SendString("QUIT :"+sReason); }
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
}
