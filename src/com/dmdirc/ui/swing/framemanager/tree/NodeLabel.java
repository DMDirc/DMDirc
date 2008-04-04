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

package com.dmdirc.ui.swing.framemanager.tree;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.interfaces.IconChangeListener;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;

import net.miginfocom.layout.PlatformDefaults;

/**
 *
 */
public class NodeLabel extends JLabel implements SelectionListener,
        NotificationListener, IconChangeListener, ConfigChangeListener,
        MouseListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Parent frame manager. */
    private final TreeFrameManager manager;
    /** Node window. */
    private final Window window;
    /** Config manager. */
    private final ConfigManager config;
    /** Rollover colours. */
    private Color rolloverColour;
    /** Active bold. */
    private boolean activeBold;
    /** Active background. */
    private Color activeBackground;
    /** Active foreground. */
    private Color activeForeground;
    /** notification set? */
    private boolean notification;

    /** 
     * Instantiates a new node label.
     * 
     * @param manager Parent manager.
     * @param window Window for this node
     */
    public NodeLabel(final TreeFrameManager manager, final Window window) {
        super();

        this.manager = manager;
        this.window = window;
        config = IdentityManager.getGlobalConfig();

        setColours();

        addMouseListener(this);
        config.addChangeListener("ui", this);
        config.addChangeListener("treeview", this);

        init();
    }

    /**
     * Initialises the label.
     */
    private void init() {
        if (window == null) {
            return;
        }

        //window.getContainer().addSelectionListener(this);
        window.getContainer().addIconChangeListener(this);
        window.getContainer().addNotificationListener(this);

        setText(window.getContainer().toString());

        setBackground(manager.getTree().getBackground());
        setForeground(manager.getTree().getForeground());
        setOpaque(true);
        setToolTipText(null);
        setIcon(window.getContainer().getIcon());
        setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));

        setPreferredSize(new Dimension(100000, getFont().getSize() +
                (int) PlatformDefaults.getUnitValueX("related").
                getValue()));
        notification = false;
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final Window window) {
        if (equals(window)) {
            if (activeBold) {
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            setBackground(activeBackground);
            setForeground(activeForeground);
        } else if (!notification) {
            setFont(getFont().deriveFont(Font.PLAIN));
            setBackground(manager.getTree().getBackground());
            setForeground(manager.getTree().getForeground());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notificationSet(final Window window, final Color colour) {
        if (equals(window)) {
            setForeground(colour);
            notification = true;
            manager.getTree().repaint();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notificationCleared(final Window window) {
        if (equals(window)) {
            setForeground(manager.getTree().getForeground());
            notification = false;
            manager.getTree().repaint();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void iconChanged(final Window window, final Icon icon) {
        if (equals(window)) {
            setIcon(icon);
            manager.getTree().repaint();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if (("ui".equals(domain) || "treeview".equals(domain)) &&
                ("treeviewRolloverColour".equals(key) ||
                "treeviewActiveBackground".equals(key) ||
                "treeviewActiveForeground".equals(key) ||
                "treeviewActiveBold".equals(key) ||
                "backgroundcolour".equals(key) ||
                "foregroundcolour".equals(key))) {
            setColours();
        }
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
    //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
    //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
    //Ignore
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        setBackground(rolloverColour);
    }

    /** 
     * {@inheritDoc}
     * 
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        setBackground(manager.getTree().getBackground());
    }
    
    /** {@inheritDoc} */
    @Override   
    public boolean equals(final Object obj) {
        if (window == null) {
            return false;
        }
        if (obj == null) {
            return false;
        }
        return window.equals(obj);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.window == null ? 0 : this.window.hashCode());
        return hash;
    }

    /** Sets the colours for the renderer. */
    private void setColours() {
        rolloverColour = config.getOptionColour(
                "ui", "treeviewRolloverColour",
                config.getOptionColour("treeview", "backgroundcolour",
                config.getOptionColour("ui", "backgroundcolour",
                ColourManager.parseColour("f0f0f0"))));
        activeBackground = config.getOptionColour(
                "ui", "treeviewActiveBackground",
                config.getOptionColour("treeview", "backgroundcolour",
                config.getOptionColour("ui", "backgroundcolour",
                manager.getTree().getBackground())));
        activeForeground = config.getOptionColour(
                "ui", "treeviewActiveForeground",
                config.getOptionColour("treeview", "foregroundcolour",
                config.getOptionColour("ui", "foregroundcolour",
                manager.getTree().getForeground())));
        activeBold = config.getOptionBool("ui", "treeviewActiveBold", false);
    }
}
