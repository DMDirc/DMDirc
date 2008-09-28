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

import com.dmdirc.ChannelClientProperty;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.parser.irc.ChannelClientInfo;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/** Renders the nicklist. */
public final class NicklistRenderer extends DefaultListCellRenderer implements
        ConfigChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    /** Config manager. */
    private final ConfigManager config;
    /** Nicklist alternate background colour. */
    private Color altBackgroundColour;
    /** Show nick colours. */
    private boolean showColours;
    /** The list that we're using for the nicklist. */
    private final JList nicklist;

    /**
     * Creates a new instance of NicklistRenderer.
     *
     * @param config ConfigManager for the associated channel
     * @param nicklist The nicklist that we're rendering for.
     */
    public NicklistRenderer(final ConfigManager config, final JList nicklist) {
        super();

        this.config = config;
        this.nicklist = nicklist;

        config.addChangeListener("ui", "shownickcoloursinnicklist", this);
        config.addChangeListener("ui", "nicklistbackgroundcolour", this);
        config.addChangeListener("ui", "backgroundcolour", this);
        config.addChangeListener("ui", "nickListAltBackgroundColour", this);
        altBackgroundColour =
                config.getOptionColour("ui", "nickListAltBackgroundColour",
                config.getOptionColour("ui", "nicklistbackgroundcolour",
                config.getOptionColour("ui", "backgroundcolour",
                Color.WHITE)));
        showColours =
                config.getOptionBool("ui", "shownickcoloursinnicklist", false);
    }

    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected,
                cellHasFocus);

        if (!isSelected && (index & 1) == 1) {
            setBackground(altBackgroundColour);
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

        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if ("shownickcoloursinnicklist".equals(key)) {
            showColours =
                    config.getOptionBool("ui", "shownickcoloursinnicklist",
                    false);

        } else {
            altBackgroundColour =
                    config.getOptionColour("ui", "nickListAltBackgroundColour",
                    config.getOptionColour("ui", "nicklistbackgroundcolour",
                    config.getOptionColour("ui", "backgroundcolour",
                    Color.WHITE)));
        }
        nicklist.repaint();
    }
}
