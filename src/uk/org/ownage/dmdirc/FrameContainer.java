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

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;

import uk.org.ownage.dmdirc.commandparser.CommandWindow;

/**
 * Standard interface for all objects that contain an innerframe. Provides
 * methods for interfacing with the frame via the owner.
 * @author chris
 */
public interface FrameContainer {
    
    /**
     * Requests that this object's frame be activated.
     */
    void activateFrame();
    
    /**
     * Determines if this object, or any of its children, owns the specified
     * frame.
     * @param target The frame to check
     * @return true iff the frame is owned by this object, false otherwise
     */
    boolean ownsFrame(JInternalFrame target);
    
    /**
     * Adds a line to the main text area of the frame.
     * @param line line to be added
     */
    void addLine(String line);
    
    /**
     * Sends a notification to the frame manager that this object has been
     * updated.
     */
    void sendNotification();
    
    /**
     * Returns the internal frame associated with this object.
     * @return The internal frame associated with this object
     */
    CommandWindow getFrame();
    
    /**
     * Returns a string identifier for this object/its frame.
     * @return String identifier
     */
    String toString();
    
    /**
     * Returns a reference to the icon that is currently being used by the
     * frame.
     * @return The frame's icon
     */
    ImageIcon getIcon();
    
    /**
     * Closes this container (and it's associated frame).
     */
    void close();
    
    /**
     * Returns the server instance associated with this frame.
     *
     * @return the associated server connection
     */
    Server getServer();
    
}
