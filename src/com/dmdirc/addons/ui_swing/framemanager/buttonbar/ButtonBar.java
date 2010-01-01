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
package com.dmdirc.addons.ui_swing.framemanager.buttonbar;

import com.dmdirc.FrameContainer;
import com.dmdirc.FrameContainerComparator;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.FrameInfoListener;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.FramemanagerPosition;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.util.MapList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

/**
 * The button bar manager is a grid of buttons that presents a manager similar
 * to that used by mIRC.
 *
 * @author chris
 */
public final class ButtonBar implements FrameManager, ActionListener,
        ComponentListener, Serializable, NotificationListener,
        SelectionListener, FrameInfoListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** A map of parent containers to their respective windows. */
    private final MapList<FrameContainer, FrameContainer> windows;
    /** A map of containers to the buttons we're using for them. */
    private final Map<FrameContainer, JToggleButton> buttons;
    /** The position of this frame manager. */
    private final FramemanagerPosition position;
    /** The parent for the manager. */
    private JComponent parent;
    /** The panel used for our buttons. */
    private final JPanel panel;
    /** The currently selected window. */
    private transient FrameContainer selected;
    /** Selected window. */
    private Window activeWindow;
    /** The number of buttons per row or column. */
    private int cells = 1;
    /** The number of buttons to render per {cell,row}. */
    private int maxButtons = Integer.MAX_VALUE;
    /** The width of buttons. */
    private int buttonWidth;

    /** Creates a new instance of DummyFrameManager. */
    public ButtonBar() {
        windows = new MapList<FrameContainer, FrameContainer>();
        buttons = new HashMap<FrameContainer, JToggleButton>();
        position = FramemanagerPosition.getPosition(
                IdentityManager.getGlobalConfig().getOption("ui", "framemanagerPosition"));

        if (position.isHorizontal()) {
            panel = new JPanel(new MigLayout("ins 0, fill, flowx"));
        } else {
            panel = new JPanel(new MigLayout("ins 0, fill, flowy"));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(final JComponent parent) {
        this.parent = parent;

        parent.setLayout(new MigLayout());
        parent.add(panel);

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

        for (Map.Entry<FrameContainer, List<FrameContainer>> entry : windows.entrySet()) {
            buttons.get(entry.getKey()).setPreferredSize(new Dimension(buttonWidth, 25));
            buttons.get(entry.getKey()).setMinimumSize(new Dimension(buttonWidth, 25));
            panel.add(buttons.get(entry.getKey()));

            Collections.sort(entry.getValue(), new FrameContainerComparator());

            for (FrameContainer child : entry.getValue()) {
                buttons.get(child).setPreferredSize(new Dimension(buttonWidth, 25));
                buttons.get(child).setMinimumSize(new Dimension(buttonWidth, 25));
                panel.add(buttons.get(child));
            }
        }
        panel.validate();
    }

    /**
     * Adds a button to the button array with the details from the specified
     * container.
     *
     * @param source The Container to get title/icon info from
     */
    private void addButton(final FrameContainer source) {
        final JToggleButton button = new JToggleButton(source.toString(),
                IconManager.getIconManager().getIcon(source.getIcon()));

        button.addActionListener(this);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new Insets(0, 0, 0, 0));

        buttons.put(source, button);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPositionVertically() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPositionHorizontally() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer window) {
        windows.add(window);
        addButton(window);

        relayout();
        window.addNotificationListener(this);
        window.addSelectionListener(this);
        window.addFrameInfoListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer window) {
        windows.remove(window);

        relayout();
        window.removeNotificationListener(this);
        window.removeFrameInfoListener(this);
        window.removeSelectionListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer parent, final FrameContainer window) {
        windows.add(parent, window);
        addButton(window);

        relayout();
        window.addNotificationListener(this);
        window.addSelectionListener(this);
        window.addFrameInfoListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer parent, final FrameContainer window) {
        windows.remove(parent, window);

        relayout();
        window.removeNotificationListener(this);
        window.removeFrameInfoListener(this);
        window.removeSelectionListener(this);
    }

    /**
     * Called when the user clicks on one of the buttons.
     *
     * @param e The action event associated with this action
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        for (Map.Entry<FrameContainer, JToggleButton> entry : buttons.entrySet()) {
            if (entry.getValue().equals(e.getSource())) {
                if (entry.getKey().getFrame().equals(activeWindow)) {
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
    @Override
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
    @Override
    public void componentMoved(final ComponentEvent e) {
        // Do nothing
    }

    /**
     * Called when the parent component is made visible.
     *
     * @param e A ComponentEvent corresponding to this event.
     */
    @Override
    public void componentShown(final ComponentEvent e) {
        // Do nothing
    }

    /**
     * Called when the parent component is made invisible.
     *
     * @param e A ComponentEvent corresponding to this event.
     */
    @Override
    public void componentHidden(final ComponentEvent e) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void notificationSet(final Window window, final Color colour) {
        if (buttons.containsKey(window.getContainer())) {
            buttons.get(window.getContainer()).setForeground(colour);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notificationCleared(final Window window) {
        notificationSet(window, window.getContainer().getNotification());
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final Window window) {
        activeWindow = window;   
        if (selected != null && buttons.containsKey(selected)) {
            buttons.get(selected).setSelected(false);
        }

        selected = window.getContainer();

        if (buttons.containsKey(window.getContainer())) {
            buttons.get(window.getContainer()).setSelected(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void iconChanged(final Window window, final String icon) {
        buttons.get(window.getContainer()).setIcon(IconManager.getIconManager().getIcon(icon));
    }


    /** {@inheritDoc} */
    @Override
    public void nameChanged(final Window window, final String name) {
        buttons.get(window.getContainer()).setText(name);
    }
}
