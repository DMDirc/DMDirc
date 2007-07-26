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

package com.dmdirc.ui.swing.framemanager.buttonbar;

import com.dmdirc.Channel;
import com.dmdirc.Config;
import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.ui.swing.UIUtilities;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.FramemanagerPosition;
import com.dmdirc.ui.interfaces.Window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * The button bar manager is a grid of buttons that presents a manager similar
 * to that used by mIRC.
 *
 * @author chris
 */
public final class ButtonBar implements FrameManager, ActionListener,
        ComponentListener, Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** A map of servers to their respective windows. */
    private final Map<Server, List<FrameContainer>> windows;
    
    /** A map of containers to the buttons we're using for them. */
    private final Map<FrameContainer, JToggleButton> buttons;
    
    /** The position of this frame manager. */
    private final FramemanagerPosition position;
    
    /** The parent for the manager. */
    private JComponent parent;
    
    /** The panel used for our buttons. */
    private JPanel panel;
    
    /** The currently selected window. */
    private FrameContainer selected;
    
    /** The number of buttons per row or column. */
    private int cells = 1;
    
    /** The number of buttons to render per {cell,row}. */
    private int maxButtons = Integer.MAX_VALUE;
    
    /** The width of buttons. */
    private int buttonWidth;
    
    /** Creates a new instance of DummyFrameManager. */
    public ButtonBar() {
        windows = new HashMap<Server, List<FrameContainer>>();
        buttons = new HashMap<FrameContainer, JToggleButton>();
        position = FramemanagerPosition.getPosition(Config.getOption("ui", "framemanagerPosition"));
        
        panel = new JPanel(new GridBagLayout());
    }
    
    /** {@inheritDoc} */
    public void setParent(final JComponent parent) {
        this.parent = parent;
        
        parent.setLayout(new BorderLayout());
        parent.setBorder(BorderFactory.createEmptyBorder(
                UIUtilities.SMALL_BORDER, UIUtilities.SMALL_BORDER,
                UIUtilities.SMALL_BORDER, UIUtilities.SMALL_BORDER));
        parent.add(panel, BorderLayout.NORTH);
        
        buttonWidth = position.isHorizontal() ? 150 : (parent.getWidth() - UIUtilities.SMALL_BORDER * 3) / cells;
        
        if (position.isHorizontal()) {
            maxButtons = parent.getWidth() / (buttonWidth + UIUtilities.SMALL_BORDER * 2);
        }
        
        parent.addComponentListener(this);
    }
    
    /**
     * Removes all buttons from the bar and readds them.
     */
    private void relayout() {
        panel.removeAll();
        
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets.set(UIUtilities.SMALL_BORDER,
                UIUtilities.SMALL_BORDER, 0, 0);
        
        for (Map.Entry<Server, List<FrameContainer>> entry : windows.entrySet()) {
            buttons.get(entry.getKey()).setPreferredSize(new Dimension(buttonWidth, 25));
            buttons.get(entry.getKey()).setMinimumSize(new Dimension(buttonWidth, 25));
            panel.add(buttons.get(entry.getKey()), constraints);
            increment(constraints);
            
            Collections.sort(entry.getValue(), new ButtonComparator());
            
            for (FrameContainer child : entry.getValue()) {
                buttons.get(child).setPreferredSize(new Dimension(buttonWidth, 25));
                buttons.get(child).setMinimumSize(new Dimension(buttonWidth, 25));
                panel.add(buttons.get(child), constraints);
                increment(constraints);
            }
        }
        panel.validate();
    }
    
    /**
     * Increments the x and y offsets (where appropriate) of the gridbag
     * constraints.
     *
     * @param constraints The constraints to modify
     */
    public void increment(final GridBagConstraints constraints) {
        if (position.isHorizontal()) {
            constraints.gridx++;
            if (constraints.gridx > maxButtons) {
                constraints.gridy++;
                constraints.gridx = 0;
            }
        } else {
            constraints.gridy++;
            if (constraints.gridy > maxButtons) {
                constraints.gridx++;
                constraints.gridy = 0;
            }
        }
    }
    
    /**
     * Adds a button to the button array with the details from the specified
     * container.
     *
     * @param source The Container to get title/icon info from
     */
    private void addButton(final FrameContainer source) {
        final JToggleButton button = new JToggleButton(source.toString(), source.getIcon());
        
        button.addActionListener(this);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new Insets(0, 0, 0, 0));
        
        buttons.put(source, button);
    }
    
    /** {@inheritDoc} */
    public boolean canPositionVertically() {
        return true;
    }
    
    /** {@inheritDoc} */
    public boolean canPositionHorizontally() {
        return true;
    }
    
    /** {@inheritDoc} */
    public void setSelected(final FrameContainer source) {
        if (selected != null && buttons.containsKey(selected)) {
            buttons.get(selected).setSelected(false);
        }
        
        selected = source;
        
        if (buttons.containsKey(source)) {
            buttons.get(source).setSelected(true);
        }
    }
    
    /** {@inheritDoc} */
    public void showNotification(final FrameContainer source, final Color colour) {
        if (buttons.containsKey(source)) {
            buttons.get(source).setForeground(colour);
        }
    }
    
    /** {@inheritDoc} */
    public void clearNotification(final FrameContainer source) {
        showNotification(source, source.getNotification());
    }
    
    /** {@inheritDoc} */
    public void addServer(final Server server) {
        windows.put(server, new ArrayList<FrameContainer>());
        addButton(server);
        
        relayout();
    }
    
    /** {@inheritDoc} */
    public void delServer(final Server server) {
        windows.remove(server);
        
        relayout();
    }
    
    /** {@inheritDoc} */
    public void addChannel(final Server server, final Channel channel) {
        addCustom(server, channel);
    }
    
    /** {@inheritDoc} */
    public void delChannel(final Server server, final Channel channel) {
        delCustom(server, channel);
    }
    
    /** {@inheritDoc} */
    public void addQuery(final Server server, final Query query) {
        addCustom(server, query);
    }
    
    /** {@inheritDoc} */
    public void delQuery(final Server server, final Query query) {
        delCustom(server, query);
    }
    
    /** {@inheritDoc} */
    public void addCustom(final Server server, final FrameContainer window) {
        windows.get(server).add(window);
        addButton(window);
        
        relayout();
    }
    
    /** {@inheritDoc} */
    public void delCustom(final Server server, final FrameContainer window) {
        windows.get(server).remove(window);
        
        relayout();
    }
    
    /** {@inheritDoc} */
    public void iconUpdated(final FrameContainer window) {
        buttons.get(window).setIcon(window.getIcon());
    }    
    
    /**
     * Called when the user clicks on one of the buttons.
     *
     * @param e The action event associated with this action
     */
    public void actionPerformed(final ActionEvent e) {
        final Window active = Main.getUI().getMainWindow().getActiveFrame();
        
        for (Map.Entry<FrameContainer, JToggleButton> entry : buttons.entrySet()) {
            if (entry.getValue().equals(e.getSource())) {
                if (entry.getKey().getFrame().equals(active)) {
                    entry.getValue().setSelected(true);
                }
                
                entry.getKey().activateFrame();
            }
        }
    }
    
    /**
     * Called when the parent component is resized.
     *
     * @param e A ComponentEvent corresponding to this event.
     */
    public void componentResized(final ComponentEvent e) {
        buttonWidth = position.isHorizontal() ? 150 : (parent.getWidth() - UIUtilities.SMALL_BORDER * 3) / cells;
        
        if (position.isHorizontal()) {
            maxButtons = parent.getWidth() / (buttonWidth + UIUtilities.SMALL_BORDER * 2);
        }
        
        relayout();
    }
    
    /**
     * Called when the parent component is moved.
     *
     * @param e A ComponentEvent corresponding to this event.
     */
    public void componentMoved(final ComponentEvent e) {
        // Do nothing
    }
    
    /**
     * Called when the parent component is made visible.
     *
     * @param e A ComponentEvent corresponding to this event.
     */
    public void componentShown(final ComponentEvent e) {
        // Do nothing
    }
    
    /**
     * Called when the parent component is made invisible.
     *
     * @param e A ComponentEvent corresponding to this event.
     */
    public void componentHidden(final ComponentEvent e) {
        // Do nothing
    }
    
}
