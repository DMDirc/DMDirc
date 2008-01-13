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

package com.dmdirc.ui.swing.components.renderers;

import com.dmdirc.plugins.PluginInfo;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;

import net.miginfocom.swing.MigLayout;

/**
 * Handles the rendering of the JList used for plugin management.
 * @author chris
 */
public final class PluginCellRenderer extends JPanel implements ListCellRenderer {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;    
    
    /** Creates a new instance of PluginCellRenderer. */
    public PluginCellRenderer() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        final PluginInfo plugin = (PluginInfo) value;
        
        removeAll();
        setLayout(new MigLayout("fill, ins 3 0 0 0"));        
        
        if (isSelected) {
            setBackground(list.getSelectionBackground());
        } else {
            setBackground(list.getBackground());
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
        
        final JLabel version = new JLabel(plugin.getFriendlyVersion());
        version.setForeground(foreground);
        version.setHorizontalAlignment(JLabel.CENTER);
        
        final JLabel author = new JLabel(plugin.getAuthor());
        author.setForeground(foreground);
        
        final JLabel desc = new JLabel(plugin.getDescription());
        desc.setForeground(foreground);
        desc.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, 0, 0, 0));
        
        add(name, "gapleft 3");
        add(version, "pushx");
        add(author, "wrap, gapright 3");
        add(desc, "span 3, growx, wrap, gapleft 3, gapright 3");
        add(new JSeparator(), "span 3, growx");
        
        return this;
    }
    
}
