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

import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.FramemanagerPosition;
import com.dmdirc.ui.interfaces.MainWindow;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.swing.components.InputTextFrame;
import com.dmdirc.ui.swing.components.SwingStatusBar;
import com.dmdirc.ui.swing.framemanager.buttonbar.ButtonBar;
import com.dmdirc.ui.swing.framemanager.ctrltab.CtrlTabFrameManager;
import com.dmdirc.ui.swing.framemanager.tree.TreeFrameManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * The main application frame.
 */
public final class MainFrame extends JFrame implements WindowListener,
        MainWindow, ConfigChangeListener, FrameManager {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 9;
    /** The number of pixels each new internal frame is offset by. */
    private static final int FRAME_OPENING_OFFSET = 30;
    /** Whether the internal frames are maximised or not. */
    private boolean maximised;
    /** The current number of pixels to displace new frames in the X
     * direction. */
    private int xOffset;
    /** The current number of pixels to displace new frames in the Y
     * direction. */
    private int yOffset;
    /** The main application icon. */
    private ImageIcon imageIcon;
    /** The frame manager that's being used. */
    private FrameManager mainFrameManager;
    /** Dekstop pane. */
    private DMDircDesktopPane desktopPane;
    /** Main panel. */
    private JPanel frameManagerPanel;
    /** Frame manager position. */
    private FramemanagerPosition position;
    /** Show version? */
    private boolean showVersion;
    /** Status bar. */
    private final SwingStatusBar statusBar;
    /** Menu bar. */
    private MenuBar menu;

    /**
     * Creates new form MainFrame.
     * 
     * @param statusBar The status bar to use
     */
    protected MainFrame(final SwingStatusBar statusBar) {
        super();
        
        this.statusBar = statusBar;

        initComponents();
        initKeyHooks();

        setTitle(getTitlePrefix());

        imageIcon =
                new ImageIcon(IconManager.getIconManager().getImage("icon"));
        setIconImage(imageIcon.getImage());

        // Get the Location of the mouse pointer
        final PointerInfo myPointerInfo = MouseInfo.getPointerInfo();
        // Get the Device (screen) the mouse pointer is on
        final GraphicsDevice myDevice = myPointerInfo.getDevice();
        // Get the configuration for the device
        final GraphicsConfiguration myGraphicsConfig =
                myDevice.getDefaultConfiguration();
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

        addWindowListener(this);

        showVersion =
                IdentityManager.getGlobalConfig().
                getOptionBool("ui", "showversion", false);
        IdentityManager.getGlobalConfig().
                addChangeListener("ui", "showversion", this);
        IdentityManager.getGlobalConfig().
                addChangeListener("icon", "icon", this);

        //TODO: Remove me when we switch to java7
        addWindowFocusListener(new WindowFocusListener() {

            /** {@inheritDoc} */
            @Override
            public void windowGainedFocus(WindowEvent e) {
                //Ignore
            }

            /** {@inheritDoc} */
            @Override
            public void windowLostFocus(WindowEvent e) {
                MenuSelectionManager.defaultManager().clearSelectedPath();
            }
        });
    }

    /** {@inheritDoc}. */
    @Override
    public void setActiveFrame(final Window frame) {
        if (frame != null) {
            try {
                ((JInternalFrame) frame).setVisible(true);
                ((JInternalFrame) frame).setIcon(false);
                ((JInternalFrame) frame).moveToFront();
                ((JInternalFrame) frame).setSelected(true);
            } catch (PropertyVetoException ex) {
                Logger.userError(ErrorLevel.LOW, "Unable to set active window");
            }

            if (maximised) {
                setTitle(getTitlePrefix() + " - " + frame.getTitle());
            }

            ActionManager.processEvent(CoreActionType.CLIENT_FRAME_CHANGED, null,
                    frame.getContainer());
        }
        if (frame instanceof InputTextFrame) {
            ((InputTextFrame) frame).requestInputFieldFocus();
        }
    }

    /**
     * Returns the size of the frame manager.
     *
     * @return Frame manager size.
     */
    public int getFrameManagerSize() {
        if (position == FramemanagerPosition.LEFT ||
                position == FramemanagerPosition.RIGHT) {
            return frameManagerPanel.getWidth();
        } else {
            return frameManagerPanel.getHeight();
        }
    }

    /** {@inheritDoc}. */
    @Override
    public ImageIcon getIcon() {
        return imageIcon;
    }

    /**
     * Returns the window that is currently active.
     *
     * @return The active window
     */
    public Window getActiveFrame() {
        if (desktopPane.getSelectedFrame() instanceof Window) {
            return (Window) desktopPane.getSelectedFrame();
        } else {
            return null;
        }
    }

    /** {@inheritDoc}. */
    @Override
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
                    Logger.userError(ErrorLevel.LOW, "Unable to maximise window");
                }
            }
        }
    }

    /** {@inheritDoc}. */
    @Override
    public String getTitlePrefix() {
        if (showVersion) {
            if ("SVN".equals(Main.VERSION)) {
                return "DMDirc " + Main.VERSION + " (" + Main.SVN_REVISION + ")";
            } else {
                return "DMDirc " + Main.VERSION;
            }
        } else {
            return "DMDirc";
        }
    }

    /** {@inheritDoc}. */
    @Override
    public boolean getMaximised() {
        return maximised;
    }

    /**
     * Returns the desktop pane for the frame.
     * 
     * @return JDesktopPane for the frame
     */
    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }

    /**
     * Returns the status bar for the frame.
     * 
     * @return Status bar.
     * @deprecated Should be retrieved via the controller instead
     */
    @Deprecated
    public SwingStatusBar getStatusBar() {
        return statusBar;
    }

    /** {@inheritDoc}. */
    @Override
    public void windowOpened(final WindowEvent windowEvent) {
        //ignore
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing(final WindowEvent windowEvent) {
        quit();
    }

    /** {@inheritDoc}. */
    @Override
    public void windowClosed(final WindowEvent windowEvent) {
        //ignore
    }

    /** {@inheritDoc}. */
    @Override
    public void windowIconified(final WindowEvent windowEvent) {
        ActionManager.processEvent(CoreActionType.CLIENT_MINIMISED, null);
    }

    /** {@inheritDoc}. */
    @Override
    public void windowDeiconified(final WindowEvent windowEvent) {
        ActionManager.processEvent(CoreActionType.CLIENT_UNMINIMISED, null);
    }

    /** {@inheritDoc}. */
    @Override
    public void windowActivated(final WindowEvent windowEvent) {
        //ignore
    }

    /** {@inheritDoc}. */
    @Override
    public void windowDeactivated(final WindowEvent windowEvent) {
        //ignore
    }

    /** Initialiases the frame managers. */
    private void initFrameManagers() {
        final String manager =
                IdentityManager.getGlobalConfig().
                getOption("ui", "framemanager", "treeview");

        final FrameManager frameManager;
        if (manager.equalsIgnoreCase("buttonbar")) {
            frameManager = new ButtonBar();
            WindowManager.addFrameManager(frameManager);
        } else {
            frameManager = new TreeFrameManager();
            WindowManager.addFrameManager(frameManager);
        }
        mainFrameManager = frameManager;
        frameManager.setParent(frameManagerPanel);

        WindowManager.addFrameManager(new CtrlTabFrameManager(desktopPane));
        WindowManager.addFrameManager(this);
    }

    /**
     * Initialises the components for this frame.
     */
    private void initComponents() {
        final JSplitPane mainSplitPane = new JSplitPane();

        frameManagerPanel = new JPanel();
        desktopPane = new DMDircDesktopPane();
        desktopPane.setBackground(new Color(238, 238, 238));

        initFrameManagers();

        initSplitPane(mainSplitPane);

        menu = new MenuBar();
        setJMenuBar(menu);
        Apple.getApple().setMenuBar(menu);

        setPreferredSize(new Dimension(800, 600));

        getContentPane().setLayout(new MigLayout("fill, ins rel, wrap 1, hidemode 2"));
        getContentPane().add(mainSplitPane, "grow, push");
        getContentPane().add(statusBar, "hmax 20, wmax 100%-2*rel, wmin 100%-2*rel");

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        setTitle("DMDirc");
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

        mainSplitPane.setDividerSize((int) PlatformDefaults.getPanelInsets(0).getValue());
        mainSplitPane.setOneTouchExpandable(false);

        position =
                FramemanagerPosition.getPosition(IdentityManager.getGlobalConfig().
                getOption("ui", "framemanagerPosition"));

        if (position == FramemanagerPosition.UNKNOWN) {
            position = FramemanagerPosition.LEFT;
        }

        if (!mainFrameManager.canPositionVertically() &&
                (position == FramemanagerPosition.LEFT ||
                position == FramemanagerPosition.RIGHT)) {
            position = FramemanagerPosition.BOTTOM;
        }
        if (!mainFrameManager.canPositionHorizontally() &&
                (position == FramemanagerPosition.TOP ||
                position == FramemanagerPosition.BOTTOM)) {
            position = FramemanagerPosition.LEFT;
        }

        switch (position) {
            case TOP:
                mainSplitPane.setTopComponent(frameManagerPanel);
                mainSplitPane.setBottomComponent(desktopPane);
                mainSplitPane.setResizeWeight(0.0);
                mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE,
                        IdentityManager.getGlobalConfig().
                        getOptionInt("ui", "frameManagerSize", 50)));
                frameManagerPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE,
                        50));
                desktopPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                        Integer.MAX_VALUE));
                desktopPane.setMinimumSize(new Dimension(Integer.MAX_VALUE, 300));
                break;
            case LEFT:
                mainSplitPane.setLeftComponent(frameManagerPanel);
                mainSplitPane.setRightComponent(desktopPane);
                mainSplitPane.setResizeWeight(0.0);
                mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(IdentityManager.getGlobalConfig().
                        getOptionInt("ui", "frameManagerSize", 150),
                        Integer.MAX_VALUE));
                frameManagerPanel.setMinimumSize(new Dimension(150, 0));
                desktopPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                        Integer.MAX_VALUE));
                desktopPane.setMinimumSize(new Dimension(300, 0));
                break;
            case BOTTOM:
                mainSplitPane.setTopComponent(desktopPane);
                mainSplitPane.setBottomComponent(frameManagerPanel);
                mainSplitPane.setResizeWeight(1.0);
                mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE,
                        IdentityManager.getGlobalConfig().
                        getOptionInt("ui", "frameManagerSize", 50)));
                frameManagerPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE,
                        50));
                desktopPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                        Integer.MAX_VALUE));
                desktopPane.setMinimumSize(new Dimension(Integer.MAX_VALUE, 300));
                break;
            case RIGHT:
                mainSplitPane.setLeftComponent(desktopPane);
                mainSplitPane.setRightComponent(frameManagerPanel);
                mainSplitPane.setResizeWeight(1.0);
                mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(IdentityManager.getGlobalConfig().
                        getOptionInt("ui", "frameManagerSize", 150),
                        Integer.MAX_VALUE));
                frameManagerPanel.setMinimumSize(new Dimension(150, 0));
                desktopPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                        Integer.MAX_VALUE));
                desktopPane.setMinimumSize(new Dimension(300, 0));
                break;
            default:
                break;
        }

        mainSplitPane.setContinuousLayout(true);
    }

    /** Initialises the key hooks. */
    private void initKeyHooks() {
        final KeyStroke[] keyStrokes = new KeyStroke[12];

        keyStrokes[0] =
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
        keyStrokes[1] =
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        keyStrokes[2] =
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        keyStrokes[3] =
                KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
        keyStrokes[4] =
                KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
        keyStrokes[5] =
                KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
        keyStrokes[6] =
                KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
        keyStrokes[7] =
                KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        keyStrokes[8] =
                KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
        keyStrokes[9] =
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
        keyStrokes[10] =
                KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
        keyStrokes[11] =
                KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);

        for (final KeyStroke keyStroke : keyStrokes) {
            getRootPane().getActionMap().
                    put(KeyEvent.getKeyText(keyStroke.getKeyCode()) + "Action",
                    new AbstractAction(KeyEvent.getKeyText(keyStroke.getKeyCode()) +
                    "Action") {

                        private static final long serialVersionUID = 5;

                        /** {@inheritDoc} */
                        @Override
                        public void actionPerformed(final ActionEvent evt) {
                            ActionManager.processEvent(CoreActionType.CLIENT_FKEY_PRESSED,
                                    null,
                                    KeyStroke.getKeyStroke(keyStroke.getKeyCode(),
                                    evt.getModifiers()));
                        }
                    });
            getRootPane().
                    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                    put(KeyStroke.getKeyStroke(keyStroke.getKeyCode(), 0),
                    KeyEvent.getKeyText(keyStroke.getKeyCode()) + "Action");
        }
    }

    /** {@inheritDoc}. */
    @Override
    public void quit() {
        if (IdentityManager.getGlobalConfig().
                getOptionBool("ui", "confirmQuit", false) &&
                JOptionPane.showConfirmDialog(this,
                "You are about to quit DMDirc, are you sure?", "Quit confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) !=
                JOptionPane.YES_OPTION) {
            return;
        }
        ServerManager.getServerManager().
                closeAll(IdentityManager.getGlobalConfig().
                getOption("general", "closemessage"));
        IdentityManager.getConfigIdentity().
                setOption("ui", "frameManagerSize",
                String.valueOf(this.getFrameManagerSize()));
        Main.quit();
    }

    /** {@inheritDoc}. */
    @Override
    public void setVisible(final boolean visible) {
        //NOPMD
        super.setVisible(visible);
    }

    /** {@inheritDoc}. */
    @Override
    public boolean isVisible() {
        //NOPMD
        return super.isVisible();
    }

    /** {@inheritDoc}. */
    @Override
    public void setTitle(final String newTitle) {
        //NOPMD
        super.setTitle(newTitle);
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if ("ui".equals(domain)) {
            showVersion =
                    IdentityManager.getGlobalConfig().
                    getOptionBool("ui", "showversion", false);
        } else {
            imageIcon =
                    new ImageIcon(IconManager.getIconManager().getImage("icon"));
            setIconImage(imageIcon.getImage());
        }
    }

    /** {@inheritDoc}. */
    @Override
    public void setParent(final JComponent parent) {
        //Ignore
    }

    /** {@inheritDoc}. */
    @Override
    public boolean canPositionVertically() {
        return true;
    }

    /** {@inheritDoc}. */
    @Override
    public boolean canPositionHorizontally() {
        return true;
    }

    /** {@inheritDoc}. */
    @Override
    public void addWindow(final FrameContainer window) {
        addWindow(window, desktopPane.getAllFrames().length - 1);
    }

    /**
     * Adds a window to this frame manager.
     * 
     * @param window The server to be added
     * @param index Index of the window to be added
     */
    public void addWindow(final FrameContainer window, final int index) {
        final JInternalFrame frame = (JInternalFrame) window.getFrame();

        // Add the frame
        desktopPane.add(frame, index);

        // Make sure it'll fit with our offsets
        if (frame.getWidth() + xOffset > desktopPane.getWidth()) {
            xOffset = 0;
        }
        if (frame.getHeight() + yOffset > desktopPane.getHeight()) {
            yOffset = 0;
        }

        // Position the frame
        frame.setLocation(xOffset, yOffset);

        // Increase the offsets
        xOffset += FRAME_OPENING_OFFSET;
        yOffset += FRAME_OPENING_OFFSET;
    }

    /** {@inheritDoc}. */
    @Override
    public void delWindow(FrameContainer window) {
        if (desktopPane.getAllFrames().length == 1) {
            setTitle(getTitlePrefix());
        } else {
            setActiveFrame((Window) desktopPane.selectFrame(true));
        }
        desktopPane.remove((JInternalFrame) window.getFrame());
    }

    /** {@inheritDoc}. */
    @Override
    public void addWindow(final FrameContainer parent, final FrameContainer window) {
        addWindow(window);
    }

    /** {@inheritDoc}. */
    @Override
    public void delWindow(final FrameContainer parent, final FrameContainer window) {
        delWindow(window);
    }
}
