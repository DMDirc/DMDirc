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

import java.net.URL;

import javax.swing.ImageIcon;

import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IDataIn;
import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IDataOut;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.ServerFrame;

/**
 * Handles the raw window (which shows the user raw data being sent and
 * received to/from the server).
 * @author chris
 */
public final class Raw extends FrameContainer implements IDataIn, IDataOut {
    
    /**
     * The server object that's being monitored.
     */
    private Server server;
    /**
     * A serverframe instance used for displaying the raw data.
     */
    private ServerFrame frame;
    
    /**
     * Creates a new instance of Raw.
     * @param newServer the server to monitor
     */
    public Raw(final Server newServer) {
        this.server = newServer;
        
        final ClassLoader cldr = this.getClass().getClassLoader();
        final URL imageURL = cldr.getResource("uk/org/ownage/dmdirc/res/raw.png");
        imageIcon = new ImageIcon(imageURL);
        
        frame = new ServerFrame(server);
        frame.setTitle("(Raw log)");
        frame.addInternalFrameListener(this);
        MainFrame.getMainFrame().addChild(frame);
        frame.setTabCompleter(server.getTabCompleter());
        frame.setFrameIcon(imageIcon);
        
        frame.open();
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
    
    /**
     * Returns the internal frame belonging to this object.
     * @return This object's internal frame
     */
    public CommandWindow getFrame() {
        return frame;
    }
    
    /**
     * Called when the parser receives a line of data. The data is simply
     * logged to the raw window.
     * @param parser A reference to the IRC parser
     * @param data The data that was received
     */
    public void onDataIn(final IRCParser parser, final String data) {
        addLine("rawIn", data);
    }
    
    /**
     * Called when the parser receives a line of data. The data is simply
     * logged to the raw window.
     * @param parser A reference to the IRC parser
     * @param data The data that was received
     * @param fromParser true if sent from parser, false otherwise
     */
    public void onDataOut(final IRCParser parser, final String data,
            final boolean fromParser) {
        addLine("rawOut", data);
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
