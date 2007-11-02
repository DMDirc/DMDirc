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

package com.dmdirc.ui.swing.components;

import com.dmdirc.util.BrowserLauncher;
import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.util.StringTranscoder;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.IRCTextAttribute;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.actions.SearchAction;
import com.dmdirc.ui.swing.textpane.TextPane;
import com.dmdirc.ui.swing.textpane.TextPaneListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.AttributedCharacterIterator;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.synth.SynthLookAndFeel;

/**
 * Implements a generic (internal) frame.
 */
public abstract class Frame extends JInternalFrame implements Window,
        PropertyChangeListener, InternalFrameListener,
        MouseListener, ActionListener, KeyListener, TextPaneListener,
        ConfigChangeListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    
    /** The channel object that owns this frame. */
    private final FrameContainer parent;
    
    /** Frame output pane. */
    private TextPane textPane;
    
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
    
    /** String transcoder. */
    private StringTranscoder transcoder;
    
    /** Channel popup menu. */
    protected JPopupMenu channelPopup;
    
    /** URL popup menu. */
    protected JPopupMenu urlPopup;
    
    /** Frame buffer size. */
    private int frameBufferSize;
    
    /** Quick copy? */
    private boolean quickCopy;
    
    /**
     * Creates a new instance of Frame.
     *
     * @param owner FrameContainer owning this frame.
     */
    public Frame(final FrameContainer owner) {
        super();
        final ConfigManager config = owner.getConfigManager();
        final Boolean pref = config.getOptionBool("ui", "maximisewindows", false);
        frameBufferSize = config.getOptionInt("ui", "frameBufferSize", 
                Integer.MAX_VALUE);
        quickCopy = config.getOptionBool("ui", "quickCopy", false);
        parent = owner;
        
        setFrameIcon(Main.getUI().getMainWindow().getIcon());
        
        try {
            transcoder = new StringTranscoder(Charset.forName(
                    config.getOption("channel", "encoding", "UTF-8")));
        } catch (UnsupportedCharsetException ex) {
            transcoder = new StringTranscoder(Charset.forName("UTF-8"));
        } catch (IllegalCharsetNameException ex) {
            transcoder = new StringTranscoder(Charset.forName("UTF-8"));
        } catch (IllegalArgumentException ex) {
            transcoder = new StringTranscoder(Charset.forName("UTF-8"));
        }
        
        initComponents();
        setMaximizable(true);
        setClosable(true);
        setResizable(true);
        setIconifiable(true);
        setPreferredSize(new Dimension(((MainFrame) Main.getUI().getMainWindow()).getWidth() / 2,
                ((MainFrame) Main.getUI().getMainWindow()).getHeight() / 3));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        addPropertyChangeListener("maximum", this);
        addPropertyChangeListener("UI", this);
        addInternalFrameListener(this);
        
        getTextPane().setBackground(config.getOptionColour("ui", "backgroundcolour", Color.WHITE));
        getTextPane().setForeground(config.getOptionColour("ui", "foregroundcolour", Color.BLACK));
        
        config.addChangeListener("ui", "foregroundcolour", this);
        config.addChangeListener("ui", "backgroundcolour", this);
        config.addChangeListener("ui", "quickCopy", this);
        
        if (pref || Main.getUI().getMainWindow().getMaximised()) {
            hideTitlebar();
        }
        
        Main.getUI().getMainWindow().addChild(this);
    }
    
    /** {@inheritDoc} */
    public void open() {
        setVisible(true);
    }
    
    /** {@inheritDoc} */
    public final void addLine(final String line, final boolean timestamp) {
        final String encodedLine = transcoder.decode(line);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (String myLine : encodedLine.split("\n")) {
                    if (timestamp) {
                        getTextPane().addStyledString(new String[]{
                            Formatter.formatMessage("timestamp", new Date()),
                            myLine, });
                    } else {
                        getTextPane().addStyledString(myLine);
                    }
                    if (frameBufferSize > 0) {
                        textPane.trim(frameBufferSize);
                    }
                }
            }
        });
    }
    
    /** {@inheritDoc} */
    public final void addLine(final String messageType, final Object... args) {
        if (!messageType.isEmpty()) {
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
        setTextPane(new TextPane(getContainer()));
        
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
        
        channelPopup = new JPopupMenu();
        urlPopup = new JPopupMenu();
        
        searchBar = new SearchBar(this);
        searchBar.setVisible(false);
        
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "searchAction");
        
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                InputEvent.CTRL_DOWN_MASK), "searchAction");
        
        getActionMap().put("searchAction", new SearchAction(searchBar));
    }
    
    /**
     * Removes and reinserts the border of an internal frame on maximising.
     * {@inheritDoc}
     */
    public final void propertyChange(final PropertyChangeEvent event) {
        if ("maximum".equals(event.getPropertyName())) {
            if (event.getNewValue().equals(Boolean.TRUE)) {
                hideTitlebar();
                Main.getUI().getMainWindow().setMaximised(true);
            } else {
                showTitlebar();
                
                Main.getUI().getMainWindow().setMaximised(false);
                Main.getUI().getMainWindow().setActiveFrame(this);
            }
        } else if ("UI".equals(event.getPropertyName()) && isMaximum()) {
            hideTitlebar();
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
                Logger.appError(ErrorLevel.MEDIUM, "Unable to readd titlebar", ex);
            } catch (NoSuchMethodException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to readd titlebar", ex);
            } catch (InstantiationException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to readd titlebar", ex);
            } catch (IllegalAccessException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to readd titlebar", ex);
            } catch (InvocationTargetException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to readd titlebar", ex);
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
        parent.windowOpened();
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameClosing(final InternalFrameEvent event) {
        parent.windowClosing();
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameClosed(final InternalFrameEvent event) {
        parent.windowClosed();
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
        parent.windowActivated();
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameDeactivated(final InternalFrameEvent event) {
        parent.windowDeactivated();
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
    
    /**
     * Returns the transcoder for this frame.
     *
     * @return String transcoder for this frame
     */
    public StringTranscoder getTranscoder() {
        return transcoder;
    }
    
    /** {@inheritDoc} */
    @Override
    public final String getName() {
        if (parent == null) {
            return "";
        }
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
        if (getConfigManager().getOptionBool("ui", "quickCopy", false)
        && mouseEvent.getSource() == getTextPane()) {
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
        if (!getConfigManager().getOptionBool("ui", "quickCopy", false)
        && (event.getModifiers() & KeyEvent.CTRL_MASK) !=  0
                && event.getKeyCode() == KeyEvent.VK_C) {
            getTextPane().copy();
        }
    }
    
    /** {@inheritDoc} */
    public void keyReleased(final KeyEvent event) {
        //Ignore.
    }
    
    /** {@inheritDoc} */
    public void hyperlinkClicked(final String url, final MouseEvent e) {
        if (e.isPopupTrigger()) {
            //Show hyperlink popup
        } else {
            Main.getUI().getStatusBar().setMessage("Opening: " + url);
            BrowserLauncher.openURL(url);
        }
    }
    
    /** {@inheritDoc} */
    public void channelClicked(final String channel, final MouseEvent e) {
        if (e.isPopupTrigger()) {
            //Show channel popup
        } else if (parent != null && parent.getServer() != null) {
            if (parent.getServer().hasChannel(channel)) {
                parent.getServer().join(channel);
            } else {
                parent.getServer().getChannel(channel).activateFrame();
            }
        }
    }
    
    /** {@inheritDoc} */
    public void nickNameClicked(final String nickname, final MouseEvent e) {
        //Ignore
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
            Logger.userError(ErrorLevel.LOW, "Unable to close frame");
        }
    }
    
    /** Minimises the frame. */
    public void minimise() {
        try {
            setIcon(true);
        } catch (PropertyVetoException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to minimise frame");
        }
    }
    
    /** {@inheritDoc} */
    public void configChanged(final String domain, final String key) {
        if (getConfigManager() == null) {
            return;
        }
        if ("ui".equals(domain)) {
            if ("foregroundcolour".equals(key)) {
                getTextPane().setForeground(getConfigManager().
                        getOptionColour("ui", "foregroundcolour", Color.BLACK));
            } else if ("backgroundcolour".equals(key)) {
                getTextPane().setBackground(getConfigManager().
                        getOptionColour("ui", "backgroundcolour", Color.WHITE));
            } else if ("frameBufferSize".equals(key)) {
                frameBufferSize = getContainer().getConfigManager().
                        getOptionInt("ui", "frameBufferSize", Integer.MAX_VALUE);
            } else if ("quickCopy".equals(key)) {
                quickCopy = getContainer().getConfigManager().
                        getOptionBool("ui", "quickCopy", false);
            }
        }
    }
}
