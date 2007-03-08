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

package uk.org.ownage.dmdirc.ui.framemanager.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * A simple sorted tree data model based on DefaultTreeModel.
 */
public class TreeViewModel extends DefaultTreeModel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * Creates a tree in which any node can have children.
     *
     * @param root a TreeNode object that is the root of the tree
     */
    public TreeViewModel(TreeNode root) {
        super(root);
    }
    
    /**
     * Creates a tree specifying whether any node can have children,
     * or whether only certain nodes can have children.
     * @param asksAllowsChildren true = ask whether child can have chilren,
     * false all nodes can have chilren
     * @param root a root TreeNode
     */
    public TreeViewModel(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    }
    
    /**
     * Inserts a new node into the tree and fires the appropriate events
     * @param newChild child to be added
     * @param parent parent child is to be added too
     */
    public void insertNodeInto(DefaultMutableTreeNode newChild, DefaultMutableTreeNode parent) {
        int index = 0;
        index = doComparison(newChild, parent);
        super.insertNodeInto(newChild, parent, index);
    }
    
    /*
     *
     */
    /**
     * Compares the new child with the existing children or parent to decide
     * where it needs to be inserted
     *
     * @param newChild
     * @param parent
     * @return
     */
    private int doComparison(DefaultMutableTreeNode newChild, DefaultMutableTreeNode parent) {
        if (parent == root) {
            return root.getChildCount();
        }
        return parent.getChildCount();
    }
}


