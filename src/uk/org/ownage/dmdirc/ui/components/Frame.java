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

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.FrameContainer;
import uk.org.ownage.dmdirc.commandparser.ChannelCommandParser;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
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
        PropertyChangeListener, InternalFrameListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
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
    
    /** Frame input field. */
    protected JTextField inputField;
    
    /** Frame output pane. */
    protected JTextPane textPane;
    
    /** scrollpane. */
    protected JScrollPane scrollPane;
    
    /** holds the scrollbar for the frame. */
    protected JScrollBar scrollBar;
    
    /** Creates a new instance of Frame. */
    public Frame(final FrameContainer owner) {
        parent = owner;
        
        setFrameIcon(MainFrame.getMainFrame().getIcon());
        
        initComponents();
        setMaximizable(true);
        setClosable(true);
        setResizable(true);
        
        addPropertyChangeListener("maximum", this);
        addInternalFrameListener(this);
        
        scrollBar = scrollPane.getVerticalScrollBar();
        
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
    public void addLine(final String line) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (String myLine : line.split("\n")) {
                    String ts = Formatter.formatMessage("timestamp", new Date());
                    if (!getTextPane().getText().equals("")) { ts = "\n" + ts; }
                    Styliser.addStyledString(getTextPane().getStyledDocument(), ts);
                    Styliser.addStyledString(getTextPane().getStyledDocument(), myLine);
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
    public void addLine(final String messageType, final Object... args) {
        addLine(Formatter.formatMessage(messageType, args));
    }
    
    /**
     * Clears the main text area of the frame.
     */
    public void clear() {
        getTextPane().setText("");
    }
    
    /**
     * Sets the tab completer for this frame's input handler.
     * @param tabCompleter The tab completer to use
     */
    public final void setTabCompleter(final TabCompleter tabCompleter) {
        getInputHandler().setTabCompleter(tabCompleter);
    }
    
    private void initComponents() {
        scrollPane = new JScrollPane();
        inputField = new JTextField();
        textPane = new JTextPane();
    }
    
    public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
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
    
    public void internalFrameOpened(final InternalFrameEvent internalFrameEvent) {
    }
    
    public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
    }
    
    public void internalFrameClosed(final InternalFrameEvent internalFrameEvent) {
    }
    
    public void internalFrameIconified(final InternalFrameEvent internalFrameEvent) {
    }
    
    public void internalFrameDeiconified(final InternalFrameEvent internalFrameEvent) {
    }
    
    public void internalFrameActivated(final InternalFrameEvent internalFrameEvent) {
        getInputField().requestFocus();
    }
    
    public void internalFrameDeactivated(final InternalFrameEvent internalFrameEvent) {
    }

    public FrameContainer getFrameParent() {
        return parent;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public JTextField getInputField() {
        return inputField;
    }

    public void setInputField(JTextField inputField) {
        this.inputField = inputField;
    }

    public JTextPane getTextPane() {
        return textPane;
    }

    public void setTextPane(JTextPane textPane) {
        this.textPane = textPane;
    }
    
}
