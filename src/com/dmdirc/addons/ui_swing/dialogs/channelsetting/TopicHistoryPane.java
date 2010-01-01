/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.Channel;
import com.dmdirc.Topic;
import com.dmdirc.util.ListenerList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/**
 * Topic history panel.
 */
public class TopicHistoryPane extends JPanel implements ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** The table used to list previous topics. */
    private final TopicTable topicHistory = new TopicTable();
    /** The scrollpane for the list panel. */
    private final JScrollPane scrollPane = new JScrollPane(topicHistory,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    /** Listener list. */
    private final ListenerList listeners = new ListenerList();
    /** Selected topic. */
    private int selectedTopic = -1;

    /**
     * Instantiates a new topic history pane.
     * 
     * @param channel Parent channel
     */
    public TopicHistoryPane(final Channel channel) {
        topicHistory.getSelectionModel().addListSelectionListener(this);
        topicHistory.getSelectionModel().setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        final List<Topic> topics = channel.getTopics();
        Collections.reverse(topics);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);

        for (Topic topic : topics) {
            topicHistory.getModel().addRow(new Object[]{new TopicLabel(topic),});
        }
        topicHistory.getSelectionModel().setSelectionInterval(0, 0);

        setLayout(new MigLayout("fill, ins 0"));
        add(scrollPane, "hmin 50, hmax 200, wmax 450");

        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
    }

    /**
     * Returns the select historical topic.
     *
     * @return Selected topic
     */
    public Topic getSelectedTopic() {
        if (topicHistory.getSelectedRow() == -1) {
            return null;
        }
        final Object topicLabel = topicHistory.getValueAt(topicHistory.
                getSelectedRow(), 0);
        if (topicLabel instanceof TopicLabel) {
            return ((TopicLabel) topicLabel).getTopic();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (topicHistory.getSelectedRow() == -1) {
                topicHistory.getSelectionModel().setSelectionInterval(
                        selectedTopic, selectedTopic);
                return;
            }
            selectedTopic = topicHistory.getSelectedRow();
            fireActionEvent();
        }
    }

    /**
     * Adds an action listener to this object.
     *
     * @param listener Listener to add
     */
    public void addActionListener(final ActionListener listener) {
        listeners.add(ActionListener.class, listener);
    }

    /**
     * Removes an action listener from this object.
     *
     * @param listener Listener to remove
     */
    public void removeActionListener(final ActionListener listener) {
        listeners.remove(ActionListener.class, listener);
    }

    /**
     * Fires a new action event.
     */
    private void fireActionEvent() {
        for (ActionListener listener : listeners.get(ActionListener.class)) {
            listener.actionPerformed(new ActionEvent(this,
                    ActionEvent.ACTION_PERFORMED, ""));
        }
    }
}
