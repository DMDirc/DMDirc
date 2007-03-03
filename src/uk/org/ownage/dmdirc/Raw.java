/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package uk.org.ownage.dmdirc;

import uk.org.ownage.dmdirc.commandparser.ServerCommandParser;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IDataIn;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IDataOut;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.ServerFrame;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.parser.callbacks.CallbackNotFound;

/**
 * Handles the raw window (which shows the user raw data being sent and
 * received to/from the server)
 * @author chris
 */
public class Raw implements IDataIn, IDataOut {
    
    /**
     * The server object that's being monitored
     */
    private Server server;
    /**
     * A serverframe instance used for displaying the raw data
     */
    private ServerFrame frame;
    
    /**
     * Creates a new instance of Raw
     * @param server the server to monitor
     */
    public Raw(Server server) {
        this.server = server;
        
        frame = new ServerFrame(server, new ServerCommandParser(server));
        frame.setTitle("(Raw log)");
        
        MainFrame.getMainFrame().addChild(frame);
        
        try {
            server.getParser().getCallbackManager().addCallback("OnDataIn", this);
            server.getParser().getCallbackManager().addCallback("OnDataOut", this);
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.FATAL, ex);
        }
    }
    
    void close() {
        server.getParser().getCallbackManager().delCallback("OnDataIn", this);
        server.getParser().getCallbackManager().delCallback("OnDataOut", this);
        
        frame.setVisible(false);
        MainFrame.getMainFrame().delChild(frame);
        frame = null;
        server = null;
    }
    
    public void onDataIn(IRCParser tParser, String sData) {
        frame.addLine("<<< "+sData);
    }
    
    public void onDataOut(IRCParser tParser, String sData, boolean bFromParser) {
        frame.addLine(">>> "+sData);
    }
    
}
