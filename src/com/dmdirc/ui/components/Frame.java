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

package com.dmdirc.ui.components;

import com.dmdirc.BrowserLauncher;
import com.dmdirc.Config;
import com.dmdirc.FrameContainer;
import com.dmdirc.identities.ConfigManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.ui.textpane.IRCTextAttribute;
import com.dmdirc.ui.textpane.TextPane;
import com.dmdirc.ui.textpane.TextPaneListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.AttributedCharacterIterator;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.synth.SynthLookAndFeel;

/**
 * Implements a generic (internal) frame.
 */
public abstract class Frame extends JInternalFrame implements InputWindow,
        PropertyChangeListener, InternalFrameListener,
        MouseListener, ActionListener, KeyListener, TextPaneListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    
    /** Frame output pane. */
    private TextPane textPane;
    
    /** The channel object that owns this frame. */
    private final FrameContainer parent;
    
    /** Popupmenu for this frame. */
    private JPopupMenu popup;
    
    /** popup menu item. */
    private JMenuItem copyMI;
    
    /** hyperlink menu item. */
    private JMenuItem hyperlinkCopyMI;
    
    /** hyperlink menu item. */
    private JMenuItem hyperlinkOpenMI;
    
    /** search bar. */
    private SearchBar searchBar;
    
    /**
     * Creates a new instance of Frame.
     *
     * @param owner FrameContainer owning this frame.
     */
    public Frame(final FrameContainer owner) {
        super();
        parent = owner;
        
        setFrameIcon(MainFrame.getMainFrame().getIcon());
        
        initComponents();
        setMaximizable(true);
        setClosable(true);
        setResizable(true);
        setIconifiable(true);
        setPreferredSize(new Dimension(MainFrame.getMainFrame().getWidth() / 2,
                MainFrame.getMainFrame().getHeight() / 3));
        
        addPropertyChangeListener("maximum", this);
        addInternalFrameListener(this);
        
        final ConfigManager config = owner.getConfigManager();
        
        getTextPane().setBackground(config.getOptionColour("ui", "backgroundcolour", Color.WHITE));
        getTextPane().setForeground(config.getOptionColour("ui", "foregroundcolour", Color.BLACK));
        
        final Boolean pref = Config.getOptionBool("ui", "maximisewindows");
        if (pref || MainFrame.getMainFrame().getMaximised()) {
            hideTitlebar();
        }
    }
    
    /**
     * Makes this frame visible. We don't call this from the constructor
     * so that we can register an actionlistener for the open event before
     * the frame is opened.
     */
    public void open() {
        setVisible(true);
    }
    
    /**
     * Adds a line of text to the main text area.
     *
     * @param line text to add
     * @param timestamp Whether to timestamp the line or not
     */
    public final void addLine(final String line, final boolean timestamp) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (String myLine : line.split("\n")) {
                    if (timestamp) {
                        Styliser.addStyledString(getTextPane(), new String[]{
                            Formatter.formatMessage("timestamp", new Date()),
                            myLine, });
                    } else {
                        Styliser.addStyledString(getTextPane(), myLine);
                    }
                    textPane.trim(Config.getOptionInt("ui", "frameBufferSize", Integer.MAX_VALUE));
                }
            }
        });
    }
    
    /** {@inheritDoc} */
    public final void addLine(final String messageType, final Object... args) {
        if (messageType.length() > 0) {
            addLine(Formatter.formatMessage(messageType, args), true);
        }
    }
    
    /** {@inheritDoc} */
    public final void addLine(final StringBuffer messageType, final Object... args) {
        if (messageType != null) {
            addLine(messageType.toString(), args);
        }
    }
    
    /** {@inheritDoc} */
    public final void clear() {
        getTextPane().clear();
    }
    
    /**
     * Initialises the components for this frame.
     */
    private void initComponents() {
        setTextPane(new TextPane(this));
        
        getTextPane().addMouseListener(this);
        getTextPane().addKeyListener(this);
        getTextPane().addTextPaneListener(this);
        
        popup = new JPopupMenu();
        
        copyMI = new JMenuItem("Copy");
        copyMI.addActionListener(this);
        
        hyperlinkCopyMI = new JMenuItem("Copy URL");
        hyperlinkCopyMI.addActionListener(this);
        hyperlinkCopyMI.setVisible(false);
        
        hyperlinkOpenMI = new JMenuItem("Open URL");
        hyperlinkOpenMI.addActionListener(this);
        hyperlinkOpenMI.setVisible(false);
        
        popup.add(hyperlinkOpenMI);
        popup.add(hyperlinkCopyMI);
        popup.add(copyMI);
        popup.setOpaque(true);
        popup.setLightWeightPopupEnabled(true);
        
        searchBar = new SearchBar(this);
        searchBar.setVisible(false);
    }
    
    /**
     * Removes and reinserts the border of an internal frame on maximising.
     * {@inheritDoc}
     */
    public final void propertyChange(final PropertyChangeEvent event) {
        if (event.getNewValue().equals(Boolean.TRUE)) {
            hideTitlebar();
            MainFrame.getMainFrame().setMaximised(true);
        } else {
            showTitlebar();
            
            MainFrame.getMainFrame().setMaximised(false);
            MainFrame.getMainFrame().setActiveFrame(this);
        }
    }
    
    /** Hides the titlebar for this frame. */
    private void hideTitlebar() {
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ((BasicInternalFrameUI) getUI()).setNorthPane(null);
    }
    
    /** Shows the titlebar for this frame. */
    private void showTitlebar() {
        final Class< ? > c;
        Object temp = null;
        Constructor< ? > constructor;
        
        final String componentUI = (String) UIManager.get("InternalFrameUI");
        
        if ("javax.swing.plaf.synth.SynthLookAndFeel".equals(componentUI)) {
            temp = SynthLookAndFeel.createUI(this);
        } else {
            try {
                c = getClass().getClassLoader().loadClass(componentUI);
                constructor = c.getConstructor(new Class[] {javax.swing.JInternalFrame.class});
                temp = constructor.newInstance(new Object[] {this});
            } catch (ClassNotFoundException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to readd titlebar", ex);
            } catch (NoSuchMethodException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to readd titlebar", ex);
            } catch (InstantiationException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to readd titlebar", ex);
            } catch (IllegalAccessException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to readd titlebar", ex);
            } catch (InvocationTargetException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to readd titlebar", ex);
            }
        }
        
        setBorder(UIManager.getBorder("InternalFrame.border"));
        if (temp == null) {
            temp = new BasicInternalFrameUI(this);
        }
        this.setUI((BasicInternalFrameUI) temp);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameOpened(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameClosing(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameClosed(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /**
     * Makes the internal frame invisible. {@inheritDoc}
     */
    public void internalFrameIconified(final InternalFrameEvent event) {
        event.getInternalFrame().setVisible(false);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameDeiconified(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /**
     * Activates the input field on frame focus. {@inheritDoc}
     */
    public void internalFrameActivated(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameDeactivated(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /** {@inheritDoc} */
    public FrameContainer getContainer() {
        return parent;
    }
    
    /** {@inheritDoc} */
    public ConfigManager getConfigManager() {
        return getContainer().getConfigManager();
    }
    
    /**
     * Returns the text pane for this frame.
     *
     * @return Text pane for this frame
     */
    public final TextPane getTextPane() {
        return textPane;
    }
    
    /** {@inheritDoc} */
    @Override
    public final String getName() {
        return parent.toString();
    }
    
    /**
     * Sets the frames text pane.
     *
     * @param newTextPane new text pane to use
     */
    protected final void setTextPane(final TextPane newTextPane) {
        this.textPane = newTextPane;
    }
    
    /**
     * Checks for url's, channels and nicknames. {@inheritDoc}
     */
    public void mouseClicked(final MouseEvent mouseEvent) {
        if (mouseEvent.getSource() == getTextPane()) {
            processMouseEvent(mouseEvent);
        }
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mousePressed(final MouseEvent mouseEvent) {
        processMouseEvent(mouseEvent);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mouseReleased(final MouseEvent mouseEvent) {
        if (Config.getOptionBool("ui", "quickCopy") && mouseEvent.getSource() == getTextPane()) {
            getTextPane().copy();
            getTextPane().clearSelection();
        }
        processMouseEvent(mouseEvent);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mouseEntered(final MouseEvent mouseEvent) {
        //Ignore.
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mouseExited(final MouseEvent mouseEvent) {
        //Ignore.
    }
    
    /**
     * Processes every mouse button event to check for a popup trigger.
     *
     * @param e mouse event
     */
    @Override
    public void processMouseEvent(final MouseEvent e) {
        if (e.isPopupTrigger() && e.getSource() == getTextPane()) {
            final Point point = getTextPane().getMousePosition();
            if (point != null) {
                hyperlinkOpenMI.setActionCommand("");
                hyperlinkOpenMI.setVisible(false);
                hyperlinkCopyMI.setVisible(false);
                final int[] info = textPane.getClickPosition(point);
                if (info[0] != -1) {
                    final AttributedCharacterIterator iterator = textPane.getLine(info[0]).getIterator();
                    iterator.setIndex(info[2]);
                    final Object linkattr = iterator.getAttributes().get(IRCTextAttribute.HYPERLINK);
                    if (linkattr instanceof String) {
                        hyperlinkCopyMI.setVisible(true);
                        hyperlinkOpenMI.setVisible(true);
                        hyperlinkOpenMI.setActionCommand((String) linkattr);
                    }
                }
                
                final int[] selection = textPane.getSelectedRange();
                if ((selection[0] == selection[2] && selection[1] == selection[3])) {
                    copyMI.setEnabled(false);
                } else {
                    copyMI.setEnabled(true);
                }
                getPopup().show(this, (int) point.getX(), (int) point.getY());
            }
        }
        super.processMouseEvent(e);
    }
    
    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getSource() == copyMI) {
            getTextPane().copy();
        } else if (actionEvent.getSource() == hyperlinkCopyMI) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(hyperlinkOpenMI.getActionCommand()), null);
        } else if (actionEvent.getSource() == hyperlinkOpenMI) {
            BrowserLauncher.openURL(hyperlinkOpenMI.getActionCommand());
        }
    }
    
    /**
     * returns the popup menu for this frame.
     *
     * @return JPopupMenu for this frame
     */
    public final JPopupMenu getPopup() {
        return popup;
    }
    
    /** {@inheritDoc} */
    public void keyTyped(final KeyEvent event) {
        //Ignore.
    }
    
    /** {@inheritDoc} */
    public void keyPressed(final KeyEvent event) {
        if ((event.getModifiers() & KeyEvent.CTRL_MASK) ==  0) {
            if (event.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                getTextPane().pageUp();
            } else if (event.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                getTextPane().pageDown();
            }
        } else {
            if (event.getKeyCode() == KeyEvent.VK_HOME) {
                getTextPane().setScrollBarPosition(0);
            } else if (event.getKeyCode() == KeyEvent.VK_END) {
                getTextPane().setScrollBarPosition(textPane.getNumLines());
            }
        }
        if (event.getKeyCode() == KeyEvent.VK_F3) {
            if (!getSearchBar().isVisible()) {
                getSearchBar().open();
            }
            getSearchBar().search();
        }
        if (event.getKeyCode() == KeyEvent.VK_F
                && (event.getModifiers() & KeyEvent.CTRL_MASK) !=  0
                && (event.getModifiers() & KeyEvent.SHIFT_MASK) ==  0) {
            doSearchBar();
        }
        if (!Config.getOptionBool("ui", "quickCopy")
                && (event.getModifiers() & KeyEvent.CTRL_MASK) !=  0
                && event.getKeyCode() == KeyEvent.VK_C) {
            getTextPane().copy();
        }
    }
    
    /** Opens, closes or focuses the search bar as appropriate. */
    private void doSearchBar() {
        if (getSearchBar().isVisible()) {
            getSearchBar().getFocus();
        } else {
            getSearchBar().open();
        }
    }
    
    /** {@inheritDoc} */
    public void keyReleased(final KeyEvent event) {
        //Ignore.
    }
    
    /** {@inheritDoc} */
    public void hyperlinkClicked(final String url) {
        MainFrame.getMainFrame().getStatusBar().setMessage("Opening: " + url);
        BrowserLauncher.openURL(url);
    }
    
    /** {@inheritDoc} */
    public void channelClicked(final String channel) {
        if (parent.getServer().getParser().getChannelInfo(channel) == null) {
            parent.getServer().getParser().joinChannel(channel);
        } else {
            parent.getServer().getChannel(channel).activateFrame();
        }
    }
    
    /**
     * Gets the search bar.
     *
     * @return the frames search bar
     */
    public final SearchBar getSearchBar() {
        return searchBar;
    }
    
    /** Closes this frame. */
    public void close() {
        try {
            setClosed(true);
        } catch (PropertyVetoException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to close frame", ex);
        }
    }
    
    /** Minimises the frame. */
    public void minimise() {
        try {
            setIcon(true);
        } catch (PropertyVetoException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to minimise frame", ex);
        }
    }
}
