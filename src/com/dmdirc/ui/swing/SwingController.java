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

package com.dmdirc.ui.swing;


import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.PreferencesInterface;
import com.dmdirc.ui.interfaces.PreferencesPanel;
import com.dmdirc.ui.interfaces.QueryWindow;
import com.dmdirc.ui.interfaces.ServerWindow;
import com.dmdirc.ui.interfaces.StatusBar;
import com.dmdirc.ui.interfaces.UIController;
import com.dmdirc.ui.interfaces.UpdaterDialog;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.swing.components.SwingPreferencesPanel;
import com.dmdirc.ui.swing.components.SwingStatusBar;
import com.dmdirc.ui.swing.dialogs.SwingUpdaterDialog;
import com.dmdirc.ui.swing.dialogs.channelsetting.ChannelSettingsDialog;
import com.dmdirc.ui.swing.dialogs.error.ErrorListDialog;
import com.dmdirc.ui.swing.dialogs.firstrunwizard.SwingFirstRunWizard;
import com.dmdirc.ui.swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.updater.Update;

import java.awt.Toolkit;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Controls the main swing UI.
 */
public final class SwingController implements UIController {
    
    /**
     * Singleton instance of MainFrame.
     */
    private static MainFrame me;
    
    /** Status bar. */
    private SwingStatusBar statusBar;
    
    /** Instantiates a new SwingController. */
    public SwingController() {
        //Do nothing
    }
    
    /** {@inheritDoc} */
    public synchronized MainFrame getMainWindow() {
        if (me == null) {
            statusBar = new SwingStatusBar();
            me = new MainFrame(statusBar);
            ErrorListDialog.getErrorListDialog();
        }
        return me;
    }
    
    /** {@inheritDoc} */
    public synchronized StatusBar getStatusBar() {
        if (statusBar == null)  {
            getMainWindow();
        }
        
        return statusBar;
    }
    
    /** {@inheritDoc} */
    public FrameManager getFrameManager() {
        return getMainWindow().getFrameManager();
    }
    
    /** {@inheritDoc} */
    public ChannelWindow getChannel(final Channel channel) {
        return new ChannelFrame(channel);
    }
    
    /** {@inheritDoc} */
    public ServerWindow getServer(final Server server) {
        return new ServerFrame(server);
    }
    
    /** {@inheritDoc} */
    public QueryWindow getQuery(final Query query) {
        return new QueryFrame(query);
    }
    
    /** {@inheritDoc} */
    public Window getWindow(final FrameContainer owner) {
        return new CustomFrame(owner);
    }
    
    /** {@inheritDoc} */
    public InputWindow getInputWindow(final WritableFrameContainer owner,
            final CommandParser commandParser) {
        return new CustomInputFrame(owner, commandParser);
    }
    
    /** {@inheritDoc} */
    public PreferencesPanel getPreferencesPanel(
            final PreferencesInterface parent, final String title) {
        return new SwingPreferencesPanel(parent, title);
    }
    
    /** {@inheritDoc} */
    public UpdaterDialog getUpdaterDialog(final List<Update> updates) {
        return new SwingUpdaterDialog(updates);
    }
    
    /** {@inheritDoc} */
    public void showFirstRunWizard() {
        new SwingFirstRunWizard().display();
    }
    
    /** {@inheritDoc} */
    public void showMigrationWizard() {
        new SwingFirstRunWizard(false).display();
    }    
    
    /** {@inheritDoc} */
    public void showChannelSettingsDialog(final Channel channel) {
        ChannelSettingsDialog.getChannelSettingDialog(channel).setVisible(true);
    }
    
    /** {@inheritDoc} */
    public void showServerSettingsDialog(final Server server) {
        new ServerSettingsDialog(server).setVisible(true);
    }
    
    /** {@inheritDoc} */
    public void initUISettings() {
        // For this to work it *HAS* to be before anything else UI related.
        if (IdentityManager.getGlobalConfig().hasOption("ui", "antialias")) {
            final String aaSetting = IdentityManager.getGlobalConfig().getOption("ui", "antialias");
            System.setProperty("awt.useSystemAAFontSettings", aaSetting);
            System.setProperty("swing.aatext", aaSetting);
        } else {
            IdentityManager.getConfigIdentity().setOption("ui", "antialias", "true");
            System.setProperty("awt.useSystemAAFontSettings", "true");
            System.setProperty("swing.aatext", "true");
        }
        
        try {
            UIUtilities.initUISettings();
            
            final String lnfName = UIUtilities.getLookAndFeel(
                    IdentityManager.getGlobalConfig().getOption("ui", "lookandfeel"));
            
            if (IdentityManager.getGlobalConfig().hasOption("ui", "lookandfeel") && !lnfName.isEmpty()) {
                UIManager.setLookAndFeel(lnfName);
            }
            
            //These are likely to change lots, and i cant test them - Greboid
            UIManager.put("apple.awt.showGrowBox", true);
            UIManager.put("apple.laf.useScreenMenuBar", true);
            UIManager.put("com.apple.mrj.application.apple.menu.about.name", "DMDirc: " + Main.VERSION);
            UIManager.put("com.apple.mrj.application.growbox.intrudes", false);
            UIManager.put("com.apple.mrj.application.live-resize", true);
            
        } catch (UnsupportedOperationException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to set UI Settings");
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to set UI Settings");
        } catch (IllegalAccessException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to set UI Settings");
        } catch (InstantiationException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to set UI Settings");
        } catch (ClassNotFoundException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to set UI Settings");
        }
        
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new DMDircEventQueue());
    }
}
