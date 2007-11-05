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

package com.dmdirc.ui.swing.dialogs.channelsetting;

import com.dmdirc.Channel;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.swing.UIUtilities;
import com.dmdirc.ui.swing.actions.NoNewlinesPasteAction;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Date;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Topic panel.
 */
public final class TopicPane extends JPanel implements KeyListener,
        DocumentListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** Parent channel. */
    private final Channel channel;
    
    /** Parent dialog. */
    private final ChannelSettingsDialog parent;
    
    /**
     * the maximum length allowed for a topic.
     */
    private int topicLengthMax;
    
    /**
     * label showing the number of characters left in a topic.
     */
    private JLabel topicLengthLabel;
    
    /**
     * Topic text entry text area.
     */
    private JTextArea topicText;
    
    /**
     * Creates a new instance of TopicModesPane.
     *
     * @param channel Parent channel
     * @param parent Parent dialog
     */
    public TopicPane(final Channel channel,
            final ChannelSettingsDialog parent) {
        super();
        
        this.channel = channel;
        this.parent = parent;
        
        final Map<String, String> iSupport =
                channel.getServer().getParser().get005();
        if (iSupport.containsKey("TOPICLEN")) {
            try {
                topicLengthMax =
                        Integer.parseInt(iSupport.get("TOPICLEN"));
            } catch (NumberFormatException ex) {
                topicLengthMax = 0;
                Logger.userError(ErrorLevel.LOW,
                        "IRCD doesnt supply topic length");
            }
        }
        
        initTopicsPanel();
        
        topicText.getDocument().addDocumentListener(this);
        
        setVisible(true);
    }
    
    /** Updates the panel. */
    public void update() {
        setVisible(false);
        
        removeAll();
        initTopicsPanel();
        
        setVisible(true);
    }
    
    /**
     * Initialises the topic panel.
     */
    private void initTopicsPanel() {
        final GridBagConstraints constraints = new GridBagConstraints();
        final JLabel topicWho = new JLabel();
        final String topic = channel.getChannelInfo().getTopic();
        final JScrollPane scrollPane;
        topicLengthLabel = new JLabel();
        topicText = new JTextArea(100, 4);
        
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER));
        
        topicText.setText(channel.getChannelInfo().getTopic());
        topicText.setLineWrap(true);
        topicText.addKeyListener(this);
        topicText.setWrapStyleWord(true);
        topicText.setRows(5);
        topicText.setColumns(30);
        scrollPane = new JScrollPane(topicText);
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridy = 1;
        add(scrollPane, constraints);
        
        topicChanged();
        
        constraints.gridy = 2;
        constraints.weighty = 0.0;
        add(topicLengthLabel, constraints);
        
        topicWho.setSize(30, 0);
        topicWho.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, 0, 0, 0));
        if (topic.isEmpty()) {
            topicWho.setText("No topic set.");
        } else {
            topicWho.setText("<html>Set by "
                    + channel.getChannelInfo().getTopicUser() + "<br> on "
                    + new Date(1000 * channel.getChannelInfo().getTopicTime())
                    + "</html>");
        }
        
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.gridy = 3;
        add(topicWho, constraints);
        
        topicText.getActionMap().put("paste-from-clipboard", new NoNewlinesPasteAction());
        UIUtilities.addUndoManager(topicText);
    }
    
    /** Processes the topic and changes it if necessary. */
    protected void setChangedTopic() {
        if (!channel.getChannelInfo().getTopic().equals(topicText.getText())) {
            channel.setTopic(topicText.getText());
        }
    }
    
    /** {@inheritDoc}. */
    @Override
    public void keyTyped(final KeyEvent keyEvent) {
        //Ignore
    }
    
    /** Handles the topic change. */
    private void topicChanged() {
        if (topicLengthMax == 0) {
            topicLengthLabel.setForeground(Color.BLACK);
            topicLengthLabel.setText(topicText.getText().length()
            + " characters");
        } else {
            final int charsLeft = topicLengthMax - topicText.getText().length();
            if (charsLeft >= 0) {
                topicLengthLabel.setForeground(Color.BLACK);
                topicLengthLabel.setText(charsLeft + " of " + topicLengthMax
                        + " available");
            } else {
                topicLengthLabel.setForeground(Color.RED);
                topicLengthLabel.setText(0 + " of " + topicLengthMax
                        + " available " + (-1 * charsLeft)
                        + " too many characters");
            }
        }
    }
    
    /** {@inheritDoc}. */
    @Override
    public void keyPressed(final KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER
                && keyEvent.getSource() == topicText) {
            keyEvent.consume();
            parent.save();
        }
    }
    
    /** {@inheritDoc}. */
    @Override
    public void keyReleased(final KeyEvent keyEvent) {
        //ignore, unused.
    }
    
    /** {@inheritDoc}. */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        topicChanged();
    }
    
    /** {@inheritDoc}. */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        topicChanged();
    }
    
    /** {@inheritDoc}. */
    @Override
    public void changedUpdate(final DocumentEvent e) {
        //Ignore
    }
}
