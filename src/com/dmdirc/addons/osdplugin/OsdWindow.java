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

import com.dmdirc.Main;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.swing.MainFrame;
import static com.dmdirc.ui.swing.UIUtilities.LARGE_BORDER;
import com.dmdirc.ui.swing.components.ColourChooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The OSD Window is an always-on-top window designed to convey information
 * about events to the user.
 * @author chris
 */
public final class OsdWindow extends JDialog implements MouseListener,
        MouseMotionListener, ChangeListener, ActionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** OSD Label. */
    private final JLabel label;
    
    /** OSD Panel. */
    private final JPanel panel;

    /**
     * Creates a new instance of OsdWindow.
     * @param text The text to be displayed in the OSD window
     * @param config Is the window being configured (should it timeout and
     * allow itself to be moved)
     */
    public OsdWindow(final String text, final boolean config) {
        super(((MainFrame) Main.getUI().getMainWindow()), false);
        
        setFocusableWindowState(false);
        setAlwaysOnTop(true);
        setSize(new Dimension(500, 
                IdentityManager.getGlobalConfig().getOptionInt("plugin-OSD", "fontSize", 20)
                + LARGE_BORDER));
        setResizable(false);
        setUndecorated(true);
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setLocation(IdentityManager.getGlobalConfig().getOptionInt("plugin-OSD", "locationX", 20),
                IdentityManager.getGlobalConfig().getOptionInt("plugin-OSD", "locationY", 20));
        
        panel = new JPanel();
        panel.setBorder(new LineBorder(Color.BLACK));
        panel.setBackground(IdentityManager.getGlobalConfig().getOptionColour("plugin-OSD",
                "bgcolour", Color.decode("#2222aa")));
        
        setContentPane(panel);
        setLayout(new BorderLayout());
        
        label = new JLabel(text);
        label.setForeground(IdentityManager.getGlobalConfig().getOptionColour("plugin-OSD",
                "fgcolour", Color.decode("#ffffff")));
        label.setFont(label.getFont().deriveFont(
                (float) IdentityManager.getGlobalConfig().getOptionInt("plugin-OSD",
                "fontSize", 20)));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label);
        
        setVisible(true);
        
        if (config) {
            this.addMouseMotionListener(this);
        } else {
            addMouseListener(this);
            new Timer("OSD Display Timer").schedule(new TimerTask() {
                public void run() {
                    setVisible(false);
                }
            }, IdentityManager.getGlobalConfig().getOptionInt("plugin-OSD", "timeout", 15) * 1000);
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

    /** {@inheritDoc} */
    public void stateChanged(final ChangeEvent e) {
        final float size = (((SpinnerNumberModel) ((JSpinner) e.getSource()).
                getModel()).getNumber()).floatValue();
        label.setFont(label.getFont().deriveFont(size));
        setSize(new Dimension(500, (int) size + LARGE_BORDER));
    }

    /** {@inheritDoc} */
    public void actionPerformed(final ActionEvent e) {
        if ("backgroundColour".equals(e.getActionCommand())) {
            panel.setBackground(ColourManager.parseColour(
                    ((ColourChooser) e.getSource()).getColour()));
        }
        if ("foregroundColour".equals(e.getActionCommand())) {
            label.setForeground(ColourManager.parseColour(
                    ((ColourChooser) e.getSource()).getColour()));
        }
    }
    
}
