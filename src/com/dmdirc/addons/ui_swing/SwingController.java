/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.addons.ui_swing.components.frames.ServerFrame;
import com.dmdirc.addons.ui_swing.components.frames.CustomFrame;
import com.dmdirc.addons.ui_swing.components.frames.QueryFrame;
import com.dmdirc.addons.ui_swing.components.frames.CustomInputFrame;
import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.ui.IconManager;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.core.dialogs.sslcertificate.SSLCertificateDialogModel;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.QueryWindow;
import com.dmdirc.ui.interfaces.ServerWindow;
import com.dmdirc.ui.interfaces.StatusBar;
import com.dmdirc.ui.interfaces.UIController;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.addons.ui_swing.components.pluginpanel.PluginPanel;
import com.dmdirc.addons.ui_swing.components.statusbar.FeedbackNag;
import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
import com.dmdirc.addons.ui_swing.components.themepanel.ThemePanel;
import com.dmdirc.addons.ui_swing.dialogs.updater.SwingUpdaterDialog;
import com.dmdirc.addons.ui_swing.dialogs.url.URLDialog;
import com.dmdirc.addons.ui_swing.dialogs.channelsetting.ChannelSettingsDialog;
import com.dmdirc.addons.ui_swing.dialogs.error.ErrorListDialog;
import com.dmdirc.addons.ui_swing.dialogs.prefs.URLConfigPanel;
import com.dmdirc.addons.ui_swing.dialogs.prefs.UpdateConfigPanel;
import com.dmdirc.addons.ui_swing.wizard.firstrun.SwingFirstRunWizard;
import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.addons.ui_swing.dialogs.sslcertificate.SSLCertificateDialog;
import com.dmdirc.addons.ui_swing.wizard.WizardListener;
import com.dmdirc.config.Identity;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.config.prefs.validator.NumericalValidator;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.updater.Update;
import com.dmdirc.util.ReturnableThread;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import net.miginfocom.layout.PlatformDefaults;

/**
 * Controls the main swing UI.
 */
public final class SwingController extends Plugin implements UIController {

    /** Logger to use. */
    private static final java.util.logging.Logger LOGGER =
            java.util.logging.Logger.getLogger(SwingController.class.getName());
    /** Singleton instance of MainFrame. */
    private MainFrame me;
    /** Status bar. */
    private SwingStatusBar statusBar;
    /** Top level window list. */
    private final List<java.awt.Window> windows;
    /** Waiting on mainframe creation. */
    private AtomicBoolean mainFrameCreated = new AtomicBoolean(false);
    /** Error dialog. */
    private ErrorListDialog errorDialog;

    /** Instantiates a new SwingController. */
    public SwingController() {
        windows = new ArrayList<java.awt.Window>();
    }

    /**
     * Does the main frame exist?
     *
     * @return true iif mainframe exists
     */
    protected boolean hasMainFrame() {
        return mainFrameCreated.get();
    }

    /** {@inheritDoc} */
    @Override
    public MainFrame getMainWindow() {
        return getMainFrame();
    }

