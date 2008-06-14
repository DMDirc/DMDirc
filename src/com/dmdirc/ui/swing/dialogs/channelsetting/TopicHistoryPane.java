/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.ui.swing.components.renderers.TopicCellRenderer;
import com.dmdirc.ui.swing.UIUtilities;
import com.dmdirc.Channel;
import com.dmdirc.Topic;

import java.awt.Dimension;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Topic history panel.
 */
public class TopicHistoryPane extends JPanel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Topic history list. */
    private JList topicHistory;

    /**
     * Instantiates a new topic history pane.
     * 
     * @param channel Parent channel
     */
    public TopicHistoryPane(final Channel channel) {
        final List<Topic> topics = channel.getTopics();
        Collections.reverse(topics);
        if (topics.size() > 0) {
            topics.remove(topics.size() - 1);
        }
        
        topicHistory = new JList(new DefaultListModel());
        topicHistory.setCellRenderer(new TopicCellRenderer());
        
        for (Topic topic : topics) {
            ((DefaultListModel) topicHistory.getModel()).addElement(topic);
        }
        
        if (topicHistory.getModel().getSize() == 0) {
            ((DefaultListModel) topicHistory.getModel()).addElement("No previous topics.");
            topicHistory.setBackground(getBackground());
            topicHistory.setForeground(getForeground());
        }
        
        setLayout(new MigLayout("ins 0"));
        topicHistory.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        add(topicHistory, "growy, growx");
        
        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
    }

}
