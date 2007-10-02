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

package com.dmdirc.ui.swing;

import com.dmdirc.ChannelClientProperty;
import com.dmdirc.config.ConfigChangeListener;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.parser.ChannelClientInfo;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Renders the nicklist.
 */
public final class NicklistRenderer extends DefaultListCellRenderer implements
        ConfigChangeListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    
    /** Config manager. */
    private final ConfigManager config;
    
    /** Nicklist alternate background colour. */
    private Color altBackgroundColour;
    
    /** Show nick colours. */
    private boolean showColours;
    
    /**
     * Creates a new instance of NicklistRenderer.
     *
     * @param config ConfigManager for the associated channel
     */
    public NicklistRenderer(final ConfigManager config) {
        super();
        this.config = config;
        config.addChangeListener("ui", "shownickcoloursinnicklist", this);
        config.addChangeListener("nicklist", "altBackgroundColour", this);
        
        altBackgroundColour = config.getOptionColour("nicklist",
                "altBackgroundColour", getBackground());
        showColours = config.getOptionBool("ui", "shownickcoloursinnicklist", false);
    }
    
    /** {@inheritDoc} */
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (!isSelected && (index & 1) == 1) {
            this.setBackground(altBackgroundColour);
        }
        
        final Map map = ((ChannelClientInfo) value).getMap();
        
        if (showColours && map != null) {
            if (map.containsKey(ChannelClientProperty.NICKLIST_FOREGROUND)) {
                setForeground((Color) map.get(ChannelClientProperty.NICKLIST_FOREGROUND));
            }
            
            if (map.containsKey(ChannelClientProperty.NICKLIST_BACKGROUND)) {
                setBackground((Color) map.get(ChannelClientProperty.NICKLIST_BACKGROUND));
            }
        }
        
        this.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        
        return this;
    }
    
    /** {@inheritDoc} */
    public void configChanged(final String domain, final String key) {
        if ("nicklist".equals(domain) && "altBackgroundColour".equals(key)) {
            altBackgroundColour = config.getOptionColour("nicklist", 
                    "altBackgroundColour", getBackground());
        }
        if ("ui".equals(domain) && "shownickcoloursinnicklist".equals(key)) {
            showColours = config.getOptionBool("ui", "shownickcoloursinnicklist", false);
        }
    }
    
}
