/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.channelsetting;

import com.dmdirc.Channel;
import com.dmdirc.Topic;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.NoNewlinesPasteAction;
import com.dmdirc.addons.ui_swing.components.SwingInputHandler;
import com.dmdirc.addons.ui_swing.components.TextAreaInputField;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
    private TextAreaInputField topicText;
    /** Topic who. */
    private TextLabel topicWho;
    /** Topic history panel. */
    private TopicHistoryPane topicHistoryPane;

    /**
     * Creates a new instance of TopicModesPane.
     *
     * @param channel Parent channel
     * @param parent Parent dialog
     */
    public TopicPane(final Channel channel,
            final ChannelSettingsDialog parent) {
        super();

        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        this.channel = channel;
        this.parent = parent;

        topicLengthMax = channel.getServer().getParser().getMaxTopicLength();

        update();
    }

    /** Updates the panel. */
    public void update() {
        setVisible(false);

        removeAll();
        initTopicsPanel();
        layoutComponents();

        topicText.getDocument().addDocumentListener(this);
        topicHistoryPane.addActionListener(this);

        setVisible(true);
    }

    /** Initialises the topic panel. */
    private void initTopicsPanel() {
        topicLengthLabel = new JLabel();
        topicText = new TextAreaInputField(100, 4);
        topicWho = new TextLabel();
        topicHistoryPane = new TopicHistoryPane(channel);

        topicText.setText(channel.getChannelInfo().getTopic());
        topicText.setLineWrap(true);
        topicText.setWrapStyleWord(true);
        topicText.setRows(5);
        topicText.setColumns(30);
        new SwingInputHandler(topicText, channel.getFrame().getCommandParser(),
                channel.getFrame()).setTypes(false, false, true, false);

        topicText.getActionMap().
                put("paste-from-clipboard",
                new NoNewlinesPasteAction());
        topicText.getInputMap().
                put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                new EnterAction());
        topicText.getInputMap().
                put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, UIUtilities.
                getCtrlDownMask()),
                new EnterAction());

        UIUtilities.addUndoManager(topicText);

        topicChanged();
        actionPerformed(null);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("wrap 1, fill, wmax 450"));

        add(new JScrollPane(topicText), "grow, push");
        add(topicLengthLabel, "pushx, growx, pushx");
        add(topicWho, "growx, pushx");
        add(topicHistoryPane, "grow, pushy");
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

    /** 
     * {@inheritDoc}. 
     * 
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e != null && e.getSource() == topicHistoryPane) {
            final Topic topic = topicHistoryPane.getSelectedTopic();
            if (topic == null) {
                actionPerformed(null);
            } else {
                topicText.setText(topic.getTopic());
            }
        } else {
            final Topic topic = channel.getCurrentTopic();
            if (topic == null) {
                topicWho.setText("No topic set.");
            } else {
                topicWho.setText("Current topic set by " + topic.getClient() +
                        "<br> on " + new Date(1000 * topic.
                        getTime()));
                topicText.setText(topic.getTopic());
            }
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

        /** 
         * {@inheritDoc}
         * 
         * @param e Action event
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            parent.save();
        }
    }

}
