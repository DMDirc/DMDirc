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

package com.dmdirc.ui.swing.framemanager.tree;

import com.dmdirc.Channel;
import com.dmdirc.Config;
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.FrameManager;
import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Manages open windows in the application in a tree style view.
 */
public final class TreeFrameManager implements FrameManager,
        MouseListener, ActionListener, MouseMotionListener, MouseWheelListener,
        AdjustmentListener, Serializable {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * display tree.
     */
    private final JTree tree;
    
    /**
     * root node.
     */
    private final DefaultMutableTreeNode root;
    
    /**
     * data model.
     */
    private final TreeViewModel model;
    
    /**
     * node storage, used for adding and deleting nodes correctly.
     */
    private final Map<FrameContainer, DefaultMutableTreeNode> nodes;
    
    /**
     * stores colour associated with a node, cheap hack till i rewrite the model.
     */
    private final Map<FrameContainer, Color> nodeColours;
    
    /**
     * stores background colour associated with a node,
     * cheap hack till i rewrite the model.
     */
    private DefaultMutableTreeNode rolloverNode;
    
    /**
     * popup menu for menu items on nodes.
     */
    private final JPopupMenu popup;
    
    /**
     * close menu item used in popup menus.
     */
    private final JMenuItem closeMenuItem;
    
    /**
     * The object that is currently selected.
     */
    private transient FrameContainer selected;
    
    /** Parent JComponent. */
    private JComponent parent;
    
    /**
     *node under right click operation.
     */
    private DefaultMutableTreeNode popupNode;
    
    /**
     * creates a new instance of the TreeFrameManager.
     */
    public TreeFrameManager() {
        final TreeViewTreeCellRenderer renderer = new TreeViewTreeCellRenderer(this);
        
        nodes = new Hashtable<FrameContainer, DefaultMutableTreeNode>();
        nodeColours = new Hashtable<FrameContainer, Color>();
        popup = new JPopupMenu();
        closeMenuItem = new JMenuItem("Close window");
        root = new DefaultMutableTreeNode("DMDirc");
        model = new TreeViewModel(root);
        tree = new JTree(model);
        
        closeMenuItem.setActionCommand("Close");
        popup.add(closeMenuItem);
        
        popup.setOpaque(true);
        popup.setLightWeightPopupEnabled(true);
        
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.setUI(new javax.swing.plaf.metal.MetalTreeUI());
        tree.getInputMap().setParent(null);
        tree.getInputMap(JComponent.WHEN_FOCUSED).clear();
        tree.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).clear();
        tree.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
        
        tree.getSelectionModel().
                setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(renderer);
        tree.setRootVisible(false);
        tree.setRowHeight(0);
        tree.setShowsRootHandles(false);
        tree.setOpaque(true);
        tree.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                SMALL_BORDER, SMALL_BORDER, SMALL_BORDER));
        tree.setVisible(true);
        
        closeMenuItem.addActionListener(this);
        
        tree.addMouseListener(this);
        tree.addMouseMotionListener(this);
        tree.addMouseWheelListener(this);
    }
    
    /** {@inheritDoc} */
    public boolean canPositionVertically() {
        return true;
    }
    
    /** {@inheritDoc} */
    public boolean canPositionHorizontally() {
        return false;
    }
    
    /** {@inheritDoc} */
    public void setSelected(final FrameContainer source) {
        selected = source;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tree.repaint();
            }
        });
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
    
    /** {@inheritDoc} */
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
    
    /** {@inheritDoc} */
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
    
    /** {@inheritDoc} */
    public void setParent(final JComponent parent) {
        final JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setAutoscrolls(true);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
        
        parent.setLayout(new BorderLayout());
        parent.add(scrollPane);
        
        tree.setBackground(Config.getOptionColor("treeview", "backgroundcolour",
                Config.getOptionColor("ui", "backgroundcolour", Color.WHITE)));
        tree.setForeground(Config.getOptionColor("treeview", "foregroundcolour",
                Config.getOptionColor("ui", "foregroundcolour", Color.BLACK)));
        
        this.parent = parent;
    }
    
    /** {@inheritDoc} */
    public void addServer(final Server server) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(server, node);
        node.setUserObject(server);
        model.insertNodeInto(node, root);
        if (root.getChildCount() == 1) {
            selected = server;
        }
        tree.expandPath(new TreePath(node.getPath()).getParentPath());
        final Rectangle view = tree.getRowBounds(tree.getRowForPath(new TreePath(node.getPath())));
        tree.scrollRectToVisible(new Rectangle(0, (int) view.getY(), 0, 0));
    }
    
    /** {@inheritDoc} */
    public void delServer(final Server server) {
        if (nodes != null && nodes.get(server) != null) {
            final DefaultMutableTreeNode node = nodes.get(server);
            if (node.getLevel() == 0) {
                Logger.appError(ErrorLevel.MEDIUM, 
                        "delServer triggered for root node", 
                        new IllegalArgumentException());
            } else {
                model.removeNodeFromParent(nodes.get(server));
            }
        }
    }
    
    /** {@inheritDoc} */
    @Deprecated
    public void addChannel(final Server server, final Channel channel) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(channel, node);
        node.setUserObject(channel);
        model.insertNodeInto(node, nodes.get(server));
        tree.expandPath(new TreePath(node.getPath()).getParentPath());
        final Rectangle view = tree.getRowBounds(tree.getRowForPath(new TreePath(node.getPath())));
        tree.scrollRectToVisible(new Rectangle(0, (int) view.getY(), 0, 0));
    }
    
    /** {@inheritDoc} */
    @Deprecated
    public void delChannel(final Server server, final Channel channel) {
        if (nodes != null && nodes.get(channel) != null) {
            model.removeNodeFromParent(nodes.get(channel));
        }
    }
    
    /** {@inheritDoc} */
    @Deprecated
    public void addQuery(final Server server, final Query query) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(query, node);
        node.setUserObject(query);
        model.insertNodeInto(node, nodes.get(server));
        tree.expandPath(new TreePath(node.getPath()).getParentPath());
        final Rectangle view = tree.getRowBounds(tree.getRowForPath(new TreePath(node.getPath())));
        tree.scrollRectToVisible(new Rectangle(0, (int) view.getY(), 0, 0));
    }
    
    /** {@inheritDoc} */
    @Deprecated
    public void delQuery(final Server server, final Query query) {
        if (nodes != null && nodes.get(query) != null) {
            model.removeNodeFromParent(nodes.get(query));
        }
    }
    
    /** {@inheritDoc} */
    
    public void addCustom(final Server server, final FrameContainer window) {
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
        nodes.put(window, node);
        node.setUserObject(window);
        model.insertNodeInto(node, nodes.get(server));
        tree.expandPath(new TreePath(node.getPath()).getParentPath());
        final Rectangle view = tree.getRowBounds(tree.getRowForPath(new TreePath(node.getPath())));
        tree.scrollRectToVisible(new Rectangle(0, (int) view.getY(), 0, 0));
    }
    
    /** {@inheritDoc} */
    public void delCustom(final Server server, final FrameContainer window) {
        if (nodes != null && nodes.get(window) != null) {
            model.removeNodeFromParent(nodes.get(window));
        }
    }
    
    /** {@inheritDoc} */
    public void iconUpdated(final FrameContainer window) {
        // Do nothing
    }
    
    /**
     * Returns the maximum size a node can be without causing scrolling.
     *
     * @return Maximum node width
     */
    public int getNodeWidth() {
        if (parent == null) {
            return 0;
        } else {
            return parent.getWidth() - 25;
        }
    }
    
    /** {@inheritDoc} */
    public void adjustmentValueChanged(final AdjustmentEvent e) {
        //HACK Disregard all scrolling events
        ((JScrollBar) e.getSource()).setValue(0);
    }
    
    /**
     * Invoked when the mouse button has been clicked (pressed and released)
     * on a component.
     * @param event mouse event.
     */
    public void mouseClicked(final MouseEvent event) {
        processMouseEvent(event);
    }
    
    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param event mouse event.
     */
    public void mousePressed(final MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON1) {
            final TreePath selectedPath = tree.getPathForLocation(event.getX(), event.getY());
            if (selectedPath != null) {
                final DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                final Object nodeInfo = node.getUserObject();
                if (nodeInfo instanceof FrameContainer) {
                    ((FrameContainer) nodeInfo).activateFrame();
                } else {
                    Logger.appError(ErrorLevel.MEDIUM, "Unknown node type.",
                            new IllegalArgumentException("Node: " + nodeInfo.getClass()));
                }
            }
        }
        processMouseEvent(event);
    }
    
    /**
     * Invoked when a mouse button has been released on a component.
     * @param event mouse event.
     */
    public void mouseReleased(final MouseEvent event) {
        processMouseEvent(event);
    }
    
    /**
     * Invoked when the mouse enters a component.
     * @param event mouse event.
     */
    public void mouseEntered(final MouseEvent event) {
        //Do nothing
    }
    
    /**
     * Invoked when the mouse exits a component.
     * @param event mouse event.
     */
    public void mouseExited(final MouseEvent event) {
        //Do nothing
    }
    
    /**
     * Processes every mouse button event to check for a popup trigger.
     * @param event mouse event
     */
    public void processMouseEvent(final MouseEvent event) {
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
     * Invoked when an action occurs.
     * @param event action event.
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
        final DefaultMutableTreeNode node = getNodeForLocation(event.getX(), event.getY());
        this.showRollover(node);
        if (node != null) {
            ((FrameContainer) node.getUserObject()).activateFrame();
        }
    }
    
    /**
     * Invoked when the mouse cursor has been moved onto a component but no
     * buttons have been pushed.
     *
     * @param event mouse event.
     */
    public void mouseMoved(final MouseEvent event) {
        final DefaultMutableTreeNode node = getNodeForLocation(event.getX(), event.getY());
        this.showRollover(node);
    }
    
    /**
     * Returns the node for the specified location, returning null if rollover
     * is disabled or there is no node at the specified location.
     *
     * @param x x coordiantes
     * @param y y coordiantes
     *
     * @return node or null
     */
    private DefaultMutableTreeNode getNodeForLocation(final int x, final int y) {
        DefaultMutableTreeNode node = null;
        if (Config.getOptionBool("ui", "treeviewRolloverEnabled")) {
            final TreePath selectedPath = tree.getPathForLocation(x, y);
            if (selectedPath == null) {
                this.showRollover(null);
            } else {
                node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                this.showRollover((DefaultMutableTreeNode) selectedPath.getLastPathComponent());
            }
        }
        return node;
    }
    
    /**
     * Invoked when the mouse wheel is rotated.
     * @param event mouse event.
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
     * @param direction true = up, false = down.
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
            nextNode = changeFocusUp(thisNode);
        } else {
            //down
            nextNode = changeFocusDown(thisNode);
        }
        //activate the nodes frame
        ((FrameContainer) nextNode.getUserObject()).activateFrame();
    }
    
    /**
     * Changes the tree focus up.
     *
     * @param node Start node
     *
     * @return next node
     */
    private DefaultMutableTreeNode changeFocusUp(final DefaultMutableTreeNode node) {
        DefaultMutableTreeNode thisNode, nextNode;
        
        thisNode = node;
        
        if (thisNode.getUserObject() instanceof Server) {
            if (thisNode.getParent().getIndex(thisNode) == 0) {
                //first server - last child of parent's last child
                nextNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode)
                thisNode.getParent()).getLastChild();
                if (nextNode.getChildCount() > 0) {
                    nextNode = (DefaultMutableTreeNode) nextNode.getLastChild();
                }
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
        DefaultMutableTreeNode thisNode, nextNode;
        
        thisNode = node;
        
        if (thisNode.getUserObject() instanceof Server) {
            if (thisNode.getChildCount() > 0) {
                //server has frames, use first
                nextNode = (DefaultMutableTreeNode) thisNode.getFirstChild();
            } else {
                //server has no frames, use next server
                nextNode = ((DefaultMutableTreeNode) thisNode.getParent()).getNextSibling();
                //no next server, use first
                if (nextNode == null) {
                    nextNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode)
                    thisNode.getParent()).getFirstChild();
                }
            }
        } else {
            if (thisNode.getParent().getIndex(thisNode) == thisNode.getParent().getChildCount() - 1) {
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
        
        return nextNode;
    }
    
}
