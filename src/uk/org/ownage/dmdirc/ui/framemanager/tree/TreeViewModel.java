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

import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.Query;
import uk.org.ownage.dmdirc.Raw;
import uk.org.ownage.dmdirc.Server;

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
     * @param root a TreeNode object that is the root of the tree.
     */
    public TreeViewModel(final TreeNode root) {
	super(root);
    }
    
    /**
     * Creates a tree specifying whether any node can have children,
     * or whether only certain nodes can have children.
     * @param asksAllowsChildren true = ask whether child can have chilren,
     * false all nodes can have chilren.
     * @param root a root TreeNode.
     */
    public TreeViewModel(final TreeNode root, final boolean asksAllowsChildren) {
	super(root, asksAllowsChildren);
    }
    
    /**
     * Inserts a new node into the tree and fires the appropriate events.
     * @param newChild child to be added.
     * @param parent parent child is to be added too.
     */
    public final void insertNodeInto(final DefaultMutableTreeNode newChild,
	    final DefaultMutableTreeNode parent) {
	int index = 0;
	index = getIndex(newChild, parent);
	super.insertNodeInto(newChild, parent, index);
    }
    
    /**
     * Compares the new child with the existing children or parent to decide
     * where it needs to be inserted.
     *
     * @param newChild new node to be inserted.
     * @param parent node the new node will be inserted into.
     * @return index where new node is to be inserted.
     */
    private int getIndex(final DefaultMutableTreeNode newChild,
	    final DefaultMutableTreeNode parent) {
	
	if (parent.equals(root)
	&& !Config.getOptionBool("ui", "sortservers")) {
	    return parent.getChildCount();
	}
	
	for (int i = 0; i < parent.getChildCount(); i++) {
	    final DefaultMutableTreeNode child =
		    (DefaultMutableTreeNode) parent.getChildAt(i);
	    if (sortBefore(newChild, child)) {
		return i;
	    } else if (!sortAfter(newChild, child)
	    && Config.getOptionBool("ui", "sortwindows")
	    && newChild.getUserObject().toString().compareToIgnoreCase(
		    child.getUserObject().toString()) < 0) {
		return i;
	    }
	}
	
	return parent.getChildCount();
    }
    
    /**
     * Compares the types of the specified nodes' objects to see if the new
     * node should be sorted before the other.
     * @param newChild The new child to be tested
     * @param child The existing child that it's being tested against
     * @return True iff newChild should be sorted before child
     */
    private boolean sortBefore(final DefaultMutableTreeNode newChild,
	    final DefaultMutableTreeNode child) {
	
	return getPosition(newChild.getUserObject())
	< getPosition(child.getUserObject());
    }
    
    /**
     * Compares the types of the specified nodes' objects to see if the new
     * node should be sorted after the other.
     * @param newChild The new child to be tested
     * @param child The existing child that it's being tested against
     * @return True iff newChild should be sorted before child
     */
    private boolean sortAfter(final DefaultMutableTreeNode newChild,
	    final DefaultMutableTreeNode child) {
	
	return getPosition(newChild.getUserObject())
	> getPosition(child.getUserObject());
    }
    
    /**
     * Returns an integer corresponding to the expected order of an object.
     * @param object The object to be tested
     * @return Position of the object
     */
    private int getPosition(final Object object) {
	if (object instanceof Server) {
	    return 1;
	} else if (object instanceof Raw) {
	    return 2;
	} else if (object instanceof Channel) {
	    return 3;
	} else if (object instanceof Query) {
	    return 4;
	} else {
	    return 5;
	}
    }
}


