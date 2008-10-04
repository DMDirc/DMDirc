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

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * DMDirc Extentions to JDesktopPane.
 */
public class DMDircDesktopPane extends JDesktopPane {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** The current number of pixels to displace new frames in the X
     * direction. */
    private int xOffset;
    /** The current number of pixels to displace new frames in the Y
     * direction. */
    private int yOffset;
    /** The number of pixels each new internal frame is offset by. */
    private static final int FRAME_OPENING_OFFSET = 30;

    /**
     * Initialises the DMDirc desktop pane.
     */
    public DMDircDesktopPane() {
        setBackground(new Color(238, 238, 238));
        setBorder(BorderFactory.createEtchedBorder());
    }

    /**
     * Add a specified component at the specified index.
     * 
     * @param comp Component to add
     * @param index Index for insertion
     */
    public void add(final JComponent comp, final int index) {
        addImpl(comp, null, index);
        
        // Make sure it'll fit with our offsets
        if (comp.getWidth() + xOffset > getWidth()) {
            xOffset = 0;
        }
        if (comp.getHeight() + yOffset > getHeight()) {
            yOffset = 0;
        }

        // Position the frame
        comp.setLocation(xOffset, yOffset);

        // Increase the offsets
        xOffset += FRAME_OPENING_OFFSET;
        yOffset += FRAME_OPENING_OFFSET;
    }

    /** {@inheritDoc} */
    @Override
    public JInternalFrame getSelectedFrame() {
        if (getComponentCount() > 0) {
            return super.getSelectedFrame();
        } else {
            return null;
        }
    }
    
}
