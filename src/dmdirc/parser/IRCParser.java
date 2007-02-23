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
import java.util.Enumeration;

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
	protected static final boolean bDebug = true;
	
	private Socket socket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	
	public MyInfo me = new MyInfo(); // This is what the user wants, nickname here is *not* fact.
	public ServerInfo server = new ServerInfo(); // Server Info requested by user

	protected String sServerName;

	protected String sThinkNickname; // This is what we want the nickname to be.
	protected boolean TriedAlt = false;
	
	protected boolean Got001 = false;
	protected boolean HasBegan = false;
	protected boolean IsFirst = true;
	
	// Better alternative to hashtable?
	// Valid Boolean Modes are stored as Hashtable.pub('m',1); where 'm' is the mode and 1 is a numeric value
	// Numeric values are powers of 2. This allows up to 32 modes at present (expandable to 64)
	// ChannelInfo/ChannelClientInfo etc provide methods to view the modes in a human way.
	// PrefixModes are o/v etc Prefix map contains 2 pairs for each mode. (eg @ => o and o => @)
	protected Hashtable<Character,Integer> hPrefixModes = new Hashtable<Character,Integer>();
	protected Hashtable<Character,Character> hPrefixMap = new Hashtable<Character,Character>();
	protected int nNextKeyPrefix = 1;
	protected Hashtable<Character,Integer> hUserModes = new Hashtable<Character,Integer>();
	protected int nNextKeyUser = 1;
	protected Hashtable<Character,Integer> hChanModesBool = new Hashtable<Character,Integer>();
	protected int nNextKeyCMBool = 1;
	// Non Boolean Modes (for Channels) are stored together in this arraylist, the value param
	// is used to show the type of variable. (List (1), Param just for set (2), Param for Set and Unset (2+4=6))
	protected Hashtable<Character,Byte> hChanModesOther = new Hashtable<Character,Byte>();	
	protected static final byte cmList = 1;
	protected static final byte cmSet = 2;	
	protected static final byte cmUnset = 4;
	
	// Channel Prefixs
	// The value for these is always true.
	protected Hashtable<Character,Boolean> hChanPrefix = new Hashtable<Character,Boolean>();
	
	protected Hashtable<String,ClientInfo> hClientList = new Hashtable<String,ClientInfo>();	
	protected Hashtable<String,ChannelInfo> hChannelList = new Hashtable<String,ChannelInfo>();
	protected ClientInfo cMyself = null;
	
	protected Hashtable<String,String> h005Info = new Hashtable<String,String>();
	
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
	public interface IChannelPart { public void onPartChannel(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason ); }
	ArrayList<IChannelPart> cbChannelPart = new ArrayList<IChannelPart>();
	public interface IChannelQuit { public void onQuitChannel(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason ); }
	ArrayList<IChannelQuit> cbChannelQuit = new ArrayList<IChannelQuit>();
	public interface IChannelTopic { public void onTopic(IRCParser tParser, ChannelInfo cChannel, boolean bIsNewTopic); }
	ArrayList<IChannelTopic> cbChannelTopic = new ArrayList<IChannelTopic>();
	public interface IChannelMessage { public void onChannelMessage(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost ); }
	ArrayList<IChannelMessage> cbChannelMessage = new ArrayList<IChannelMessage>();
	public interface IChannelAction { public void onChannelAction(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost ); }
	ArrayList<IChannelAction> cbChannelAction = new ArrayList<IChannelAction>();
	public interface IChannelNotice { public void onChannelNotice(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost ); }
	ArrayList<IChannelNotice> cbChannelNotice = new ArrayList<IChannelNotice>();
	public interface IPrivateMessage { public void onPrivateMessage(IRCParser tParser, ClientInfo cClient, String sMessage, String sHost ); }
	ArrayList<IPrivateMessage> cbPrivateMessage = new ArrayList<IPrivateMessage>();
	public interface IPrivateAction { public void onPrivateAction(IRCParser tParser, ClientInfo cClient, String sMessage, String sHost ); }
	ArrayList<IPrivateAction> cbPrivateAction = new ArrayList<IPrivateAction>();
	public interface IPrivateNotice { public void onPrivateNotice(IRCParser tParser, ClientInfo cClient, String sMessage, String sHost ); }
	ArrayList<IPrivateNotice> cbPrivateNotice = new ArrayList<IPrivateNotice>();
	public interface IUnknownMessage { public void onUnknownMessage(IRCParser tParser, ClientInfo cClient, String sMessage, String sHost ); }
	ArrayList<IUnknownMessage> cbUnknownMessage = new ArrayList<IUnknownMessage>();
	public interface IUnknownAction { public void onUnknownAction(IRCParser tParser, ClientInfo cClient, String sMessage, String sHost ); }
	ArrayList<IUnknownAction> cbUnknownAction = new ArrayList<IUnknownAction>();
	public interface IUnknownNotice { public void onUnknownNotice(IRCParser tParser, ClientInfo cClient, String sMessage, String sHost ); }
	ArrayList<IUnknownNotice> cbUnknownNotice = new ArrayList<IUnknownNotice>();
	public interface IChannelCTCP { public void onChannelCTCP(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sType, String sMessage, String sHost ); }
	ArrayList<IChannelCTCP> cbChannelCTCP = new ArrayList<IChannelCTCP>();
	public interface IPrivateCTCP { public void onPrivateCTCP(IRCParser tParser, ClientInfo cClient, String sType, String sMessage, String sHost ); }
	ArrayList<IPrivateCTCP> cbPrivateCTCP = new ArrayList<IPrivateCTCP>();
	public interface IUnknownCTCP { public void onUnknownCTCP(IRCParser tParser, ClientInfo cClient, String sType, String sMessage, String sHost ); }
	ArrayList<IUnknownCTCP> cbUnknownCTCP = new ArrayList<IUnknownCTCP>();
	public interface IChannelCTCPReply { public void onChannelCTCPReply(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sType, String sMessage, String sHost ); }
	ArrayList<IChannelCTCPReply> cbChannelCTCPReply = new ArrayList<IChannelCTCPReply>();
	public interface IPrivateCTCPReply { public void onPrivateCTCPReply(IRCParser tParser, ClientInfo cClient, String sType, String sMessage, String sHost ); }
	ArrayList<IPrivateCTCPReply> cbPrivateCTCPReply = new ArrayList<IPrivateCTCPReply>();
	public interface IUnknownCTCPReply { public void onUnknownCTCPReply(IRCParser tParser, ClientInfo cClient, String sType, String sMessage, String sHost ); }
	ArrayList<IUnknownCTCPReply> cbUnknownCTCPReply = new ArrayList<IUnknownCTCPReply>();
	public interface IQuit { public void onQuit(IRCParser tParser, ClientInfo cClient, String sReason ); }
	ArrayList<IQuit> cbQuit = new ArrayList<IQuit>();	
	public interface IGotNames { public void onGotNames(IRCParser tParser, ChannelInfo cChannel); }	
	ArrayList<IGotNames> cbGotNames = new ArrayList<IGotNames>();
	

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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddMOTDEnd(Object eMethod) { AddCallback(eMethod, cbEndOfMOTD); }
	/**
	 * Delete callback for MOTDEnd (onMOTDEnd).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelMOTDEnd(Object eMethod) { DelCallback(eMethod, cbEndOfMOTD); }
	protected boolean CallMOTDEnd() {
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddDataOut(Object eMethod) { AddCallback((IDataOut)eMethod, cbDataOut); }
	/**
	 * Delete callback for DataOut (onDataOut).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelDataOut(Object eMethod) { DelCallback((IDataOut)eMethod, cbDataOut); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddNickInUse(Object eMethod) { AddCallback(eMethod, cbNickInUse); }
	/**
	 * Delete callback for NickInUse (onNickInUse).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelNickInUse(Object eMethod) { DelCallback(eMethod, cbNickInUse); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddErrorInfo(Object eMethod) { AddCallback(eMethod, cbErrorInfo); }
	/**
	 * Delete callback for ErrorInfo (onError).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelErrorInfo(Object eMethod) { DelCallback(eMethod, cbErrorInfo); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelJoin(Object eMethod) { AddCallback(eMethod, cbChannelJoin); }
	/**
	 * Delete callback for ChannelJoin (onJoinChannel).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelJoin(Object eMethod) { DelCallback(eMethod, cbChannelJoin); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelPart(Object eMethod) { AddCallback(eMethod, cbChannelPart); }
	/**
	 * Delete callback for ChannelPart (onPartChannel).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelPart(Object eMethod) { DelCallback(eMethod, cbChannelPart); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelQuit(Object eMethod) { AddCallback(eMethod, cbChannelQuit); }
	/**
	 * Delete callback for ChannelQuit (onQuitChannel).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelQuit(Object eMethod) { DelCallback(eMethod, cbChannelQuit); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddQuit(Object eMethod) { AddCallback(eMethod, cbQuit); }
	/**
	 * Delete callback for Quit (onQuit).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelQuit(Object eMethod) { DelCallback(eMethod, cbQuit); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddTopic(Object eMethod) { AddCallback(eMethod, cbChannelTopic); }
	/**
	 * Delete callback for ChannelTopic (onTopic).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelTopic(Object eMethod) { DelCallback(eMethod, cbChannelTopic); }
	protected boolean CallTopic(ChannelInfo cChannel, boolean bIsJoinTopic) {
		boolean bResult = false;
		for (int i = 0; i < cbChannelTopic.size(); i++) {
			cbChannelTopic.get(i).onTopic(this, cChannel, bIsJoinTopic);
			bResult = true;
		}
		return bResult;
	}
	
	/**
	 * Add callback for ChannelMessage (onChannelMessage).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelMessage(Object eMethod) { AddCallback(eMethod, cbChannelMessage); }
	/**
	 * Delete callback for ChannelMessage (onChannelMessage).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelMessage(Object eMethod) { DelCallback(eMethod, cbChannelMessage); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelAction(Object eMethod) { AddCallback(eMethod, cbChannelAction); }
	/**
	 * Delete callback for ChannelAction (onChannelAction).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelAction(Object eMethod) { DelCallback(eMethod, cbChannelAction); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelNotice(Object eMethod) { AddCallback(eMethod, cbChannelNotice); }
	/**
	 * Delete callback for ChannelNotice (onChannelNotice).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelNotice(Object eMethod) { DelCallback(eMethod, cbChannelNotice); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddPrivateMessage(Object eMethod) { AddCallback(eMethod, cbPrivateMessage); }
	/**
	 * Delete callback for PrivateMessage (onPrivateMessage).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelPrivateMessage(Object eMethod) { DelCallback(eMethod, cbPrivateMessage); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddPrivateAction(Object eMethod) { AddCallback(eMethod, cbPrivateAction); }
	/**
	 * Delete callback for PrivateAction (onPrivateAction).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelPrivateAction(Object eMethod) { DelCallback(eMethod, cbPrivateAction); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddPrivateNotice(Object eMethod) { AddCallback(eMethod, cbPrivateNotice); }
	/**
	 * Delete callback for PrivateNotice (onPrivateNotice).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelPrivateNotice(Object eMethod) { DelCallback(eMethod, cbPrivateNotice); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddUnknownMessage(Object eMethod) { AddCallback(eMethod, cbUnknownMessage); }
	/**
	 * Delete callback for UnknownMessage (onUnknownMessage).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelUnknownMessage(Object eMethod) { DelCallback(eMethod, cbUnknownMessage); }
	protected boolean CallUnknownMessage(ClientInfo cClient, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbUnknownMessage.size(); i++) {
			cbUnknownMessage.get(i).onUnknownMessage(this, cClient, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for UnknownAction (onUnknownAction).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddUnknownAction(Object eMethod) { AddCallback(eMethod, cbUnknownAction); }
	/**
	 * Delete callback for UnknownAction (onUnknownAction).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelUnknownAction(Object eMethod) { DelCallback(eMethod, cbUnknownAction); }
	protected boolean CallUnknownAction(ClientInfo cClient, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbUnknownAction.size(); i++) {
			cbUnknownAction.get(i).onUnknownAction(this, cClient, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}	

	/**
	 * Add callback for UnknownNotice (onUnknownNotice).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddUnknownNotice(Object eMethod) { AddCallback(eMethod, cbUnknownNotice); }
	/**
	 * Delete callback for UnknownNotice (onUnknownNotice).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelUnknownNotice(Object eMethod) { DelCallback(eMethod, cbUnknownNotice); }
	protected boolean CallUnknownNotice(ClientInfo cClient, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbUnknownNotice.size(); i++) {
			cbUnknownNotice.get(i).onUnknownNotice(this, cClient, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for ChannelCTCP (onChannelCTCP).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelCTCP(Object eMethod) { AddCallback(eMethod, cbChannelCTCP); }
	/**
	 * Delete callback for ChannelCTCP (onChannelCTCP).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelCTCP(Object eMethod) { DelCallback(eMethod, cbChannelCTCP); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddPrivateCTCP(Object eMethod) { AddCallback(eMethod, cbPrivateCTCP); }
	/**
	 * Delete callback for PrivateCTCP (onPrivateCTCP).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelPrivateCTCP(Object eMethod) { DelCallback(eMethod, cbPrivateCTCP); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddUnknownCTCP(Object eMethod) { AddCallback(eMethod, cbUnknownCTCP); }
	/**
	 * Delete callback for UnknownCTCP (onUnknownCTCP).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelUnknownCTCP(Object eMethod) { DelCallback(eMethod, cbUnknownCTCP); }
	protected boolean CallUnknownCTCP(ClientInfo cClient, String sType, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbUnknownCTCP.size(); i++) {
			cbUnknownCTCP.get(i).onUnknownCTCP(this, cClient, sType, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}	
	
	/**
	 * Add callback for ChannelCTCPReply (onChannelCTCPReply).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddChannelCTCPReply(Object eMethod) { AddCallback(eMethod, cbChannelCTCPReply); }
	/**
	 * Delete callback for ChannelCTCPReply (onChannelCTCPReply).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelChannelCTCPReply(Object eMethod) { DelCallback(eMethod, cbChannelCTCPReply); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddPrivateCTCPReply(Object eMethod) { AddCallback(eMethod, cbPrivateCTCPReply); }
	/**
	 * Delete callback for PrivateCTCPReply (onPrivateCTCPReply).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelPrivateCTCPReply(Object eMethod) { DelCallback(eMethod, cbPrivateCTCPReply); }
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
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddUnknownCTCPReply(Object eMethod) { AddCallback(eMethod, cbUnknownCTCPReply); }
	/**
	 * Delete callback for UnknownCTCPReply (onUnknownCTCPReply).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelUnknownCTCPReply(Object eMethod) { DelCallback(eMethod, cbUnknownCTCPReply); }
	protected boolean CallUnknownCTCPReply(ClientInfo cClient, String sType, String sMessage, String sHost) {
		boolean bResult = false;
		for (int i = 0; i < cbUnknownCTCPReply.size(); i++) {
			cbUnknownCTCPReply.get(i).onUnknownCTCPReply(this, cClient, sType, sMessage, sHost);
			bResult = true;
		}
		return bResult;
	}		
	
	/**
	 * Add callback for GotNames (onGotNames).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void AddGotNames(Object eMethod) { AddCallback(eMethod, cbGotNames); }
	/**
	 * Delete callback for GotNames (onGotNames).
	 *
	 * @param eMethod     Reference to object that handles the callback
	 */
	public void DelGotNames(Object eMethod) { DelCallback(eMethod, cbGotNames); }
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

	protected String GetParam(String line) {
		String[] params = null;
		params = line.split(" :",2);
		return params[params.length-1];
	}
	
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
	
	/**
	 * Send a line to the server.
	 *
	 * @param line Line to send (\r\n termination is added automatically)
	 */
	public void SendLine(String line) { CallDataOut(line,false); out.printf("%s\r\n",line);} // This should do some checks on stuff, public event!
	
	// Our Method
	protected void SendString(String line) {
		CallDataOut(line,true);
		out.printf("%s\r\n",line);
	}
	
	
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
		
	private void ProcessStringParam(String sParam, String token[]) {
		// Process a line where the parameter is a string (IE PRIVMSG, NOTICE etc - Not including PING!)
		if (sParam.equalsIgnoreCase("PRIVMSG") || sParam.equalsIgnoreCase("NOTICE")) { ProcessIRCMessage(sParam,token); }
		else if (sParam.equalsIgnoreCase("JOIN")) { ProcessJoinChannel(sParam,token); }
		else if (sParam.equalsIgnoreCase("PART")) { ProcessPartChannel(sParam,token); }
		else if (sParam.equalsIgnoreCase("QUIT")) { ProcessQuit(sParam,token); }
		else if (sParam.equalsIgnoreCase("TOPIC")) { ProcessTopic(sParam,token); }
	}
	
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
			
			iChannel = GetChannelInfo(token[2]);
		
			if (iChannel == null) { 
				CallErrorInfo(errWarning+errCanContinue, "Got names for channel ("+token[2]+") that I am not on.");
				iChannel = new ChannelInfo(token[2]);
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
				if (iClient == null) { iClient = new ClientInfo(sName); hClientList.put(iClient.getNickname().toLowerCase(),iClient); }
				iChannelClient = iChannel.addClient(iClient);
				iChannelClient.setChanMode(nPrefix);

				sName = "";
				sModes = "";
				nPrefix = 0;
			}
		}
	}
	
	// This horrible thing handles PRIVMSGs and NOTICES
	// This inclues CTCPs and CTCPReplies
	// It handles all 3 targets (Channel, Private, Unknown)
	// Actions are handled here aswell separately from CTCPs.
	// Each type has 5 Calls, making 15 callbacks handled here.
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
						CallUnknownCTCP(iClient, sCTCP, sMessage, token[0]);
					} else {
						CallUnknownMessage(iClient, sMessage, token[0]);
					}
				} else {
					CallUnknownAction(iClient, sMessage, token[0]);
				}
			} else if (sParam.equalsIgnoreCase("NOTICE")) {
				if (isCTCP) {
					CallUnknownCTCPReply(iClient, sCTCP, sMessage, token[0]);
				} else {
					CallUnknownNotice(iClient, sMessage, token[0]);
				}
			}
		}
		
		// 			
		// sParam.equalsIgnoreCase("NOTICE")
	}
	
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
	
	private void ProcessJoinChannel(String sParam, String token[]) {
		// :nick!ident@host JOIN (:)#Channel
		if (token.length < 3) { return; }
		ClientInfo iClient;
		ChannelInfo iChannel;
		ChannelClientInfo iChannelClient;
		
		iClient = GetClientInfo(token[0]);
		iChannel = GetChannelInfo(token[token.length-1]);
		
		if (iClient == null) { iClient = new ClientInfo(token[0]); hClientList.put(iClient.getNickname().toLowerCase(),iClient); }
		if (iChannel == null) { 
			if (iClient != cMyself) {
				CallErrorInfo(errWarning+errCanContinue, "Got join for channel ("+token[token.length-1]+") that I am not on. [User: "+token[0]+"]");
			}
			iChannel = new ChannelInfo(token[2]);
			hChannelList.put(iChannel.getName().toLowerCase(),iChannel);
		} else {
			// This is only done if we are on the channel. Else we wait for names.
			iChannelClient = iChannel.addClient(iClient);
			CallChannelJoin(iChannel, iChannelClient);	
		}
	}	
	
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
				hChannelList.remove(iChannel);
			}
		}
	}
	
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
					hChannelList.remove(iChannel);
				} else {
					iChannel.delClient(iClient);
				}
			}
		}

		CallQuit(iClient,sReason);
		if (iClient == cMyself) {
			hClientList.clear();
		} else {
			hClientList.remove(iClient);
		}
	}	

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

	private void ProcessEndOfMOTD(int nParam, String token[]) {
		ParseChanModes();
		ParseChanPrefix();
		ParsePrefixModes();
		ParseUserModes();
		CallMOTDEnd();
	}

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
		if (cMyself == null) { cMyself = new ClientInfo(sNick); }
	}

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

// eo