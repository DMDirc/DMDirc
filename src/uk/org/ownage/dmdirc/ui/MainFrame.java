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

package uk.org.ownage.dmdirc.ui;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.Main;
import uk.org.ownage.dmdirc.ServerManager;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.ui.framemanager.FrameManager;
import uk.org.ownage.dmdirc.ui.framemanager.tree.TreeFrameManager;

import org.jdesktop.layout.GroupLayout;

/**
 * The main application frame.
 */
public final class MainFrame extends JFrame implements WindowListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
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
    private ImageIcon imageIcon;
    /**
     * The frame manager that's being used.
     */
    private FrameManager frameManager;
    
    /** Sekstop pane. */
    private JDesktopPane desktopPane;
    
    /** File menu. */
    private JMenu fileMenu;
    
    /** Menu bar. */
    private JMenuBar jMenuBar1;
    
    /** Main panel. */
    private JPanel jPanel1;
    
    /** Second panel. */
    private JPanel jPanel2;
    
    /** Add server menu item. */
    private JMenuItem miAddServer;
    
    /** Preferences menu item. */
    private JMenuItem miPreferences;
    
    /** Toggle state menu item. */
    private JMenuItem toggleStateMenuItem;
    
    /** Window menu. */
    private JMenu windowMenu;
    
    /**
     * Creates new form MainFrame.
     */
    public MainFrame() {
        initComponents();
        
        setTitle(getTitlePrefix());
        
        // Load an icon
        final ClassLoader cldr = this.getClass().getClassLoader();
        
        final URL imageURL = cldr.getResource("uk/org/ownage/dmdirc/res/icon.png");
        imageIcon = new ImageIcon(imageURL);
        setIconImage(imageIcon.getImage());
        
        frameManager = new TreeFrameManager();
        frameManager.setParent(jPanel1);
        
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
                new PreferencesDialog(MainFrame.getMainFrame(), true).setVisible(true);
            }
        });
        
        toggleStateMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                try {
                    getActiveFrame().setMaximum(!getActiveFrame().isMaximum());
                } catch (PropertyVetoException ex) {
                    Logger.error(ErrorLevel.WARNING, ex);
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
    public static MainFrame getMainFrame() {
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
     * Adds the specified InternalFrame as a child of the main frame.
     * @param frame the frame to be added
     */
    public void addChild(final JInternalFrame frame) {
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
     * Removes the specified InternalFrame from our desktop pane.
     * @param frame The frame to be removed
     */
    public void delChild(final JInternalFrame frame) {
        desktopPane.remove(frame);
    }
    
    /**
     * Sets the active internal frame to the one specified.
     * @param frame The frame to be activated
     */
    public void setActiveFrame(final JInternalFrame frame) {
        frame.moveToFront();
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.error(ErrorLevel.ERROR, ex);
        }
        if (maximised) {
            setTitle(getTitlePrefix() + " - " + frame.getTitle());
        }
    }
    
    /**
     * Retrieves the frame manager that's currently in use.
     * @return The current frame manager
     */
    public FrameManager getFrameManager() {
        return frameManager;
    }
    
    /**
     * Retrieves the application icon.
     * @return The application icon
     */
    public ImageIcon getIcon() {
        return imageIcon;
    }
    
    /**
     * Returns the JInternalFrame that is currently active.
     * @return The active JInternalFrame
     */
    public JInternalFrame getActiveFrame() {
        return desktopPane.getSelectedFrame();
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
                    Logger.error(ErrorLevel.ERROR, ex);
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
        if (Boolean.parseBoolean(Config.getOption("ui", "showversion"))) {
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
     * Called when the window is opened. Not implemented.
     * @param windowEvent The event associated with this callback
     */
    public void windowOpened(final WindowEvent windowEvent) {
    }
    
    /**
     * Called when the window is closing. Saves the config and has all servers
     * disconnect with the default close method
     * @param windowEvent The event associated with this callback
     */
    public void windowClosing(final WindowEvent windowEvent) {
        ServerManager.getServerManager().closeAll(Config.getOption("general", "closemessage"));
        Config.save();
    }
    
    /**
     * Called when the window is closed. Not implemented.
     * @param windowEvent The event associated with this callback
     */
    public void windowClosed(final WindowEvent windowEvent) {
    }
    
    /**
     * Called when the window is iconified. Not implemented.
     * @param windowEvent The event associated with this callback
     */
    public void windowIconified(final WindowEvent windowEvent) {
    }
    
    /**
     * Called when the window is deiconified. Not implemented.
     * @param windowEvent The event associated with this callback
     */
    public void windowDeiconified(final WindowEvent windowEvent) {
    }
    
    /**
     * Called when the window is activated. Not implemented.
     * @param windowEvent The event associated with this callback
     */
    public void windowActivated(final WindowEvent windowEvent) {
    }
    
    /**
     * Called when the window is deactivated. Not implemented.
     * @param windowEvent The event associated with this callback
     */
    public void windowDeactivated(final WindowEvent windowEvent) {
    }
    
    /**
     * Initialises the components for this frame.
     */
    private void initComponents() {
        final GroupLayout jPanel1Layout;
        final GroupLayout jPanel2Layout;
        final GroupLayout layout = new GroupLayout(getContentPane());
        
        jPanel1 = new JPanel();
        jPanel2 = new JPanel();
        desktopPane = new JDesktopPane();
        jMenuBar1 = new JMenuBar();
        fileMenu = new JMenu();
        miAddServer = new JMenuItem();
        miPreferences = new JMenuItem();
        windowMenu = new JMenu();
        toggleStateMenuItem = new JMenuItem();
        
        jPanel1Layout = new GroupLayout(jPanel1);
        jPanel2Layout = new GroupLayout(jPanel2);
        
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("DMDirc");
        jPanel1.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.LEADING)
                .add(0, 153, Short.MAX_VALUE)
                );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.LEADING)
                .add(0, 488, Short.MAX_VALUE)
                );
        
        jPanel2.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        desktopPane.setBackground(new Color(238, 238, 238));
       
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.LEADING)
                .add(GroupLayout.TRAILING, desktopPane, GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
                );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.LEADING)
                .add(desktopPane, GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                );
        
        fileMenu.setMnemonic('f');
        fileMenu.setText("File");
        miAddServer.setText("New Server...");
        fileMenu.add(miAddServer);
        
        miPreferences.setText("Preferences");
        fileMenu.add(miPreferences);
        
        jMenuBar1.add(fileMenu);
        
        windowMenu.setMnemonic('w');
        windowMenu.setText("Window");
        toggleStateMenuItem.setMnemonic('m');
        toggleStateMenuItem.setText("Maximise");
        windowMenu.add(toggleStateMenuItem);
        
        jMenuBar1.add(windowMenu);
        
        setJMenuBar(jMenuBar1);
        
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
        pack();
    }
}
