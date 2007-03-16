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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.commandparser.CommandParser;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.ui.input.InputHandler;
import uk.org.ownage.dmdirc.ui.input.TabCompleter;
import uk.org.ownage.dmdirc.ui.messages.ColourManager;
import uk.org.ownage.dmdirc.ui.messages.Formatter;
import uk.org.ownage.dmdirc.ui.messages.Styliser;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * The ServerFrame is the MDI window that shows server messages to the user.
 * @author chris
 */
public class ServerFrame extends JInternalFrame implements CommandWindow {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    
    /**
     * The border used when the frame is not maximised.
     */
    private Border myborder;
    /**
     * The dimensions of the titlebar of the frame.
     **/
    private Dimension titlebarSize;
    /**
     * whether to auto scroll the textarea when adding text.
     */
    private boolean autoScroll = true;
    /**
     * holds the scrollbar for the frame.
     */
    private JScrollBar scrollBar;
    /**
     * The command parser that this frame has been assigned.
     */
    private CommandParser commandParser;
    /**
     * The InputHandler for our input field.
     */
    private InputHandler inputHandler;
    
    /** scroll pane. */
    private JScrollPane jScrollPane1;
    
    /** text field. */
    private JTextField jTextField1;
    
    /** text pane. */
    private JTextPane jTextPane1;
    
    /**
     * Creates a new ServerFrame.
     * @param newCommandParser The command parser to use
     */
    public ServerFrame(final CommandParser newCommandParser) {
        initComponents();
        
        inputHandler = new InputHandler(jTextField1, newCommandParser, this);
        
        setMaximizable(true);
        setClosable(true);
        setResizable(true);
        
        jTextPane1.setBackground(ColourManager.getColour(
                Integer.parseInt(Config.getOption("ui", "backgroundcolour"))));
        jTextPane1.setForeground(ColourManager.getColour(
                Integer.parseInt(Config.getOption("ui", "foregroundcolour"))));
        jTextField1.setBackground(ColourManager.getColour(
                Integer.parseInt(Config.getOption("ui", "backgroundcolour"))));
        jTextField1.setForeground(ColourManager.getColour(
                Integer.parseInt(Config.getOption("ui", "foregroundcolour"))));
        jTextField1.setCaretColor(ColourManager.getColour(
                Integer.parseInt(Config.getOption("ui", "foregroundcolour"))));
        
        scrollBar = jScrollPane1.getVerticalScrollBar();
        this.commandParser = commandParser;
        
        addPropertyChangeListener("maximum", new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
                if (propertyChangeEvent.getNewValue().equals(Boolean.TRUE)) {
                    ServerFrame.this.myborder = getBorder();
                    ServerFrame.this.titlebarSize =
                            ((BasicInternalFrameUI) getUI())
                            .getNorthPane().getPreferredSize();
                    
                    ((BasicInternalFrameUI) getUI()).getNorthPane()
                    .setPreferredSize(new Dimension(0, 0));
                    setBorder(new EmptyBorder(0, 0, 0, 0));
                    
                    MainFrame.getMainFrame().setMaximised(true);
                } else {
                    autoScroll = scrollBar.getValue()
                    + scrollBar.getVisibleAmount() != scrollBar.getMaximum();
                    if (autoScroll) {
                        jTextPane1.setCaretPosition(jTextPane1.getStyledDocument().getLength());
                    }
                    
                    setBorder(ServerFrame.this.myborder);
                    ((BasicInternalFrameUI) getUI()).getNorthPane()
                    .setPreferredSize(ServerFrame.this.titlebarSize);
                    
                    MainFrame.getMainFrame().setMaximised(false);
                    MainFrame.getMainFrame().setActiveFrame(ServerFrame.this);
                }
            }
        });
        
        addInternalFrameListener(new InternalFrameListener() {
            public void internalFrameActivated(final InternalFrameEvent internalFrameEvent) {
                jTextField1.requestFocus();
            }
            public void internalFrameClosed(final InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameDeactivated(final InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameDeiconified(final InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameIconified(final InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameOpened(final InternalFrameEvent internalFrameEvent) {
            }
        });
        
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
     * Clears the main text area of the command window.
     */
    public void clear() {
        jTextPane1.setText("");
    }
    
    /**
     * Sets the tab completer for this frame's input handler.
     * @param tabCompleter The tab completer to use
     */
    public void setTabCompleter(final TabCompleter tabCompleter) {
        inputHandler.setTabCompleter(tabCompleter);
    }
    
    /**
     * Adds a line of text to the main text area, and scrolls the text pane
     * down so that it's visible if the scrollbar is already at the bottom.
     * @param line text to add
     */
    public void addLine(final String line) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (String myLine : line.split("\n")) {
                    String ts = Formatter.formatMessage("timestamp", new Date());
                    if (!jTextPane1.getText().equals("")) { ts = "\n" + ts; }
                    Styliser.addStyledString(jTextPane1.getStyledDocument(), ts);
                    Styliser.addStyledString(jTextPane1.getStyledDocument(), myLine);
                    
                    if (scrollBar.getValue() + Math.round(scrollBar.getVisibleAmount() * 1.5) < scrollBar.getMaximum()) {
                        SwingUtilities.invokeLater(new Runnable() {
                            private Rectangle prevRect = jTextPane1.getVisibleRect();
                            public void run() {
                                jTextPane1.scrollRectToVisible(prevRect);
                            }
                        });
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                jTextPane1.setCaretPosition(jTextPane1.getDocument().getLength());
                            }
                        });
                    }
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
     * Initialises components in this frame.
     */
    private void initComponents() {
        jTextField1 = new JTextField();
        jScrollPane1 = new JScrollPane();
        jTextPane1 = new JTextPane();
        
        setTitle("Server Frame");
        
        jScrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jTextPane1.setEditable(false);
        jScrollPane1.setViewportView(jTextPane1);
        
        final GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(jTextField1, GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                .add(GroupLayout.TRAILING, jScrollPane1, GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.LEADING)
                .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(jTextField1, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED, 5, 5))
                );
        pack();
    }
}
