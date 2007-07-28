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

import java.io.Serializable;

/**
 * Handles the raw window (which shows the user raw data being sent and
 * received to/from the server).
 * @author chris
 */
public final class Raw extends WritableFrameContainer implements IDataIn, 
        IDataOut, Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The server object that's being monitored. */
    private Server server;
    
    /** A serverframe instance used for displaying the raw data.*/
    private InputWindow frame;
    
    /**
     * Creates a new instance of Raw.
     *
     * @param newServer the server to monitor
     */
    public Raw(final Server newServer) {
        super();
        
        this.server = newServer;
        
        icon = IconManager.getIconManager().getIcon("raw");
        
        frame = Main.getUI().getInputWindow(this, newServer.getFrame().getCommandParser());
        frame.setTitle("(Raw log)");
        frame.getInputHandler().setTabCompleter(server.getTabCompleter());
        frame.setFrameIcon(icon);
        
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
            Logger.appError(ErrorLevel.HIGH, "Unable to register raw callbacks", ex);
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
        Main.getUI().getMainWindow().delChild(frame);
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
        frame.addLine("rawIn", sData);
    }
    
    /** {@inheritDoc} */
    public void onDataOut(final IRCParser tParser, final String sData,
            final boolean bFromParser) {
        frame.addLine("rawOut", sData);
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
    
    /** {@inheritDoc} */
    public void sendLine(final String line) {
        server.sendLine(line);
    }
    
    /** {@inheritDoc} */
    public int getMaxLineLength() {
        return server.getMaxLineLength();
    }
}
