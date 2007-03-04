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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JScrollBar;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.commandparser.ChannelCommandParser;
import uk.org.ownage.dmdirc.commandparser.CommandWindow;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.ui.input.InputHandler;
import uk.org.ownage.dmdirc.ui.input.TabCompleter;
import uk.org.ownage.dmdirc.ui.messages.ColourManager;
import uk.org.ownage.dmdirc.ui.messages.Formatter;
import uk.org.ownage.dmdirc.ui.messages.Styliser;

/**
 *
 * @author  chris
 */
public class ChannelFrame extends javax.swing.JInternalFrame implements CommandWindow {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;

    /**
     * The InputHandler for our input field
     */
    private InputHandler inputHandler;    
    /**
     * The channel object that owns this frame
     */
    private Channel parent;
    /**
     * The nick list model used for this channel's nickname list
     */
    private NicklistListModel nicklistModel;
    /**
     * The border used when the frame is not maximised
     */
    private Border myborder;
    /**
     * The dimensions of the titlebar of the frame
     **/
    private Dimension titlebarSize;
    /**
     * whether to auto scroll the textarea when adding text
     */
    private boolean autoScroll = true;
    /**
     * holds the scrollbar for the frame
     */
    private JScrollBar scrollBar;
    /**
     * This channel's command parser
     */
    private ChannelCommandParser commandParser;
    
    /**
     * Creates a new instance of ChannelFrame. Sets up callbacks and handlers,
     * and default options for the form.
     * @param parent The Channel object that owns this frame
     */
    public ChannelFrame(Channel parent) {
        this.parent = parent;
        
        setFrameIcon(MainFrame.getMainFrame().getIcon());
        
        nicklistModel = new NicklistListModel();
        initComponents();
        setMaximizable(true);
        setClosable(true);
        setResizable(true);
        
        jTextPane1.setBackground(ColourManager.getColour(Integer.parseInt(Config.getOption("ui","backgroundcolour"))));
        jTextPane1.setForeground(ColourManager.getColour(Integer.parseInt(Config.getOption("ui","foregroundcolour"))));
        jTextField1.setBackground(ColourManager.getColour(Integer.parseInt(Config.getOption("ui","backgroundcolour"))));
        jTextField1.setForeground(ColourManager.getColour(Integer.parseInt(Config.getOption("ui","foregroundcolour"))));
        jTextField1.setCaretColor(ColourManager.getColour(Integer.parseInt(Config.getOption("ui","foregroundcolour"))));
        jList1.setBackground(ColourManager.getColour(Integer.parseInt(Config.getOption("ui","backgroundcolour"))));
        jList1.setForeground(ColourManager.getColour(Integer.parseInt(Config.getOption("ui","foregroundcolour"))));        
        
        inputHandler = new InputHandler(jTextField1);        
        
        commandParser = new ChannelCommandParser(parent.getServer(), parent);
        
        scrollBar = jScrollPane1.getVerticalScrollBar();
        
        jTextField1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    ChannelFrame.this.commandParser.parseCommand(ChannelFrame.this, jTextField1.getText());
                } catch (Exception e) {
                    Logger.error(ErrorLevel.ERROR, e);
                }
                jTextField1.setText("");
            }
        });
        
        addPropertyChangeListener("maximum", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (propertyChangeEvent.getNewValue().equals(Boolean.TRUE)) {
                    ChannelFrame.this.myborder = getBorder();
                    ChannelFrame.this.titlebarSize =
                            ((BasicInternalFrameUI)getUI())
                            .getNorthPane().getPreferredSize();
                    
                    ((BasicInternalFrameUI)getUI()).getNorthPane()
                    .setPreferredSize(new Dimension(0,0));
                    setBorder(new EmptyBorder(0,0,0,0));
                    
                    MainFrame.getMainFrame().setMaximised(true);
                } else {
                    autoScroll = ((scrollBar.getValue() + scrollBar.getVisibleAmount())
                    != scrollBar.getMaximum());
                    if(autoScroll) {
                        jTextPane1.setCaretPosition(jTextPane1.getStyledDocument().getLength());
                    }
                    
                    setBorder(ChannelFrame.this.myborder);
                    ((BasicInternalFrameUI)getUI()).getNorthPane()
                    .setPreferredSize(ChannelFrame.this.titlebarSize);
                    
                    MainFrame.getMainFrame().setMaximised(false);
                }
            }
        });
        
        addInternalFrameListener(new InternalFrameListener() {
            public void internalFrameActivated(InternalFrameEvent internalFrameEvent) {
                jTextField1.requestFocus();
            }
            public void internalFrameClosed(InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameClosing(InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameDeactivated(InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameDeiconified(InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameIconified(InternalFrameEvent internalFrameEvent) {
            }
            public void internalFrameOpened(InternalFrameEvent internalFrameEvent) {
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
     * Sets the tab completer for this frame's input handler
     * @param tabCompleter The tab completer to use
     */
    public void setTabCompleter(TabCompleter tabCompleter) {
        inputHandler.setTabCompleter(tabCompleter);
    }    
    
    /**
     * Adds a line of text to the main text area
     * @param line text to add
     */
    public void addLine(String line) {
        autoScroll = ((scrollBar.getValue() + scrollBar.getVisibleAmount())
        != scrollBar.getMaximum());
        
        String ts = Formatter.formatMessage("timestamp", new Date());
        Styliser.addStyledString(jTextPane1.getStyledDocument(), ts);
        Styliser.addStyledString(jTextPane1.getStyledDocument(), line+"\n");
        
        if(autoScroll) {
            jTextPane1.setCaretPosition(jTextPane1.getStyledDocument().getLength());
        }
    }
    
    /**
     * Formats the arguments using the Formatter, then adds the result to the
     * main text area
     * @param messageType The type of this message
     * @param args The arguments for the message
     */
    public void addLine(String messageType, Object... args) {
        addLine(Formatter.formatMessage(messageType, args));
    }
    
    /**
     * Updates the list of clients on this channel
     * @param newNames The new list of clients
     */
    public void updateNames(ArrayList<ChannelClientInfo> newNames) {
        nicklistModel.replace(newNames);
    }
    
    /**
     * Adds a client to this channels' nicklist
     * @param newName the new client to be added
     */
    public void addName(ChannelClientInfo newName) {
        nicklistModel.add(newName);
    }

    /**
     * Removes a client from this channels' nicklist
     * @param name the client to be deleted
     */    
    public void removeName(ChannelClientInfo name) {
        nicklistModel.remove(name);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        setToolTipText("Channel frame");
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jTextPane1.setEditable(false);
        jScrollPane1.setViewportView(jTextPane1);

        jList1.setModel(nicklistModel);
        jScrollPane2.setViewportView(jList1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 112, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
    
}
