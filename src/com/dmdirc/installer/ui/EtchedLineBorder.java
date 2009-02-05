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

package com.dmdirc.installer.ui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.EtchedBorder;

import static com.dmdirc.installer.ui.InstallerDialog.SMALL_GAP;

/**
 * An etched line border.
 */
class EtchedLineBorder extends EtchedBorder {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Border side. */
    private final BorderSide side;

    /** Border side. */
    public enum BorderSide {

        /** Creates a border at the top. */
        TOP,
        /** Creates a border at the bottom. */
        BOTTOM,
    };

    /**
     * Creates a new etched line border.
     * 
     * @param type Etch type
     * @param side Border side
     */
    public EtchedLineBorder(final int type, final BorderSide side) {
        super(type);

        this.side = side;
    }

    /** {@inheritDoc} */
    @Override
    public void paintBorder(final Component c, final Graphics g, final int x,
                            final int y, final int width, final int height) {
        g.translate(x, y);

        g.setColor(
                etchType == LOWERED ? getShadowColor(c) : getHighlightColor(c));
        switch (side) {
            case TOP:
                g.drawLine(-SMALL_GAP, -SMALL_GAP, width + SMALL_GAP, -SMALL_GAP);
                break;
            case BOTTOM:
                g.drawLine(-SMALL_GAP, height + SMALL_GAP, width + SMALL_GAP, height + SMALL_GAP);
                break;
            default:
                break;
        }

        g.setColor(
                etchType == LOWERED ? getHighlightColor(c) : getShadowColor(c));
        switch (side) {
            case TOP:
                g.drawLine(-SMALL_GAP, -SMALL_GAP - 1, width + SMALL_GAP, -SMALL_GAP - 1);
                break;
            case BOTTOM:
                g.drawLine(-SMALL_GAP, height + SMALL_GAP - 1, width + SMALL_GAP, height + SMALL_GAP - 1);
                break;
            default:
                break;
        }

        g.translate(-x, -y);
    }
}
