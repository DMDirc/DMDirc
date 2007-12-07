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

package com.dmdirc.ui.swing.components.renderers;

import com.dmdirc.plugins.PluginInfo;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;

/**
 * Handles the rendering of the JList used for plugin management.
 * @author chris
 */
public final class PluginCellRenderer implements ListCellRenderer {
    
    /** Creates a new instance of PluginCellRenderer. */
    public PluginCellRenderer() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        
        final JPanel res = new JPanel();
        final PluginInfo plugin = (PluginInfo) value;
        
        res.setLayout(new BorderLayout());
        
        res.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, 
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        
        if (isSelected) {
            res.setBackground(list.getSelectionBackground());
        } else {
            res.setBackground(list.getBackground());
        }
        
        Color foreground;
        
        if (plugin.isLoaded()) {
            foreground = Color.BLACK;
        } else {
            foreground = Color.GRAY;
        }
        
        final JLabel name = new JLabel(plugin.getNiceName());
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        name.setForeground(foreground);
        
        final JLabel version = new JLabel("v" + plugin.getFriendlyVersion());
        version.setForeground(foreground);
        version.setHorizontalAlignment(JLabel.CENTER);
        
        final JLabel author = new JLabel(plugin.getAuthor());
        author.setForeground(foreground);
        
        final JLabel desc = new JLabel(plugin.getDescription());
        desc.setForeground(foreground);
        desc.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, 0, 0, 0));
        
        res.add(version, BorderLayout.CENTER);
        res.add(name, BorderLayout.WEST);
        res.add(author, BorderLayout.EAST);
        res.add(desc, BorderLayout.SOUTH);

        final int width = list.getWidth() - 2 * SMALL_BORDER;
        name.setPreferredSize(new Dimension(5 * width / 16, 15));
        version.setPreferredSize(new Dimension(width / 8, 15));
        author.setPreferredSize(new Dimension(9 * width / 16, 15));
        
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBackground(res.getBackground());
        wrapper.add(res, BorderLayout.NORTH);
        wrapper.add(new JSeparator(), BorderLayout.SOUTH);
        
        return wrapper;
    }
    
}
