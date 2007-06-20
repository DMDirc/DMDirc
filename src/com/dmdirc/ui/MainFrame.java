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

package com.dmdirc.ui;

import com.dmdirc.Config;
import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.components.Frame;
import com.dmdirc.ui.components.StatusBar;
import com.dmdirc.ui.dialogs.ActionsManagerDialog;
import com.dmdirc.ui.dialogs.NewServerDialog;
import com.dmdirc.ui.dialogs.PluginDialog;
import com.dmdirc.ui.dialogs.PreferencesDialog;
import com.dmdirc.ui.dialogs.ProfileEditorDialog;
import com.dmdirc.ui.dialogs.about.AboutDialog;
import com.dmdirc.ui.framemanager.FrameManager;
import com.dmdirc.ui.framemanager.FramemanagerPosition;
import com.dmdirc.ui.framemanager.MainFrameManager;
import com.dmdirc.ui.framemanager.windowmenu.WindowMenuFrameManager;
import com.dmdirc.ui.interfaces.Window;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;

/**
 * The main application frame.
 */
public final class MainFrame extends JFrame implements WindowListener,
        ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 7;
    
    /**
     * The number of pixels each new internal frame is offset by.
     */
    private static final int FRAME_OPENING_OFFSET = 30;
    
    /**
     * Singleton instance of MainFrame.
     */
    private static MainFrame me;
    /**
     * Whether the internal frames are maximised or not.
     */
    private boolean maximised;
    /**
     * The current number of pixels to displace new frames in the X direction.
     */
    private int xOffset;
    /**
     * The current number of pixels to displace new frames in the Y direction.
     */
    private int yOffset;
    /**
     * The main application icon.
     */
    private final ImageIcon imageIcon;
    
    /**
     * The frame manager that's being used.
     */
    private final transient MainFrameManager mainFrameManager;
    
    /**
     * The window menu frame manager that's being used.
     */
    private final transient WindowMenuFrameManager windowListFrameManager;
    
    /** Dekstop pane. */
    private JDesktopPane desktopPane;
    
    /** Plugin menu item. */
    private JMenu pluginsMenu;
    
    /** Windows menu item. */
    private JMenu windowsMenu;
    
    /** Main panel. */
    private JPanel frameManagerPanel;
    
    /** Add server menu item. */
    private JMenuItem miAddServer;
    
    /** Preferences menu item. */
    private JMenuItem miPreferences;
    
    /** Toggle state menu item. */
    private JMenuItem toggleStateMenuItem;
    
    /** status bar. */
    private StatusBar statusBar;
    
    /** Frame manager position. */
    private FramemanagerPosition position;
    
    /**
     * Creates new form MainFrame.
     */
    private MainFrame() {
        super();
        
        windowListFrameManager = new WindowMenuFrameManager();
        mainFrameManager = new MainFrameManager();
        
        initComponents();
        initKeyHooks();
        
        setTitle(getTitlePrefix());
        
        // Load an icon
        final ClassLoader cldr = this.getClass().getClassLoader();
        
        final URL imageURL = cldr.getResource("com/dmdirc/res/icon.png");
        imageIcon = new ImageIcon(imageURL);
        setIconImage(imageIcon.getImage());
        
        mainFrameManager.setParent(frameManagerPanel);
        
        // Get the Location of the mouse pointer
        final PointerInfo myPointerInfo = MouseInfo.getPointerInfo();
        // Get the Device (screen) the mouse pointer is on
        final GraphicsDevice myDevice = myPointerInfo.getDevice();
        // Get the configuration for the device
        final GraphicsConfiguration myGraphicsConfig = myDevice.getDefaultConfiguration();
        // Get the bounds of the device
        final Rectangle gcBounds = myGraphicsConfig.getBounds();
        // Calculate the center of the screen
        // gcBounds.x and gcBounds.y give the co ordinates where the screen
        // starts. gcBounds.width and gcBounds.height return the size in pixels
        // of the screen.
        final int xPos = gcBounds.x + ((gcBounds.width - getWidth()) / 2);
        final int yPos = gcBounds.y + ((gcBounds.height - getHeight()) / 2);
        // Set the location of the window
        setLocation(xPos, yPos);
        
        setVisible(true);
        
        miAddServer.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                NewServerDialog.showNewServerDialog();
            }
        });
        
        miPreferences.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                PreferencesDialog.showPreferencesDialog();
            }
        });
        
        toggleStateMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                try {
                    getActiveFrame().setMaximum(!getActiveFrame().isMaximum());
                } catch (PropertyVetoException ex) {
                    Logger.error(ErrorLevel.WARNING, "Unable to maximise window", ex);
                }
            }
        });
        
        addWindowListener(this);
        
        checkWindowState();
    }
    
    /**
     * Returns the singleton instance of MainFrame.
     * @return MainFrame instance
     */
    public static synchronized MainFrame getMainFrame() {
        if (me == null) {
            me = new MainFrame();
        }
        return me;
    }
    
    /**
     * Indicates whether the main frame has been initialised or not.
     * @return True iff the main frame exists
     */
    public static boolean hasMainFrame() {
        return me != null;
    }
    
    /**
     * Adds the specified window as a child of the main frame.
     *
     * @param window the window to be added
     */
    public void addChild(final Window window) {
        final JInternalFrame frame = (JInternalFrame) window;
        
        // Add the frame
        desktopPane.add(frame);
        
        // Make sure it'll fit with our offsets
        if (frame.getWidth() + xOffset > desktopPane.getWidth()) {
            xOffset = 0;
        }
        if (frame.getHeight() + yOffset > desktopPane.getHeight()) {
            yOffset = 0;
        }
        
        // Position the frame
        frame.setLocation(xOffset, yOffset);
        frame.moveToFront();
        
        // Increase the offsets
        xOffset += FRAME_OPENING_OFFSET;
        yOffset += FRAME_OPENING_OFFSET;
    }
    
    /**
     * Removes the specified window from our desktop pane.
     *
     * @param window The window to be removed
     */
    public void delChild(final Window window) {
        desktopPane.remove((JInternalFrame) window);
        if (desktopPane.getAllFrames().length == 0) {
            setTitle(getTitlePrefix());
        }
    }
    
    /**
     * Sets the active internal frame to the one specified.
     * @param frame The frame to be activated
     */
    public void setActiveFrame(final Window frame) {
        try {
            ((JInternalFrame) frame).setVisible(true);
            ((JInternalFrame) frame).setIcon(false);
            ((JInternalFrame) frame).moveToFront();
            ((JInternalFrame) frame).setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to set active window", ex);
        }
        
        if (maximised) {
            setTitle(getTitlePrefix() + " - " + frame.getTitle());
        }
        
        ActionManager.processEvent(CoreActionType.CLIENT_FRAME_CHANGED, null, frame.getContainer());
    }
    
    /**
     * Retrieves the frame manager that's currently in use.
     * @return The current frame manager
     */
    public FrameManager getFrameManager() {
        return mainFrameManager;
    }
    
    /**
     * Returns the size of the frame manager.
     *
     * @return Frame manager size.
     */
    public int getFrameManagerSize() {
        if (position == FramemanagerPosition.LEFT
                || position == FramemanagerPosition.RIGHT) {
            return frameManagerPanel.getWidth();
        } else {
            return frameManagerPanel.getHeight();
        }
    }
    
    /**
     * Retrieves the application icon.
     * @return The application icon
     */
    public ImageIcon getIcon() {
        return imageIcon;
    }
    
    /**
     * Returns the window that is currently active.
     * @return The active window
     */
    public Window getActiveFrame() {
        if (desktopPane.getSelectedFrame() instanceof Window) {
            return (Window) desktopPane.getSelectedFrame();
        } else {
            return null;
        }
    }
    
    /**
     * Adds a JMenuItem to the plugin menu.
     * @param menuItem The menu item to be added.
     */
    public void addPluginMenu(final JMenuItem menuItem) {
        if (pluginsMenu.getComponents().length == 1) {
            final JSeparator seperator = new JSeparator();
            pluginsMenu.add(seperator);
        }
        
        pluginsMenu.add(menuItem);
    }
    
    /**
     * Removes a JMenuItem from the plugin menu.
     * @param menuItem The menu item to be removed.
     */
    public void removePluginMenu(final JMenuItem menuItem) {
        pluginsMenu.remove(menuItem);
        
        if (pluginsMenu.getComponents().length == 2) {
            pluginsMenu.remove(2);
        }
    }
    
    /**
     * Sets whether or not the internal frame state is currently maximised.
     * @param max whether the frame is maxomised
     */
    public void setMaximised(final boolean max) {
        maximised = max;
        
        if (max) {
            if (getActiveFrame() != null) {
                setTitle(getTitlePrefix() + " - " + getActiveFrame().getTitle());
            }
        } else {
            setTitle(getTitlePrefix());
            for (JInternalFrame frame : desktopPane.getAllFrames()) {
                try {
                    frame.setMaximum(false);
                } catch (PropertyVetoException ex) {
                    Logger.error(ErrorLevel.ERROR, "Unable to maximise window", ex);
                }
            }
        }
        
        checkWindowState();
    }
    
    /**
     * Returns a prefix for use in the titlebar. Includes the version number
     * if the config option is set.
     * @return Titlebar prefix
     */
    public String getTitlePrefix() {
        if (Config.getOptionBool("ui", "showversion")) {
            return "DMDirc " + Main.VERSION;
        } else {
            return "DMDirc";
        }
    }
    
    /**
     * Gets whether or not the internal frame state is currently maximised.
     * @return True iff frames should be maximised, false otherwise
     */
    public boolean getMaximised() {
        return maximised;
    }
    
    /**
     * Checks the current state of the internal frames, and configures the
     * window menu to behave appropriately.
     */
    private void checkWindowState() {
        if (getActiveFrame() == null) {
            toggleStateMenuItem.setEnabled(false);
            return;
        }
        
        toggleStateMenuItem.setEnabled(true);
        
        if (maximised) {
            toggleStateMenuItem.setText("Restore");
            toggleStateMenuItem.setMnemonic('r');
            toggleStateMenuItem.invalidate();
        } else {
            toggleStateMenuItem.setText("Maximise");
            toggleStateMenuItem.setMnemonic('m');
            toggleStateMenuItem.invalidate();
        }
    }
    
    /**
     * Returns the status bar instance.
     *
     * @return StatusBar instance
     */
    public StatusBar getStatusBar() {
        return statusBar;
    }
    
    /** {@inheritDoc}. */
    public void windowOpened(final WindowEvent windowEvent) {
        //ignore
    }
    
    /**
     * Called when the window is closing. Saves the config and has all servers
     * disconnect with the default close method
     * @param windowEvent The event associated with this callback
     */
    public void windowClosing(final WindowEvent windowEvent) {
        ServerManager.getServerManager().closeAll(Config.getOption("general", "closemessage"));
        quit();
    }
    
    /** {@inheritDoc}. */
    public void windowClosed(final WindowEvent windowEvent) {
        //ignore
    }
    
    /** {@inheritDoc}. */
    public void windowIconified(final WindowEvent windowEvent) {
        //ignore
    }
    
    /** {@inheritDoc}. */
    public void windowDeiconified(final WindowEvent windowEvent) {
        //ignore
    }
    
    /** {@inheritDoc}. */
    public void windowActivated(final WindowEvent windowEvent) {
        //ignore
    }
    
    /** {@inheritDoc}. */
    public void windowDeactivated(final WindowEvent windowEvent) {
        //ignore
    }
    
    /**
     * Initialises the components for this frame.
     */
    private void initComponents() {
        final JSplitPane mainSplitPane = new JSplitPane();
        
        frameManagerPanel = new JPanel();
        desktopPane = new JDesktopPane();
        desktopPane.setBackground(new Color(238, 238, 238));
        statusBar = new StatusBar();
        
        initSplitPane(mainSplitPane);
        
        initMenuBar();
        
        setPreferredSize(new Dimension(800, 600));
        
        getContentPane().setLayout(new BorderLayout(0, 0));
        
        getContentPane().add(mainSplitPane, BorderLayout.CENTER);
        
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        setTitle("DMDirc");
        frameManagerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        desktopPane.setBorder(UIManager.getBorder("TextField.border"));
        
        pack();
    }
    
    /**
     * Initialises the split pane.
     *
     * @param mainSplitPane JSplitPane to initialise
     */
    private void initSplitPane(final JSplitPane mainSplitPane) {
        mainSplitPane.setBorder(null);
        
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        
        mainSplitPane.setDividerSize(SMALL_BORDER);
        mainSplitPane.setOneTouchExpandable(false);
        
        position = FramemanagerPosition.getPosition(
                Config.getOption("ui", "framemanagerPosition"));
        
        if (position == FramemanagerPosition.UNKNOWN) {
            position = FramemanagerPosition.LEFT;
        }
        
        if (!mainFrameManager.canPositionVertically()
        && (position == FramemanagerPosition.LEFT
                || position == FramemanagerPosition.RIGHT)) {
            position = FramemanagerPosition.BOTTOM;
        }
        if (!mainFrameManager.canPositionHorizontally()
        && (position == FramemanagerPosition.TOP
                || position == FramemanagerPosition.BOTTOM)) {
            position = FramemanagerPosition.LEFT;
        }
        
        switch (position) {
            case TOP:
                mainSplitPane.setTopComponent(frameManagerPanel);
                mainSplitPane.setBottomComponent(desktopPane);
                mainSplitPane.setResizeWeight(0.0);
                mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE,
                        Config.getOptionInt("ui", "frameManagerSize", 50)));
                frameManagerPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE, 50));
                desktopPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                desktopPane.setMinimumSize(new Dimension(Integer.MAX_VALUE, 300));
                break;
            case LEFT:
                mainSplitPane.setLeftComponent(frameManagerPanel);
                mainSplitPane.setRightComponent(desktopPane);
                mainSplitPane.setResizeWeight(0.0);
                mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(
                        Config.getOptionInt("ui", "frameManagerSize", 150),
                        Integer.MAX_VALUE));
                frameManagerPanel.setMinimumSize(new Dimension(150, Integer.MAX_VALUE));
                desktopPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                desktopPane.setMinimumSize(new Dimension(300, Integer.MAX_VALUE));
                break;
            case BOTTOM:
                mainSplitPane.setTopComponent(desktopPane);
                mainSplitPane.setBottomComponent(frameManagerPanel);
                mainSplitPane.setResizeWeight(1.0);
                mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE,
                        Config.getOptionInt("ui", "frameManagerSize", 50)));
                frameManagerPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE,
                        50));
                desktopPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                desktopPane.setMinimumSize(new Dimension(Integer.MAX_VALUE, 300));
                break;
            case RIGHT:
                mainSplitPane.setLeftComponent(desktopPane);
                mainSplitPane.setRightComponent(frameManagerPanel);
                mainSplitPane.setResizeWeight(1.0);
                mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(
                        Config.getOptionInt("ui", "frameManagerSize", 50),
                        Integer.MAX_VALUE));
                frameManagerPanel.setMinimumSize(new Dimension(50, Integer.MAX_VALUE));
                desktopPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                desktopPane.setMinimumSize(new Dimension(300, Integer.MAX_VALUE));
                break;
            default:
                break;
        }
        
        mainSplitPane.setContinuousLayout(true);
    }
    
    /** Initialises the key hooks. */
    private void initKeyHooks() {
        final KeyStroke[] keyStrokes = new KeyStroke[12];
        
        keyStrokes[0] = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
        keyStrokes[1] = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        keyStrokes[2] = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        keyStrokes[3] = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
        keyStrokes[4] = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
        keyStrokes[5] = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
        keyStrokes[6] = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
        keyStrokes[7] = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        keyStrokes[8] = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
        keyStrokes[9] = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
        keyStrokes[10] = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
        keyStrokes[11] = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
        
        for (final KeyStroke keyStroke : keyStrokes) {
            getRootPane().getActionMap().put(
                    KeyEvent.getKeyText(keyStroke.getKeyCode()) + "Action",
                    new AbstractAction(
                    KeyEvent.getKeyText(keyStroke.getKeyCode()) + "Action") {
                private static final long serialVersionUID = 5;
                public void actionPerformed(final ActionEvent evt) {
                    ActionManager.processEvent(CoreActionType.CLIENT_FKEY_PRESSED,
                            null, KeyStroke.getKeyStroke(keyStroke.getKeyCode(),
                            evt.getModifiers()));
                }
            }
            );
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(keyStroke.getKeyCode(), 0),
                    KeyEvent.getKeyText(keyStroke.getKeyCode()) + "Action");
        }
    }
    
    /** Initialises the menu bar. */
    private void initMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        JMenuItem menuItem;
        
        final JMenu fileMenu = new JMenu();
        miAddServer = new JMenuItem();
        miPreferences = new JMenuItem();
        final JMenu settingsMenu = new JMenu();
        final JMenu helpMenu = new JMenu();
        toggleStateMenuItem = new JMenuItem();
        
        settingsMenu.setText("Settings");
        settingsMenu.setMnemonic('s');
        
        fileMenu.setMnemonic('f');
        fileMenu.setText("File");
        
        miAddServer.setText("New Server...");
        miAddServer.setMnemonic('n');
        fileMenu.add(miAddServer);
        
        miPreferences.setText("Preferences");
        miPreferences.setMnemonic('p');
        settingsMenu.add(miPreferences);
        
        menuItem = new JMenuItem();
        menuItem.setMnemonic('m');
        menuItem.setText("Profile Manager");
        menuItem.setActionCommand("Profile");
        menuItem.addActionListener(this);
        settingsMenu.add(menuItem);
        
        menuItem = new JMenuItem();
        menuItem.setMnemonic('a');
        menuItem.setText("Actions Manager");
        menuItem.setActionCommand("Actions");
        menuItem.addActionListener(this);
        settingsMenu.add(menuItem);
        
        menuItem = new JMenuItem();
        menuItem.setMnemonic('x');
        menuItem.setText("Exit");
        menuItem.setActionCommand("Exit");
        menuItem.addActionListener(this);
        fileMenu.add(menuItem);
        
        
        populateWindowMenu(new HashMap<FrameContainer, JMenuItem>());
        
        
        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");
        
        menuItem = new JMenuItem();
        menuItem.setMnemonic('a');
        menuItem.setText("About");
        menuItem.setActionCommand("About");
        menuItem.addActionListener(this);
        helpMenu.add(menuItem);
        
        pluginsMenu = new JMenu("Plugins");
        pluginsMenu.setMnemonic('p');
        settingsMenu.add(pluginsMenu);
        
        menuItem = new JMenuItem();
        menuItem.setMnemonic('m');
        menuItem.setText("Manage plugins");
        menuItem.setActionCommand("ManagePlugins");
        menuItem.addActionListener(this);
        pluginsMenu.add(menuItem);
        
        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);
        menuBar.add(windowsMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Initialises the window menu.
     *
     * @param windows Map of windows
     */
    public void populateWindowMenu(final Map<FrameContainer, JMenuItem> windows) {
        if (windowsMenu == null) {
            windowsMenu = new JMenu();
        }
        if (windowsMenu.isShowing()) {
            windowsMenu.setSelected(false);
            windowsMenu.setPopupMenuVisible(false);
        }
        windowsMenu.removeAll();
        
        JMenuItem menuItem;
        
        windowsMenu.setMnemonic('w');
        windowsMenu.setText("Window");
        
        windowsMenu.add(toggleStateMenuItem);
        
        menuItem = new JMenuItem();
        menuItem.setMnemonic('n');
        menuItem.setText("Minimise");
        menuItem.setActionCommand("Minimise");
        menuItem.addActionListener(this);
        windowsMenu.add(menuItem);
        
        menuItem = new JMenuItem();
        menuItem.setMnemonic('c');
        menuItem.setText("Close");
        menuItem.setActionCommand("Close");
        menuItem.addActionListener(this);
        windowsMenu.add(menuItem);
        
        menuItem = new JMenuItem();
        menuItem.setMnemonic('a');
        menuItem.setText("Close all");
        menuItem.setActionCommand("CloseAll");
        menuItem.addActionListener(this);
        windowsMenu.add(menuItem);
        
        windowsMenu.addSeparator();
        
        int i = 0;
        final Collection<JMenuItem> windowsValues = windows.values();
        for (JMenuItem mi : windowsValues) {
            if (i > 34) {
                windowsMenu.add(new JMenuItem("..."));
                break;
            }
            windowsMenu.add(mi);
            i++;
        }
    }
    
    /** Quits the client. */
    public void quit() {
        Config.setOption("ui", "frameManagerSize",
                String.valueOf(MainFrame.getMainFrame().getFrameManagerSize()));
        Main.quit();
    }
    
    /**
     * {@inheritDoc}.
     */
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("About")) {
            AboutDialog.showAboutDialog();
        } else if (e.getActionCommand().equals("Profile")) {
            ProfileEditorDialog.showActionsManagerDialog();
        } else if (e.getActionCommand().equals("Exit")) {
            quit();
        } else if (e.getActionCommand().equals("ManagePlugins")) {
            PluginDialog.showPluginDialog();
        } else if (e.getActionCommand().equals("Actions")) {
            ActionsManagerDialog.showActionsManagerDialog();
        } else if (e.getActionCommand().equals("Minimise")) {
            ((Frame) MainFrame.getMainFrame().getActiveFrame()).minimise();
        } else if (e.getActionCommand().equals("Close")) {
            ((Frame) MainFrame.getMainFrame().getActiveFrame()).close();
        } else if (e.getActionCommand().equals("CloseAll")) {
            ServerManager.getServerManager().closeAll();
        }
    }
    
    /** Initialises UI Settings. */
    public static void initUISettings() {
        // For this to work it *HAS* to be before anything else UI related.
        if (Config.hasOption("ui", "antialias")) {
            final String aaSetting = Config.getOption("ui", "antialias");
            System.setProperty("awt.useSystemAAFontSettings", aaSetting);
            System.setProperty("swing.aatext", aaSetting);
        }
        final String lnfName = getLookAndFeel(Config.getOption("ui", "lookandfeel"));
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            
            final FontUIResource font = new FontUIResource("Dialog", Font.PLAIN , 12);
            
            UIManager.put("TableHeader.font", font);
            UIManager.put("ToggleButton.font", font);
            UIManager.put("ScrollPane.font", font);
            UIManager.put("Spinner.font", font);
            UIManager.put("RadioButtonMenuItem.font", font);
            UIManager.put("Slider.font", font);
            UIManager.put("OptionPane.font", font);
            UIManager.put("ToolBar.font", font);
            UIManager.put("CheckBoxMenuItem.font", font);
            UIManager.put("Table.font", font);
            UIManager.put("MenuBar.font", font);
            UIManager.put("PopupMenu.font", font);
            UIManager.put("DesktopIcon.font", font);
            UIManager.put("TextPane.font", font);
            UIManager.put("ProgressBar.font", font);
            UIManager.put("FormattedTextField.font", font);
            UIManager.put("ColorChooser.font", font);
            UIManager.put("Viewport.font", font);
            UIManager.put("ToolTip.font", font);
            UIManager.put("Label.font", font);
            UIManager.put("TextField.font", font);
            UIManager.put("PasswordField.font", font);
            UIManager.put("Button.font", font);
            UIManager.put("RadioButton.font", font);
            UIManager.put("CheckBox.font", font);
            UIManager.put("ComboBox.font", font);
            UIManager.put("Menu.font", font);
            UIManager.put("List.font", font);
            UIManager.put("MenuItem.font", font);
            UIManager.put("Panel.font", font);
            UIManager.put("TitledBorder.font", font);
            UIManager.put("TabbedPane.font", font);
            UIManager.put("Tree.font", font);
            UIManager.put("InternalFrame.titleFont", font);
            UIManager.put("EditorPane.font", font);
            UIManager.put("TextArea.font", font);
            
            UIManager.put("CheckBoxMenuItem.acceleratorFont", 
                    font.deriveFont((float) font.getSize() - 2));
            UIManager.put("Menu.acceleratorFont", 
                    font.deriveFont((float) font.getSize() - 2));
            UIManager.put("MenuItem.acceleratorFont", 
                    font.deriveFont((float) font.getSize() - 2));
            
            UIManager.put("swing.boldMetal", false);
            UIManager.put("InternalFrame.useTaskBar", false);
            UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(1, 1, 1, 1));
            UIManager.put("Tree.scrollsOnExpand", true);
            UIManager.put("Tree.scrollsHorizontallyAndVertically", true);
            
            UIManager.put("Tree.dropCellBackground", Color.WHITE);
            UIManager.put("Tree.selectionBackground", Color.WHITE);
            UIManager.put("Tree.textBackground", Color.WHITE);
            UIManager.put("Tree.selectionBorderColor", Color.WHITE);
            UIManager.put("Tree.drawsFocusBorder", false);
            UIManager.put("Tree.drawHorizontalLines", true);
            UIManager.put("Tree.drawVerticalLines", true);
            UIManager.put("Tree.background", Color.WHITE);
            
            if (Config.hasOption("ui", "lookandfeel") && lnfName.length() != 0) {
                UIManager.setLookAndFeel(lnfName);
            }
        } catch (InstantiationException ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to set look and feel: " + lnfName, ex);
        } catch (ClassNotFoundException ex) {
            Logger.error(ErrorLevel.ERROR, "Look and feel not available: " + lnfName, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.error(ErrorLevel.ERROR, "Look and feel not available: " + lnfName, ex);
        } catch (IllegalAccessException ex) {
            Logger.error(ErrorLevel.ERROR, "Unable to set look and feel: " + lnfName, ex);
        }
    }
    
    /**
     * Returns the class name of the look and feel from its display name.
     *
     * @param displayName Look and feel display name
     *
     * @return Look and feel class name or a zero length string
     */
    private static String getLookAndFeel(final String displayName) {
        final StringBuilder classNameBuilder = new StringBuilder();
        
        if (displayName != null && !"".equals(displayName)) {
            for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                if (laf.getName().equals(displayName)) {
                    classNameBuilder.setLength(0);
                    classNameBuilder.append(laf.getClassName());
                    break;
                }
            }
        }
        return classNameBuilder.toString();
    }
}
