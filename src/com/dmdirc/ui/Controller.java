/*
 * Controller.java
 * 
 * Created on 21-Jul-2007, 16:10:20
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.dmdirc.ui;

import com.dmdirc.Channel;
import com.dmdirc.Server;
import com.dmdirc.ui.components.StatusBar;
import com.dmdirc.ui.framemanager.FrameManager;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.ServerWindow;
import com.dmdirc.ui.interfaces.UIController;

/**
 * Controls the main swing UI.
 * 
 * @author Chris
 */
public class Controller implements UIController {

    /** {@inheritDoc} */
    public MainFrame getMainWindow() {
        return MainFrame.getMainFrame();
    }

    /** {@inheritDoc} */
    public StatusBar getStatusBar() {
        return MainFrame.getMainFrame().getStatusBar();
    }

    /** {@inheritDoc} */
    public FrameManager getFrameManager() {
        return MainFrame.getMainFrame().getFrameManager();
    }

    /** {@inheritDoc} */
    public ChannelWindow getChannel(final Channel channel) {
        return new ChannelFrame(channel);
    }

    /** {@inheritDoc} */
    public ServerWindow getServer(final Server server) {
        return new ServerFrame(server);
    }

}
