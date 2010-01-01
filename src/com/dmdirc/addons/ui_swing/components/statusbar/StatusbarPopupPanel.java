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
package com.dmdirc.addons.ui_swing.components.statusbar;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.ui.interfaces.StatusBarComponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;

/**
 * A panel shown in the status bar which displays a {@link StatusbarPopupWindow}
 * when the user mouses over it.
 *
 * @since 0.6.3m1
 * @author chris
 */
public abstract class StatusbarPopupPanel extends JPanel
        implements StatusBarComponent, MouseListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** The label we use to show information. */
    protected final JLabel label;

    /** The popup window we're using to show extra info. */
    private StatusbarPopupWindow dialog;

    /**
     * Creates a new {@link StatusbarPopupPanel}, using a default text label.
     */
    public StatusbarPopupPanel() {
        this(new JLabel("Unknown"));
    }

    /**
     * Creates a new {@link StatusbarPopupPanel}, using the specified label.
     *
     * @param label The label to be displayed in the status bar
     */
    public StatusbarPopupPanel(final JLabel label) {
        super();

        this.label = label;

        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new MigLayout("ins 0 rel 0 rel, aligny center"));
        add(label);
        
        addMouseListener(this);
    }
    
    /**
     * Closes and reopens the dialog to update information and border positions.
     */
    public final void refreshDialog() {
        UIUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (StatusbarPopupPanel.this) {
                    if (dialog != null) {
                        closeDialog();
                        openDialog();
                    }
                }
            }
        });
    }

    /**
     * Opens the information dialog.
     */
    protected final void openDialog() {
        synchronized (StatusbarPopupPanel.this) {
            dialog = getWindow();
            dialog.setVisible(true);
        }
    }

    /**
     * Closes the information dialog.
     */
    protected final void closeDialog() {
        synchronized (StatusbarPopupPanel.this) {
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
                dialog = null;
            }
        }
    }
    
    /**
     * Sets the text for this label.
     * 
     * @param text New text
     */
    public void setText(final String text) {
        label.setText(text);
    }

    /**
     * Retrieves the implementation of {@link StatusbarPopupWindow} that should
     * be shown by this panel when the user mouses over it.
     *
     * @return A concrete {@link StatusbarPopupWindow} implementation to use
     */
    protected abstract StatusbarPopupWindow getWindow();
    
    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        // Don't care
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        // Don't care
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        // Don't care
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        setBackground(UIManager.getColor("ToolTip.background"));
        setForeground(UIManager.getColor("ToolTip.foreground"));
        setBorder(new ToplessEtchedBorder());

        openDialog();
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        setBackground(null);
        setForeground(null);
        setBorder(new EtchedBorder());

        closeDialog();
    }
    
    /**
     * An {@link EtchedBorder} with no top.
     */
    private static class ToplessEtchedBorder extends EtchedBorder {

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
            g.drawLine(0, h-2, w, h-2);
            g.drawLine(0, 0, 0, h-1);
            g.drawLine(w-2, 0, w-2, h-1);

            g.setColor(Color.WHITE);
            g.drawLine(0, h-1, w, h-1);
            g.drawLine(w-1, 0, w-1, h-1);

            g.translate(-x, -y);
        }

    }
}