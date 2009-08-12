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

package com.dmdirc.addons.ui_swing.components.desktopPane;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.TreeScroller;
import com.dmdirc.addons.ui_swing.components.frames.InputTextFrame;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.framemanager.tree.TreeViewModel;
import com.dmdirc.addons.ui_swing.framemanager.tree.TreeViewNode;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.interfaces.FrameListener;
import com.dmdirc.util.ReturnableThread;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.plaf.DesktopPaneUI;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * DMDirc Extentions to JDesktopPane.
 */
public class DMDircDesktopPane extends JDesktopPane implements FrameListener,
        SelectionListener, PropertyChangeListener {

    /** Logger to use. */
    private static final java.util.logging.Logger LOGGER =
            java.util.logging.Logger.getLogger(DMDircDesktopPane.class.getName());
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** The current number of pixels to displace new frames in the X
     * direction. */
    private int xOffset;
    /** The current number of pixels to displace new frames in the Y
     * direction. */
    private int yOffset;
    /** The number of pixels each new internal frame is offset by. */
    private static final int FRAME_OPENING_OFFSET = 30;
    /** Node storage, used for adding and deleting nodes correctly. */
    private final Map<FrameContainer, TreeViewNode> nodes;
    /** Data model. */
    private final TreeViewModel model;
    /** Selected model. */
    private final TreeSelectionModel selectionModel;
    /** Tree Scroller. */
    private final TreeScroller treeScroller;
    /** Selected window. */
    private Window selectedWindow;
    /** Maximised state. */
    private boolean maximised;
    /** Changing maximisation. */
    private AtomicBoolean changing = new AtomicBoolean(false);
    /** Main Frame. */
    private MainFrame mainFrame;

    /**
     * Initialises the DMDirc desktop pane.
     * 
     * @param mainFrame Main frame
     */
    public DMDircDesktopPane(final MainFrame mainFrame) {
        super();

        this.mainFrame = mainFrame;
        setBackground(new Color(238, 238, 238));
        setBorder(BorderFactory.createEtchedBorder());
        setUI(new ProxyDesktopPaneUI(getUI(), this));

        nodes = new HashMap<FrameContainer, TreeViewNode>();
        model = new TreeViewModel(new TreeViewNode(null, null));
        selectionModel = new DefaultTreeSelectionModel();
        treeScroller = new TreeScroller(model, selectionModel, false) {

            /** {@inheritDoc} */
            @Override
            protected void setPath(final TreePath path) {
                super.setPath(path);
                ((TreeViewNode) path.getLastPathComponent()).getFrameContainer().
                        activateFrame();
            }
        };

        WindowManager.addFrameListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void setUI(final DesktopPaneUI ui) {
        if (ui instanceof ProxyDesktopPaneUI) {
            super.setUI(ui);
        } else {
            super.setUI(new ProxyDesktopPaneUI(ui, this));
        }
    }

    /**
     * Add a specified component at the specified index.
     * 
     * @param comp Component to add
     * @param index Index for insertion
     */
    public void add(final JComponent comp, final int index) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                addImpl(comp, null, index);

                // Make sure it'll fit with our offsets
                if (comp.getWidth() + xOffset > getWidth()) {
                    xOffset = 0;
                }
                if (comp.getHeight() + yOffset > getHeight()) {
                    yOffset = 0;
                }

                // Position the frame
                comp.setLocation(xOffset, yOffset);

                // Increase the offsets
                xOffset += FRAME_OPENING_OFFSET;
                yOffset += FRAME_OPENING_OFFSET;
            }
        });
    }

    /**
     * Returns the select window.
     * 
     * @return Selected window, or null.
     */
    public Window getSelectedWindow() {
        return UIUtilities.invokeAndWait(new ReturnableThread<Window>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(selectedWindow);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer window) {
        addWindow(model.getRootNode(), window);
    }

    @Override
    public void addWindow(final FrameContainer parent,
            final FrameContainer window) {
        UIUtilities.invokeAndWait(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized (nodes) {
                    addWindow(nodes.get(parent), window);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer parent,
            final FrameContainer window) {
        delWindow(window);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer window) {
        UIUtilities.invokeAndWait(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (nodes == null || nodes.get(window) == null) {
                    return;
                }
                final TreeViewNode node = nodes.get(window);
                if (node.getLevel() == 0) {
                    Logger.appError(ErrorLevel.MEDIUM,
                            "delServer triggered for root node" +
                            node.toString(),
                            new IllegalArgumentException());
                } else {
                    model.removeNodeFromParent(nodes.get(window));
                }
                nodes.remove(window);
                window.removeSelectionListener(DMDircDesktopPane.this);
                ((TextFrame) window.getFrame()).removePropertyChangeListener(
                        DMDircDesktopPane.this);
                if (getAllFrames().length == 0) {
                    mainFrame.setTitle(null);
                }
            }
        });
    }

    /**
     * Adds a window to the frame container.
     *
     * @param parent Parent node
     * @param window Window to add
     */
    public void addWindow(final TreeViewNode parent,
            final FrameContainer window) {
        UIUtilities.invokeAndWait(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final TreeViewNode node = new TreeViewNode(null, window);
                synchronized (nodes) {
                    nodes.put(window, node);
                }
                node.setUserObject(window);
                model.insertNodeInto(node, parent);
                window.addSelectionListener(DMDircDesktopPane.this);
                ((TextFrame) window.getFrame()).addPropertyChangeListener(
                        DMDircDesktopPane.this);
            }
        });
    }

    /** Scrolls up. */
    public void scrollUp() {
        treeScroller.changeFocus(true);
    }

    /** Scrolls down. */
    public void scrollDown() {
        treeScroller.changeFocus(false);
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final Window window) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                selectedWindow = window;
                final TreeNode[] path =
                        model.getPathToRoot(nodes.get(window.getContainer()));
                if (path != null && path.length > 0) {
                    selectionModel.setSelectionPath(new TreePath(path));
                }
                if (window instanceof InputTextFrame) {
                    ((InputTextFrame) window).requestInputFieldFocus();
                }
                mainFrame.setTitle(window.getTitle());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if ("title".equals(evt.getPropertyName())) {
            handleTitleEvent((Window) evt.getSource(),
                    ((Window) evt.getSource()).getTitle());
        } else if ("maximum".equals(evt.getPropertyName())) {
            handleMaximiseEvent((Boolean) evt.getNewValue(),
                    ((Window) evt.getSource()).getTitle());
        }
    }

    private void handleTitleEvent(final Window window, final String title) {
        if (maximised && (window == selectedWindow)) {
            mainFrame.setTitle(title);
        } else if (!maximised) {
            mainFrame.setTitle(null);
        }
    }

    private void handleMaximiseEvent(final boolean isMaximised,
            final String title) {
        if (changing.get()) {
            return;
        }
        changing.set(true);
        maximised = isMaximised;
        Stack<JInternalFrame> stack = new Stack<JInternalFrame>();
        stack.addAll(Arrays.asList(getAllFrames()));

        while (!stack.empty()) {
            JInternalFrame frame = stack.pop();
            if (isMaximised) {
                if (!frame.isMaximum()) {
                    ((Window) frame).maximise();
                }
            } else {
                if (frame.isMaximum()) {
                    ((Window) frame).restore();
                }
            }
        }
        if (selectedWindow != null) {
            selectedWindow.activateFrame();
        }
        if (!isMaximised) {
            mainFrame.setTitle(title);
        } else {
            mainFrame.setTitle(null);
        }
        changing.set(false);
    }
}
