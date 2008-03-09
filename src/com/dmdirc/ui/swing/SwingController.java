/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.IconManager;
import com.dmdirc.Main;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.QueryWindow;
import com.dmdirc.ui.interfaces.ServerWindow;
import com.dmdirc.ui.interfaces.StatusBar;
import com.dmdirc.ui.interfaces.UIController;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.swing.components.FeedbackNag;
import com.dmdirc.ui.swing.components.SwingStatusBar;
import com.dmdirc.ui.swing.dialogs.updater.SwingUpdaterDialog;
import com.dmdirc.ui.swing.dialogs.URLDialog;
import com.dmdirc.ui.swing.dialogs.channelsetting.ChannelSettingsDialog;
import com.dmdirc.ui.swing.dialogs.error.ErrorListDialog;
import com.dmdirc.ui.swing.dialogs.wizard.firstrun.SwingFirstRunWizard;
import com.dmdirc.ui.swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.ui.swing.dialogs.wizard.WizardListener;
import com.dmdirc.util.resourcemanager.ResourceManager;
import com.dmdirc.updater.Update;

import java.awt.Font;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Controls the main swing UI.
 */
public final class SwingController implements UIController {

    /** Singleton instance of MainFrame. */
    private static MainFrame me;
    /** Status bar. */
    private static SwingStatusBar statusBar;
    /** Wizard. */
    private SwingFirstRunWizard wizard;

    /** Instantiates a new SwingController. */
    public SwingController() {
    //Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public synchronized MainFrame getMainWindow() {
        return getMainFrame();
    }

    /**
     * Retrieves the main window used by this UI.
     *
     * @return This UI's main window
     */
    public synchronized static MainFrame getMainFrame() {
        if (me == null) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    /** {@inheritDoc} */
                    @Override
                    public void run() {
                        me = new MainFrame();
                        statusBar = me.getStatusBar();
                    }
                });
                SwingUtilities.invokeLater(new Runnable() {

                    /** {@inheritDoc} */
                    @Override
                    public void run() {
                        ErrorListDialog.getErrorListDialog();
                    }
                });
            } catch (InterruptedException ex) {
            //Ignore
            } catch (InvocationTargetException ex) {
                Logger.appError(ErrorLevel.HIGH, "Unable to create MainFrame",
                        ex.getCause());
            //Ignore
            }
        }

        return me;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized StatusBar getStatusBar() {
        if (statusBar == null) {
            getMainWindow();
        }

        return statusBar;
    }

    /** {@inheritDoc} */
    @Override
    public ChannelWindow getChannel(final Channel channel) {
        return new ChannelFrame(channel);
    }

    /** {@inheritDoc} */
    @Override
    public ServerWindow getServer(final Server server) {
        return new ServerFrame(server);
    }

    /** {@inheritDoc} */
    @Override
    public QueryWindow getQuery(final Query query) {
        return new QueryFrame(query);
    }

    /** {@inheritDoc} */
    @Override
    public Window getWindow(final FrameContainer owner) {
        return new CustomFrame(owner);
    }

    /** {@inheritDoc} */
    @Override
    public InputWindow getInputWindow(final WritableFrameContainer owner,
            final CommandParser commandParser) {
        return new CustomInputFrame(owner, commandParser);
    }

    /** {@inheritDoc} */
    @Override
    public SwingUpdaterDialog getUpdaterDialog(final List<Update> updates) {
        return SwingUpdaterDialog.getSwingUpdaterDialog(updates);
    }

    /** {@inheritDoc} */
    @Override
    public void showFirstRunWizard() {
        showFirstRunWizard(true);
    }

    /** {@inheritDoc} */
    @Override
    public void showMigrationWizard() {
        showFirstRunWizard(false);
    }

    /**
     * Shows a first run wizard, or a migration wizard.
     * 
     * @param firstRun First run?
     */
    private synchronized void showFirstRunWizard(final boolean firstRun) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final WizardListener listener = new WizardListener() {

                    /** {@inheritDoc} */
                    @Override
                    public void wizardFinished() {
                        synchronized (SwingController.this) {
                            SwingController.this.notifyAll();
                        }
                    }

                    /** {@inheritDoc} */
                    @Override
                    public void wizardCancelled() {
                        synchronized (SwingController.this) {
                            SwingController.this.notifyAll();
                        }
                    }
                };
                wizard = new SwingFirstRunWizard(firstRun);
                wizard.getWizardDialog().addWizardListener(listener);
                wizard.display();
            }
            });
        try {
            wait();
        } catch (InterruptedException ex) {
        //Ignore
        }
    }

    /** {@inheritDoc} */
    @Override
    public void showChannelSettingsDialog(final Channel channel) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                ChannelSettingsDialog.showChannelSettingsDialog(channel);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void showServerSettingsDialog(final Server server) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                ServerSettingsDialog.showServerSettingsDialog(server);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void initUISettings() {
        // For this to work it *HAS* to be before anything else UI related.
        if (IdentityManager.getGlobalConfig().hasOption("ui", "antialias")) {
            final String aaSetting =
                    IdentityManager.getGlobalConfig().getOption("ui",
                    "antialias");
            System.setProperty("awt.useSystemAAFontSettings", aaSetting);
            System.setProperty("swing.aatext", aaSetting);
        } else {
            IdentityManager.getConfigIdentity().setOption("ui", "antialias",
                    "true");
            System.setProperty("awt.useSystemAAFontSettings", "true");
            System.setProperty("swing.aatext", "true");
        }
        
        // This will do nothing on non OS X Systems
        if (Apple.isApple()) {
            Apple apple = Apple.getApple();
            
            apple.setUISettings();
            apple.setListener();
        }

        try {
            UIUtilities.initUISettings();

            if (IdentityManager.getGlobalConfig().hasOption("ui", "lookandfeel")) {
                final String lnfName =
                        UIUtilities.getLookAndFeel(IdentityManager.getGlobalConfig().
                        getOption("ui", "lookandfeel"));
                if (!lnfName.isEmpty()) {
                    UIManager.setLookAndFeel(lnfName);
                }
            }

            final Font defaultFont = new Font(Font.DIALOG, Font.TRUETYPE_FONT,
                    12);
            if (UIManager.getFont("TextField.font") == null) {
                UIManager.put("TextField.font", defaultFont);
            }
            if (UIManager.getFont("TextPane.font") == null) {
                UIManager.put("TextPane.font", defaultFont);
            }

            UIManager.put("Tree.collapsedIcon",
                    IconManager.getIconManager().getIcon("nothing"));
            UIManager.put("Tree.expandedIcon",
                    IconManager.getIconManager().getIcon("nothing"));

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

        Toolkit.getDefaultToolkit().getSystemEventQueue().
                push(new DMDircEventQueue());
    }

    /** {@inheritDoc} */
    @Override
    public Window getActiveWindow() {
        if (me == null) {
            return null;
        }
        return me.getActiveFrame();
    }

    /** {@inheritDoc} */
    @Override
    public Server getActiveServer() {
        if (me == null) {
            return null;
        }
        if (me.getActiveFrame() == null) {
            return null;
        } else {
            return me.getActiveFrame().getContainer().getServer();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void showURLDialog(final URI url) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                URLDialog.showURLDialog(url);

            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void showFeedbackNag() {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                new FeedbackNag();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void showMessageDialog(final String title, final String message) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, message, title,
                        JOptionPane.PLAIN_MESSAGE);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public String getUserInput(final String prompt) {
        return JOptionPane.showInputDialog(prompt);
    }
}
