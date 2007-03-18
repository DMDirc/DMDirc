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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import javax.swing.tree.ExpandVetoException;
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
 * Manages open windows in the application in a tree style view.
 */
public final class TreeFrameManager implements FrameManager, TreeModelListener,
        TreeSelectionListener, TreeExpansionListener, TreeWillExpandListener,
        MouseListener, ActionListener, MouseMotionListener, MouseWheelListener,
        KeyListener {
    
    /**
     * The size of the border to use around the tree.
     */
    private static final int BORDER_SIZE = 5;
    
    /**
     * display tree.
     */
    private final transient JTree tree;
    
    /**
     * root node.
     */
    private final transient DefaultMutableTreeNode root;
    
    /**
     * data model.
     */
    private final transient TreeViewModel model;
    
    /**
     * node storage, used for adding and deleting nodes correctly.
     */
    private final transient Hashtable<FrameContainer, DefaultMutableTreeNode> nodes;
    
    /**
     * stores colour associated with a node, cheap hack till i rewrite the model.
     */
    private final transient Hashtable<FrameContainer, Color> nodeColours;
    
    /**
     * stores background colour associated with a node,
     * cheap hack till i rewrite the model.
     */
    private transient DefaultMutableTreeNode rolloverNode;
    
    /**
     * popup menu for menu items on nodes.
     */
    private final transient JPopupMenu popup;
    
    /**
     * close menu item used in popup menus.
     */
    private final transient JMenuItem closeMenuItem;
    
    /**
     * The object that is currently selected.
     */
    private FrameContainer selected;
    
    /**
     *node under right click operation.
     */
    private transient DefaultMutableTreeNode popupNode;
    
    /**
     * creates a new instance of the TreeFrameManager.
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
        model = new TreeViewModel(root);
        tree = new JTree(model);
        tree.addMouseListener(this);
        tree.getSelectionModel().
                setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        final TreeViewTreeCellRenderer renderer = new TreeViewTreeCellRenderer();
        tree.setCellRenderer(renderer);
        tree.setRootVisible(false);
        tree.setRowHeight(0);
        tree.addMouseMotionListener(this);
        tree.addMouseWheelListener(this);
    }
    /**
     * Indicates whether this frame manager can be positioned vertically
     * (i.e., at the side of the screen).
     * @return True iff the frame manager can be positioned vertically.
     */
    public boolean canPositionVertically() {
        return true;
    }
    
    /**
     * Indicates whether this frame manager can be positioned horizontally
     * (i.e., at the top or bottom of the screen).
     * @return True iff the frame manager can be positioned horizontally.
     */
    public boolean canPositionHorizontally() {
        return false;
    }
    
    /**
     * Indicates that there is a new active frame.
     * @param source The object that now has focus.
     */
    public void setSelected(final FrameContainer source) {
        selected = source;
        if (nodes.containsKey(source)) {
            tree.scrollPathToVisible(new TreePath(nodes.get(source).getPath()));
            //tree.setSelectionPath(new TreePath(nodes.get(source).getPath()));
        }
        tree.repaint();
    }
    
    /**
     * Retrieves the currently selected object.
     * @return The object that is currently selected.
     */
    public FrameContainer getSelected() {
        return selected;
    }
    
    /**
     * Retrieves the currently selected node.
     * @return The node that is currently selected.
     */
    public DefaultMutableTreeNode getSelectedNode() {
        return nodes.get(selected);
    }
    
    /**
     * Shows an event notification to the user by colouring the corresponding
     * element to the source a specific colour.
     * @param source The object requesting notification.
     * @param colour The colour that should be used to indicate the notification.
     */
    public void showNotification(final FrameContainer source, final Color colour) {
        if (nodeColours != null) {
            nodeColours.put(source, colour);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    tree.repaint();
                }
            });
        }
    }
    
    /**
     * Sets the rollover node and repaints the tree.
     * @param node rollover node.
     */
    public void showRollover(final DefaultMutableTreeNode node) {
        rolloverNode = node;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tree.repaint();
            }
        });
    }
    
    /**
     * retrives the rollover node.
     * @return rollover node.
     */
    public DefaultMutableTreeNode getRollover() {
        return rolloverNode;
    }
    
    /**
     * Clears a notification from a frame and its node.
     * @param source Frame to remove notification from.
     */
    public void clearNotification(final FrameContainer source) {
        if (nodeColours != null && nodeColours.containsKey(source)) {
            nodeColours.remove(source);
        }
    }
    
    /**
     * retrieves the colour of a specific node.
     * @param source node to check colour of.
     * @return colour of the node.
     */
    public Color getNodeColour(final FrameContainer source) {
        if (nodeColours != null && nodeColours.containsKey(source)) {
            return nodeColours.get(source);
        }
        return null;
    }
    
    /**
     * Sets the parent component in the main UI.
     * @param newParent parent component.
     */
    public void setParent(final JComponent parent) {
        final JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setAutoscrolls(false);
        parent.setLayout(new BorderLayout());
        parent.add(scrollPane);
        scrollPane.setPreferredSize(new Dimension(parent.getWidth(), 0));
        tree.setForeground(parent.getForeground());
        tree.setBorder(new EmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
        tree.setVisible(true);
        tree.addKeyListener(this);
    }
    
    /**
     * adds a server to the tree.
     * @param server associated server.
     */
    public void addServer(final Server server) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(server, node);
        node.setUserObject(server);
        model.insertNodeInto(node, root);
        if (root.getChildCount() == 1) {
            selected = server;
        }
        tree.scrollPathToVisible(new TreePath(node.getPath()));
    }
    
    /**
     * removes a server from the tree.
     * @param server associated server.
     */
    public void delServer(final Server server) {
        model.removeNodeFromParent(nodes.get(server));
    }
    
    /**
     * adds a channel to the tree.
     * @param server associated server.
     * @param channel associated framecontainer.
     */
    public void addChannel(final Server server, final Channel channel) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(channel, node);
        node.setUserObject(channel);
        model.insertNodeInto(node, nodes.get(server));
        tree.scrollPathToVisible(new TreePath(node.getPath()));
    }
    
    /**
     * deletes a channel from the tree.
     * @param server associated server.
     * @param channel associated framecontainer.
     */
    public void delChannel(final Server server, final Channel channel) {
        model.removeNodeFromParent(nodes.get(channel));
    }
    
    /**
     * adds a query to the tree.
     * @param server associated server.
     * @param query associated framecontainer.
     */
    public void addQuery(final Server server, final Query query) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(query, node);
        node.setUserObject(query);
        model.insertNodeInto(node, nodes.get(server));
        tree.scrollPathToVisible(new TreePath(node.getPath()));
    }
    
    /**
     * deletes a query from the tree.
     * @param server associated server.
     * @param query associated framecontainer.
     */
    public void delQuery(final Server server, final Query query) {
        model.removeNodeFromParent(nodes.get(query));
    }
    
    /**
     * adds a raw to the tree.
     * @param server associated server.
     * @param raw associated framecontainer.
     */
    public void addRaw(final Server server, final Raw raw) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(raw, node);
        node.setUserObject(raw);
        model.insertNodeInto(node, nodes.get(server));
        tree.scrollPathToVisible(new TreePath(node.getPath()));
    }
    
    /**
     * deletes a raw from the tree.
     * @param server associated server.
     * @param raw associated framecontainer.
     */
    public void delRaw(final Server server, final Raw raw) {
        model.removeNodeFromParent(nodes.get(raw));
    }
    
    /**
     * valled whenever the value of the selection changes.
     * @param e selection event.
     */
    public void valueChanged(final TreeSelectionEvent event) {
        final DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        
        if (node == null) { return; }
        
        final Object nodeInfo = node.getUserObject();
        if (nodeInfo instanceof FrameContainer) {
            ((FrameContainer) nodeInfo).activateFrame();
        } else {
            Logger.error(ErrorLevel.WARNING, "Unknown node type.");
        }
    }
    
    /**
     * Called after the tree has been expanded.
     * @param event expansion event.
     */
    public void treeExpanded(final TreeExpansionEvent event) {
        //Do nothing
    }
    
    /**
     * Called after the tree has been collapsed.
     * @param event expansion event.
     */
    public void treeCollapsed(final TreeExpansionEvent event) {
        //Do nothing
    }
    
    /**
     * Called when the tree is about to expand.
     * @param event expansion event.
     * @throws javax.swing.tree.ExpandVetoException thrown to prevent.
     * node expanding
     */
    public void treeWillExpand(final TreeExpansionEvent event) throws
            ExpandVetoException {
        //Do nothing
    }
    
    /**
     * Called when the tree is about to collapse.
     * @param event expansion event.
     * @throws javax.swing.tree.ExpandVetoException throw to prevent.
     * node collapsing
     */
    public void treeWillCollapse(final TreeExpansionEvent event) throws
            ExpandVetoException {
        //Do nothing
    }
    
    /**
     * called after a node, or set of nodes, changes.
     * @param e change event.
     */
    public void treeNodesChanged(final TreeModelEvent event) {
        //Do nothing
    }
    
    /**
     * called after a node has been inserted into the tree.
     * @param e change event.
     */
    public void treeNodesInserted(final TreeModelEvent event) {
        //Do nothing
    }
    
    /**
     * Called when a node is removed from the tree.
     * @param e change event.
     */
    public void treeNodesRemoved(final TreeModelEvent event) {
        //Do nothing
    }
    
    /**
     * Called when a tree changes structure.
     * @param e change event.
     */
    public void treeStructureChanged(final TreeModelEvent event) {
        //Do nothing
    }
    
    /**
     * Invoked when the mouse button has been clicked (pressed and released)
     * on a component.
     * @param e mouse event.
     */
    public void mouseClicked(final MouseEvent event) {
        //Do nothing
    }
    
    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e mouse event.
     */
    public void mousePressed(final MouseEvent event) {
        if (event.isPopupTrigger()) {
            final JTree source = (JTree) event.getSource();
            final TreePath path = tree.getPathForLocation(event.getX(), 
                    event.getY());
            if (path != null) {
                popupNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                popup.show(source, event.getX(), event.getY());
            }
        }
    }
    
    /**
     * Invoked when a mouse button has been released on a component.
     * @param e mouse event.
     */
    public void mouseReleased(final MouseEvent event) {
        //Do nothing
    }
    
    /**
     * Invoked when the mouse enters a component.
     * @param e mouse event.
     */
    public void mouseEntered(final MouseEvent event) {
        //Do nothing
    }
    
    /**
     * Invoked when the mouse exits a component.
     * @param e mouse event.
     */
    public void mouseExited(final MouseEvent event) {
        //Do nothing
    }
    
    /**
     * Invoked when an action occurs.
     * @param e action event.
     */
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() == closeMenuItem && popupNode != null) {
            ((FrameContainer) popupNode.getUserObject()).close();
        }
    }
    
    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * 
     * @param event mouse event.
     */
    public void mouseDragged(final MouseEvent event) {
        TreePath selectedPath, currentSelectedPath, oldSelectedPath = null;
        DefaultMutableTreeNode node = null;
        if (tree.getRowForLocation(event.getX(), event.getY()) < 0) {
            currentSelectedPath = oldSelectedPath;
            if (currentSelectedPath == null) {
                if (Config.hasOption("ui", "rolloverEnabled")
                && Config.getOption("ui", "rolloverEnabled").equals("true")) {
                    this.showRollover(node);
                }
            } else {
                node = (DefaultMutableTreeNode) currentSelectedPath.getLastPathComponent();
                if (Config.hasOption("ui", "rolloverEnabled")
                && Config.getOption("ui", "rolloverEnabled").equals("true")) {
                    this.showRollover(node);
                }
                ((FrameContainer) node.getUserObject()).activateFrame();
            }
        } else {
            selectedPath = tree.getPathForLocation(event.getX(), event.getY());
            if ((oldSelectedPath == null) || !selectedPath.equals(oldSelectedPath)) {
                oldSelectedPath = selectedPath;
                node = (DefaultMutableTreeNode) oldSelectedPath.getLastPathComponent();
                if (Config.hasOption("ui", "rolloverEnabled")
                && Config.getOption("ui", "rolloverEnabled").equals("true")) {
                    this.showRollover(node);
                }
                ((FrameContainer) node.getUserObject()).activateFrame();
            } else {
                if (Config.hasOption("ui", "rolloverEnabled")
                && Config.getOption("ui", "rolloverEnabled").equals("true")) {
                    this.showRollover(node);
                }
            }
        }
    }
    
    /**
     * Invoked when the mouse cursor has been moved onto a component but no
     * buttons have been pushed.
     * 
     * @param event mouse event.
     */
    public void mouseMoved(final MouseEvent event) {
        if (Config.hasOption("ui", "rolloverEnabled")
        && Config.getOption("ui", "rolloverEnabled").equals("true")) {
            TreePath selectedPath, currentSelectedPath, oldSelectedPath = null;
            DefaultMutableTreeNode node;
            if (tree.getRowForLocation(event.getX(), event.getY()) < 0) {
                currentSelectedPath = oldSelectedPath;
                if (currentSelectedPath == null) {
                    this.showRollover(null);
                } else {
                    node = (DefaultMutableTreeNode) currentSelectedPath.getLastPathComponent();
                    this.showRollover(node);
                }
            } else {
                selectedPath = tree.getPathForLocation(event.getX(), event.getY());
                if ((oldSelectedPath == null) || !selectedPath.equals(oldSelectedPath)) {
                    oldSelectedPath = selectedPath;
                    node = (DefaultMutableTreeNode) oldSelectedPath.getLastPathComponent();
                    this.showRollover(node);
                } else {
                    this.showRollover(null);
                }
            }
        }
    }
    
    /**
     * Invoked when the mouse wheel is rotated.
     * @param e mouse event.
     */
    public void mouseWheelMoved(final MouseWheelEvent event) {
        //get the number of notches (used only for direction)
        if (event.getWheelRotation() < 0) {
            changeFocus(true);
        } else {
            changeFocus(false);
        }
    }
    
    /**
     * Activates the node above or below the active node in the tree.
     *
     *@param direction true = up, false = down.
     */
    private void changeFocus(final boolean direction) {
        DefaultMutableTreeNode thisNode, nextNode;
        
        if (getSelectedNode() == null) {
            //no selected node, get the root node
            thisNode = root;
            //are there any servers to select?
            if (thisNode.getChildCount() > 0) {
                thisNode = (DefaultMutableTreeNode) thisNode.getChildAt(0);
            } else {
                //then wait till there are
                return;
            }
        } else {
            //use the selected node to start from
            thisNode = getSelectedNode();
        }
        //are we going up or down?
        if (direction) {
            //up
            if (thisNode.getUserObject() instanceof Server) {
                if (thisNode.getParent().getIndex(thisNode) == 0) {
                    //first server - last child of parent's last child
                    nextNode = (DefaultMutableTreeNode)
                    ((DefaultMutableTreeNode) ((DefaultMutableTreeNode)
                    thisNode.getParent()).getLastChild()).getLastChild();
                } else {
                    //other servers - last child of previous sibling
                    nextNode = (DefaultMutableTreeNode)
                    (thisNode.getPreviousSibling()).getLastChild();
                }
            } else {
                if (thisNode.getParent().getIndex(thisNode) == 0) {
                    //first frame - parent
                    nextNode = (DefaultMutableTreeNode) thisNode.getParent();
                } else {
                    //other frame - previous sibling
                    nextNode = thisNode.getPreviousSibling();
                }
            }
        } else {
            //down
            if (thisNode.getUserObject() instanceof Server) {
                //all servers - get the first child
                nextNode = (DefaultMutableTreeNode) thisNode.getFirstChild();
            } else {
                if (thisNode.getParent().getIndex(thisNode)
                == thisNode.getParent().getChildCount() - 1) {
                    //last frame - get the parents next sibling
                    nextNode = ((DefaultMutableTreeNode) thisNode.getParent()).getNextSibling();
                    //parent doesnt have a next sibling, get the first child of the grandparent
                    if (nextNode == null) {
                        nextNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode)
                        thisNode.getParent().getParent()).getFirstChild();
                    }
                } else {
                    //other frames - get the next sibling
                    nextNode = thisNode.getNextSibling();
                }
            }
        }
        //activate the nodes frame
        //((FrameContainer) nextNode.getUserObject()).activateFrame();
        tree.setSelectionPath(new TreePath(nextNode));
    }
    
    /**
     * Invoked when a key has been typed.
     * @param e key event.
     */
    public void keyTyped(final KeyEvent event) {
        //Do nothing
    }
    
    /**
     * Invoked when a key has been pressed.
     * @param e key event.
     */
    public void keyPressed(final KeyEvent event) {
        //Do nothing
    }
    
    /**
     * Invoked when a key has been released.
     * @param e key event.
     */
    public void keyReleased(final KeyEvent event) {
        //Do nothing
    }
}
