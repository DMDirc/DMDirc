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

package com.dmdirc.addons.ui_swing.components.renderers;

import com.dmdirc.Topic;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;

import net.miginfocom.swing.MigLayout;

/**
 * Topic list cell renderer.
 */
public class TopicCellRenderer extends JPanel implements ListCellRenderer {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index,
            final boolean isSelected, final boolean cellHasFocus) {
        removeAll();
        setLayout(new MigLayout("ins 0, wrap 1"));

        if (isSelected) {
            setBackground(list.getSelectionBackground());
        } else {
            setBackground(list.getBackground());
        }

        if (value instanceof Topic) {
            final Topic topic = (Topic) value;

            final TextLabel label = new TextLabel(topic.getTopic());
            label.setMaximumSize(new Dimension(list.getWidth(), 0));
            add(label, "growx, pushx");
            add(new JLabel(new Date(topic.getTime() * 1000).toString()),
                    "split 2");
            add(new JLabel(topic.getClient()), "growx, pushx");
        } else {
            add(new JLabel(value.toString()), "grow, push");
        }
        add(new JSeparator(), "growx, pushx");
        return this;
    }
}
