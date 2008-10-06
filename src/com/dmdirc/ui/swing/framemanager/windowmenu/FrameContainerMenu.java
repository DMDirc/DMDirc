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

package com.dmdirc.ui.swing.framemanager.windowmenu;

import com.dmdirc.FrameContainer;
import com.dmdirc.interfaces.IconChangeListener;
import com.dmdirc.ui.interfaces.Window;

import java.awt.Font;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;

/**
 *
 */
public class FrameContainerMenu extends JMenu implements IconChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Wrapped frame. */
    private FrameContainer frame;

    /**
     * Instantiates a new FrameContainer menu item wrapping the specified frame.
     * 
     * @param frame Wrapped frame
     */
    public FrameContainerMenu(final FrameContainer frame) {
        super(frame.toString());

        this.frame = frame;

        setIcon(frame.getIcon());
        frame.addIconChangeListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void iconChanged(final Window window, final Icon icon) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if ((frame != null && window != null) &&
                        frame.equals(window.getContainer())) {
                    setIcon(icon);
                }
            }
        });
    }

    /**
     * Called when a new window is selected.
     *
     * @param window The window that's now selected
     */
    public void selectionChanged(final Window window) {
        if (frame.equals(window.getContainer())) {
            setFont(getFont().deriveFont(Font.ITALIC));
        } else {
            setFont(getFont().deriveFont(Font.PLAIN));
        }
    }

    /**
     * Returns the wrapped frame container.
     * 
     * @return Wrapped frame container
     */
    public FrameContainer getFrame() {
        return frame;
    }

    /** {@inheritDoc} */
    @Override
    public void setPopupMenuVisible(final boolean b) {
        if (getMenuComponentCount() == 0) {
            return;
        }
        super.setPopupMenuVisible(b);
    }
}
