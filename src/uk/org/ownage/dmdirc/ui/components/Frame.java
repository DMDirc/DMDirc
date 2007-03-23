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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.JInternalFrame;
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
import uk.org.ownage.dmdirc.commandparser.ChannelCommandParser;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.input.InputHandler;
import uk.org.ownage.dmdirc.ui.input.TabCompleter;
import uk.org.ownage.dmdirc.ui.messages.ColourManager;
import uk.org.ownage.dmdirc.ui.messages.Formatter;
import uk.org.ownage.dmdirc.ui.messages.Styliser;

/**
 * Frame component.
 */
public abstract class Frame extends JInternalFrame implements CommandWindow,
        PropertyChangeListener, InternalFrameListener, MouseListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Frame input field. */
    private JTextField inputField;
    
    /** Frame output pane. */
    private JTextPane textPane;
    
    /** scrollpane. */
    private JScrollPane scrollPane;
    
    /** holds the scrollbar for the frame. */
    private JScrollBar scrollBar;
    
    /** The InputHandler for our input field. */
    private InputHandler inputHandler;
    
    /** The channel object that owns this frame. */
    private FrameContainer parent;
    
    /** This channel's command parser. */
    private ChannelCommandParser commandParser;
    
    /** The border used when the frame is not maximised. */
    private Border myborder;
    
    /** The dimensions of the titlebar of the frame. **/
    private Dimension titlebarSize;
    
    /**
     * Creates a new instance of Frame.
     *
     * @param owner FrameContainer owning this frame.
     */
    public Frame(final FrameContainer owner) {
        parent = owner;
        
        setFrameIcon(MainFrame.getMainFrame().getIcon());
        
        initComponents();
        setMaximizable(true);
        setClosable(true);
        setResizable(true);
        
        addPropertyChangeListener("maximum", this);
        addInternalFrameListener(this);
        
        scrollBar = getScrollPane().getVerticalScrollBar();
        
        getTextPane().setBackground(ColourManager.getColour(
                Integer.parseInt(Config.getOption("ui", "backgroundcolour"))));
        getTextPane().setForeground(ColourManager.getColour(
                Integer.parseInt(Config.getOption("ui", "foregroundcolour"))));
        getInputField().setBackground(ColourManager.getColour(
                Integer.parseInt(Config.getOption("ui", "backgroundcolour"))));
        getInputField().setForeground(ColourManager.getColour(
                Integer.parseInt(Config.getOption("ui", "foregroundcolour"))));
        getInputField().setCaretColor(ColourManager.getColour(
                Integer.parseInt(Config.getOption("ui", "foregroundcolour"))));
    }
    
    /**
     * Makes this frame visible. We don't call this from the constructor
     * so that we can register an actionlistener for the open event before
     * the frame is opened.
     */
    public final void open() {
        setVisible(true);
    }
    
    /**
     * Adds a line of text to the main text area.
     * @param line text to add
     */
    public final void addLine(final String line) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (String myLine : line.split("\n")) {
                    String ts = Formatter.formatMessage("timestamp", new Date());
                    if (!getTextPane().getText().equals("")) { ts = "\n" + ts; }
                    Styliser.addStyledString(getTextPane().getStyledDocument(), ts);
                    Styliser.addStyledString(getTextPane().getStyledDocument(), myLine);
                }
                try {
                    int frameBufferSize = Integer.MAX_VALUE;
                    if (Config.hasOption("ui", "frameBufferSize")) {
                        frameBufferSize = Integer.parseInt(Config.getOption("ui", "frameBufferSize"));
                    }
                    Document doc = getTextPane().getDocument();
                    if (doc.getLength() > frameBufferSize) {
                        doc.remove(0, (doc.getText(2, 512).indexOf('\n') + 3));
                    }
                } catch (NumberFormatException ex) {
                    Logger.error(ErrorLevel.WARNING, ex);
                } catch (BadLocationException ex) {
                    Logger.error(ErrorLevel.WARNING, ex);
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
        addLine(Formatter.formatMessage(messageType, args));
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
    }
    
    /**
     * Removes and reinserts the border of an internal frame on maximising.
     * {@inheritDoc}
     */
    public final void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue().equals(Boolean.TRUE)) {
            Frame.this.myborder = getBorder();
            Frame.this.titlebarSize =
                    ((BasicInternalFrameUI) getUI())
                    .getNorthPane().getPreferredSize();
            
            ((BasicInternalFrameUI) getUI()).getNorthPane()
            .setPreferredSize(new Dimension(0, 0));
            setBorder(new EmptyBorder(0, 0, 0, 0));
            
            MainFrame.getMainFrame().setMaximised(true);
        } else {
            setBorder(Frame.this.myborder);
            ((BasicInternalFrameUI) getUI()).getNorthPane()
            .setPreferredSize(Frame.this.titlebarSize);
            
            MainFrame.getMainFrame().setMaximised(false);
            MainFrame.getMainFrame().setActiveFrame(Frame.this);
        }
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public final void internalFrameOpened(final InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public final void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public final void internalFrameClosed(final InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public final void internalFrameIconified(final InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public final void internalFrameDeiconified(final InternalFrameEvent internalFrameEvent) {
    }
    
    /**
     * Activates the input field on frame focus. {@inheritDoc}
     */
    public final void internalFrameActivated(final InternalFrameEvent internalFrameEvent) {
        getInputField().requestFocus();
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public final void internalFrameDeactivated(final InternalFrameEvent internalFrameEvent) {
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
     * Checks for url's, channels and nicknames. {@inheritDoc}
     */
    public final void mouseClicked(final MouseEvent mouseEvent) {
        final int pos = getTextPane().getCaretPosition();
        final int length = getTextPane().getDocument().getLength();
        String text;
        
        if (pos == 0) {
            return;
        }
        
        int start = (pos - 510 < 0) ? 0 : pos - 510;
        int end = (pos + 510 > length) ? length : pos + 510;
        
        try {
            text = getTextPane().getText(start, end);
        } catch (BadLocationException ex) {
            return;
        }
        
        start = pos;
        end = pos;
        
        // Traverse backwards
        while (start > 0 && start < text.length() && text.charAt(start) != ' ') {
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
        
        if (text.length() <= 2) {
            return;
        }
        
        if (text.toLowerCase().startsWith("http://")
        || text.toLowerCase().startsWith("https://")
        || text.toLowerCase().startsWith("www.")) {
            BrowserLauncher.openURL(text);
        }  else if (parent.getServer().getParser().isValidChannelName(text)) {
            if (parent.getServer().getParser().getChannelInfo(text) == null) {
                parent.getServer().getParser().joinChannel(text);
            } else {
                parent.getServer().getChannel(text).activateFrame();
            }
        }
        mouseButtonClicked(mouseEvent);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mousePressed(final MouseEvent mouseEvent) {
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public final void mouseReleased(final MouseEvent mouseEvent) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getInputField().grabFocus();
            }
        });
        mouseButtonReleased(mouseEvent);
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mouseEntered(final MouseEvent mouseEvent) {
    }
    
    /**
     * Not needed for this class. {@inheritDoc}
     */
    public void mouseExited(final MouseEvent mouseEvent) {
    }
    
    /**
     * Called when the mouse is clicked, after the default click operations
     * have been performed.
     * @param mouseEvent mouse event
     */
    public void mouseButtonClicked(final MouseEvent mouseEvent) {
    }
    
    /**
     * Called when the mouse is released, after the default release operations
     * have been performed.
     * @param mouseEvent mouse event
     */
    private void mouseButtonReleased(final MouseEvent mouseEvent) {
    }
    
}
