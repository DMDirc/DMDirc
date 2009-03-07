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
package com.dmdirc.addons.lagdisplay;

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

public class LagDisplayPanel extends JPanel implements StatusBarComponent, MouseListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3333052848780326565L;
    /** The label we use to show lag. */
    private final JLabel label = new JLabel("Unknown");
    /** The dialog we're using to show extra info. */
    private ServerInfoDialog dialog;
    /** Lag display plugin. */
    private LagDisplayPlugin plugin;

    public LagDisplayPanel(final LagDisplayPlugin plugin) {
        super();
        
        this.plugin = plugin;

        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new MigLayout("ins 0 rel 0 rel, aligny center"));
        add(label);
        
        addMouseListener(this);
    }
    
    /**
     * Closes and reopens the dialog to update information and border positions.
     */
    protected void refreshDialog() {
        UIUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (ServerInfoDialog.class) {
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
    protected void openDialog() {
        synchronized (ServerInfoDialog.class) {
            dialog = new ServerInfoDialog(plugin, this);
            dialog.setVisible(true);
        }
    }

    /**
     * Closes the information dialog.
     */
    protected void closeDialog() {
        synchronized (ServerInfoDialog.class) {
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

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width,
                                int height) {
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