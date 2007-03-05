package uk.org.ownage.dmdirc.ui.framemanager.tree;

import java.io.Serializable;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import uk.org.ownage.dmdirc.FrameContainer;

public class TreeViewModel implements Serializable, TreeModel {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    protected TreeViewNode root;
    
    protected EventListenerList listenerList = new EventListenerList();
    
    public TreeViewModel(TreeViewNode root) {
        this.root = root;
    }
    
    public Object getRoot() {
        return root;
    }
    
    public Object getChild(Object parent, int index) {
        return ((TreeViewNode)parent).getChildAt(index);
    }
    
    public int getChildCount(Object parent) {
        return ((TreeViewNode)parent).getChildCount();
    }
    
    public boolean isLeaf(Object node) {
        return ((TreeViewNode)node).isLeaf();
    }
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        TreeViewNode node = (TreeViewNode)path.getLastPathComponent();
        node.setFrameContainerObject((FrameContainer)newValue);
        //TODO nodeChanged
    }
    
    public int getIndexOfChild(Object parent, Object child) {
        return -1;
    }
    
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }
    
    public void insert(TreeViewNode parent, TreeViewNode child) {
        parent.addChild(child);
    }
    
    public void remove(TreeViewNode parent, TreeViewNode child) {
        parent.removeChild(child);
    }
}


