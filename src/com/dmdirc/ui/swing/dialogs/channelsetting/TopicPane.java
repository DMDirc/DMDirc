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
import com.dmdirc.Topic;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.swing.UIUtilities;
import com.dmdirc.ui.swing.actions.NoNewlinesPasteAction;
import com.dmdirc.ui.swing.components.TextLabel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

/** Topic panel. */
public final class TopicPane extends JPanel implements DocumentListener,
        ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Parent channel. */
    private final Channel channel;
    /** Parent dialog. */
    private final ChannelSettingsDialog parent;
    /** the maximum length allowed for a topic. */
    private int topicLengthMax;
    /** label showing the number of characters left in a topic.*/
    private JLabel topicLengthLabel;
    /** Topic text entry text area. */
    private JTextArea topicText;
    /** Topic who. */
    private TextLabel topicWho;
    /** Topic history. */
    private JComboBox topicHistory;

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

        update();
    }

    /** Updates the panel. */
    public void update() {
        setVisible(false);

        removeAll();
        initTopicsPanel();
        layoutComponents();

        topicText.getDocument().addDocumentListener(this);
        topicHistory.addActionListener(this);

        setVisible(true);
    }

    /** Initialises the topic panel. */
    private void initTopicsPanel() {
        final List<Topic> topics = channel.getTopics();
        Collections.reverse(topics);
        topicLengthLabel = new JLabel();
        topicText = new JTextArea(100, 4);
        topicHistory =
                new JComboBox(new DefaultComboBoxModel(topics.toArray()));
        topicWho = new TextLabel();

        if (topicHistory.getModel().getSize() == 0) {
            topicHistory.setEnabled(false);
        }

        topicText.setText(channel.getChannelInfo().getTopic());
        topicText.setLineWrap(true);
        topicText.setWrapStyleWord(true);
        topicText.setRows(5);
        topicText.setColumns(30);

        topicText.getActionMap().
                put("paste-from-clipboard",
                new NoNewlinesPasteAction());
        topicText.getInputMap().
                put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                new EnterAction());
        UIUtilities.addUndoManager(topicText);

        topicChanged();
        actionPerformed(null);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("wrap 1, fill"));

        add(topicHistory, "growx, wmin 450, wmax 450");
        add(new JScrollPane(topicText), "grow");
        add(topicLengthLabel, "pushx, growx");
        add(topicWho, "growx, wmin 450, wmax 450");
    }

    /** Processes the topic and changes it if necessary. */
    protected void setChangedTopic() {
        if (!channel.getChannelInfo().getTopic().equals(topicText.getText())) {
            channel.setTopic(topicText.getText());
        }
    }

    /** Handles the topic change. */
    private void topicChanged() {
        if (topicLengthMax == 0) {
            topicLengthLabel.setForeground(Color.BLACK);
            topicLengthLabel.setText(topicText.getText().length() + " characters");
        } else {
            final int charsLeft = topicLengthMax - topicText.getText().length();
            if (charsLeft >= 0) {
                topicLengthLabel.setForeground(Color.BLACK);
                topicLengthLabel.setText(charsLeft + " of " + topicLengthMax +
                        " available");
            } else {
                topicLengthLabel.setForeground(Color.RED);
                topicLengthLabel.setText(0 + " of " + topicLengthMax +
                        " available " + (-1 * charsLeft) + " too many characters");
            }
        }
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

    /** {@inheritDoc}. */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final Topic topic = (Topic) topicHistory.getSelectedItem();
        if (topic == null) {
            topicWho.setText("No topic set.");
        } else {
            topicWho.setText("Set by " + topic.getClient() + "\n on " +
                    new Date(1000 * topic.getTime()));
            topicText.setText(topic.getTopic());
        }
    }

/** Closes and saves the topic when enter is pressed. */
    private class EnterAction extends AbstractAction {

        /**
         * A version number for this class. It should be changed whenever the class
         * structure is changed (or anything else that would prevent serialized
         * objects being unserialized with the new class).
         */
        private static final long serialVersionUID = 1;

        /** {@inheritDoc} */
        @Override
        public void actionPerformed(final ActionEvent e) {
            parent.save();
        }
    }
}