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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;
import javax.swing.tree.TreeNode;
import uk.org.ownage.dmdirc.FrameContainer;

public class TreeViewNode implements Cloneable, TreeNode, Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    private TreeViewNode parent =  null;
    
    private Vector<TreeViewNode> children = null;
    
    transient private FrameContainer frameContainer = null;
    
    private boolean allowsChildren = true;
    
    private boolean collapsed = false;
    
    public TreeViewNode() {
    }
    
    public TreeViewNode(FrameContainer frameContainer) {
        this.frameContainer = frameContainer;
    }
    
    public TreeViewNode(FrameContainer frameContainer, boolean allowsChildren) {
        parent = null;
        this.allowsChildren = allowsChildren;
        this.frameContainer = frameContainer;
    }
    
    public TreeNode getChildAt(int childIndex) throws IndexOutOfBoundsException {
        if (children == null) {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }
        return children.get(childIndex);
    }
    
    public int getChildCount() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }
    
    public TreeNode getParent() {
        return parent;
    }
    
    public int getIndex(TreeNode node) {
        if (children != null && children.contains(node)) {
            return children.indexOf(node);
        }
        return -1;
    }
    
    public boolean getAllowsChildren() {
        return allowsChildren;
    }
    
    public boolean isLeaf() {
        if (children == null) {
            return true;
        }
        return false;
    }
    
    public Enumeration children() {
        if (children == null) {
            return new Enumeration<TreeNode>() {
                public boolean hasMoreElements() { return false; }
                public TreeNode nextElement() {
                    throw new NoSuchElementException("No more elements");
                }
            };
        } else {
            return children.elements();
        }
    }
    
    public void setFrameContainerObject(FrameContainer frameContainer) {
        this.frameContainer = frameContainer;
    }
    
    public FrameContainer getFrameContainerObject() {
        return frameContainer;
    }
    
    public boolean isCollapsed() {
        return collapsed;
    }
    
    public boolean setCollapsed(boolean collapsed) {
        return this.collapsed = collapsed;
    }
    
    public void addChild(TreeViewNode node) {
        if (!allowsChildren) {
            throw new UnsupportedOperationException("Node does not allow children");
        }
        if (children == null) {
            children = new Vector<TreeViewNode>();
        }
        children.add(node);
    }
    
    public boolean removeChild(TreeViewNode node) {
        if (children != null && children.contains(node)) {
            children.remove(node);
        }
        return false;
    }
    
    public String toString() {
        if (frameContainer == null) {
            return "Generic Node";
        }
        return frameContainer.toString();
    }
    
    public boolean equals(Object object) {
        if (frameContainer == null) {
            return super.equals(object);
        }
        return frameContainer.equals(object);
    }
}
