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
import com.dmdirc.commandparser.CommandWindow;
import com.dmdirc.identities.ConfigManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.MainFrame;
import com.dmdirc.ui.dialogs.PasteDialog;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.ui.textpane.TextPane;
import com.dmdirc.ui.textpane.TextPaneListener;
import static com.dmdirc.ui.UIUtilities.SMALL_BORDER;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * Frame component.
 */
public abstract class Frame extends JInternalFrame implements CommandWindow,
        PropertyChangeListener, InternalFrameListener,
        MouseListener, ActionListener, KeyListener, TextPaneListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
    /** Input field panel. */
    private JPanel inputPanel;
    
    /** Frame input field. */
    private JTextField inputField;
    
    /** Frame output pane. */
    private TextPane textPane;
    
    /** The InputHandler for our input field. */
    private InputHandler inputHandler;
    
    /** The channel object that owns this frame. */
    private final FrameContainer parent;
    
    /** Popupmenu for this frame. */
    private JPopupMenu popup;
    
    /** Popupmenu for this frame. */
    private JPopupMenu inputFieldPopup;
    
    /** popup menu item. */
    private JMenuItem copyMI;
    
    /** popup menu item. */
    private JMenuItem inputPasteMI;
    
    /** popup menu item. */
    private JMenuItem inputCopyMI;
    
    /** popup menu item. */
    private JMenuItem inputCutMI;
    
    /** search bar. */
    private SearchBar searchBar;
    
    /** Robot for the frame. */
    private Robot robot;
    
    /** Away label. */
    private JLabel awayLabel;
    
    /**
     * Creates a new instance of Frame.
     *
     * @param owner FrameContainer owning this frame.
     */
    public Frame(final FrameContainer owner) {
        super();
        parent = owner;
        
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.error(ErrorLevel.TRIVIAL, "Error creating robot", ex);
        }
        
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
        
        getInputField().setBackground(config.getOptionColour("ui", "inputbackgroundcolour",
                config.getOptionColour("ui", "backgroundcolour", Color.WHITE)));
        getInputField().setForeground(config.getOptionColour("ui", "inputforegroundcolour",
                config.getOptionColour("ui", "foregroundcolour", Color.BLACK)));
        getInputField().setCaretColor(config.getOptionColour("ui", "inputforegroundcolour",
                config.getOptionColour("ui", "foregroundcolour", Color.BLACK)));
        
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
    public final void open() {
        setVisible(true);
        if (Config.getOptionBool("ui", "awayindicator")) {
            awayLabel.setVisible(getServer().isAway());
        }
    }
    
    /**
     * Adds a line of text to the main text area.
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
    
    /**
     * Formats the arguments using the Formatter, then adds the result to the
     * main text area.
     * @param messageType The type of this message
     * @param args The arguments for the message
     */
    public final void addLine(final String messageType, final Object... args) {
        if (messageType.length() > 0) {
            addLine(Formatter.formatMessage(messageType, args), true);
        }
    }
    
    /**
     * Formats the arguments using the Formatter, then adds the result to the
     * main text area.
     * @param messageType The type of this message
     * @param args The arguments for the message
     */
    public final void addLine(final StringBuffer messageType, final Object... args) {
        if (messageType != null) {
            addLine(messageType.toString(), args);
        }
    }
    
    /**
     * Clears the main text area of the frame.
     */
    public final void clear() {
        getTextPane().clear();
    }
    
    /**
     * Sets the tab completer for this frame's input handler.
     * @param tabCompleter The tab completer to use
     */
    public final void setTabCompleter(final TabCompleter tabCompleter) {
        getInputHandler().setTabCompleter(tabCompleter);
    }
    
    /**
     * Initialises the components for this frame.
     */
    private void initComponents() {
        setInputField(new JTextField());
        setTextPane(new TextPane(this));
        
        getInputField().setBorder(
                BorderFactory.createCompoundBorder(
                getInputField().getBorder(),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        
        getTextPane().addMouseListener(this);
        getTextPane().addKeyListener(this);
        getTextPane().addTextPaneListener(this);
        getInputField().addKeyListener(this);
        getInputField().addMouseListener(this);
        
        popup = new JPopupMenu();
        inputFieldPopup = new JPopupMenu();
        
        copyMI = new JMenuItem("Copy");
        copyMI.addActionListener(this);
        
        inputPasteMI = new JMenuItem("Paste");
        inputPasteMI.addActionListener(this);
        inputCopyMI = new JMenuItem("Copy");
        inputCopyMI.addActionListener(this);
        inputCutMI = new JMenuItem("Cut");
        inputCutMI.addActionListener(this);
        
        popup.add(copyMI);
        popup.setOpaque(true);
        popup.setLightWeightPopupEnabled(true);
        
        inputFieldPopup.add(inputCutMI);
        inputFieldPopup.add(inputCopyMI);
        inputFieldPopup.add(inputPasteMI);
        inputFieldPopup.setOpaque(true);
        inputFieldPopup.setLightWeightPopupEnabled(true);
        
        searchBar = new SearchBar(this);
        searchBar.setVisible(false);
        
        awayLabel = new JLabel();
        awayLabel.setText("(away)");
        awayLabel.setVisible(false);
        awayLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0,
                SMALL_BORDER));
        
        inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(awayLabel, BorderLayout.LINE_START);
        inputPanel.add(inputField, BorderLayout.CENTER);
        
        initInputField();
    }
    
    private void initInputField() {
        final UndoManager undo = new UndoManager();
        final Document doc = getInputField().getDocument();
        
        // Listen for undo and redo events
        doc.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent evt) {
                undo.addEdit(evt.getEdit());
            }
        });
        
        // Create an undo action and add it to the text component
        getInputField().getActionMap().put("Undo",
                new AbstractAction("Undo") {
            private static final long serialVersionUID = 1;
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undo.canUndo()) {
                        undo.undo();
                    }
                } catch (CannotUndoException ex) {
                    Logger.error(ErrorLevel.TRIVIAL, "Unable to undo", ex);
                }
            }
        });
        
        // Bind the undo action to ctl-Z
        getInputField().getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        
        // Create a redo action and add it to the text component
        getInputField().getActionMap().put("Redo",
                new AbstractAction("Redo") {
            private static final long serialVersionUID = 1;
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undo.canRedo()) {
                        undo.redo();
                    }
                } catch (CannotRedoException ex) {
                    Logger.error(ErrorLevel.TRIVIAL, "Unable to redo", ex);
                }
            }
        });
        
        // Bind the redo action to ctl-Y
        getInputField().getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
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
    
    /** Shows tht titlebar for this frame. */
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
        getInputField().requestFocus();
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameDeactivated(final InternalFrameEvent event) {
        //Ignore.
    }
    
    /**
     * Returns the parent Frame container for this frame.
     *
     * @return FrameContainer parent
     */
    public final FrameContainer getFrameParent() {
        return parent;
    }
    
    /**
     * Returns the input handler associated with this frame.
     *
     * @return Input handlers for this frame
     */
    public final InputHandler getInputHandler() {
        return inputHandler;
    }
    
    /**
     * Sets the input handler for this frame.
     *
     * @param newInputHandler input handler to set for this frame
     */
    public final void setInputHandler(final InputHandler newInputHandler) {
        this.inputHandler = newInputHandler;
    }
    
    /**
     * returns the input field for this frame.
     *
     * @return JTextField input field for the frame.
     */
    public final JTextField getInputField() {
        return inputField;
    }
    
    /**
     * Returns the input panel for this frame.
     *
     * @return JPanel input panel
     */
    public final JPanel getInputPanel() {
        return inputPanel;
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
     * Sets the frames input field.
     *
     * @param newInputField new input field to use
     */
    protected final void setInputField(final JTextField newInputField) {
        this.inputField = newInputField;
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
     * Returns the away label for this server connection.
     *
     * @return JLabel away label
     */
    public JLabel getAwayLabel() {
        return awayLabel;
    }
    
    /**
     * Sets the away indicator on or off.
     * @param awayState away state
     */
    public void setAwayIndicator(final boolean awayState) {
        if (awayState) {
            getInputPanel().add(awayLabel, BorderLayout.LINE_START);
            awayLabel.setVisible(true);
        } else {
            awayLabel.setVisible(false);
        }
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
     * @param e mouse event
     */
    public void processMouseEvent(final MouseEvent e) {
        if (e.isPopupTrigger() && e.getSource() == getTextPane()) {
            final Point point = getTextPane().getMousePosition();
            if (point != null) {
                final int[] selection = textPane.getSelectedRange();
                if (selection[0] == selection[2] && selection[1] == selection[3]) {
                    copyMI.setEnabled(false);
                } else {
                    copyMI.setEnabled(true);
                }
                getPopup().show(this, (int) point.getX(), (int) point.getY());
            }
        } else if (e.isPopupTrigger() && e.getSource() == getInputField()) {
            final Point point = getInputField().getMousePosition();
            
            if (point != null) {
                inputFieldPopup.show(this, (int) point.getX(),
                        (int) point.getY() + getTextPane().getHeight()
                        + SMALL_BORDER);
            }
        } else {
            super.processMouseEvent(e);
        }
    }
    
    /** {@inheritDoc}. */
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getSource() == copyMI) {
            getTextPane().copy();
        } else if (actionEvent.getSource() == inputCopyMI) {
            getInputField().copy();
        } else if (actionEvent.getSource() == inputPasteMI) {
            getInputField().paste();
        } else if (actionEvent.getSource() == inputCutMI) {
            getInputField().cut();
        }
    }
    
    /**
     * returns the popup menu for this frame.
     * @return JPopupMenu for this frame
     */
    public final JPopupMenu getPopup() {
        return popup;
    }
    
    /** {@inheritDoc}. */
    public void keyTyped(final KeyEvent event) {
        //Ignore.
    }
    
    /** {@inheritDoc}. */
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
        if (event.getSource() == getTextPane()) {
            if ((Config.getOptionBool("ui", "quickCopy")
            || (event.getModifiers() & KeyEvent.CTRL_MASK) ==  0)) {
                event.setSource(getInputField());
                getInputField().requestFocus();
                if (robot != null && event.getKeyCode() != KeyEvent.VK_UNDEFINED) {
                    robot.keyPress(event.getKeyCode());
                    if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
                        robot.keyRelease(event.getKeyCode());
                    }
                }
            } else if (event.getKeyCode() == KeyEvent.VK_C) {
                getTextPane().copy();
            }
        } else if ((event.getModifiers() & KeyEvent.CTRL_MASK) != 0
                && event.getKeyCode() == KeyEvent.VK_V) {
            doPaste(event);
        }
    }
    
    /**
     * Checks and pastes text.
     *
     * @param event the event that triggered the paste
     */
    private void doPaste(final KeyEvent event) {
        String clipboard = null;
        String[] clipboardLines = new String[]{"", };
        
        try {
            clipboard = getInputField().getText()
            + (String) Toolkit.getDefaultToolkit().getSystemClipboard()
            .getData(DataFlavor.stringFlavor);
            clipboardLines = clipboard.split(System.getProperty("line.separator"));
        } catch (HeadlessException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to get clipboard contents", ex);
        } catch (IOException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to get clipboard contents", ex);
        } catch (UnsupportedFlavorException ex) {
            Logger.error(ErrorLevel.WARNING, "Unable to get clipboard contents", ex);
        }
        if (clipboard != null && clipboard.indexOf('\n') >= 0) {
            event.consume();
            final int pasteTrigger = Config.getOptionInt("ui", "pasteProtectionLimit", 1);
            if (getNumLines(clipboard) > pasteTrigger) {
                showPasteDialog(clipboard, clipboardLines);
            } else {
                for (String clipboardLine : clipboardLines) {
                    this.sendLine(clipboardLine);
                }
            }
        }
    }
    
    /**
     * Shows the paste dialog.
     *
     * @param clipboard contents of the clipboard
     * @param clipboardLines clipboard contents split per line
     */
    private void showPasteDialog(final String clipboard,
            final String[] clipboardLines) {
        final String[] options = {"Send", "Edit", "Cancel", };
        final int n = JOptionPane.showOptionDialog(this,
                "<html>Paste would be sent as "
                + getNumLines(clipboard) + " lines.<br>"
                + "Do you want to continue?</html>",
                "Multi-line Paste",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        switch (n) {
            case 0:
                for (String clipboardLine : clipboardLines) {
                    this.sendLine(clipboardLine);
                }
                break;
            case 1:
                new PasteDialog(this, clipboard).setVisible(true);
                break;
            case 2:
                break;
            default:
                break;
        }
    }
    
    /** Opens, closes of focuses the search bar as appropriate. */
    private void doSearchBar() {
        if (getSearchBar().isVisible()) {
            getSearchBar().getFocus();
        } else {
            getSearchBar().open();
        }
    }
    
    /** {@inheritDoc}. */
    public void keyReleased(final KeyEvent event) {
        //Ignore.
    }
    
    /** {@inheritDoc}. */
    public void hyperlinkClicked(final String url) {
        MainFrame.getMainFrame().getStatusBar().setMessage("Opening: " + url);
        BrowserLauncher.openURL(url);
    }
    
    /** {@inheritDoc}. */
    public void channelClicked(final String channel) {
        if (parent.getServer().getParser().getChannelInfo(channel) == null) {
            parent.getServer().getParser().joinChannel(channel);
        } else {
            parent.getServer().getChannel(channel).activateFrame();
        }
    }
    
    /**
     * Send the line to the frame container.
     *
     * @param line the line to send
     */
    public abstract void sendLine(final String line);
    
    /**
     * Gets the search bar.
     *
     * @return the frames search bar
     */
    public final SearchBar getSearchBar() {
        return searchBar;
    }
    
    /**
     * Re4urns the number of lines the specified string would be sent as.
     * @param line line to be checked
     * @return number of ,ines that would be sent
     */
    public final int getNumLines(final String line) {
        int lines;
        final String[] splitLines = line.split("\n");
        lines = splitLines.length;
        for (String splitLine : splitLines) {
            lines += (int) Math.ceil(splitLine.length() / getMaxLineLength());
        }
        return lines;
    }
    
    /**
     * Returns the maximum length a line can be in this frame.
     * @return max line length
     */
    public abstract int getMaxLineLength();
}
