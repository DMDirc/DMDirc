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

/**
 * IRC Parser.
 *
 * @author            Shane Mc Cormack
 * @version           $Id$
 */
public class IRCParser implements Runnable {

	public static final int ndInfo = 1;
	public static final int ndSocket = 2;
//	public static final int ndSomething = 4;

	private Socket socket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;

	public MyInfo me = new MyInfo(); // This is what the user wants, nickname here is *not* fact.
	public ServerInfo server = new ServerInfo(); // Server Info requested by user

	private String sThinkNickname; // This is what we want the nickname to be.
	private String sConfirmedNickname; // This is what the nickname actually is.
	private boolean TriedAlt = false;

	private boolean Got001 = false;
	private boolean Got005 = false;
	private boolean HasBegan = false;
	private boolean IsFirst = true;

	// Events
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
	

        @SuppressWarnings("unchecked")
	private void AddCallback (Object eMethod, ArrayList CallbackList) {
		for (int i = 0; i < CallbackList.size(); i++) {
			if (eMethod.equals(CallbackList.get(i))) { return; }
		}
		CallbackList.add(eMethod);
	}
	private void DelCallback (Object eMethod, ArrayList CallbackList) {
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
  * Default constructor.
  */
	public IRCParser () { }
/**
  * Constructor with ServerInfo.
	* 
	* @param serverDetails Server information.
  */
	public IRCParser (ServerInfo serverDetails) { this(null,serverDetails); }
/**
  * Constructor with MyInfo.
	* 
	* @param myDetails Client information.|
  */
	public IRCParser (MyInfo myDetails) { this(myDetails,null); }
/**
  * Constructor with ServerInfo and MyInfo.
	* 
	* @param serverDetails Server information.
	* @param myDetails Client information.|
  */
	public IRCParser (MyInfo myDetails, ServerInfo serverDetails) {
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
			socket = new Socket(server.sHost,server.nPort);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) { throw e; }
	}

/**
  * Begin execution. Connect to server, and start parsing incomming lines
  */
	public void run() /*throws Exception*/ {
		if (HasBegan) { return; } else { HasBegan = true; }
		try { connect(); } catch (Exception e) { CallDebugInfo(ndSocket,"Error Connecting, Aborted"); return; }
		// :HACK: While true loops really really suck.
		CallDebugInfo(ndSocket,"Socket Connected");
		String line = "";
		while(true) {
			try {
				line = in.readLine(); // Blocking :/
				if (IsFirst) {
					if (!server.sPassword.equals("")) {
						SendString("PASS "+server.sPassword);
                                        }
					SetNickname(me.sNickname);
					SendString("USER "+me.sUsername.toLowerCase()+" * * :"+me.sRealname);
					IsFirst = false;
				}
				ProcessLine(line);
			} catch (IOException e) {
				CallDebugInfo(ndSocket,"Socket Closed");
				break;
			}
		}
	}

	protected void finalize(){
		try {
			socket.close();
		} catch (IOException e) {
			System.out.println("Could not close socket");
			System.exit(-1);
		}
	}

	private String GetParam(String line) {
		String[] params = null;
		params = line.split(" :",2);
		return params[params.length-1];
	}

	private String[] IRCTokenise(String line) {
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
					case 1: // 001
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
	}

	private void ProcessEndOfMOTD(String sParam,String token[]) { CallMOTDEnd(); }

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