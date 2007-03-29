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

package uk.org.ownage.dmdirc.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;

import uk.org.ownage.dmdirc.identities.ConfigManager;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.ui.messages.ColourManager;

/**
 *
 */
public class NicklistRenderer extends DefaultListCellRenderer {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** ConfigManager. */
    ConfigManager config;
    
    /**
     * Creates a new instance of NicklistRenderer.
     */
    public NicklistRenderer(ConfigManager newConfig) {
	config = newConfig;
    }
    
    public Component getListCellRendererComponent(final JList list,
	    final Object value, final int index, final boolean selected,
	    final boolean focused) {
	
	super.getListCellRendererComponent(list, value, index, selected, focused);
	
	if (config.hasOption("nicklist", "altBackground")
	&& Boolean.parseBoolean(config.getOption("nicklist", "altBackground")) && !selected) {
	    ChannelClientInfo client = (ChannelClientInfo) value;
	    if (index % 2 == 0) {
		Color colour = ColourManager.getColour("f0f0f0");
		if (config.hasOption("nicklist", "altBackgroundColour")) {
		    colour = ColourManager.getColour(config.getOption("nicklist", "altBackgroundColour"));
		}
		this.setBackground(colour);
	    }
	}
	this.setBorder(new EmptyBorder(0, 2, 0, 2));
	
	return this;
    }
    
}
