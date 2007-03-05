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
