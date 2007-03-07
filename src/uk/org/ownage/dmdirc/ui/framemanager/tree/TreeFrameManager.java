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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Hashtable;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Config;
import uk.org.ownage.dmdirc.FrameContainer;
import uk.org.ownage.dmdirc.Query;
import uk.org.ownage.dmdirc.Raw;
import uk.org.ownage.dmdirc.Server;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;
import uk.org.ownage.dmdirc.ui.framemanager.FrameManager;

/**
 * Manages open windows in the application in a tree style view
 */
public class TreeFrameManager implements FrameManager, TreeModelListener,
        TreeSelectionListener, TreeExpansionListener, TreeWillExpandListener,
        MouseListener, ActionListener, MouseMotionListener, MouseWheelListener {
    
    /**
     * display tree
     */
    private JTree tree;
    
    /**
     * Scrollpane for the tree
     */
    private JScrollPane scrollPane;
    
    /**
     * root node
     */
    private DefaultMutableTreeNode root;
    
    /**
     * node renderer
     */
    private TreeViewTreeCellRenderer renderer;
    
    /**
     * data model
     */
    private DefaultTreeModel model;
    
    /**
     * node storage, used for adding and deleting nodes correctly
     */
    private Hashtable<FrameContainer, DefaultMutableTreeNode> nodes;
    
    /**
     * stores colour associated with a node, cheap hack till i rewrite the model
     */
    private Hashtable<FrameContainer, Color> nodeColours;
    
    /**
     * stores background colour associated with a node, cheap hack till i rewrite the model
     */
    private DefaultMutableTreeNode rolloverNode;
    
    /**
     * popup menu for menu items on nodes
     */
    private JPopupMenu popup;
    
    /**
     * close menu item used in popup menus
     */
    private JMenuItem closeMenuItem;
    
    /**
     * The object that is currently selected
     */
    private FrameContainer selected;
    
    /**
     *Parent component for the frame manager
     */
    private JComponent parent;
    
    /**
     * whether the mouse button is currently pressed
     */
    private boolean mouseClicked = true;

    /**
     *node under right click operation
     */
    private DefaultMutableTreeNode popupNode;
    
    /**
     * creates a new instance of the TreeFrameManager
     */
    public TreeFrameManager() {
        nodes = new Hashtable<FrameContainer, DefaultMutableTreeNode>();
        nodeColours = new Hashtable<FrameContainer, Color>();
        popup = new JPopupMenu();
        closeMenuItem = new JMenuItem("Close window");
        closeMenuItem.addActionListener(this);
        closeMenuItem.setActionCommand("Close");
        popup.add(closeMenuItem);
        popup.setOpaque(true);
        popup.setLightWeightPopupEnabled(true);
        root = new DefaultMutableTreeNode("DMDirc");
        model = new DefaultTreeModel(root);
        tree = new JTree(model);
        tree.addMouseListener(this);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        renderer = new TreeViewTreeCellRenderer();
        tree.setCellRenderer(renderer);
        tree.setRootVisible(false);
        tree.setRowHeight(0);
        tree.addMouseMotionListener(this);
        tree.addMouseWheelListener(this);
    }
    /**
     * Indicates whether this frame manager can be positioned vertically
     * (i.e., at the side of the screen)
     * @return True iff the frame manager can be positioned vertically
     */
    public boolean canPositionVertically() {
        return true;
    }
    
    /**
     * Indicates whether this frame manager can be positioned horizontally
     * (i.e., at the top or bottom of the screen)
     * @return True iff the frame manager can be positioned horizontally
     */
    public boolean canPositionHorizontally() {
        return false;
    }
    
    /**
     * Indicates that there is a new active frame
     * @param source The object that now has focus
     */
    public void setSelected(FrameContainer source) {
        selected = source;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tree.repaint();
            }
        });
    }
    
    /**
     * Retrieves the currently selected object
     * @return The object that is currently selected
     */
    public FrameContainer getSelected() {
        return selected;
    }
    
    /**
     * Shows an event notification to the user by colouring the corresponding
     * element to the source a specific colour
     * @param source The object requesting notification
     * @param colour The colour that should be used to indicate the notification
     */
    public void showNotification(FrameContainer source, Color colour) {
        if (nodeColours != null) {
            nodeColours.put(source, colour);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    tree.repaint();
                }
            });
        }
    }
    
    public void showRollover(DefaultMutableTreeNode node) {
        rolloverNode = node;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tree.repaint();
            }
        });
    }
    
    public DefaultMutableTreeNode getRollover() {
        return rolloverNode;
    }
    
    public void clearNotification(FrameContainer source) {
        if (nodeColours != null && nodeColours.containsKey(source)) {
            nodeColours.remove(source);
        }
    }
    
    public Color getNodeColour(FrameContainer source) {
        if (nodeColours != null && nodeColours.containsKey(source)) {
            return nodeColours.get(source);
        }
        return null;
    }
    
    /**
     * Sets the parent component in the main UI
     * @param parent parent component
     */
    public void setParent(JComponent parent) {
        this.parent = parent;
        scrollPane = new JScrollPane(tree);
        scrollPane.setAutoscrolls(false);
        parent.setLayout(new BorderLayout());
        parent.add(scrollPane);
        //parent.setPreferredSize(new Dimension(150, 0));
        scrollPane.setPreferredSize(new Dimension(parent.getWidth(), 0));;
        //tree.setBackground(parent.getBackground());
        tree.setForeground(parent.getForeground());
        tree.setBorder(new EmptyBorder(5, 5, 5, 5));
        tree.setVisible(true);
    }
    
    /**
     * adds a server to the tree
     * @param server associated server
     */
    public void addServer(Server server) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(server, node);
        node.setUserObject(server);
        model.insertNodeInto(node, (MutableTreeNode)root, root.getChildCount());
        tree.scrollPathToVisible(new TreePath(node.getPath()));
    }
    
    /**
     * removes a server from the tree
     * @param server associated server
     */
    public void delServer(Server server) {
        model.removeNodeFromParent(nodes.get(server));
    }
    
    /**
     * adds a channel to the tree
     * @param server associated server
     * @param channel associated framecontainer
     */
    public void addChannel(Server server, Channel channel) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(channel, node);
        node.setUserObject(channel);
        model.insertNodeInto(node, (MutableTreeNode)nodes.get(server), nodes.get(server).getChildCount());
        tree.scrollPathToVisible(new TreePath(node.getPath()));
    }
    
    /**
     * deletes a channel from the tree
     * @param server associated server
     * @param channel associated framecontainer
     */
    public void delChannel(Server server, Channel channel) {
        model.removeNodeFromParent(nodes.get(channel));
    }
    
    /**
     * adds a query to the tree
     * @param server associated server
     * @param query associated framecontainer
     */
    public void addQuery(Server server, Query query) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(query, node);
        node.setUserObject(query);
        model.insertNodeInto(node, (MutableTreeNode)nodes.get(server), nodes.get(server).getChildCount());
        tree.scrollPathToVisible(new TreePath(node.getPath()));
    }
    
    /**
     * deletes a query from the tree
     * @param server associated server
     * @param query associated framecontainer
     */
    public void delQuery(Server server, Query query) {
        model.removeNodeFromParent(nodes.get(query));
    }
    
    /**
     * adds a raw to the tree
     * @param server associated server
     * @param raw associated framecontainer
     */
    public void addRaw(Server server, Raw raw) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(raw, node);
        node.setUserObject(raw);
        model.insertNodeInto(node, (MutableTreeNode)nodes.get(server), nodes.get(server).getChildCount());
        tree.scrollPathToVisible(new TreePath(node.getPath()));
    }
    
    /**
     * deletes a raw from the tree
     * @param server associated server
     * @param raw associated framecontainer
     */
    public void delRaw(Server server, Raw raw) {
        model.removeNodeFromParent(nodes.get(raw));
    }
    
    /**
     * valled whenever the value of the selection changes
     * @param e selection event
     */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        
        if (node == null) return;
        
        Object nodeInfo = node.getUserObject();
        if (nodeInfo instanceof FrameContainer) {
            ((FrameContainer)nodeInfo).activateFrame();
        } else {
            Logger.error(ErrorLevel.WARNING, "Unknown node type.");
        }
    }
    
    /**
     * Called after the tree has been expanded
     * @param event expansion event
     */
    public void treeExpanded(TreeExpansionEvent event) {
    }
    
    /**
     * Called after the tree has been collapsed
     * @param event expansion event
     */
    public void treeCollapsed(TreeExpansionEvent event) {
    }
    
    /**
     * Called when the tree is about to expand
     * @param event expansion event
     * @throws javax.swing.tree.ExpandVetoException thrown to prevent node expanding
     */
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
    }
    
    /**
     * Called when the tree is about to collapse
     * @param event expansion event
     * @throws javax.swing.tree.ExpandVetoException throw to prevent node collapsing
     */
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
    }
    
    /**
     * called after a node, or set of nodes, changes
     * @param e change event
     */
    public void treeNodesChanged(TreeModelEvent e) {
    }
    
    /**
     * called after a node has been inserted into the tree
     * @param e change event
     */
    public void treeNodesInserted(TreeModelEvent e) {
    }
    
    /**
     * Called when a node is removed from the tree
     * @param e change event
     */
    public void treeNodesRemoved(TreeModelEvent e) {
    }
    
    /**
     * Called when a tree changes structure
     * @param e change event
     */
    public void treeStructureChanged(TreeModelEvent e) {
    }
    
    public void mouseClicked(MouseEvent e) {
    }
    
    public void mousePressed(MouseEvent e) {
        mouseClicked = true;
        if (e.isPopupTrigger()) {
            JTree source = (JTree)e.getSource();
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if (path != null) {
                popupNode = (DefaultMutableTreeNode)path.getLastPathComponent();
                popup.show(source, e.getX(), e.getY());
            }
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        mouseClicked = false;
    }
    
    public void mouseEntered(MouseEvent e) {
    }
    
    public void mouseExited(MouseEvent e) {
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == closeMenuItem && popupNode != null) {
            ((FrameContainer)popupNode.getUserObject()).close();
            popupNode = null;
        }
    }
    
    public void mouseDragged(MouseEvent e) {
        TreePath selectedPath, currentSelectedPath, oldSelectedPath = null;
        DefaultMutableTreeNode node = null;
        DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        if (selRow < 0) {
            currentSelectedPath = oldSelectedPath;
            oldSelectedPath = null;
            if (currentSelectedPath != null) {
                node = (DefaultMutableTreeNode)currentSelectedPath.getLastPathComponent();
                if (Config.hasOption("ui", "rolloverEnabled") &&
                        Config.getOption("ui", "rolloverEnabled").equals("true")) {
                    this.showRollover(node);
                }
                ((FrameContainer)node.getUserObject()).activateFrame();
            } else {
                if (Config.hasOption("ui", "rolloverEnabled") &&
                        Config.getOption("ui", "rolloverEnabled").equals("true")) {
                    this.showRollover(node);
                }
            }
        } else {
            selectedPath = tree.getPathForLocation(e.getX(), e.getY());
            if ((oldSelectedPath== null) || !selectedPath.equals(oldSelectedPath)) {
                oldSelectedPath = selectedPath;
                node = (DefaultMutableTreeNode)oldSelectedPath.getLastPathComponent();
                if (Config.hasOption("ui", "rolloverEnabled") &&
                        Config.getOption("ui", "rolloverEnabled").equals("true")) {
                    this.showRollover(node);
                }
                ((FrameContainer)node.getUserObject()).activateFrame();
            } else {
                if (Config.hasOption("ui", "rolloverEnabled") &&
                        Config.getOption("ui", "rolloverEnabled").equals("true")) {
                    this.showRollover(node);
                }
            }
        }
    }
    
    public void mouseMoved(MouseEvent e) {
        if (Config.hasOption("ui", "rolloverEnabled") &&
                Config.getOption("ui", "rolloverEnabled").equals("true")) {
            TreePath selectedPath, currentSelectedPath, oldSelectedPath = null;
            DefaultMutableTreeNode node;
            DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            if (selRow < 0) {
                currentSelectedPath = oldSelectedPath;
                oldSelectedPath = null;
                if (currentSelectedPath != null) {
                    node = (DefaultMutableTreeNode)currentSelectedPath.getLastPathComponent();
                    this.showRollover(node);
                } else {
                    this.showRollover(null);
                }
            } else {
                selectedPath = tree.getPathForLocation(e.getX(), e.getY());
                if ((oldSelectedPath== null) || !selectedPath.equals(oldSelectedPath)) {
                    oldSelectedPath = selectedPath;
                    node = (DefaultMutableTreeNode)oldSelectedPath.getLastPathComponent();
                    this.showRollover(node);
                } else {
                    this.showRollover(null);
                }
            }
        }
    }
    
    public void mouseWheelMoved(MouseWheelEvent e) {
        DefaultMutableTreeNode node;
        TreePath path;
        int index;
        int notches = e.getWheelRotation();
        path = tree.getSelectionPath();
        if (path == null) {
            node = (DefaultMutableTreeNode)tree.getModel().getRoot();
            node = (DefaultMutableTreeNode)node.getChildAt(0);
        } else {
            node = (DefaultMutableTreeNode)path.getLastPathComponent();
        }
    }
}
