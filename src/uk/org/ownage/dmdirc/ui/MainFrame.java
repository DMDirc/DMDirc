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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import javax.swing.ImageIcon;
import uk.org.ownage.dmdirc.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JInternalFrame;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.ui.framemanager.FrameManager;
import uk.org.ownage.dmdirc.ui.framemanager.tree.TreeFrameManager;

/**
 * The main application frame
 * @author chris
 */
public class MainFrame extends javax.swing.JFrame implements WindowListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * Singleton instance of MainFrame
     */
    private static MainFrame me;
    /**
     * Whether the internal frames are maximised or not
     */
    private boolean maximised = false;
    /**
     * The current number of pixels to displace new frames in the X direction
     */
    private int xOffset = 0;
    /**
     * The current number of pixels to displace new frames in the Y direction
     */
    private int yOffset = 0;
    /**
     * The main application icon
     */
    private ImageIcon imageIcon;
    /**
     * The frame manager that's being used
     */
    private FrameManager frameManager;
    
    /**
     * Returns the singleton instance of MainFrame
     * @return MainFrame instance
     */
    public static MainFrame getMainFrame() {
        if (me == null) {
            me = new MainFrame();
        }
        return me;
    }
    
    /** Creates new form MainFrame */
    public MainFrame() {
        initComponents();
        
        // Load an icon
        ClassLoader cldr = this.getClass().getClassLoader();
        
        java.net.URL imageURL = cldr.getResource("uk/org/ownage/dmdirc/res/icon.png");
        imageIcon = new ImageIcon(imageURL);
        setIconImage(imageIcon.getImage());
        
        frameManager = new TreeFrameManager();
        frameManager.setParent(jPanel1);
        
        setVisible(true);
        
        miAddServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                NewServerDialog.showNewServerDialog();
            }
        });
        
        toggleStateMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
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
     * Adds the specified InternalFrame as a child of the main frame
     * @param frame the frame to be added
     */
    public void addChild(JInternalFrame frame) {
        // Add the frame
        desktopPane.add(frame);
        
        // Make sure it'll fit with our offsets
        if (frame.getWidth()+xOffset > desktopPane.getWidth()) {
            xOffset = 0;
        }
        if (frame.getHeight()+yOffset > desktopPane.getHeight()) {
            yOffset = 0;
        }
        
        // Position the frame
        frame.setLocation(xOffset, yOffset);
        frame.moveToFront();
        
        // Increase the offsets
        xOffset += 30;
        yOffset += 30;
    }
    
    /**
     * Removes the specified InternalFrame from our desktop pane
     * @param frame The frame to be removed
     */
    public void delChild(JInternalFrame frame) {
        desktopPane.remove(frame);
    }
    
    /**
     * Sets the active internal frame to the one specified
     * @param frame The frame to be activated
     */
    public void setActiveFrame(JInternalFrame frame) {
        frame.moveToFront();
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.error(ErrorLevel.ERROR, ex);
        }
        if (maximised) {
            setTitle("DMDirc - "+frame.getTitle());
        }
    }
    
    /**
     * Retrieves the frame manager that's currently in use
     * @return The current frame manager
     */
    public FrameManager getFrameManager() {
        return frameManager;
    }
    
    /**
     * Retrieves the application icon
     * @return The application icon
     */
    public ImageIcon getIcon() {
        return imageIcon;
    }
    
    /**
     * Returns the JInternalFrame that is currently active
     * @return The active JInternalFrame
     */
    public JInternalFrame getActiveFrame() {
        return desktopPane.getSelectedFrame();
    }
    
    /**
     * Sets whether or not the internal frame state is currently maximised
     * @param max whether the frame is maxomised
     */
    public void setMaximised(boolean max) {
        maximised = max;
        
        if (max) {
            if (getActiveFrame() != null) {
                setTitle("DMDirc - "+getActiveFrame().getTitle());
            }
        } else {
            setTitle("DMDirc");
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
     * Gets whether or not the internal frame state is currently maximised
     * @return True iff frames should be maximised, false otherwise
     */
    public boolean getMaximised() {
        return maximised;
    }
    
    /**
     * Checks the current state of the internal frames, and configures the
     * window menu to behave appropriately
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
    public void windowOpened(WindowEvent windowEvent) {
    }
    
    /**
     * Called when the window is closing. Saves the config and has all servers
     * disconnect with the default close method
     * @param windowEvent The event associated with this callback
     */
    public void windowClosing(WindowEvent windowEvent) {
        ServerManager.getServerManager().closeAll(Config.getOption("general","closemessage"));
        Config.save();
    }
    
    /**
     * Called when the window is closed. Not implemented.
     * @param windowEvent The event associated with this callback
     */
    public void windowClosed(WindowEvent windowEvent) {
    }
    
    /**
     * Called when the window is iconified. Not implemented.
     * @param windowEvent The event associated with this callback
     */
    public void windowIconified(WindowEvent windowEvent) {
    }
    
    /**
     * Called when the window is deiconified. Not implemented.
     * @param windowEvent The event associated with this callback
     */
    public void windowDeiconified(WindowEvent windowEvent) {
    }
    
    /**
     * Called when the window is activated. Not implemented.
     * @param windowEvent The event associated with this callback
     */
    public void windowActivated(WindowEvent windowEvent) {
    }
    
    /**
     * Called when the window is deactivated. Not implemented.
     * @param windowEvent The event associated with this callback
     */
    public void windowDeactivated(WindowEvent windowEvent) {
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        desktopPane = new javax.swing.JDesktopPane();
        jPanel1 = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        miAddServer = new javax.swing.JMenuItem();
        windowMenu = new javax.swing.JMenu();
        toggleStateMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DMDirc");
        desktopPane.setBackground(new java.awt.Color(238, 238, 238));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 135, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 493, Short.MAX_VALUE)
        );

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");
        miAddServer.setText("New Server...");
        fileMenu.add(miAddServer);

        jMenuBar1.add(fileMenu);

        windowMenu.setMnemonic('w');
        windowMenu.setText("Window");
        toggleStateMenuItem.setMnemonic('m');
        toggleStateMenuItem.setText("Maximise");
        windowMenu.add(toggleStateMenuItem);

        jMenuBar1.add(windowMenu);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(desktopPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, desktopPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDesktopPane desktopPane;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JMenuItem miAddServer;
    private javax.swing.JMenuItem toggleStateMenuItem;
    private javax.swing.JMenu windowMenu;
    // End of variables declaration//GEN-END:variables
    
}

