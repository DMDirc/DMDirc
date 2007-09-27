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

package com.dmdirc.ui.dummy;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.CommandParser;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.MainWindow;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;
import com.dmdirc.ui.interfaces.QueryWindow;
import com.dmdirc.ui.interfaces.ServerWindow;
import com.dmdirc.ui.interfaces.StatusBar;
import com.dmdirc.ui.interfaces.UIController;
import com.dmdirc.ui.interfaces.UpdaterDialog;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.updater.Update;

import java.util.List;

/**
 * Implements a dummy UI controller.
 */
public final class DummyController implements UIController {
    
    /** Main window. */
    private final MainWindow mainWindow = new DummyMainWindow();
    
    /**
     * Creates a new instance of DummyController.
     */
    public DummyController() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    public MainWindow getMainWindow() {
        return mainWindow;
    }
    
    /** {@inheritDoc} */
    public StatusBar getStatusBar() {
        return new DummyStatusBar();
    }
    
    /** {@inheritDoc} */
    public FrameManager getFrameManager() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** {@inheritDoc} */
    public ChannelWindow getChannel(final Channel channel) {
        return new DummyChannelWindow(channel);
    }
    
    /** {@inheritDoc} */
    public ServerWindow getServer(final Server server) {
        return new DummyServerWindow(server);
    }
    
    /** {@inheritDoc} */
    public QueryWindow getQuery(final Query query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** {@inheritDoc} */
    public Window getWindow(final FrameContainer owner) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** {@inheritDoc} */
    public InputWindow getInputWindow(final WritableFrameContainer owner, 
            final CommandParser commandParser) {
        return new DummyInputWindow(owner, commandParser);
    }
    
    /** {@inheritDoc} */
    public PreferencesPanel getPreferencesPanel(final PreferencesInterface parent, 
            final String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** {@inheritDoc} */
    public UpdaterDialog getUpdaterDialog(final List<Update> updates) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** {@inheritDoc} */
    public void showFirstRunWizard() {
        System.out.println("DummyController.showFirstRunWizard()");
    }
    
    /** {@inheritDoc} */
    public void showMigrationWizard() {
        System.out.println("DummyController.showMigrationWizard()");
    }    
    
    /** {@inheritDoc} */
    public void showChannelSettingsDialog(final Channel channel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** {@inheritDoc} */
    public void showServerSettingsDialog(final Server server) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /** {@inheritDoc} */
    public void initUISettings() {
        // Do nothing
    }
    
}
