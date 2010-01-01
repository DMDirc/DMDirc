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

package com.dmdirc.addons.osd;

import com.dmdirc.Main;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import net.miginfocom.swing.MigLayout;

/**
 * The OSD Window is an always-on-top window designed to convey information
 * about events to the user.
 * @author chris
 */
public final class OsdWindow extends JDialog implements MouseListener,
        MouseMotionListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    
    /** Gap between vertically stacked windows. */
    private static final int WINDOW_GAP = 5;
    
    /** A list of open OSD windows. */
    private static List<OsdWindow> windows = new ArrayList<OsdWindow>();

    /** Plugin this window belongs to. */
    private final OsdPlugin plugin;
    
    /** OSD Label. */
    private final JLabel label;
    
    /** OSD Panel. */
    private final JPanel panel;
    
    /** Starting positions of the mouse. */
    private int startX, startY;
    
    /** Is this a config instance? */
    private final boolean config;

    /**
     * Creates a new instance of OsdWindow.
     * 
     * @param text The text to be displayed in the OSD window
     * @param config Is the window being configured (should it timeout and
     * allow itself to be moved)
     * @param plugin The plugin that owns this window
     */    
    public OsdWindow(final String text, final boolean config, final OsdPlugin plugin) {
        this(text, config, IdentityManager.getGlobalConfig()
                .getOptionInt(plugin.getDomain(), "locationX"), getYPosition(plugin), plugin);
    }

    /**
     * Creates a new instance of OsdWindow.
     *
     * @param text The text to be displayed in the OSD window
     * @param config Is the window being configured (should it timeout and
     * allow itself to be moved)
     * @param x The x-axis position for the OSD Window
     * @param y The y-axis position for the OSD window
     * @param plugin Parent OSD Plugin
     */
    public OsdWindow(final String text, final boolean config, final int x,
            final int y, final OsdPlugin plugin) {
        super((MainFrame) Main.getUI().getMainWindow(), false);

        this.plugin = plugin;
        this.config = config;

        setFocusableWindowState(false);
        setAlwaysOnTop(true);
        setResizable(false);
        setUndecorated(true);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        setLocation(x, y);

        panel = new JPanel();
        panel.setBorder(new LineBorder(Color.BLACK));
        panel.setBackground(IdentityManager.getGlobalConfig().getOptionColour(plugin.getDomain(),
                "bgcolour"));

        final int width = IdentityManager.getGlobalConfig().getOptionInt(plugin.getDomain(),
                "width");
        setContentPane(panel);
        setLayout(new MigLayout("wmin " + width + ", wmax " + width + ", ins rel, fill"));

        label = new JLabel(text);
        label.setForeground(IdentityManager.getGlobalConfig().getOptionColour(plugin.getDomain(),
                "fgcolour"));
        label.setFont(label.getFont().deriveFont(
                (float) IdentityManager.getGlobalConfig().getOptionInt(plugin.getDomain(),
                "fontSize")));
        label.setHorizontalAlignment(SwingConstants.CENTER);
            add(label, "alignx center");

        setVisible(true);
        pack();

        if (config) {
            this.addMouseMotionListener(this);
            this.addMouseListener(this);
        } else {
            addMouseListener(this);
            new Timer("OSD Display Timer").schedule(new TimerTask() {
                
                /** {@inheritDoc} */
                @Override
                public void run() {
                    setVisible(false);
                    dispose();
                }
            }, IdentityManager.getGlobalConfig().getOptionInt(plugin.getDomain(),
                    "timeout") * 1000);
        }
    }
    
    /**
     * Retrieves the y-axis position for a new OSD window, based on the user's
     * configured policy.
     * 
     * @return The y-axis position for a new OSD window.
     */
    private static int getYPosition(final OsdPlugin plugin) {
        final String policy = IdentityManager.getGlobalConfig()
                .getOption(plugin.getDomain(), "newbehaviour");
        int y = IdentityManager.getGlobalConfig().getOptionInt(plugin.getDomain(),
                "locationY");
        
        if ("down".equals(policy)) {
            // Place our new window below old windows
            for (OsdWindow window : new ArrayList<OsdWindow>(windows)) {
                if (window.isVisible()) {
                    y = Math.max(y, window.getY() + window.getHeight() + WINDOW_GAP);
                }
            }
        } else if ("up".equals(policy)) {
            // Place our new window above old windows
            for (OsdWindow window : new ArrayList<OsdWindow>(windows)) {
                if (window.isVisible()) {
                    y = Math.min(y, window.getY() - window.getHeight() - WINDOW_GAP);
                }
            }            
        } else if ("close".equals(policy)) {
            // Close existing windows and use their place
            closeAll();
        }        
        
        return y;
    }
    
    /**
     * Closes all open OSD windows.
     */
    protected static void closeAll() {
        for (OsdWindow window : new ArrayList<OsdWindow>(windows)) {
            window.setVisible(false);
            window.dispose();
        }        
    }
    
    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (!config) {
            setVisible(false);
            dispose();
        }
    }
    
    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        if (config) {
            startX = e.getPoint().x;
            startY = e.getPoint().y;
        }
    }
    
    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        // Do nothing
    }
    
    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        // Do nothing
    }
    
    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        // Do nothing
    }
    
    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseDragged(final MouseEvent e) {
        final Point p = e.getLocationOnScreen();
        p.translate(-1 * startX, -1 * startY);
        setLocation(p);
    }
    
    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseMoved(final MouseEvent e) {
        // Do nothing
    }
    
    /**
     * Sets the font size that this OSD uses.
     * 
     * @param size The new size of the font
     */
    public void setFontSize(final int size) {
        label.setFont(label.getFont().deriveFont((float) size));
    }

    /**
     * Sets the background colour for this OSD.
     * 
     * @param colour The background colour to use
     */
    public void setBackgroundColour(final String colour) {
        panel.setBackground(ColourManager.parseColour(colour));
    }

    /**
     * Sets the foreground colour for this OSD.
     * 
     * @param colour The foreground colour to use
     */
    public void setForegroundColour(final String colour) {
        label.setForeground(ColourManager.parseColour(colour));
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(final boolean b) {
        super.setVisible(b);
        
        if (b) {
            windows.add(this);
            transferFocusBackward();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        super.dispose();
        
        windows.remove(this);
    }
}
