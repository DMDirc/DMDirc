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
package com.dmdirc.addons.ui_swing.components;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Utility class to provide mouse wheel scrolling to a JTree.
 */
public class TreeScroller implements MouseWheelListener {

    /** Tree to scroll. */
    private final DefaultTreeModel model;
    /** Tree to scroll. */
    private final TreeSelectionModel selectionModel;
    /** Root visible. */
    private final boolean rootVisible;
    /** Root node. */
    private final DefaultMutableTreeNode rootNode;
    /** Tree. */
    protected JTree tree;

    /**
     * Creates a new instance of TreeScroller.
     *
     * @param tree Tree to scroll over
     */
    public TreeScroller(final JTree tree) {
        this.tree = tree;
        this.model = (DefaultTreeModel) tree.getModel();
        this.selectionModel = tree.getSelectionModel();

        rootVisible = tree.isRootVisible();
        rootNode =
                (DefaultMutableTreeNode) tree.getModel().getRoot();

        tree.addMouseWheelListener(this);
    }

    /**
     * Creates a new instance of TreeScroller.
     *
     * @param model Tree model to scroll over
     * @param selectionModel Tree selection model to scroll over
     * @param rootVisible Is the root node visible
     */
    public TreeScroller(final DefaultTreeModel model,
            final TreeSelectionModel selectionModel, final boolean rootVisible) {
        this.model = model;
        this.selectionModel = selectionModel;

        this.rootVisible = rootVisible;
        rootNode = (DefaultMutableTreeNode) model.getRoot();
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse wheel event
     */
    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {
            changeFocus(true);
        } else {
            changeFocus(false);
        }
    }

    /**
     * Activates the node above or below the active node in the tree.
     *
     * @param direction true = up, false = down.
     */
    public void changeFocus(final boolean direction) {
        DefaultMutableTreeNode thisNode;
        DefaultMutableTreeNode nextNode;

        if (rootNode == null) {
            //no root node or root node not visible
            return;
        }

        if (!rootVisible && rootNode.getChildCount() == 0) {
            //root node has no children
            return;
        }

        if (selectionModel.isSelectionEmpty()) {
            if (rootVisible) {
                thisNode = rootNode;
            } else {
                thisNode = (DefaultMutableTreeNode) rootNode.getChildAt(0);
            }
        } else {
            thisNode = (DefaultMutableTreeNode) selectionModel.getSelectionPath().getLastPathComponent();
        }

        //are we going up or down?
        if (direction) {
            //up
            nextNode = changeFocusUp(thisNode);
        } else {
            //down
            nextNode = changeFocusDown(thisNode);
        }
        setPath(new TreePath(model.getPathToRoot(nextNode)));
    }

    /**
     * Sets the tree selection path.
     *
     * @param path Path
     */
    protected void setPath(final TreePath path) {
        selectionModel.setSelectionPath(path);
    }

    /**
     * Changes the tree focus up.
     *
     * @param node Start node
     *
     * @return next node
     */
    private DefaultMutableTreeNode changeFocusUp(final DefaultMutableTreeNode node) {
        DefaultMutableTreeNode nextNode;

        nextNode = node.getPreviousNode();

        if (nextNode == null || (nextNode == model.getRoot() && !rootVisible)) {
            nextNode = rootNode.getLastLeaf();
        }

        return nextNode;
    }

    /**
     * Changes the tree focus down.
     *
     * @param node Start node
     *
     * @return next node
     */
    private DefaultMutableTreeNode changeFocusDown(final DefaultMutableTreeNode node) {
        DefaultMutableTreeNode nextNode;

        nextNode = node.getNextNode();

        if (nextNode == null && !rootVisible) {
            nextNode =
                    (DefaultMutableTreeNode) rootNode.getFirstChild();
        } else if (nextNode == null) {
            nextNode = (DefaultMutableTreeNode) model.getRoot();
        }

        return nextNode;
    }
}
