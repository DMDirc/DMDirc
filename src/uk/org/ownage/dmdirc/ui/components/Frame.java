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

package uk.org.ownage.dmdirc.ui.components;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import uk.org.ownage.dmdirc.BrowserLauncher;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.FrameContainer;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.identities.ConfigManager;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.dialogs.PasteDialog;
import uk.org.ownage.dmdirc.ui.input.InputHandler;
import uk.org.ownage.dmdirc.ui.input.TabCompleter;
import uk.org.ownage.dmdirc.ui.messages.Formatter;
import uk.org.ownage.dmdirc.ui.messages.Styliser;

import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;

/**
 * Frame component.
 */
public abstract class Frame extends JInternalFrame implements CommandWindow,
        PropertyChangeListener, InternalFrameListener,
        MouseListener, ActionListener, KeyListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Input field panel. */
    private JPanel inputPanel;
    
    /** Frame input field. */
    private JTextField inputField;
    
    /** Frame output pane. */
    private JTextPane textPane;
    
    /** scrollpane. */
    private JScrollPane scrollPane;
    
    /** holds the scrollbar for the frame. */
    private final JScrollBar scrollBar;
    
    /** The InputHandler for our input field. */
    private InputHandler inputHandler;
    
    /** The channel object that owns this frame. */
    private final FrameContainer parent;
    
    /** The border used when the frame is not maximised. */
    private Border myborder;
    
    /** The dimensions of the titlebar of the frame. **/
    private Dimension titlebarSize;
    
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
        
        scrollBar = getScrollPane().getVerticalScrollBar();
        
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
            hideBorder();
        }
    }
    
    /**
     * Makes this frame visible. We don't call this from the constructor
     * so that we can register an actionlistener for the open event before
     * the frame is opened.
     */
    public final void open() {
        setVisible(true);
        if (Config.hasOption("ui", "awayindicator")
        && Config.getOptionBool("ui", "awayindicator")) {
            awayLabel.setVisible(getServer().isAway());
        }
    }
    
    /**
     * Adds a line of text to the main text area (with a timestamp).
     * @param line text to add
     */
    public final void addLine(final String line) {
        addLine(line, true);
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
                        String ts = Formatter.formatMessage("timestamp", new Date());
                        if (!getTextPane().getText().equals("")) { ts = "\n" + ts; }
                        Styliser.addStyledString(getTextPane().getStyledDocument(), ts);
                    }
                    if (!timestamp && !getTextPane().getText().equals("")) { myLine = "\n" + myLine; }
                    Styliser.addStyledString(getTextPane().getStyledDocument(), myLine);
                }
                
                final int frameBufferSize = parent.getConfigManager().getOptionInt("ui", "frameBufferSize", Integer.MAX_VALUE);
                
                final Document doc = getTextPane().getDocument();
                try {
                    if (doc.getLength() > frameBufferSize) {
                        doc.remove(0, 1 + doc.getText(doc.getLength() - frameBufferSize, 512).indexOf('\n') + doc.getLength() - frameBufferSize);
                    }
                } catch (BadLocationException ex) {
                    Logger.error(ErrorLevel.WARNING, "Unable to trim buffer", ex);
                }
                
                if (scrollBar.getValue() + Math.round(scrollBar.getVisibleAmount() * 1.5) < scrollBar.getMaximum()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        private Rectangle prevRect = getTextPane().getVisibleRect();
                        public void run() {
                            getTextPane().scrollRectToVisible(prevRect);
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            getTextPane().setCaretPosition(getTextPane().getDocument().getLength());
                        }
                    });
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
            addLine(Formatter.formatMessage(messageType, args));
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
        getTextPane().setText("");
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
        setScrollPane(new JScrollPane());
        setInputField(new JTextField());
        setTextPane(new JTextPane());
        
        getInputField().setBorder(
                BorderFactory.createCompoundBorder(
                getInputField().getBorder(), new EmptyBorder(2, 2, 2, 2)));
        
        getTextPane().addMouseListener(this);
        getTextPane().addKeyListener(this);
        getScrollPane().addKeyListener(this);
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
        awayLabel.setBorder(new EmptyBorder(0, 0, 0, SMALL_BORDER));
        
        inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(awayLabel, BorderLayout.LINE_START);
        inputPanel.add(inputField, BorderLayout.CENTER);
    }
    
    /**
     * Removes and reinserts the border of an internal frame on maximising.
     * {@inheritDoc}
     */
    public final void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue().equals(Boolean.TRUE)) {
            hideBorder();
            
            MainFrame.getMainFrame().setMaximised(true);
        } else {
            setBorder(myborder);
            ((BasicInternalFrameUI) getUI()).getNorthPane()
            .setPreferredSize(titlebarSize);
            ((BasicInternalFrameUI) getUI()).getNorthPane()
            .setMaximumSize(titlebarSize);
            
            myborder = null;
            
            MainFrame.getMainFrame().setMaximised(false);
            MainFrame.getMainFrame().setActiveFrame(this);
        }
    }
    
    /**
     * Hides the border around the frame.
     */
    private void hideBorder() {
        if (myborder == null) {
            myborder = getBorder();
            titlebarSize =
                    ((BasicInternalFrameUI) getUI())
                    .getNorthPane().getPreferredSize();
            
            ((BasicInternalFrameUI) getUI()).getNorthPane()
            .setPreferredSize(new Dimension(0, 0));
            ((BasicInternalFrameUI) getUI()).getNorthPane()
            .setMaximumSize(new Dimension(0, 0));
            setBorder(new EmptyBorder(0, 0, 0, 0));
        }
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameOpened(final InternalFrameEvent internalFrameEvent) {
        //Ignore.
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
        //Ignore.
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameClosed(final InternalFrameEvent internalFrameEvent) {
        //Ignore.
    }
    
    /**
     * Makes the internal frame invisible. {@inheritDoc}
     */
    public void internalFrameIconified(final InternalFrameEvent internalFrameEvent) {
        internalFrameEvent.getInternalFrame().setVisible(false);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameDeiconified(final InternalFrameEvent internalFrameEvent) {
        //Ignore.
    }
    
    /**
     * Activates the input field on frame focus. {@inheritDoc}
     */
    public void internalFrameActivated(final InternalFrameEvent internalFrameEvent) {
        getInputField().requestFocus();
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void internalFrameDeactivated(final InternalFrameEvent internalFrameEvent) {
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
     * @return JTextPane text pane for this frame
     */
    public final JTextPane getTextPane() {
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
    protected final void setTextPane(final JTextPane newTextPane) {
        this.textPane = newTextPane;
    }
    
    /**
     * Gets the frames input field.
     *
     * @return returns the JScrollPane used in this frame.
     */
    protected final JScrollPane getScrollPane() {
        return scrollPane;
    }
    
    /**
     * Sets the frames scroll pane.
     *
     * @param newScrollPane new scroll pane to use
     */
    protected final void setScrollPane(final JScrollPane newScrollPane) {
        this.scrollPane = newScrollPane;
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
            final int pos = getTextPane().getCaretPosition();
            final int length = getTextPane().getDocument().getLength();
            String text;
            
            if (pos == 0) {
                return;
            }
            
            int start = (pos - 510 < 0) ? 0 : pos - 510;
            int end = (start + 1020 >= length) ? length - start : 1020;
            
            try {
                text = getTextPane().getText(start, end);
            } catch (BadLocationException ex) {
                Logger.error(ErrorLevel.TRIVIAL, "Unable to select text (start: "
                        + start + ", end: " + end + ")");
                return;
            }
            
            start = pos - start;
            end = start;
            
            // Traverse backwards
            while (start > 0 && start < text.length() && text.charAt(start) != ' '
                    && text.charAt(start) != '\n') {
                start--;
            }
            if (start + 1 < text.length() && text.charAt(start) == ' ') { start++; }
            
            // And forwards
            while (end < text.length() && end > 0 && text.charAt(end) != ' '
                    && text.charAt(end) != '\n') {
                end++;
            }
            
            if (start > end) {
                return;
            }
            
            text = text.substring(start, end);
            
            if (text.length() < 4) {
                return;
            }
            
            checkClickText(text);
            
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
        if (Config.getOptionBool("ui", "quickCopy")) {
            getTextPane().copy();
            getTextPane().setCaretPosition(getTextPane().getCaretPosition());
            getTextPane().moveCaretPosition(getTextPane().getCaretPosition());
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
            final Point point = getScrollPane().getMousePosition();
            getPopup().show(this, (int) point.getX(), (int) point.getY());
        } else if (e.isPopupTrigger() && e.getSource() == getInputField()) {
            final Point point = getMousePosition();
            inputFieldPopup.show(this, (int) point.getX(), (int) point.getY());
        } else if (e.getSource() == getTextPane()) {
            getTextPane().requestFocus();
        } else if (e.getSource() == getInputField()) {
            getInputField().requestFocus();
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
        String clipboardContents = null;
        String[] clipboardContentsLines = new String[]{"", };
        if (event.getKeyCode() == KeyEvent.VK_F3) {
            getSearchBar().open();
        }
        if (event.getSource() == getTextPane()) {
            if ((Config.getOptionBool("ui", "quickCopy")
            || (event.getModifiers() & KeyEvent.CTRL_MASK) ==  0)) {
                event.setSource(getInputField());
                getInputField().requestFocus();
                if (robot != null) {
                    robot.keyPress(event.getKeyCode());
                }
            } else if (event.getKeyCode() == KeyEvent.VK_C) {
                getTextPane().copy();
            }
        } else if (event.getSource() == getInputField()
        && (event.getModifiers() & KeyEvent.CTRL_MASK) != 0
                && event.getKeyCode() == KeyEvent.VK_V) {
            try {
                clipboardContents =
                        (String) Toolkit.getDefaultToolkit().getSystemClipboard()
                        .getData(DataFlavor.stringFlavor);
                clipboardContentsLines = clipboardContents.split(System.getProperty("line.separator"));
            } catch (HeadlessException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to get clipboard contents", ex);
            } catch (IOException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to get clipboard contents", ex);
            } catch (UnsupportedFlavorException ex) {
                Logger.error(ErrorLevel.WARNING, "Unable to get clipboard contents", ex);
            }
            if (clipboardContents.indexOf('\n') >= 0) {
                event.consume();
                final int pasteTrigger = Config.getOptionInt("ui", "pasteProtectionLimit", 1);
                if (getNumLines(clipboardContents) > pasteTrigger) {
                    final String[] options = {"Send", "Edit", "Cancel", };
                    final int n = JOptionPane.showOptionDialog(this,
                            "<html>Paste would be sent as "
                            + getNumLines(clipboardContents) + " lines.<br>"
                            + "Do you want to continue?</html>",
                            "Multi-line Paste",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                    switch (n) {
                        case 0:
                            for (String clipboardLine : clipboardContentsLines) {
                                this.sendLine(clipboardLine);
                            }
                            break;
                        case 1:
                            new PasteDialog(this, clipboardContents).setVisible(true);
                            break;
                        case 2:
                            break;
                        default:
                            break;
                    }
                    
                } else {
                    for (String clipboardLine : clipboardContentsLines) {
                        this.sendLine(clipboardLine);
                    }
                }
            }
        }
    }
    
    /** {@inheritDoc}. */
    public void keyReleased(final KeyEvent event) {
        //Ignore.
    }
    
    /**
     * Checks text clicked on by the user, takes appropriate action.
     * @param text text to check
     */
    private void checkClickText(final String text) {
        //System.out.print("Clicked text: '" + text + "' ");
        if (text.toLowerCase(Locale.getDefault()).startsWith("http://")
        || text.toLowerCase(Locale.getDefault()).startsWith("https://")
        || text.toLowerCase(Locale.getDefault()).startsWith("www.")) {
            //System.out.print("opening browser.");
            MainFrame.getMainFrame().getStatusBar().setMessage("Opening: " + text);
            BrowserLauncher.openURL(text);
        } else if (parent.getServer().getParser().isValidChannelName(text)) {
            //System.out.print("is a valid channel ");
            if (parent.getServer().getParser().getChannelInfo(text) == null) {
                //System.out.print("joining.");
                parent.getServer().getParser().joinChannel(text);
            } else {
                //System.out.print("activating.");
                parent.getServer().getChannel(text).activateFrame();
            }
        } // else {
        //System.out.print("ignoring.");
        //}
        //System.out.println();
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
     * Returns the number of lines the specified string would be sent as.
     * @param line line to be checked
     * @return number of lines that would be sent
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
