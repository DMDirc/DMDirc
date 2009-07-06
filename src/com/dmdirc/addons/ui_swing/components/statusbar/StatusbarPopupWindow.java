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

package com.dmdirc.addons.ui_swing.components.statusbar;

import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;


    /**
 * A popup window which is shown above a status bar component to provide more
 * detailed information.
 *
 * @since 0.6.3m1
 * @author chris
 */
public abstract class StatusbarPopupWindow extends StandardDialog {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** The parent JPanel. */
    private final JPanel parent;
    /** Parent window. */
    private Window parentWindow;

    /**
     * Creates a new status bar popup window.
     *
     * @param parent The {@link JPanel} to use for positioning
     * @param parentWindow Parent window
     */
    public StatusbarPopupWindow(final JPanel parent, final Window parentWindow) {
        super(parentWindow, ModalityType.MODELESS);

        this.parent = parent;
        this.parentWindow = parentWindow;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean b) {
        if (b) {
            final JPanel panel = new JPanel();

            panel.setLayout(new MigLayout("ins 3 5 6 10, gap 10 5"));

            panel.setBackground(UIManager.getColor("ToolTip.background"));
            panel.setForeground(UIManager.getColor("ToolTip.foreground"));

            initContent(panel);

            add(panel);

            setUndecorated(true);
            setFocusableWindowState(false);
            setFocusable(false);
            setResizable(false);

            pack();

            final Point point = parent.getLocationOnScreen();
            point.translate(parent.getWidth() / 2 - this.getWidth() / 2, - this.getHeight());
            final int maxX = Math.max(parentWindow.getLocationOnScreen().x
                    + parentWindow.getWidth() - 10 - getWidth(),
                    parent.getLocationOnScreen().x + parent.getWidth() - 1 - getWidth());
            point.x = Math.min(maxX, point.x);
            setLocation(point);

            panel.setBorder(new GappedEtchedBorder());
        }

        super.setVisible(b);
    }

    /**
     * Initialises the content of the popup window.
     *
     * @param panel The {@link JPanel} to which content should be added
     */
    protected abstract void initContent(final JPanel panel);

    /**
     * An {@link EtchedBorder} that leaves a gap in the bottom where the
     * lag display panel is.
     */
    private class GappedEtchedBorder extends EtchedBorder {

        /**
         * A version number for this class. It should be changed whenever the class
         * structure is changed (or anything else that would prevent serialized
         * objects being unserialized with the new class).
         */
        private static final long serialVersionUID = 1;

        /** {@inheritDoc} */
        @Override
        public void paintBorder(final Component c, final Graphics g,
                final int x, final int y, final int width, final int height) {
            int w = width;
            int h = height;

            g.translate(x, y);

            g.setColor(etchType == LOWERED? getShadowColor(c) : getHighlightColor(c));
            g.drawLine(0, 0, w-1, 0);
            g.drawLine(0, h-1, parent.getLocationOnScreen().x
                    - getLocationOnScreen().x, h-1);
            g.drawLine(parent.getWidth() + parent.getLocationOnScreen().x
                    - getLocationOnScreen().x - 2, h-1, w-1, h-1);
            g.drawLine(0, 0, 0, h-1);
            g.drawLine(w-1, 0, w-1, h-1);

            g.translate(-x, -y);
        }

    }

}
