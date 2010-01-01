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

package com.dmdirc.addons.ui_swing.components.renderers;

import com.dmdirc.addons.ui_swing.components.pluginpanel.PluginInfoToggle;
import com.dmdirc.addons.ui_swing.components.themepanel.ThemeToggle;
import static com.dmdirc.addons.ui_swing.UIUtilities.SMALL_BORDER;

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
 * Handles the rendering of the JList used for plugin and theme management.
 * @author chris
 */
public final class AddonCellRenderer extends JPanel implements ListCellRenderer {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;    
    
    /** Creates a new instance of AddonCellRenderer. */
    public AddonCellRenderer() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
       
        removeAll();
        setLayout(new MigLayout("fill, ins 3 0 0 0"));        
        
        if (isSelected) {
            setBackground(list.getSelectionBackground());
        } else {
            setBackground(list.getBackground());
        }
        
        Color foreground = Color.BLACK;
        final JLabel name = new JLabel(), version = new JLabel(),
                author = new JLabel(), desc = new JLabel();

        if (value instanceof PluginInfoToggle) {
            final PluginInfoToggle plugin = (PluginInfoToggle) value;

            if (!plugin.getState()) {
                foreground = Color.GRAY;
            }

            name.setText(plugin.getPluginInfo().getNiceName());
            version.setText(plugin.getPluginInfo().getFriendlyVersion());
            author.setText(plugin.getPluginInfo().getAuthor());
            desc.setText(plugin.getPluginInfo().getDescription());
        } else if (value instanceof ThemeToggle) {
            final ThemeToggle theme = (ThemeToggle) value;
            
            if (!theme.getState()) {
                foreground = Color.GRAY;
            }
            
            name.setText(theme.getTheme().getName());
            version.setText(theme.getTheme().getVersion());
            author.setText(theme.getTheme().getAuthor());
            desc.setText(theme.getTheme().getDescription());
        }
        
        name.setForeground(foreground);
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        version.setForeground(foreground);
        version.setHorizontalAlignment(JLabel.CENTER);
        author.setForeground(foreground);
        desc.setForeground(foreground);
        desc.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER, 0, 0, 0));
        
        add(name, "gapleft 3");
        add(version, "pushx");
        add(author, "wrap, gapright 3");
        add(desc, "span 3, growx, pushx, wrap, gapleft 3, gapright 3");
        add(new JSeparator(), "span 3, growx, pushx");
        
        return this;
    }
    
}
