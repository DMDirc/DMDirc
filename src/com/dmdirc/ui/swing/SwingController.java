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
import com.dmdirc.ui.IconManager;
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
import com.dmdirc.updater.Update;
import com.dmdirc.util.ReturnableThread;

import java.awt.Font;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;

import java.util.concurrent.Semaphore;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.layout.UnitValue;

/**
 * Controls the main swing UI.
 */
public final class SwingController implements UIController {

    /** Singleton instance of MainFrame. */
    private static MainFrame me;
    /** Status bar. */
    private static SwingStatusBar statusBar;
    /** Semaphore used for controlling access to statusBar. */
    private static final Semaphore STATUSBAR_SEMAPHORE = new Semaphore(1);

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
                        me = new MainFrame(getSwingStatusBar());
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
                Logger.appError(ErrorLevel.FATAL, "Unable to create MainFrame",
                        ex.getCause());
            //Ignore
            }
        }

        return me;
    }

    /** {@inheritDoc} */
    @Override
    public StatusBar getStatusBar() {
        return getSwingStatusBar();
    }

    /**
     * Retrieves the Swing Status Bar used by this UI.
     * 
     * @return This UI's status bar
     */
    private static SwingStatusBar getSwingStatusBar() {
        STATUSBAR_SEMAPHORE.acquireUninterruptibly();

        if (statusBar == null) {
            final ReturnableThread<SwingStatusBar> returnable = new ReturnableThread<SwingStatusBar>() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    setObject(new SwingStatusBar());
                }
            };
            try {
                SwingUtilities.invokeAndWait(returnable);
            } catch (InterruptedException ex) {
                Logger.userError(ErrorLevel.HIGH,
                        "Unable to create the status bar");
            } catch (InvocationTargetException ex) {
                Logger.userError(ErrorLevel.HIGH,
                        "Unable to create the status bar");
            }
            statusBar = returnable.getObject();
        }

        STATUSBAR_SEMAPHORE.release();

        return statusBar;
    }

    /** {@inheritDoc} */
    @Override
    public ChannelWindow getChannel(final Channel channel) {
        final ReturnableThread<ChannelFrame> returnable = new ReturnableThread<ChannelFrame>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new ChannelFrame(channel));
            }
        };
        try {
            SwingUtilities.invokeAndWait(returnable);
        } catch (InterruptedException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create channel: " +
                    channel.toString());
        } catch (InvocationTargetException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create channel: " +
                    channel.toString());
        }
        return returnable.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public ServerWindow getServer(final Server server) {
        final ReturnableThread<ServerFrame> returnable = new ReturnableThread<ServerFrame>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new ServerFrame(server));
            }
        };
        try {
            SwingUtilities.invokeAndWait(returnable);
        } catch (InterruptedException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create server: " +
                    server.toString());
        } catch (InvocationTargetException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create server: " +
                    server.toString());
        }
        return returnable.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public QueryWindow getQuery(final Query query) {
        final ReturnableThread<QueryFrame> returnable = new ReturnableThread<QueryFrame>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new QueryFrame(query));
            }
        };
        try {
            SwingUtilities.invokeAndWait(returnable);
        } catch (InterruptedException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create query: " +
                    query.toString());
        } catch (InvocationTargetException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create query: " +
                    query.toString());
        }
        return returnable.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public Window getWindow(final FrameContainer owner) {
        final ReturnableThread<CustomFrame> returnable = new ReturnableThread<CustomFrame>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new CustomFrame(owner));
            }
        };
        try {
            SwingUtilities.invokeAndWait(returnable);
        } catch (InterruptedException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create window: " +
                    owner.toString());
        } catch (InvocationTargetException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create window: " +
                    owner.toString());
        }
        return returnable.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public InputWindow getInputWindow(final WritableFrameContainer owner,
            final CommandParser commandParser) {
        final ReturnableThread<CustomInputFrame> returnable = new ReturnableThread<CustomInputFrame>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new CustomInputFrame(owner, commandParser));
            }
        };
        try {
            SwingUtilities.invokeAndWait(returnable);
        } catch (InterruptedException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create window: " +
                    owner.toString());
        } catch (InvocationTargetException ex) {
            Logger.userError(ErrorLevel.HIGH, "Unable to create window: " +
                    owner.toString());
        }
        return returnable.getObject();
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
                final SwingFirstRunWizard wizard =
                        new SwingFirstRunWizard(firstRun);
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
        final boolean aaSetting = IdentityManager.getGlobalConfig().
                getOptionBool("ui", "antialias", true);
        System.setProperty("awt.useSystemAAFontSettings",
                Boolean.toString(aaSetting));
        System.setProperty("swing.aatext", Boolean.toString(aaSetting));

        // This will do nothing on non OS X Systems
        if (Apple.isApple()) {
            final Apple apple = Apple.getApple();

            apple.setUISettings();
            apple.setListener();
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

        try {
            UIUtilities.initUISettings();
            UIManager.setLookAndFeel(UIUtilities.getLookAndFeel(IdentityManager.getGlobalConfig().
                    getOption("ui", "lookandfeel", "")));
            PlatformDefaults.setDefaultRowAlignment(UnitValue.CENTER);

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
        if (getActiveWindow() == null) {
            return null;
        } else {
            return getActiveWindow().getContainer().getServer();
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
