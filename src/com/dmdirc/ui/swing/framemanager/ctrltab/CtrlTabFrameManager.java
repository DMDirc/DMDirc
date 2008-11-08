/*
 * Copyright (c) 2006-2008 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.ui.swing.framemanager.ctrltab;

import com.dmdirc.FrameContainer;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.FrameManager;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.swing.UIUtilities;
import com.dmdirc.ui.swing.components.TreeScroller;
import com.dmdirc.ui.swing.framemanager.tree.TreeViewModel;

import com.dmdirc.ui.swing.framemanager.tree.TreeViewNode;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Manages the ctrl tab window list.
 */
public final class CtrlTabFrameManager implements FrameManager,
        Serializable, TreeSelectionListener,
        SelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** Node storage, used for adding and deleting nodes correctly. */
    private final Map<FrameContainer, TreeViewNode> nodes;
    /** Data model. */
    private final TreeViewModel model;
    /** Selected model. */
    private final TreeSelectionModel selectionModel;
    /** Tree Scroller. */
    private final TreeScroller treeScroller;

    /**
     * Creates a new instance of WindowMenuFrameManager.
     *
     * @param desktopPane DesktopPane to register with
     */
    public CtrlTabFrameManager(final JDesktopPane desktopPane) {
        nodes = new HashMap<FrameContainer, TreeViewNode>();
        model = new TreeViewModel(new TreeViewNode(null, null));
        selectionModel = new DefaultTreeSelectionModel();
        treeScroller =
                new TreeScroller(model, selectionModel);
        selectionModel.addTreeSelectionListener(this);

        InputMap inputMap = SwingUtilities.getUIInputMap(desktopPane,
                JDesktopPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        if (inputMap == null) {
            inputMap =
                    desktopPane.getInputMap(JDesktopPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        }
        inputMap.put(KeyStroke.getKeyStroke("ctrl shift pressed TAB"),
                "selectPreviousFrame");

        ActionMap actionMap = SwingUtilities.getUIActionMap(desktopPane);
        if (actionMap == null) {
            actionMap = desktopPane.getActionMap();
        }
        actionMap.put("selectNextFrame", new AbstractAction("selectNextFrame") {

            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent evt) {
                scrollDown();
            }
        });

        SwingUtilities.getUIActionMap(desktopPane).
                put("selectPreviousFrame",
                new AbstractAction("selectPreviousFrame") {

                    private static final long serialVersionUID = 1;

                    /** {@inheritDoc} */
                    @Override
                    public void actionPerformed(final ActionEvent evt) {
                        scrollUp();
                    }
                });
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(final JComponent parent) {
    //Ignore
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
                window.removeSelectionListener(CtrlTabFrameManager.this);
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
                window.addSelectionListener(CtrlTabFrameManager.this);
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
    public void valueChanged(final TreeSelectionEvent e) {
        ((TreeViewNode) e.getPath().getLastPathComponent()).getFrameContainer().
                activateFrame();
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final Window window) {
        final TreeNode[] path =
                model.getPathToRoot(nodes.get(window.getContainer()));
        if (path != null && path.length > 0) {
            selectionModel.setSelectionPath(new TreePath(path));
        }
    }
}