    /**
     * Retrieves the main window used by this UI.
     *
     * @return This UI's main window
     */
    public MainFrame getMainFrame() {
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
    public SwingStatusBar getSwingStatusBar() {
        return statusBar;
    }

    /** {@inheritDoc} */
    @Override
    public ChannelWindow getChannel(final Channel channel) {
        return UIUtilities.invokeAndWait(new ReturnableThread<ChannelFrame>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new ChannelFrame(channel, SwingController.this));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public ServerWindow getServer(final Server server) {
        return UIUtilities.invokeAndWait(new ReturnableThread<ServerFrame>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new ServerFrame(server, SwingController.this));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public QueryWindow getQuery(final Query query) {
        return UIUtilities.invokeAndWait(new ReturnableThread<QueryFrame>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new QueryFrame(query, SwingController.this));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Window getWindow(final FrameContainer owner) {
        return UIUtilities.invokeAndWait(new ReturnableThread<CustomFrame>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new CustomFrame(owner, SwingController.this));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public InputWindow getInputWindow(final WritableFrameContainer owner,
            final CommandParser commandParser) {
        LOGGER.finest("getInputWindow()");

        return UIUtilities.invokeAndWait(new ReturnableThread<CustomInputFrame>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                LOGGER.finest("getInputWindow(): run");
                setObject(new CustomInputFrame(owner, commandParser,
                        SwingController.this));
                LOGGER.finest("getInputWindow(): object set: " + getObject());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public SwingUpdaterDialog getUpdaterDialog(final List<Update> updates) {
        return UIUtilities.invokeAndWait(new ReturnableThread<SwingUpdaterDialog>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(SwingUpdaterDialog.getSwingUpdaterDialog(updates, me));
            }
        });
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
        final Semaphore semaphore = new Semaphore(0);
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final WizardListener listener = new WizardListener() {

                    /** {@inheritDoc} */
                    @Override
                    public void wizardFinished() {
                        semaphore.release();
                    }

                    /** {@inheritDoc} */
                    @Override
                    public void wizardCancelled() {
                        semaphore.release();
                    }
                };
                final SwingFirstRunWizard wizard =
                        new SwingFirstRunWizard(firstRun);
                wizard.getWizardDialog().addWizardListener(listener);
                wizard.display();
            }
        });
        semaphore.acquireUninterruptibly();
    }

    /** {@inheritDoc} */
    @Override
    public void showChannelSettingsDialog(final Channel channel) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                ChannelSettingsDialog.showChannelSettingsDialog(channel, me);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void showServerSettingsDialog(final Server server) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                ServerSettingsDialog.showServerSettingsDialog(server, me);
            }
        });
    }

    /**
     * Updates the look and feel to the current config setting.
     */
    void updateLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIUtilities.getLookAndFeel(IdentityManager.
                    getGlobalConfig().getOption("ui", "lookandfeel")));
            final int state = UIUtilities.invokeAndWait(new ReturnableThread<Integer>() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    setObject(me.getExtendedState());
                }
            });
            UIUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    SwingUtilities.updateComponentTreeUI(errorDialog);
                }
            });
            for (final java.awt.Window window : getTopLevelWindows()) {
                UIUtilities.invokeLater(new Runnable() {

                    /** {@inheritDoc} */
                    @Override
                    public void run() {
                        SwingUtilities.updateComponentTreeUI(window);
                        if (window != me) {
                            window.pack();
                        }
                    }
                });
            }
            UIUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    me.setExtendedState(state);
                }
            });
        } catch (ClassNotFoundException ex) {
            Logger.userError(ErrorLevel.LOW,
                    "Unable to change Look and Feel: " +
                    ex.getMessage());
        } catch (InstantiationException ex) {
            Logger.userError(ErrorLevel.LOW,
                    "Unable to change Look and Feel: " +
                    ex.getMessage());
        } catch (IllegalAccessException ex) {
            Logger.userError(ErrorLevel.LOW,
                    "Unable to change Look and Feel: " +
                    ex.getMessage());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.userError(ErrorLevel.LOW,
                    "Unable to change Look and Feel: " +
                    ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void initUISettings() {
        // For this to work it *HAS* to be before anything else UI related.
        final boolean aaSetting = IdentityManager.getGlobalConfig().
                getOptionBool("ui", "antialias");
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
            UIManager.setLookAndFeel(UIUtilities.getLookAndFeel(IdentityManager.
                    getGlobalConfig().getOption("ui", "lookandfeel")));

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

        if ("Metal".equals(UIManager.getLookAndFeel().getName())) {
            PlatformDefaults.setPlatform(PlatformDefaults.WINDOWS_XP);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Window getActiveWindow() {
        return me.getActiveFrame();
    }

    /** {@inheritDoc} */
    @Override
    public Server getActiveServer() {
        if (!mainFrameCreated.get()) {
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
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                URLDialog.showURLDialog(url, me);

            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void showSSLCertificateDialog(final SSLCertificateDialogModel model) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                new SSLCertificateDialog(me, model).setVisible(true);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void showFeedbackNag() {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                new FeedbackNag(SwingController.this);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void showMessageDialog(final String title, final String message) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, message, title,
                        JOptionPane.PLAIN_MESSAGE);
            }
        });
    }

    /**
     * Shows the error dialog.
     */
    public void showErrorDialog() {
        errorDialog.display();
    }

    /** {@inheritDoc} */
    @Override
    public String getUserInput(final String prompt) {
        return JOptionPane.showInputDialog(prompt);
    }

    /** {@inheritDoc} */
    @Override
    public PreferencesInterface getPluginPrefsPanel() {
        return UIUtilities.invokeAndWait(new ReturnableThread<PluginPanel>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new PluginPanel(me));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public PreferencesInterface getUpdatesPrefsPanel() {
        return UIUtilities.invokeAndWait(new ReturnableThread<PreferencesInterface>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new UpdateConfigPanel());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public PreferencesInterface getUrlHandlersPrefsPanel() {
        return UIUtilities.invokeAndWait(new ReturnableThread<PreferencesInterface>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new URLConfigPanel(me));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public PreferencesInterface getThemesPrefsPanel() {
        return UIUtilities.invokeAndWait(new ReturnableThread<PreferencesInterface>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new ThemePanel());
            }
        });
    }

    /**
     * Returns the current look and feel.
     * 
     * @return Current look and feel
     */
    public static String getLookAndFeel() {
        return UIManager.getLookAndFeel().getName();
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException(
                    "Swing UI can't be run in a headless environment");
        }

        Toolkit.getDefaultToolkit().getSystemEventQueue().
                push(new DMDircEventQueue(this));

        UIUtilities.invokeAndWait(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                initUISettings();
                me = new MainFrame(SwingController.this);
                mainFrameCreated.set(true);
                statusBar = me.getStatusBar();
                errorDialog = new ErrorListDialog(me);
            }
        });

        if (!mainFrameCreated.get()) {
            throw new IllegalStateException(
                    "Main frame not created. Unable to continue.");
        }

        if (System.getProperty("java.vm.name", "unknown").contains("OpenJDK")) {
            JOptionPane.showMessageDialog(null, "OpenJDK has known graphical " +
                    "issues and as such is unsupported by DMDirc.  Please " +
                    "consider using the official JRE.", "Unsupported JRE",
                    JOptionPane.WARNING_MESSAGE);
        }

        Main.setUI(this);
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void domainUpdated() {
        final Identity defaults = IdentityManager.getAddonIdentity();

        defaults.setOption("ui", "textPaneFontName", "Dialog");
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesManager manager) {
        manager.getCategory("GUI").addSubCategory(createGeneralCategory());

    }

    /**
     * Creates the "Advanced" category.
     */
    private PreferencesCategory createGeneralCategory() {
        final PreferencesCategory general = new PreferencesCategory("Swing UI",
                "These config options apply only to the swing UI.",
                "category-gui");

        final Map<String, String> lafs = new HashMap<String, String>();
        final Map<String, String> framemanagers = new HashMap<String, String>();
        final Map<String, String> fmpositions = new HashMap<String, String>();

        framemanagers.put(
                "com.dmdirc.addons.ui_swing.framemanager.tree.TreeFrameManager",
                "Treeview");
        framemanagers.put(
                "com.dmdirc.addons.ui_swing.framemanager.buttonbar.ButtonBar",
                "Button bar");

        fmpositions.put("top", "Top");
        fmpositions.put("bottom", "Bottom");
        fmpositions.put("left", "Left");
        fmpositions.put("right", "Right");

        final LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();

        lafs.put("Native", "Native");
        for (LookAndFeelInfo laf : plaf) {
            lafs.put(laf.getName(), laf.getName());
        }

        general.addSetting(new PreferencesSetting("ui", "lookandfeel",
                "Look and feel", "The Java look and feel to use", lafs));
        general.addSetting(new PreferencesSetting("ui", "framemanager",
                "Window manager", "Which window manager should be used?",
                framemanagers).setRestartNeeded());
        general.addSetting(new PreferencesSetting("ui", "framemanagerPosition",
                "Window manager position", "Where should the window " +
                "manager be positioned?", fmpositions).setRestartNeeded());
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "stylelinks", "Style links", "Style links in text areas"));
        general.addSetting(new PreferencesSetting(PreferencesType.FONT,
                "ui", "textPaneFontName", "Textpane font",
                "Font for the textpane"));

        general.addSubCategory(createNicklistCategory());
        general.addSubCategory(createTreeViewCategory());
        general.addSubCategory(createAdvancedCategory());

        return general;
    }

    /**
     * Creates the "Advanced" category.
     */
    private PreferencesCategory createAdvancedCategory() {
        final PreferencesCategory advanced = new PreferencesCategory("Advanced",
                "");
        final Map<String, String> options = new HashMap<String, String>();

        options.put("alwaysShow", "Always show");
        options.put("neverShow", "Never show");
        options.put("showWhenMaximised", "Show only when windows maximised");

        advanced.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                new NumericalValidator(10, -1), "ui", "frameBufferSize",
                "Window buffer size", "The maximum number of lines in a window" +
                " buffer"));
        advanced.addSetting(new PreferencesSetting("ui", "mdiBarVisibility",
                "MDI Bar Visibility", "Controls the visibility of the MDI bar",
                options));
        advanced.addSetting(
                new PreferencesSetting(PreferencesType.BOOLEAN, "ui",
                "useOneTouchExpandable", "Use one touch expandable split panes?",
                "Use one touch expandable arrows for collapsing/expanding the split panes"));

        return advanced;
    }

    /**
     * Creates the "Treeview" category.
     */
    private PreferencesCategory createTreeViewCategory() {
        final PreferencesCategory treeview = new PreferencesCategory("Treeview",
                "", "treeview");

        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "treeview", "backgroundcolour", "Treeview background colour",
                "Background colour to use for the treeview"));
        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "treeview", "foregroundcolour", "Treeview foreground colour",
                "Foreground colour to use for the treeview"));
        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "treeviewRolloverColour", "Treeview rollover colour",
                "Background colour to use when the mouse cursor is over a node"));
        treeview.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "treeview", "sortwindows", "Sort windows",
                "Sort windows belonging to servers in the treeview?"));
        treeview.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "treeview", "sortservers", "Sort servers",
                "Sort servers in the treeview?"));
        treeview.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "treeviewActiveBold", "Active node bold",
                "Make the active node bold?"));
        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "treeviewActiveBackground", "Active node background",
                "Background colour to use for active treeview node"));
        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "treeviewActiveForeground", "Active node foreground",
                "Foreground colour to use for active treeview node"));

        return treeview;
    }

    /**
     * Creates the "Nicklist" category.
     */
    private PreferencesCategory createNicklistCategory() {
        final PreferencesCategory nicklist = new PreferencesCategory("Nicklist",
                "", "nicklist");

        nicklist.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "nicklistbackgroundcolour", "Nicklist background colour",
                "Background colour to use for the nicklist"));
        nicklist.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "nicklistforegroundcolour", "Nicklist foreground colour",
                "Foreground colour to use for the nicklist"));
        nicklist.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "nickListAltBackgroundColour",
                "Alternate background colour",
                "Background colour to use for every other nicklist entry"));
        nicklist.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "nicklist", "sortByMode", "Sort nicklist by user mode",
                "Sort nicknames by the modes that they have?"));
        nicklist.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "nicklist", "sortByCase", "Sort nicklist by case",
                "Sort nicknames in a case-sensitive manner?"));

        return nicklist;
    }

    /**
     * Adds a top level window to the window list.
     * 
     * @param source New window
     */
    protected void addTopLevelWindow(final java.awt.Window source) {
        synchronized (windows) {
            windows.add(source);
        }
    }

    /**
     * Deletes a top level window to the window list.
     * 
     * @param source Old window
     */
    protected void delTopLevelWindow(final java.awt.Window source) {
        synchronized (windows) {
            windows.remove(source);
        }
    }

    /**
     * Returns a list of top level windows.
     *
     * @return Top level window list
     */
    public List<java.awt.Window> getTopLevelWindows() {
        synchronized (windows) {
            return windows;
        }
    }
}
