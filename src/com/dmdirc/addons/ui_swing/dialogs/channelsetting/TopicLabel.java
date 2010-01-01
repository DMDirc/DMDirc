/*
 * 
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

import com.dmdirc.Topic;
import com.dmdirc.addons.ui_swing.components.text.OldTextLabel;

import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

/**
 * Topic Label for use in the topic history panel.
 */
public class TopicLabel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Topic this label represents. */
    private final Topic topic;

    /**
     * Instantiates a new topic label based on the specified topic.
     *
     * @param topic Specified topic
     */
    public TopicLabel(final Topic topic) {
        this.topic = topic;

        init();
    }

    private void init() {
        setLayout(new MigLayout("fillx, ins 0, debug", "[]0[]", "[]0[]"));

        OldTextLabel label;
        if (!topic.getTopic().isEmpty()) {
            label = new OldTextLabel(topic.getTopic());
            add(label, "wmax 450, growy, pushy, wrap, gapleft 5, gapleft 5");
        }

        if (topic.getTopic().isEmpty()) {
            label = new OldTextLabel("Topic unset by " + topic.getClient());
        } else {
            label = new OldTextLabel("Topic set by " + topic.getClient());
        }
        add(label, "wmax 450, growy, pushy, wrap, gapleft 5, pad 0");

        label = new OldTextLabel("on " + new Date(topic.getTime() * 1000).toString());
        add(label, "wmax 450, growy, pushy, wrap, gapleft 5, pad 0");

        add(new JSeparator(), "newline, span, growx, pushx");
    }

    /**
     * Returns the topic for this label.
     *
     * @return Topic
     */
    public Topic getTopic() {
        return topic;
    }
}
