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

package uk.org.ownage.dmdirc.commandparser;

import uk.org.ownage.dmdirc.FrameContainer;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.identities.ConfigManager;

/**
 * A command window is a window that allows the user to input a command (that's
 * passed to a command parser). This interface includes methods that are required
 * to allow the commands to interact with the user via the window.
 * @author chris
 */
public interface CommandWindow {
    
    /**
     * Adds a line of text to the main text area of the window.
     * @param line The line to be added
     */
    void addLine(String line);
    
    /**
     * Formats the arguments using the Formatter, then adds the result to the
     * main text area.
     * @param messageType The type of this message
     * @param args The arguments for the message
     */
    void addLine(String messageType, Object... args);
    
    /**
     * Formats the arguments using the Formatter, then adds the result to the
     * main text area.
     * @param messageType The type of this message
     * @param args The arguments for the message
     */
    void addLine(StringBuffer messageType, Object... args);    
    
    /**
     * Clears the main text area of the command window.
     */
    void clear();
    
    /**
     * Retrieves the config manager for this command window.
     * @return This window's config manager
     */
    ConfigManager getConfigManager();
    
    /**
     * Retrieves the command Parser for this command window.
     * @return This window's command Parser
     */
    CommandParser getCommandParser();
    
    /**
     * Retrieves the server associated with this command window.
     * @return This window's associated server instance
     */
    Server getServer();
    
    /**
     * Retrieves the container that owns this command window.
     * @return The container that owns this command window.
     */
    FrameContainer getContainer();
    
    /**
     * Determines if the current frame is visible.
     *
     * @return boolean visibility
     */
    boolean isVisible();
    
}
