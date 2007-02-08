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
 */

package dmdirc.parser;

import java.io.*;
import java.net.*;

interface IDataInEvent { public void onDataIn(IRCParser parser, String data); }
interface IDataOutEvent { public void onDataOut(IRCParser parser, String data); }

public class IRCParser implements Runnable {

	class MyInfo {
		String sNickname = "IRCParser";
		String sRealname = "Java Test IRCParser";
		String sUsername = "IRCParser";
	}

	private Socket socket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;

	public MyInfo me = new MyInfo();

	private boolean HasBegan = false;
	private boolean IsFirst = true;

	// Events
	public interface GeneralIRCEvent { public void GeneralEvent(String sName); }
	public interface LogEvent { public void LogEvent(String sName, String sData); }
	class AllEvents {
		GeneralIRCEvent EndOfMOTD = null;
		LogEvent DataIn = null;
		LogEvent DataOut = null;

		String sEndOfMOTD = "";
		String sDataIn = "";
		String sDataOut = "";
	}
	public AllEvents cb = new AllEvents();

	public void SetCallback(String sType, Object eMethod) throws Exception { try { SetCallback(sType,sType,eMethod); } catch (Exception e) { throw e; } }
	public void SetCallback(String sType, String sName, Object eMethod) throws Exception {
		if (sName.equals("")) { sName = sType; }
		sType = sType.toLowerCase();
		if (sType.equals("endofmotd")) { cb.EndOfMOTD = (GeneralIRCEvent)eMethod; cb.sEndOfMOTD = sName; }
		else if (sType.equals("datain")) { cb.DataIn = (LogEvent)eMethod; cb.sDataIn = sName; }
		else if (sType.equals("dataout")) { cb.DataOut = (LogEvent)eMethod; cb.sDataOut = sName; }
		else { throw new Exception("No such callback '"+sType+"'");}
	}

	// Constructor.
	IRCParser () {}

	public void connect(String sHost) throws Exception {
		try {
			connect(sHost,6667);		
		} catch (Exception e) {
			throw e;
		}
	}

	public void connect(String sHost, int nPort) throws Exception {
		if (HasBegan) { return; }
		try {
			socket = new Socket(sHost,nPort);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			throw new Exception("Socket Exception");
		} catch (Exception e) {
			throw new Exception("General Exception");
		}
	}

	public void run() {
		if (HasBegan) { return; } else { HasBegan = true; }
		// :HACK: While true loops really really suck.
		while(true){
			String line = "";
			try {
				line = in.readLine(); // Blocking :/
				if (IsFirst) {
					SendString("NICK "+me.sNickname);
					SendString("USER "+me.sUsername.toLowerCase()+" * * :"+me.sRealname);
					IsFirst = false;
				}
		        ProcessLine(line);
			} catch (IOException e) {
				System.out.println("Socket read failed");
				System.exit(-1);
			}
		}
	}

	protected void finalize(){
		try{
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
	
		String[] temp = new String[tokens.length+1];
		System.arraycopy(tokens, 0, temp, 0, tokens.length);
		tokens = temp;
		if (params.length == 2) { tokens[tokens.length-1] = params[1]; }

	    return tokens;
	}

	public void SendLine(String line) {SendString(line);} // This should do some checks on stuff possible, public event!
	
	// Our Method
	private void SendString(String line) {
		if (!cb.sDataOut.equals("")) {cb.DataOut.LogEvent(cb.sDataOut,line);} // Ugly++
		out.printf("%s\r\n",line);
	}

	private void ProcessLine(String line) {
		String[] token = IRCTokenise(line);
		String sParam = token[token.length-1];

		int nParam;
		if (!cb.sDataIn.equals("")) {cb.DataIn.LogEvent(cb.sDataIn,line);} // Ugly++

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

	private void ProcessEndOfMOTD(String sParam,String token[]) {
		// Process EndOfMOTD
		if (!cb.sEndOfMOTD.equals("")) {cb.EndOfMOTD.GeneralEvent(cb.sEndOfMOTD);} // Ugly++
	}



	//-------------------------------------------------------------------------
	public void JoinChannel(String sChannelName) {
		SendLine("JOIN "+sChannelName);
	}
}