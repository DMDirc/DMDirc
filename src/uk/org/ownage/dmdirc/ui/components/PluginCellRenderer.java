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

package uk.org.ownage.dmdirc.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import uk.org.ownage.dmdirc.plugins.Plugin;

import static uk.org.ownage.dmdirc.ui.UIUtilities.SMALL_BORDER;

/**
 * Handles the rendering of the JList used for plugin management.
 * @author chris
 */
public class PluginCellRenderer implements ListCellRenderer {
    
    /** Creates a new instance of PluginCellRenderer. */
    public PluginCellRenderer() {
        super();
    }
    
    /** {@inheritDoc} */
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        
        final JPanel res = new JPanel();
        final Plugin plugin = (Plugin) value;
        
        res.setLayout(new BorderLayout());
        
        res.setBorder(new EmptyBorder(SMALL_BORDER, SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        
        if (isSelected) {
            res.setBackground(list.getSelectionBackground());
        } else {
            res.setBackground(list.getBackground());
        }
        
        Color foreground;
        
        if (plugin.isActive()) {
            foreground = Color.BLACK;
        } else {
            foreground = Color.GRAY;
        }
        
        final JLabel name = new JLabel(plugin.toString());
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        name.setForeground(foreground);
        name.setPreferredSize(new Dimension(3 * list.getWidth() / 8, 15));
        
        final JLabel version = new JLabel("v" + plugin.getVersion());
        version.setForeground(foreground);
        version.setHorizontalAlignment(JLabel.CENTER);
        version.setPreferredSize(new Dimension(list.getWidth() / 8, 15));
        
        final JLabel author = new JLabel(plugin.getAuthor());
        author.setForeground(foreground);
        author.setHorizontalAlignment(JLabel.RIGHT);
        author.setPreferredSize(new Dimension(list.getWidth() / 2, 15));
        
        final JLabel desc = new JLabel(plugin.getDescription());
        desc.setForeground(foreground);
        desc.setBorder(new EmptyBorder(SMALL_BORDER, 0, 0, 0));
        
        res.add(version, BorderLayout.CENTER);
        res.add(name, BorderLayout.WEST);
        res.add(author, BorderLayout.EAST);
        res.add(desc, BorderLayout.SOUTH);
        
        final JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBackground(res.getBackground());
        wrapper.add(res, BorderLayout.NORTH);
        wrapper.add(new JSeparator(), BorderLayout.SOUTH);
        
        return wrapper;
    }
    
}
