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

package com.dmdirc.ui.interfaces;

import com.dmdirc.Channel;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.CommandParser;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.components.StatusBar;
import com.dmdirc.ui.framemanager.FrameManager;

/**
 * Defines the methods that should be implemented by UI controllers. Controllers
 * handle the various aspects of a UI implementation.
 *
 * @author Chris
 */
public interface UIController {
    
    /**
     * Retrieves the main window used by this UI.
     *
     * @return This UI's main window
     */
    MainFrame getMainWindow();
    
    /**
     * Retrieves the status bar component used by this UI.
     *
     * @return This UI's status bar
     */
    StatusBar getStatusBar();
    
    /**
     * Retrieves the frame manager used by this UI.
     *
     * @return This UI's frame manager
     */
    FrameManager getFrameManager();
    
    /**
     * Creates a channel window for the specified channel.
     *
     * @param channel The channel that is requesting a window be made
     * @return A new channel window for the specified channel
     */
    ChannelWindow getChannel(Channel channel);
    
    /**
     * Creates a server window for the specified server.
     *
     * @param server The server that is requesting a window be made
     * @return A new server window for the specified server
     */
    ServerWindow getServer(Server server);
    
    /**
     * Creates a query window for the specified query.
     *
     * @param query The query that is requesting a window be made
     * @return A new query window for the specified query
     */
    QueryWindow getQuery(Query query);
    
    /**
     * Creates a new custom input window instance.
     * 
     * @param owner The owner of the input window
     * @param commandParser The command parser to be used
     * @return A new custom input window
     */
    InputWindow getInputWindow(WritableFrameContainer owner, CommandParser commandParser);
    
    /**
     * Creates a new preferences panel for the specified parent.
     *
     * @param parent Preferences panel parent
     * @param text Preferences panel title
     *
     * @return PreferencesPanel
     */
    PreferencesPanel getPreferencesPanel(PreferencesInterface parent, String title);
    
    /**
     * Initialises any settings required by this UI (this is always called
     * before any aspect of the UI is instansiated).
     */
    void initUISettings();
    
}
