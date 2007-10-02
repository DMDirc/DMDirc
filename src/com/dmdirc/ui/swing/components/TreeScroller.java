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

package com.dmdirc.ui.swing.components;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Utility class to provide mouse wheel scrolling to a JTree.
 */
public class TreeScroller implements MouseWheelListener {
    
    /** Tree to scroll. */
    private final JTree tree;
    /** Root visible. */
    private final boolean rootVisible;
    /** Root node. */
    private final DefaultMutableTreeNode rootNode;
    
    /** 
     * Creates a new instance of TreeScroller.
     *
     * @param tree Tree to scroll over
     */
    public TreeScroller(final JTree tree) {
        this.tree = tree;
        
        rootVisible = tree.isRootVisible();
        rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
        
        tree.addMouseWheelListener(this);
    }
    
    /** {@inheritDoc} */
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
    private void changeFocus(final boolean direction) {
        DefaultMutableTreeNode thisNode;
        DefaultMutableTreeNode nextNode;
        
        if (rootNode == null || !rootVisible) {
            //no root node or root node not visible
            return;
        }
        
        if (rootNode.getChildCount() == 0) {
            //root node has no children
            return;
        }
        
        if (tree.getSelectionModel().isSelectionEmpty() && rootVisible) {
            //no selected node, start from the root node
            thisNode = rootNode;
        } else if (tree.getSelectionModel().isSelectionEmpty()) {
            //no selected node, get the root node
            thisNode = rootNode;
            //are there any children to select?
            if (thisNode.getChildCount() > 0) {
                thisNode = (DefaultMutableTreeNode) thisNode.getChildAt(0);
            } else {
                //then wait till there are
                return;
            }
        } else {
            //use the selected node to start from
            thisNode = (DefaultMutableTreeNode) tree.getSelectionModel().
                    getSelectionPath().getLastPathComponent();
        }
        
        //are we going up or down?
        if (direction) {
            //up
            nextNode = changeFocusUp(thisNode);
        } else {
            //down
            nextNode = changeFocusDown(thisNode);
        }
        tree.setSelectionPath(new TreePath(((DefaultTreeModel) tree.getModel()).getPathToRoot(nextNode)));
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
        
        if (nextNode == null || (nextNode == tree.getModel().getRoot()
        && !tree.isRootVisible())) {
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
        
        if (nextNode == null && !tree.isRootVisible()) {
            nextNode = (DefaultMutableTreeNode) rootNode.getFirstChild();
        } else if (nextNode == null) {
            nextNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
        }
        
        return nextNode;
    }
    
}
