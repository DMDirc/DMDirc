/*
 * 
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

package com.dmdirc.addons.ui_swing.framemanager.tree;

import javax.swing.JMenuItem;

/**
 * TreeViewNode encapsulating JMenuItem.
 */
public class TreeViewNodeMenuItem extends JMenuItem {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Tree view node. */
    private TreeViewNode treeNode;

    /**
     * Creates a new treeviewnodemenuitem.
     *
     * @param name Menu item name
     * @param actionCommand Action command
     * @param treeNode Encapsulated treeview node
     */
    public TreeViewNodeMenuItem(final String name, final String actionCommand,
            final TreeViewNode treeNode) {
        super(name);
        
        this.treeNode = treeNode;
        setActionCommand(actionCommand);
    }

    /**
     * Get the value of treeNode
     *
     * @return the value of treeNode
     */
    public TreeViewNode getTreeNode() {
        return treeNode;
    }

    /**
     * Set the value of treeNode
     *
     * @param treeNode new value of treeNode
     */
    public void setTreeNode(TreeViewNode treeNode) {
        this.treeNode = treeNode;
    }
}
