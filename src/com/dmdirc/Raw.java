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

package com.dmdirc;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.IRCParser;
import com.dmdirc.parser.callbacks.CallbackNotFound;
import com.dmdirc.parser.callbacks.interfaces.IDataIn;
import com.dmdirc.parser.callbacks.interfaces.IDataOut;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.ServerFrame;

import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Handles the raw window (which shows the user raw data being sent and
 * received to/from the server).
 * @author chris
 */
public final class Raw extends FrameContainer implements IDataIn, IDataOut {
    
    /** The server object that's being monitored. */
    private Server server;
    
    /** A serverframe instance used for displaying the raw data.*/
    private ServerFrame frame;
    
    /**
     * Creates a new instance of Raw.
     * 
     * @param newServer the server to monitor
     */
    public Raw(final Server newServer) {
        this.server = newServer;
        
        final ClassLoader cldr = this.getClass().getClassLoader();
        final URL imageURL = cldr.getResource("com/dmdirc/res/raw.png");
        imageIcon = new ImageIcon(imageURL);
        
        frame = new ServerFrame(server);
        frame.setTitle("(Raw log)");
        frame.addInternalFrameListener(this);
        MainFrame.getMainFrame().addChild(frame);
        frame.getInputHandler().setTabCompleter(server.getTabCompleter());
        frame.setFrameIcon(imageIcon);
        
        frame.open();
    }
    
    /**
     * Registers the data callbacks for this raw window.
     */
    public void registerCallbacks() {
        try {
            server.getParser().getCallbackManager().addCallback("OnDataIn", this);
            server.getParser().getCallbackManager().addCallback("OnDataOut", this);
        } catch (CallbackNotFound ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to register raw callbacks", ex);
        }
    }
    
    /**
     * Closes the raw window. Removes parser callbacks, removes the actual.
     * frame, and removes references to the frame and server.
     */
    public void close() {
        server.getParser().getCallbackManager().delCallback("OnDataIn", this);
        server.getParser().getCallbackManager().delCallback("OnDataOut", this);
        
        frame.setVisible(false);
        MainFrame.getMainFrame().delChild(frame);
        frame = null;
        
        server.delRaw();
        
        server = null;
    }
    
    /** {@inheritDoc} */
    public InputWindow getFrame() {
        return frame;
    }
    
    /** {@inheritDoc} */
    public void onDataIn(final IRCParser tParser, final String sData) {
        addLine("rawIn", sData);
    }
    
    /** {@inheritDoc} */
    public void onDataOut(final IRCParser tParser, final String sData,
            final boolean bFromParser) {
        addLine("rawOut", sData);
    }
    
    /**
     * Returns "Raw"...
     * @return A string representation of this raw object
     */
    public String toString() {
        return "Raw";
    }
    
    /**
     * Returns the server instance associated with this frame.
     *
     * @return the associated server connection
     */
    public Server getServer() {
        return server;
    }
}
