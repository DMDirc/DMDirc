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

package com.dmdirc.addons.osdplugin;

import com.dmdirc.Config;
import static com.dmdirc.ui.UIUtilities.LARGE_BORDER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

/**
 * The OSD Window is an always-on-top window designed to convey information
 * about events to the user.
 * @author chris
 */
public final class OsdWindow extends JFrame implements MouseListener,
        MouseMotionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;

    /**
     * Creates a new instance of OsdWindow.
     * @param text The text to be displayed in the OSD window
     * @param config Is the window being configured (should it timeout and
     * allow itself to be moved)
     */
    public OsdWindow(final String text, final boolean config) {
        super();
        
        setAlwaysOnTop(true);
        setMaximumSize(new Dimension(500, 
                Config.getOptionInt("plugin-OSD", "fontsize", 20) + LARGE_BORDER));
        setResizable(false);
        setUndecorated(true);
        pack();
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setLocation(Config.getOptionInt("plugin-OSD", "locationX", 20),
                Config.getOptionInt("plugin-OSD", "locationY", 20));
        
        final JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(Color.BLACK));
        panel.setBackground(Config.getOptionColor("plugin-OSD",
                "bgcolour", Color.decode("#2222aa")));
        
        setContentPane(panel);
        setLayout(new BorderLayout());
        
        final JLabel label = new JLabel(text);
        label.setForeground(Config.getOptionColor("plugin-OSD",
                "fgcolour", Color.decode("#ffffff")));
        label.setFont(label.getFont().deriveFont((float) Config.getOptionInt("plugin-OSD",
                "fontsize", 20)));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label);
        
        setVisible(true);
        
        if (config) {
            this.addMouseMotionListener(this);
        } else {
            addMouseListener(this);
            new Timer().schedule(new TimerTask() {
                public void run() {
                    setVisible(false);
                }
            }, Config.getOptionInt("plugin-OSD", "timeout", 15) * 1000);
        }
    }
    
    /** {@inheritDoc} */
    public void mouseClicked(final MouseEvent e) {
        setVisible(false);
    }
    
    /** {@inheritDoc} */
    public void mousePressed(final MouseEvent e) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    public void mouseReleased(final MouseEvent e) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    public void mouseEntered(final MouseEvent e) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    public void mouseExited(final MouseEvent e) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    public void mouseDragged(final MouseEvent e) {
        setLocation(e.getLocationOnScreen());
    }
    
    /** {@inheritDoc} */
    public void mouseMoved(final MouseEvent e) {
        // Do nothing
    }
    
}
